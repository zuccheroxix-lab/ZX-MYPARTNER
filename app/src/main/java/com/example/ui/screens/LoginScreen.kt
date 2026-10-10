package com.example.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.AuthManager
import com.example.data.Config
import com.example.ui.components.LinkHandler
import com.example.ui.components.VideoBackgroundPlayer
import kotlinx.coroutines.delay

/**
 * Dedicated App Login Screen for DYNIMETIZE ZX.
 * Layout Hierarchy:
 * APP LOGIN
 * [ PIN ]
 * [ LOGIN ]
 * [ USE BIOMETRIC ]
 */
@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var inputPin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val isBiometricReady = remember { authManager.isBiometricAvailable() }
    val remainingLockoutSeconds by authManager.lockoutTimeRemaining.collectAsState()
    val isLockedOut = remainingLockoutSeconds > 0

    // Countdown timer effect during lockout
    LaunchedEffect(isLockedOut, remainingLockoutSeconds) {
        if (isLockedOut) {
            errorMessage = "Terlalu banyak percobaan gagal. Silakan tunggu $remainingLockoutSeconds detik."
            while (authManager.isLockedOut()) {
                delay(1000)
                val sec = authManager.getRemainingLockoutSeconds()
                if (sec > 0) {
                    errorMessage = "Terlalu banyak percobaan gagal. Silakan tunggu $sec detik."
                } else {
                    errorMessage = null
                    break
                }
            }
        }
    }

    fun triggerBiometricLogin() {
        if (isLockedOut) {
            errorMessage = "Autentikasi terkunci sementara. Tunggu $remainingLockoutSeconds detik."
            return
        }

        errorMessage = null
        val activity = context as? FragmentActivity
        if (activity == null) {
            errorMessage = "Aktivitas tidak mendukung autentikasi biometrik."
            return
        }

        val biometricStatus = authManager.checkBiometricStatus()
        if (biometricStatus != BiometricManager.BIOMETRIC_SUCCESS) {
            val msg = "Biometrik tidak tersedia. Gunakan PIN aplikasi."
            errorMessage = msg
            try {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {}
            return
        }

        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    errorMessage = null
                    authManager.setAuthenticated(true, method = "BIOMETRIC")
                    onLoginSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If user canceled or dismissed, do not display persistent error
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        errorMessage = errString.toString()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    errorMessage = "Sidik jari tidak cocok. Silakan coba lagi atau gunakan PIN."
                }
            }

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autentikasi Biometrik")
                .setSubtitle("Sentuh sensor sidik jari untuk membuka aplikasi")
                .setNegativeButtonText("Gunakan PIN")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            errorMessage = "Biometrik tidak dapat diakses: ${e.localizedMessage}"
        }
    }

    fun handlePinLogin() {
        focusManager.clearFocus()
        errorMessage = null

        if (isLockedOut) {
            errorMessage = "Terlalu banyak percobaan gagal. Silakan tunggu $remainingLockoutSeconds detik."
            return
        }

        val trimmedPin = inputPin.trim()
        if (trimmedPin.isEmpty()) {
            errorMessage = "PIN tidak boleh kosong."
            return
        }

        isLoading = true
        val isValid = authManager.verifyPin(trimmedPin)
        isLoading = false

        if (isValid) {
            onLoginSuccess()
        } else {
            if (authManager.isLockedOut()) {
                errorMessage = "Terlalu banyak percobaan gagal. Silakan tunggu ${authManager.getRemainingLockoutSeconds()} detik."
            } else {
                errorMessage = "PIN tidak valid. Silakan coba lagi."
            }
        }
    }

    // Auto-prompt biometric as quick entry if biometric is ready and user is not locked out
    LaunchedEffect(Unit) {
        if (isBiometricReady && !isLockedOut) {
            triggerBiometricLogin()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen_root")
    ) {
        // 1. FULL SCREEN VIDEO BACKGROUND (1000487036.mp4)
        VideoBackgroundPlayer(
            modifier = Modifier.fillMaxSize()
        )

        // 2. DARK TRANSPARENT GRADIENT OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99050508),
                            Color(0x66090A0E),
                            Color(0xAA0A0B10),
                            Color(0xF508090D)
                        )
                    )
                )
        )

        // 3. FOREGROUND LOGIN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // BRAND & TITLE: "APP LOGIN"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp, bottom = 12.dp)
            ) {
                // Futuristic Glowing Shield Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC10121A))
                        .border(
                            width = 1.3.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF10B981))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "App Security Shield",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title: APP LOGIN (Requested)
                Text(
                    text = "APP LOGIN",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 1.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Masukkan PIN aplikasi untuk melanjutkan",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp,
                        letterSpacing = 0.3.sp
                    )
                )
            }

            // NOTIFICATION BANNER (ERROR / LOCKOUT)
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLockedOut) Color(0xEE2A1A08) else Color(0xEE2A0808))
                        .border(1.dp, if (isLockedOut) Color(0xFFFFB300) else Color(0xFFEF4444), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("login_error_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLockedOut) Icons.Default.Timer else Icons.Default.ErrorOutline,
                            contentDescription = "Status",
                            tint = if (isLockedOut) Color(0xFFFFB300) else Color(0xFFF87171),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isLockedOut) Color(0xFFFEF3C7) else Color(0xFFFECACA),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // CONTROLS CONTAINER:
            // [ PIN ]
            // [ LOGIN ]
            // [ USE BIOMETRIC ]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xEA0D0F16))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0x4438BDF8), Color(0x22FFFFFF))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // 1. [ PIN ] INPUT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PIN APLIKASI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                        if (isLockedOut) {
                            Text(
                                text = "Terkunci ($remainingLockoutSeconds s)",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputPin,
                        onValueChange = {
                            if (it.length <= 12) {
                                inputPin = it
                                errorMessage = null
                            }
                        },
                        label = { Text("PIN") },
                        placeholder = { Text("••••") },
                        singleLine = true,
                        enabled = !isLockedOut,
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { handlePinLogin() }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "PIN Lock",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0x44FFFFFF),
                            focusedLabelColor = Color(0xFF38BDF8),
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_login_pin")
                            .testTag("input_login_key")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. [ LOGIN ] BUTTON
                    Button(
                        onClick = { handlePinLogin() },
                        enabled = !isLoading && !isLockedOut,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            disabledContainerColor = Color(0xFF1E293B),
                            contentColor = Color.White,
                            disabledContentColor = Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_submit_login")
                            .testTag("btn_submit_login_key")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "LOGIN",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    // 3. [ USE BIOMETRIC ] BUTTON
                    if (isBiometricReady) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { triggerBiometricLogin() },
                            enabled = !isLockedOut,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F766E),
                                disabledContainerColor = Color(0xFF134E4A).copy(alpha = 0.5f),
                                contentColor = Color.White,
                                disabledContentColor = Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_use_biometric")
                                .testTag("btn_login_sidik_jari")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometric Icon",
                                tint = Color(0xFF2DD4BF),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "USE BIOMETRIC",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }
                    } else {
                        // Helpful note if biometric hardware is not present on this device
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sensor biometrik tidak tersedia di perangkat ini — gunakan PIN",
                            color = Color(0xFF64748B),
                            fontSize = 10.5.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(
                        color = Color(0x22FFFFFF),
                        thickness = 0.6.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. ASSISTANCE LINK (WHATSAPP)
                    Button(
                        onClick = {
                            LinkHandler.openWhatsApp(context, Config.WA_KEY_URL)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_minta_key_wa")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp Icon",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BANTUAN LOGIN VIA WHATSAPP",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.3.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Admin Support: +62 878-3546-1585",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF71717A),
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
