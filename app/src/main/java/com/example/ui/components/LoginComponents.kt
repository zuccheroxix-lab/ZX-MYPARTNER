package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.AuthManager
import com.example.ui.theme.BurgundyBorder
import com.example.ui.theme.BurgundyBright
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.BurgundyGlow
import com.example.ui.theme.BurgundyPrimary
import kotlin.math.sqrt

enum class LoginScreenView {
    METHOD_SELECTION,
    PIN_PAGE,
    PATTERN_PAGE,
    FINGERPRINT_PAGE
}

/**
 * 1. METHOD SELECTION: Displays 3 distinct, premium quick login buttons:
 *    [ PIN LOGIN ] | [ PATTERN LOGIN ] | [ FINGERPRINT LOGIN ]
 *    Plus WhatsApp assistance option.
 */
@Composable
fun LoginMethodSelector(
    onSelectMethod: (LoginScreenView) -> Unit,
    onOpenWhatsApp: () -> Unit,
    isBiometricAvailable: Boolean,
    isLockedOut: Boolean,
    lockoutSeconds: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("login_method_selector"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PILIH METODE MASUK",
            color = Color(0xFFF43F5E),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
        )
        Text(
            text = "Sentuh salah satu metode autentikasi resmi di bawah",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // BUTTON 1: PIN LOGIN
        LoginQuickCard(
            title = "PIN LOGIN",
            subtitle = "Buka dengan keypad angka 4-digit khusus APK",
            badge = "PIN: 6745",
            badgeColor = Color(0xFF0284C7),
            icon = Icons.Default.Pin,
            iconTint = Color(0xFF38BDF8),
            borderGradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
            bgGradient = listOf(Color(0xEE121826), Color(0xEE0A0E17)),
            enabled = !isLockedOut,
            testTag = "btn_nav_pin_login",
            onClick = { onSelectMethod(LoginScreenView_PIN) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // BUTTON 2: PATTERN LOGIN
        LoginQuickCard(
            title = "POLA LOGIN",
            subtitle = "Hubungkan titik pola 3×3 sentuhan jari",
            badge = "3×3 PATTERN",
            badgeColor = Color(0xFFBE123C),
            icon = Icons.Default.Pattern,
            iconTint = Color(0xFFFB7185),
            borderGradient = listOf(BurgundyPrimary, BurgundyGlow),
            bgGradient = listOf(Color(0xEE1A0A12), Color(0xEE0D0609)),
            enabled = !isLockedOut,
            testTag = "btn_nav_pattern_login",
            onClick = { onSelectMethod(LoginScreenView_PATTERN) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // BUTTON 3: FINGERPRINT LOGIN
        LoginQuickCard(
            title = "SIDIK JARI",
            subtitle = if (isBiometricAvailable) "Verifikasi cepat via sensor Android BiometricPrompt" else "Sensor sistem belum terdaftar / gunakan PIN",
            badge = if (isBiometricAvailable) "BIOMETRIC READY" else "CEK SENSOR",
            badgeColor = if (isBiometricAvailable) Color(0xFF059669) else Color(0xFFD97706),
            icon = Icons.Default.Fingerprint,
            iconTint = if (isBiometricAvailable) Color(0xFF34D399) else Color(0xFFFBBF24),
            borderGradient = if (isBiometricAvailable) listOf(Color(0xFF059669), Color(0xFF34D399)) else listOf(Color(0xFFD97706), Color(0xFFFBBF24)),
            bgGradient = listOf(Color(0xEE0A1814), Color(0xEE07100D)),
            enabled = !isLockedOut,
            testTag = "btn_nav_fingerprint_login",
            onClick = { onSelectMethod(LoginScreenView_FINGERPRINT) }
        )
    }
}

// Helper aliases to make syntax clean
val LoginScreenView_PIN = LoginScreenView.PIN_PAGE
val LoginScreenView_PATTERN = LoginScreenView.PATTERN_PAGE
val LoginScreenView_FINGERPRINT = LoginScreenView.FINGERPRINT_PAGE

@Composable
fun LoginQuickCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    borderGradient: List<Color>,
    bgGradient: List<Color>,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(bgGradient))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(borderGradient),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.8.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .border(0.6.dp, badgeColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * 2. PIN LOGIN PAGE:
 * Dedicated screen with numeric keypad (0-9, Backspace, Clear),
 * visual dots (••••), official default PIN 6745, and confirmation.
 */
@Composable
fun PinLoginPage(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    isLockedOut: Boolean,
    lockoutSeconds: Int,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun submitPin() {
        if (isLockedOut) {
            errorMessage = "Autentikasi terkunci sementara ($lockoutSeconds s)"
            return
        }
        if (enteredPin.length < 4) {
            errorMessage = "PIN harus terdiri dari minimal 4 digit angka."
            return
        }

        isLoading = true
        val valid = authManager.verifyPin(enteredPin)
        isLoading = false

        if (valid) {
            onLoginSuccess()
        } else {
            enteredPin = ""
            if (authManager.isLockedOut()) {
                errorMessage = "Terlalu banyak percobaan gagal. Silakan tunggu ${authManager.getRemainingLockoutSeconds()} detik."
            } else {
                errorMessage = "PIN salah. Coba lagi (PIN default: 6745)."
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pin_login_page"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_pin_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "HALAMAN PIN LOGIN",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "PIN khusus APK — bukan PIN layar HP Anda",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // PIN Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            for (i in 0 until 4) {
                val filled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (filled) Color(0xFF38BDF8) else Color(0xFF1E293B))
                        .border(
                            width = 1.2.dp,
                            color = if (filled) Color(0xFF0284C7) else Color(0xFF334155),
                            shape = CircleShape
                        )
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFF87171),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dedicated Numeric Keypad (1 to 9, C, 0, ⌫)
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "DEL")
            )

            keyRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when (key) {
                                        "C", "DEL" -> Color(0xFF1E212D)
                                        else -> Color(0xFF131722)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (key) {
                                        "C", "DEL" -> Color(0xFF33384C)
                                        else -> Color(0xFF262D40)
                                    },
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = !isLockedOut && !isLoading) {
                                    errorMessage = null
                                    when (key) {
                                        "C" -> enteredPin = ""
                                        "DEL" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                        else -> if (enteredPin.length < 8) enteredPin += key
                                    }
                                }
                                .testTag("keypad_btn_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "DEL") {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Hapus",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    color = if (key == "C") Color(0xFFF87171) else Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Confirm Button
        Button(
            onClick = { submitPin() },
            enabled = !isLockedOut && !isLoading && enteredPin.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0284C7),
                disabledContainerColor = Color(0xFF1E293B),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_pin_confirm")
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("KONFIRMASI PIN", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }
        }
    }
}

/**
 * 3. PATTERN LOGIN PAGE:
 * Full interactive 3x3 pattern screen with finger gesture connection,
 * dynamic canvas connecting lines, custom pattern creation/verification,
 * and secure SHA-256 storage.
 */
@Composable
fun PatternLoginPage(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    isLockedOut: Boolean,
    lockoutSeconds: Int,
    modifier: Modifier = Modifier
) {
    val selectedPoints = remember { mutableStateListOf<Int>() }
    var currentTouchPos by remember { mutableStateOf<Offset?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSetupMode by remember { mutableStateOf(!authManager.hasPatternSet()) }
    var firstPatternSetup by remember { mutableStateOf<List<Int>?>(null) }
    var setupStep by remember { mutableStateOf(if (isSetupMode) "Atur Pola Baru (Hubungkan minimal 4 titik)" else "Masukkan Pola") }

    fun checkAndSubmitPattern() {
        if (selectedPoints.size < 4) {
            errorMessage = "Hubungkan minimal 4 titik pola."
            selectedPoints.clear()
            return
        }

        if (isSetupMode) {
            if (firstPatternSetup == null) {
                firstPatternSetup = selectedPoints.toList()
                selectedPoints.clear()
                setupStep = "Konfirmasi Pola Baru (Gambar ulang pola yang sama)"
                errorMessage = null
            } else {
                if (firstPatternSetup == selectedPoints.toList()) {
                    authManager.setPattern(selectedPoints.toList())
                    authManager.setAuthenticated(true, method = "PATTERN")
                    onLoginSuccess()
                } else {
                    errorMessage = "Pola konfirmasi tidak cocok. Silakan atur ulang dari awal."
                    firstPatternSetup = null
                    selectedPoints.clear()
                    setupStep = "Atur Pola Baru (Hubungkan minimal 4 titik)"
                }
            }
        } else {
            val valid = authManager.verifyPattern(selectedPoints.toList())
            if (valid) {
                onLoginSuccess()
            } else {
                selectedPoints.clear()
                if (authManager.isLockedOut()) {
                    errorMessage = "Terlalu banyak percobaan gagal. Silakan tunggu ${authManager.getRemainingLockoutSeconds()} detik."
                } else {
                    errorMessage = "Pola salah. Coba lagi atau atur pola baru."
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pattern_login_page"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_pattern_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "HALAMAN POLA LOGIN (3×3)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = setupStep,
                    color = Color(0xFFFB7185),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFF87171),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3x3 Grid Pattern Canvas
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F0B13))
                .border(1.2.dp, BurgundyBorder, RoundedCornerShape(20.dp))
                .pointerInput(isLockedOut) {
                    if (!isLockedOut) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                errorMessage = null
                                selectedPoints.clear()
                                val pt = findDotAt(offset, size.width.toFloat(), size.height.toFloat())
                                if (pt != null) {
                                    selectedPoints.add(pt)
                                }
                                currentTouchPos = offset
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentTouchPos = change.position
                                val pt = findDotAt(change.position, size.width.toFloat(), size.height.toFloat())
                                if (pt != null && !selectedPoints.contains(pt)) {
                                    selectedPoints.add(pt)
                                }
                            },
                            onDragEnd = {
                                currentTouchPos = null
                                checkAndSubmitPattern()
                            },
                            onDragCancel = {
                                currentTouchPos = null
                                selectedPoints.clear()
                            }
                        )
                    }
                }
                .testTag("pattern_canvas_area")
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val dotPositions = (0 until 9).map { idx ->
                    val col = idx % 3
                    val row = idx / 3
                    val x = (col + 1) * (w / 4f)
                    val y = (row + 1) * (h / 4f)
                    Offset(x, y)
                }

                // Draw connected lines between selected dots
                for (i in 0 until selectedPoints.size - 1) {
                    val p1 = dotPositions[selectedPoints[i]]
                    val p2 = dotPositions[selectedPoints[i + 1]]
                    drawLine(
                        color = BurgundyGlow,
                        start = p1,
                        end = p2,
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw active line to finger drag position
                if (selectedPoints.isNotEmpty() && currentTouchPos != null) {
                    val lastPt = dotPositions[selectedPoints.last()]
                    drawLine(
                        color = BurgundyBright.copy(alpha = 0.7f),
                        start = lastPt,
                        end = currentTouchPos!!,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw 9 Dots
                dotPositions.forEachIndexed { idx, pt ->
                    val isSelected = selectedPoints.contains(idx)
                    drawCircle(
                        color = if (isSelected) BurgundyGlow else Color(0xFF33202A),
                        radius = if (isSelected) 22f else 14f,
                        center = pt
                    )
                    drawCircle(
                        color = if (isSelected) Color.White else Color(0xFF9F1239),
                        radius = if (isSelected) 10f else 6f,
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset or Configure Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedPoints.clear()
                    currentTouchPos = null
                    errorMessage = null
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Bersihkan", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    isSetupMode = !isSetupMode
                    firstPatternSetup = null
                    selectedPoints.clear()
                    setupStep = if (isSetupMode) "Atur Pola Baru (Hubungkan minimal 4 titik)" else "Masukkan Pola"
                    errorMessage = null
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isSetupMode) "Batal Atur" else "Buat Pola Baru", fontSize = 12.sp)
            }
        }
    }
}

private fun findDotAt(pos: Offset, width: Float, height: Float, threshold: Float = 60f): Int? {
    for (idx in 0 until 9) {
        val col = idx % 3
        val row = idx / 3
        val x = (col + 1) * (width / 4f)
        val y = (row + 1) * (height / 4f)
        val dist = sqrt((pos.x - x) * (pos.x - x) + (pos.y - y) * (pos.y - y))
        if (dist <= threshold) {
            return idx
        }
    }
    return null
}

/**
 * 4. FINGERPRINT LOGIN PAGE:
 * Dedicated screen for official AndroidX BiometricPrompt fingerprint scanner.
 * Clearly diagnoses device capability, explains requirements honestly,
 * and provides instant fallback to PIN if hardware is missing or un-enrolled.
 */
@Composable
fun FingerprintLoginPage(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
    onFallbackToPin: () -> Unit,
    isLockedOut: Boolean,
    lockoutSeconds: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var biometricStatus by remember { mutableStateOf(authManager.checkBiometricStatus()) }

    fun triggerPrompt() {
        if (isLockedOut) {
            errorMessage = "Autentikasi terkunci sementara ($lockoutSeconds s)"
            return
        }

        val activity = context as? FragmentActivity
        if (activity == null) {
            errorMessage = "Aktivitas tidak mendukung autentikasi biometrik."
            return
        }

        if (biometricStatus != BiometricManager.BIOMETRIC_SUCCESS) {
            errorMessage = when (biometricStatus) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Perangkat ini tidak memiliki sensor sidik jari perangkat keras."
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Sensor biometrik sedang sibuk atau tidak tersedia saat ini."
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Belum ada sidik jari yang didaftarkan di Pengaturan Keamanan HP Anda."
                else -> "Biometrik sistem Android tidak dapat digunakan."
            }
            return
        }

        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    errorMessage = null
                    authManager.setAuthenticated(true, method = "FINGERPRINT")
                    onLoginSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED
                    ) {
                        errorMessage = errString.toString()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    errorMessage = "Sidik jari tidak cocok dengan sidik jari sistem Android."
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autentikasi Sidik Jari ZX")
                .setSubtitle("Sentuh sensor sidik jari perangkat Anda")
                .setNegativeButtonText("Gunakan PIN Aplikasi")
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            errorMessage = "Gagal membuka sensor biometrik: ${e.localizedMessage}"
        }
    }

    LaunchedEffect(Unit) {
        if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS && !isLockedOut) {
            triggerPrompt()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fingerprint_login_page"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_fingerprint_back")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "HALAMAN SIDIK JARI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "AndroidX BiometricPrompt Resmi",
                    color = Color(0xFF34D399),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Scanner Icon with Pulse Ring
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS)
                        Color(0xFF064E3B).copy(alpha = 0.5f)
                    else
                        Color(0xFF78350F).copy(alpha = 0.4f)
                )
                .border(
                    width = 2.dp,
                    color = if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) Color(0xFF10B981) else Color(0xFFF59E0B),
                    shape = CircleShape
                )
                .clickable { triggerPrompt() }
                .testTag("btn_touch_fingerprint_sensor"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Sensor Sidik Jari",
                tint = if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) Color(0xFF34D399) else Color(0xFFFBBF24),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS)
                "Sentuh ikon di atas atau sensor HP untuk memindai"
            else
                "Sensor Biometrik Perangkat Tidak Siap",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp
        )

        // Honest explanation regarding hardware/enrollment
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF131722))
                .border(1.dp, Color(0xFF262D40), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) Icons.Default.Security else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS)
                            "Status Hardware: Terverifikasi & Terdaftar"
                        else
                            "Persyaratan Biometrik Android",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when (biometricStatus) {
                        BiometricManager.BIOMETRIC_SUCCESS ->
                            "Perangkat mendukung Android BiometricPrompt dan memiliki sidik jari terdaftar di keamanan sistem."
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                            "Perangkat mendukung sensor sidik jari, tetapi belum ada sidik jari yang didaftarkan di Pengaturan Keamanan HP. Silakan daftarkan sidik jari di pengaturan Android atau gunakan PIN aplikasi."
                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                            "Perangkat ini tidak memiliki sensor sidik jari hardware. Aplikasi tidak membuat pemindai palsu. Silakan gunakan PIN aplikasi (6745) atau Pola."
                        else ->
                            "Sensor biometrik tidak dapat diakses saat ini. Anda dapat langsung masuk menggunakan PIN aplikasi 6745."
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage ?: "",
                color = Color(0xFFF87171),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Trigger / Fallback Buttons
        if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) {
            Button(
                onClick = { triggerPrompt() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_trigger_biometric_scan")
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("PINDAI SIDIK JARI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = onFallbackToPin,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("btn_fallback_to_pin")
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("GUNAKAN ALTERNATIF PIN APLIKASI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
