package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.Config
import com.example.ui.components.FingerprintLoginPage
import com.example.ui.components.LinkHandler
import com.example.ui.components.LoginMethodSelector
import com.example.ui.components.LoginScreenView
import com.example.ui.components.PatternLoginPage
import com.example.ui.components.PinLoginPage
import com.example.ui.components.VideoBackgroundPlayer
import com.example.ui.theme.BurgundyBorder
import com.example.ui.theme.BurgundyBright
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.BurgundyGlow
import com.example.ui.theme.BurgundyPrimary
import kotlinx.coroutines.delay

/**
 * DYNIMETIZE ZX — 3 QUICK LOGIN BUTTONS
 *
 * Distinct Architecture:
 * 1. METHOD_SELECTION: Tiga tombol utama premium (PIN LOGIN, POLA LOGIN, SIDIK JARI)
 * 2. PIN_PAGE: Halaman keypad angka khusus APK (PIN default: 6745)
 * 3. PATTERN_PAGE: Halaman canvas pola sentuh jari 3x3 interaktif
 * 4. FINGERPRINT_PAGE: Halaman AndroidX BiometricPrompt resmi dengan diagnosa jujur
 *
 * Serta dukungan fallback WhatsApp login bantuan yang tetap terjaga.
 */
@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var currentView by remember { mutableStateOf(LoginScreenView.METHOD_SELECTION) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isBiometricReady = remember { authManager.isBiometricAvailable() }
    val remainingLockoutSeconds by authManager.lockoutTimeRemaining.collectAsState()
    val isLockedOut = remainingLockoutSeconds > 0

    // BackHandler: Returns to method selection if currently inside a sub-login page
    BackHandler(enabled = currentView != LoginScreenView.METHOD_SELECTION) {
        currentView = LoginScreenView.METHOD_SELECTION
        errorMessage = null
    }

    // Lockout countdown timer
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen_root")
    ) {
        // 1. FULL SCREEN VIDEO BACKGROUND (1000487036.mp4)
        VideoBackgroundPlayer(
            modifier = Modifier.fillMaxSize()
        )

        // 2. DARK BURGUNDY GRADIENT OVERLAY (ZX Premium Theme)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xBB0A0205),
                            Color(0x9917050D),
                            Color(0xCC0D0408),
                            Color(0xF5060103)
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
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // BRAND & TITLE: "DYNIMETIZE ZX"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 28.dp, bottom = 10.dp)
            ) {
                // Futuristic Glowing Burgundy Shield Badge
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(Color(0xEE1E0710))
                        .border(
                            width = 1.4.dp,
                            brush = Brush.linearGradient(
                                listOf(BurgundyGlow, BurgundyBright)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "ZX Security Shield",
                        tint = BurgundyGlow,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "DYNIMETIZE ZX",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        letterSpacing = 2.0.sp
                    ),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "High Performance Gaming Engine • Secure Access",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFFB7185),
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.SemiBold
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
                        .background(if (isLockedOut) Color(0xEE2A1A08) else Color(0xEE3B0716))
                        .border(1.dp, if (isLockedOut) Color(0xFFFFB300) else BurgundyGlow, RoundedCornerShape(12.dp))
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
                            tint = if (isLockedOut) Color(0xFFFFB300) else Color(0xFFFB7185),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isLockedOut) Color(0xFFFEF3C7) else Color(0xFFFECDD3),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // DYNAMIC LOGIN CONTAINER (Switches smoothly between Method Selector, PIN, Pattern, Fingerprint)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xF010060B))
                    .border(
                        width = 1.1.dp,
                        brush = Brush.verticalGradient(
                            listOf(BurgundyBorder, Color(0x33BE123C))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp)
            ) {
                AnimatedContent(
                    targetState = currentView,
                    transitionSpec = {
                        if (targetState != LoginScreenView.METHOD_SELECTION) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "login_screen_transition"
                ) { targetView ->
                    when (targetView) {
                        LoginScreenView.METHOD_SELECTION -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LoginMethodSelector(
                                    onSelectMethod = { selected ->
                                        errorMessage = null
                                        currentView = selected
                                    },
                                    onOpenWhatsApp = {
                                        LinkHandler.openWhatsApp(context, Config.WA_KEY_URL)
                                    },
                                    isBiometricAvailable = isBiometricReady,
                                    isLockedOut = isLockedOut,
                                    lockoutSeconds = remainingLockoutSeconds
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0x33BE123C), thickness = 0.8.dp)
                                Spacer(modifier = Modifier.height(14.dp))

                                // BANTUAN VIA WHATSAPP (Preserved Legacy Access)
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
                                        text = "BANTUAN VIA WHATSAPP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        letterSpacing = 0.4.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Support: +62 878-3546-1585",
                                    color = Color(0xFF71717A),
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }

                        LoginScreenView.PIN_PAGE -> {
                            PinLoginPage(
                                authManager = authManager,
                                onLoginSuccess = onLoginSuccess,
                                onBack = { currentView = LoginScreenView.METHOD_SELECTION },
                                isLockedOut = isLockedOut,
                                lockoutSeconds = remainingLockoutSeconds
                            )
                        }

                        LoginScreenView.PATTERN_PAGE -> {
                            PatternLoginPage(
                                authManager = authManager,
                                onLoginSuccess = onLoginSuccess,
                                onBack = { currentView = LoginScreenView.METHOD_SELECTION },
                                isLockedOut = isLockedOut,
                                lockoutSeconds = remainingLockoutSeconds
                            )
                        }

                        LoginScreenView.FINGERPRINT_PAGE -> {
                            FingerprintLoginPage(
                                authManager = authManager,
                                onLoginSuccess = onLoginSuccess,
                                onBack = { currentView = LoginScreenView.METHOD_SELECTION },
                                onFallbackToPin = { currentView = LoginScreenView.PIN_PAGE },
                                isLockedOut = isLockedOut,
                                lockoutSeconds = remainingLockoutSeconds
                            )
                        }
                    }
                }
            }
        }
    }
}
