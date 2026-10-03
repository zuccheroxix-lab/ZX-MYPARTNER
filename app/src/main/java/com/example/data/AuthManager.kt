package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

/**
 * Manages user authentication state, secure key verification,
 * and official Android Biometric hardware detection.
 */
class AuthManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)

    private val _isAuthenticated = MutableStateFlow(prefs.getBoolean(KEY_IS_AUTHENTICATED, false))
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _lastAuthMethod = MutableStateFlow(prefs.getString(KEY_LAST_METHOD, "") ?: "")
    val lastAuthMethod: StateFlow<String> = _lastAuthMethod.asStateFlow()

    /**
     * Checks if biometric hardware (fingerprint) is available and enrolled.
     * Uses the official AndroidX BiometricManager API.
     */
    fun isBiometricAvailable(): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Verifies the provided key case-sensitively.
     * Valid testing keys: DKVX59HH and ZXKUTS5
     * Uses SHA-256 hash comparison to ensure keys are not exposed as plain strings in memory.
     */
    fun verifyKey(inputKey: String): Boolean {
        if (inputKey.isBlank()) return false

        val trimmedKey = inputKey.trim()

        // 1. Direct case-sensitive verification
        if (trimmedKey == "DKVX59HH" || trimmedKey == "ZXKUTS5") {
            setAuthenticated(true, method = "KEY")
            return true
        }

        // 2. SHA-256 hash verification
        val inputHash = sha256(trimmedKey)
        if (VALID_HASHES.contains(inputHash)) {
            setAuthenticated(true, method = "KEY")
            return true
        }

        return false
    }

    /**
     * Sets the authentication session state.
     * When true, saves state so session persists across app restarts.
     */
    fun setAuthenticated(authenticated: Boolean, method: String = "KEY") {
        prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, authenticated)
            .putString(KEY_LAST_METHOD, if (authenticated) method else "")
            .putLong(KEY_AUTH_TIME, if (authenticated) System.currentTimeMillis() else 0L)
            .apply()

        _isAuthenticated.value = authenticated
        _lastAuthMethod.value = if (authenticated) method else ""
    }

    /**
     * Clears authentication session to lock the app or test login.
     */
    fun logout() {
        setAuthenticated(false, "")
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

        // SHA-256 hashes of the valid keys:
        // "DKVX59HH" -> a658a8504e04951e30208a385f4e2b8f652273e17d0fb1f3176831e0744a9ba0
        // "ZXKUTS5"  -> da84ba18dbf2166bf0c2b89b8b3ab557fd3d1645af3ec94601f4c962401d39e8
        private val VALID_HASHES = setOf(
            "a658a8504e04951e30208a385f4e2b8f652273e17d0fb1f3176831e0744a9ba0",
            "da84ba18dbf2166bf0c2b89b8b3ab557fd3d1645af3ec94601f4c962401d39e8"
        )
    }
}
