package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

/**
 * Premium Full Screen Video Background Login Screen for DYNIMETIZE ZX.
 * Layout:
 * [ VIDEO FULL SCREEN / BACKGROUND (1000487036.mp4) ]
 * [ DARK TRANSPARENT OVERLAY ]
 * [ BRAND: ZUCCHERO XANN ]
 * [ SECURE LOGIN ]
 * [ LOGIN DENGAN SIDIK JARI ] (Official Android BiometricPrompt)
 * [ LOGIN DENGAN KEY ] (DKVX59HH / ZXKUTS5)
 * [ MINTA KEY VIA WHATSAPP ] (+62 878-3546-1585)
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

    val isBiometricSupported = remember { authManager.isBiometricAvailable() }

    // If biometric is not available on device, automatically show the Key login input
    var isKeyModeSelected by remember { mutableStateOf(!isBiometricSupported) }
    var inputKey by remember { mutableStateOf("") }
    var keyVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    BackHandler(enabled = isKeyModeSelected && isBiometricSupported) {
        isKeyModeSelected = false
        errorMessage = null
    }

    fun triggerBiometricPrompt() {
        val activity = context as? FragmentActivity
        if (activity == null) {
            errorMessage = "Aktivitas tidak mendukung autentikasi biometrik."
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
                    // If user canceled or dismissed, do not display a fatal error
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        errorMessage = errString.toString()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    errorMessage = "Sidik jari tidak cocok. Silakan coba lagi."
                }
            }

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autentikasi Sidik Jari")
                .setSubtitle("Sentuh sensor sidik jari untuk masuk")
                .setNegativeButtonText("Gunakan Key")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            errorMessage = "Biometrik tidak dapat diakses: ${e.localizedMessage}"
        }
    }

    fun handleKeyLogin() {
        focusManager.clearFocus()
        errorMessage = null

        if (inputKey.isBlank()) {
            errorMessage = "Key tidak boleh kosong."
            return
        }

        isLoading = true

        val isValid = authManager.verifyKey(inputKey)
        isLoading = false

        if (isValid) {
            onLoginSuccess()
        } else {
            errorMessage = "Key tidak valid"
        }
    }

    // Auto-prompt Biometric on first screen display if hardware & enrollment are ready
    LaunchedEffect(Unit) {
        if (isBiometricSupported) {
            triggerBiometricPrompt()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen_root")
    ) {
        // 1. FULL SCREEN VIDEO BACKGROUND (1000487036.mp4)
        // Autoplay, looping, muted, center-cropped to fill screen completely
        VideoBackgroundPlayer(
            modifier = Modifier.fillMaxSize()
        )

        // 2. DARK TRANSPARENT GRADIENT OVERLAY
        // Keeps the stunning Gojo anime visual visible while making UI text and buttons 100% legible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99050508), // 60% black top
                            Color(0x55090A0E), // 33% black middle
                            Color(0x990A0B10), // 60% black lower-middle
                            Color(0xF508090D)  // 96% black bottom for controls
                        )
                    )
                )
        )

        // 3. FOREGROUND LOGIN CONTENT (Centered Brand & Controls)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SPACER & BRAND IDENTITY
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // Futuristic Glowing Shield Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC10121A))
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF6366F1))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Shield",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // BRAND: "ZUCCHERO XANN"
                Text(
                    text = "ZUCCHERO XANN",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 25.sp,
                        letterSpacing = 1.2.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // SUBTITLE: "SECURE LOGIN"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x330284C7))
                        .border(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SECURE LOGIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF7DD3FC),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.6.sp
                        )
                    )
                }

                Text(
                    text = Config.APP_NAME,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // MIDDLE NOTIFICATION BANNER (ERROR MESSAGES)
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
                        .background(Color(0xDD450A0A))
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("login_error_banner")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFFECACA),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }
            }

            // BOTTOM CONTROLS CARD (GLASSMORPHIC DARK CONTAINER)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xE6101218))
                    .border(
                        width = 1.dp,
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // 1. LOGIN DENGAN SIDIK JARI (Priority button when supported)
                    if (isBiometricSupported) {
                        Button(
                            onClick = { triggerBiometricPrompt() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_login_sidik_jari")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Fingerprint Icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Login dengan Sidik Jari",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    letterSpacing = 0.4.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 2. LOGIN DENGAN KEY
                    if (!isKeyModeSelected && isBiometricSupported) {
                        // Secondary button to reveal Key input
                        OutlinedButton(
                            onClick = { isKeyModeSelected = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                brush = Brush.linearGradient(
                                    listOf(Color(0x66FFFFFF), Color(0x33FFFFFF))
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_pilih_login_key")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Key Icon",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "LOGIN DENGAN KEY",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    } else {
                        // Key input form
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LOGIN DENGAN KEY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF94A3B8),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.6.sp
                                    )
                                )

                                if (isBiometricSupported) {
                                    IconButton(
                                        onClick = { isKeyModeSelected = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Tutup Form Key",
                                            tint = Color(0xFF71717A),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = inputKey,
                                onValueChange = {
                                    inputKey = it
                                    errorMessage = null
                                },
                                label = { Text("Masukkan Key") },
                                placeholder = { Text("Ketik key akses...") },
                                singleLine = true,
                                visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Ascii,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { handleKeyLogin() }
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { keyVisible = !keyVisible }) {
                                        Icon(
                                            imageVector = if (keyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Visibility",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
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
                                    .testTag("input_login_key")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // BUTTON "LOGIN"
                            Button(
                                onClick = { handleKeyLogin() },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_submit_login_key")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "LOGIN",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    HorizontalDivider(
                        color = Color(0x22FFFFFF),
                        thickness = 0.6.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. BUTTON "MINTA KEY VIA WHATSAPP"
                    Button(
                        onClick = {
                            LinkHandler.openWhatsApp(context, Config.WA_KEY_URL)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_minta_key_wa")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp Icon",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MINTA KEY VIA WHATSAPP",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.3.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "WhatsApp Owner: +62 878-3546-1585",
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
