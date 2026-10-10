package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages user authentication state, secure PIN verification (6745),
 * Android Keystore encryption, rate-limiting / lockout, and official AndroidX Biometric detection.
 */
class AuthManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    private val _isAuthenticated = MutableStateFlow(checkInitialAuthentication())
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _lastAuthMethod = MutableStateFlow(prefs.getString(KEY_LAST_METHOD, "") ?: "")
    val lastAuthMethod: StateFlow<String> = _lastAuthMethod.asStateFlow()

    private val _lockoutTimeRemaining = MutableStateFlow(getRemainingLockoutSeconds())
    val lockoutTimeRemaining: StateFlow<Int> = _lockoutTimeRemaining.asStateFlow()

    private fun checkInitialAuthentication(): Boolean {
        val isAuth = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
        if (!isAuth) return false

        // Verify with Keystore encrypted token if available
        val encryptedToken = prefs.getString(KEY_ENCRYPTED_TOKEN, null)
        val ivStr = prefs.getString(KEY_ENCRYPTED_IV, null)
        if (encryptedToken != null && ivStr != null) {
            val decrypted = decryptWithKeystore(encryptedToken, ivStr)
            if (decrypted == null || !decrypted.startsWith("SESSION_VALID")) {
                // Token invalid or corrupted
                return false
            }
        }
        return true
    }

    /**
     * Checks detailed biometric hardware and enrollment status using AndroidX BiometricManager.
     */
    fun checkBiometricStatus(): Int {
        return try {
            val biometricManager = BiometricManager.from(context)
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            biometricManager.canAuthenticate(authenticators)
        } catch (_: Exception) {
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED
        }
    }

    /**
     * Checks if biometric hardware (fingerprint) is available and enrolled.
     * Returns true only when BIOMETRIC_SUCCESS is reported.
     */
    fun isBiometricAvailable(): Boolean {
        return checkBiometricStatus() == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Checks if user is currently locked out due to multiple failed attempts.
     */
    fun isLockedOut(): Boolean {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        if (now < lockoutUntil) {
            _lockoutTimeRemaining.value = ((lockoutUntil - now) / 1000).toInt().coerceAtLeast(1)
            return true
        }
        _lockoutTimeRemaining.value = 0
        return false
    }

    /**
     * Returns remaining lockout seconds (0 if not locked out).
     */
    fun getRemainingLockoutSeconds(): Int {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        return if (now < lockoutUntil) {
            ((lockoutUntil - now) / 1000).toInt().coerceAtLeast(1)
        } else {
            0
        }
    }

    /**
     * Verifies the app PIN.
     * Official App PIN: 6745.
     * Uses SHA-256 hash comparison so PIN is never stored or compared in plain text.
     * Implements lockout rate limiting after 5 failed attempts.
     */
    fun verifyPin(inputPin: String): Boolean {
        if (isLockedOut()) {
            return false
        }

        if (inputPin.isBlank()) {
            return false
        }

        val trimmed = inputPin.trim()
        val hash = sha256(trimmed)

        val isValid = (hash == APP_PIN_HASH) || VALID_LEGACY_HASHES.contains(hash)

        if (isValid) {
            // Reset failed attempts on success
            resetFailedAttempts()
            setAuthenticated(true, method = "PIN")
            return true
        } else {
            recordFailedAttempt()
            return false
        }
    }

    /**
     * Backward-compatible key/pin verification.
     */
    fun verifyKey(inputKey: String): Boolean {
        return verifyPin(inputKey)
    }

    private fun recordFailedAttempt() {
        val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts)

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            editor.putLong(KEY_LOCKOUT_UNTIL, lockoutUntil)
            _lockoutTimeRemaining.value = (LOCKOUT_DURATION_MS / 1000).toInt()
        }
        editor.apply()
    }

    private fun resetFailedAttempts() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        _lockoutTimeRemaining.value = 0
    }

    /**
     * Sets the authentication session state and encrypts a session token using Keystore.
     */
    fun setAuthenticated(authenticated: Boolean, method: String = "PIN") {
        val editor = prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, authenticated)
            .putString(KEY_LAST_METHOD, if (authenticated) method else "")
            .putLong(KEY_AUTH_TIME, if (authenticated) System.currentTimeMillis() else 0L)

        if (authenticated) {
            resetFailedAttempts()
            val token = "SESSION_VALID_${System.currentTimeMillis()}"
            val encryptedPair = encryptWithKeystore(token)
            if (encryptedPair != null) {
                editor.putString(KEY_ENCRYPTED_TOKEN, encryptedPair.first)
                editor.putString(KEY_ENCRYPTED_IV, encryptedPair.second)
            }
        } else {
            editor.remove(KEY_ENCRYPTED_TOKEN)
            editor.remove(KEY_ENCRYPTED_IV)
        }
        editor.apply()

        _isAuthenticated.value = authenticated
        _lastAuthMethod.value = if (authenticated) method else ""
    }

    /**
     * Clears authentication session to lock the app.
     */
    fun logout() {
        setAuthenticated(false, "")
    }

    // ==========================================
    // ANDROID KEYSTORE CRYPTOGRAPHY HELPERS
    // ==========================================

    private fun getOrCreateSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            } else {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun encryptWithKeystore(plainText: String): Pair<String, String>? {
        return try {
            val key = getOrCreateSecretKey() ?: return null
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val cipherBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            Pair(cipherBase64, ivBase64)
        } catch (_: Exception) {
            null
        }
    }

    private fun decryptWithKeystore(cipherBase64: String, ivBase64: String): String? {
        return try {
            val key = getOrCreateSecretKey() ?: return null
            val cipherBytes = Base64.decode(cipherBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun sha256(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    companion object {
        private const val PREF_FILE_NAME = "zx_auth_session_prefs"
        private const val KEY_IS_AUTHENTICATED = "auth_is_authenticated"
        private const val KEY_LAST_METHOD = "auth_last_method"
        private const val KEY_AUTH_TIME = "auth_timestamp"
        private const val KEY_ENCRYPTED_TOKEN = "auth_enc_token"
        private const val KEY_ENCRYPTED_IV = "auth_enc_iv"
        private const val KEY_FAILED_ATTEMPTS = "auth_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "auth_lockout_until"

        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "ZX_APP_AUTH_KEY_2026"

        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds

        // SHA-256 of official App PIN "6745"
        // echo -n "6745" | sha256sum -> 5d5653c4eb4f09a3c5a14b499ceea37a9f025f9e7105c407eb09d81e6d4430c6
        private const val APP_PIN_HASH =
            "5d5653c4eb4f09a3c5a14b499ceea37a9f025f9e7105c407eb09d81e6d4430c6"

        // Legacy test key hashes (DKVX59HH, ZXKUTS5) preserved for backwards compatibility
        private val VALID_LEGACY_HASHES = setOf(
            "a658a8504e04951e30208a385f4e2b8f652273e17d0fb1f3176831e0744a9ba0",
            "da84ba18dbf2166bf0c2b89b8b3ab557fd3d1645af3ec94601f4c962401d39e8"
        )
    }
}
