package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ApplyResult
import com.example.data.GameSmoothnessManager
import com.example.data.ImportedZipConfig
import com.example.data.SmoothnessConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsEsports

private val GoldAccent = Color(0xFFFFB300)
private val CyanAccent = Color(0xFF00E5FF)
private val GreenAccent = Color(0xFF10B981)
private val DarkCardBg = Color(0xFF141722)
private val BorderColor = Color(0xFF262C3E)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSmoothnessScreen(
    onBack: () -> Unit = {},
    onNavigateToPage: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val smoothnessManager = remember { GameSmoothnessManager(context) }
    val savedConfig by smoothnessManager.config.collectAsState()
    val scope = rememberCoroutineScope()

    var deviceInfo by remember { mutableStateOf(smoothnessManager.getDeviceInfo()) }
    val scrollState = rememberScrollState()

    // Form inputs state
    var selectedMode by remember { mutableStateOf(savedConfig.mode) }
    var selectedHz by remember { mutableIntStateOf(savedConfig.targetRefreshRate) }
    var selectedTouchProfile by remember { mutableStateOf(savedConfig.touchProfile) }
    var selectedHaptic by remember { mutableStateOf(savedConfig.hapticIntensity) }
    var selectedDpiPreset by remember { mutableStateOf(savedConfig.dpiPreset) }
    var manualDpiText by remember { mutableStateOf(savedConfig.customDpi.toString()) }
    var manualResolutionText by remember { mutableStateOf(savedConfig.customResolution) }
    var minimalPostProcessing by remember { mutableStateOf(savedConfig.minimalPostProcessing) }

    // ZIP Import states
    var isImportingZip by remember { mutableStateOf(false) }
    var importedZipInfo by remember { mutableStateOf<ImportedZipConfig?>(null) }
    var zipErrorMessage by remember { mutableStateOf<String?>(null) }

    // Countdown & Apply states
    var showCountdownDialog by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }
    var applyResult by remember { mutableStateOf<ApplyResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    // Storage Access Framework launcher for ZIP files
    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isImportingZip = true
            zipErrorMessage = null
            scope.launch {
                val result = smoothnessManager.parseZipConfig(uri)
                isImportingZip = false
                result.fold(
                    onSuccess = { config ->
                        importedZipInfo = config
                        config.dpi?.let {
                            manualDpiText = it.toString()
                            selectedDpiPreset = "CUSTOM"
                        }
                        config.resolution?.let {
                            manualResolutionText = it
                        }
                        config.mode?.let {
                            selectedMode = it
                        }
                        config.refreshRate?.let { hz ->
                            if (deviceInfo.supportedRefreshRates.contains(hz)) {
                                selectedHz = hz
                            }
                        }
                        Toast.makeText(context, "Konfigurasi berhasil dibaca dari ZIP!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        zipErrorMessage = error.localizedMessage ?: "File ZIP tidak kompatibel atau rusak."
                    }
                )
            }
        }
    }

    BackHandler {
        if (showCountdownDialog) {
            countdownJob?.cancel()
            showCountdownDialog = false
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        deviceInfo = smoothnessManager.getDeviceInfo()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0E))
            .testTag("game_smoothness_screen")
    ) {
        // TOP APP BAR
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "GAME SMOOTHNESS",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Touch, Refresh Rate, DPI & Resolution Engine",
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        deviceInfo = smoothnessManager.getDeviceInfo()
                        Toast.makeText(context, "Data hardware layar diperbarui", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = GoldAccent
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0D0F17)
            )
        )

        // Horizontal Quick Jump Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToPage(0) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("← Beranda", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(2) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Game Space →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // MAIN CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ZIP IMPORT RESULT BANNER
            if (importedZipInfo != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF064E3B))
                        .border(1.dp, GreenAccent, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Konfigurasi ZIP Terbaca",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "File: ${importedZipInfo?.parsedEntryName} • DPI: ${importedZipInfo?.dpi ?: "-"} • Res: ${importedZipInfo?.resolution ?: "-"}",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { importedZipInfo = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color(0xFFA7F3D0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ZIP ERROR BANNER
            if (zipErrorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF450A0A))
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = zipErrorMessage ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        IconButton(
                            onClick = { zipErrorMessage = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 1. MODE SELECTION
            SectionHeader(
                title = "TUNING MODE",
                subtitle = "Pilih profil prioritas frame pacing dan stabilitas rendering"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModeSelectionCard(
                    modifier = Modifier.weight(1f),
                    title = "Smooth Mode",
                    badge = "Rekomendasi",
                    badgeColor = GreenAccent,
                    description = "Prioritas frame pacing konsisten, touch stabil, dan suhu adem tanpa throttling.",
                    isSelected = selectedMode == "SMOOTH",
                    accentColor = GreenAccent,
                    onClick = {
                        selectedMode = "SMOOTH"
                        minimalPostProcessing = false
                        smoothnessManager.triggerHapticFeedback(selectedHaptic)
                    }
                )

                ModeSelectionCard(
                    modifier = Modifier.weight(1f),
                    title = "Performance",
                    badge = "Unthrottled",
                    badgeColor = Color(0xFFEF4444),
                    description = "Prioritas refresh rate tertinggi hardware dan minimal post-processing latency.",
                    isSelected = selectedMode == "PERFORMANCE",
                    accentColor = Color(0xFFEF4444),
                    onClick = {
                        selectedMode = "PERFORMANCE"
                        minimalPostProcessing = true
                        val maxHz = deviceInfo.supportedRefreshRates.maxOrNull() ?: 60
                        selectedHz = maxHz
                        smoothnessManager.triggerHapticFeedback(selectedHaptic)
                    }
                )
            }

            // 2. REFRESH RATE
            SectionHeader(
                title = "REFRESH RATE LAYAR",
                subtitle = "Berdasarkan kemampuan Display.Mode resmi perangkat Anda"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardBg)
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Refresh Rate Aktif Saat Ini",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${deviceInfo.currentRefreshRate.roundToInt()} Hz",
                                color = GoldAccent,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2333))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Didukung: ${deviceInfo.supportedRefreshRates.joinToString("/")} Hz",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "Target Refresh Rate:",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RateChip(
                            label = "Auto",
                            subLabel = "Default",
                            isSelected = selectedHz == 0,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedHz = 0
                                smoothnessManager.triggerHapticFeedback(selectedHaptic)
                            }
                        )

                        deviceInfo.supportedRefreshRates.forEach { hz ->
                            RateChip(
                                label = "$hz Hz",
                                subLabel = if (hz == deviceInfo.supportedRefreshRates.maxOrNull()) "Maks" else "Normal",
                                isSelected = selectedHz == hz,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedHz = hz
                                    smoothnessManager.triggerHapticFeedback(selectedHaptic)
                                }
                            )
                        }
                    }

                    // Shortcut to System Display Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF191D2B))
                            .clickable {
                                try {
                                    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Buka Pengaturan > Tampilan", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DisplaySettings,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buka Pengaturan Layar Sistem HP",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 3. TOUCH SETTINGS
            SectionHeader(
                title = "TOUCH SETTINGS",
                subtitle = "Tuning responsivitas sentuhan dan haptic feedback"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardBg)
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Profil Responsivitas Touch",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TouchProfileOption(
                            label = "Standard",
                            desc = "Bawaan OS",
                            isSelected = selectedTouchProfile == "STANDARD",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTouchProfile = "STANDARD"
                                smoothnessManager.triggerHapticFeedback(selectedHaptic)
                            }
                        )
                        TouchProfileOption(
                            label = "Ultra Smooth",
                            desc = "Pacing Halus",
                            isSelected = selectedTouchProfile == "ULTRA_SMOOTH",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTouchProfile = "ULTRA_SMOOTH"
                                smoothnessManager.triggerHapticFeedback(selectedHaptic)
                            }
                        )
                        TouchProfileOption(
                            label = "Instant",
                            desc = "Low-Latency",
                            isSelected = selectedTouchProfile == "INSTANT_RESPONSE",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedTouchProfile = "INSTANT_RESPONSE"
                                smoothnessManager.triggerHapticFeedback(selectedHaptic)
                            }
                        )
                    }

                    // Haptic Feedback
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Haptic Feedback Sentuhan",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "OFF" to "Nonaktif",
                                "SOFT" to "Ringan",
                                "BALANCED" to "Sedang",
                                "CRISP" to "Kuat"
                            ).forEach { (key, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedHaptic == key) GoldAccent.copy(alpha = 0.2f) else Color(0xFF1E2333))
                                        .border(1.dp, if (selectedHaptic == key) GoldAccent else BorderColor, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedHaptic = key
                                            smoothnessManager.triggerHapticFeedback(key)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selectedHaptic == key) GoldAccent else Color(0xFFCBD5E1),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selectedHaptic == key) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. DPI & RESOLUTION (MANUAL INPUT + IMPORT ZIP)
            SectionHeader(
                title = "DPI & RESOLUTION",
                subtitle = "Input manual resolusi, DPI, dan impor konfigurasi dari ZIP"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardBg)
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Native Hardware Specs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecItem(label = "Resolusi Asli", value = "${deviceInfo.resolutionWidth} x ${deviceInfo.resolutionHeight}")
                        SpecItem(label = "DPI Asli", value = "${deviceInfo.densityDpi} DPI")
                        SpecItem(label = "Lebar Terkecil", value = "${deviceInfo.smallestWidthDp} dp")
                    }

                    HorizontalDivider(color = BorderColor, thickness = 0.6.dp)

                    // IMPORT ZIP BUTTON
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Impor Pengaturan dari ZIP",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pilih file ZIP konfigurasi (JSON / properties) yang aman",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                try {
                                    zipPickerLauncher.launch(
                                        arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*")
                                    )
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Tidak dapat membuka pemilih file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isImportingZip,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            if (isImportingZip) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "IMPORT ZIP", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(color = BorderColor, thickness = 0.6.dp)

                    // MANUAL INPUT RESOLUTION
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Input Resolusi Secara Manual",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = manualResolutionText,
                            onValueChange = { manualResolutionText = it },
                            placeholder = { Text("Contoh: 1080x2400", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Text(
                            text = "Format: Lebar x Tinggi pixel (contoh 1080x2400, 720x1600, 1440x3200)",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )
                    }

                    // MANUAL INPUT DPI
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Input DPI Secara Manual",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = manualDpiText,
                            onValueChange = {
                                manualDpiText = it.filter { ch -> ch.isDigit() }
                                selectedDpiPreset = "CUSTOM"
                            },
                            placeholder = { Text("Contoh: 420", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        val parsedDpi = manualDpiText.toIntOrNull()
                        if (parsedDpi != null && parsedDpi in 120..800) {
                            val simulatedSw = (min(deviceInfo.resolutionWidth, deviceInfo.resolutionHeight) / (parsedDpi / 160f)).roundToInt()
                            Text(
                                text = "Estimasi Densitas: ${(parsedDpi / 160f * 100).roundToInt() / 100f}x • Smallest Width: $simulatedSw dp",
                                color = GoldAccent,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Text(
                                text = "Rentang yang disarankan: 160 - 640 DPI",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                        }
                    }

                    // DPI PRESET SHORTCUTS
                    Text(
                        text = "Atau Pilih Preset Cepat:",
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DpiChip(
                            label = "Native",
                            dpi = deviceInfo.densityDpi,
                            isSelected = selectedDpiPreset == "NATIVE",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedDpiPreset = "NATIVE"
                                manualDpiText = deviceInfo.densityDpi.toString()
                                smoothnessManager.triggerHapticFeedback(selectedHaptic)
                            }
                        )
                        DpiChip(
                            label = "Compact",
                            dpi = 380,
                            isSelected = selectedDpiPreset == "COMPACT_380",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedDpiPreset = "COMPACT_380"
                                manualDpiText = "380"
                                smoothnessManager.triggerHapticFeedback(selectedHaptic)
                            }
                        )
                        DpiChip(
                            label = "Balanced",
                            dpi = 411,
                            isSelected = selectedDpiPreset == "BALANCED_411",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedDpiPreset = "BALANCED_411"
                                manualDpiText = "411"
                                smoothnessManager.triggerHapticFeedback(selectedHaptic)
                            }
                        )
                        DpiChip(
                            label = "Crisp",
                            dpi = 460,
                            isSelected = selectedDpiPreset == "EXPANDED_460",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                selectedDpiPreset = "EXPANDED_460"
                                manualDpiText = "460"
                                smoothnessManager.triggerHapticFeedback(selectedHaptic)
                            }
                        )
                    }

                    // System Boundary & Transparency Info Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF191D2B))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Status Izin Sistem: ${if (deviceInfo.hasSecureSettingsPermission) "Secure Settings Diberikan" else "Standar Android (Aman)"}",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Android melindungi integritas sistem dengan membatasi perubahan DPI seluruh OS hanya via izin WRITE_SECURE_SETTINGS (ADB/Shizuku) atau menu resmi Opsi Pengembang (Lebar Terkecil).",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            Text(
                                text = "Buka Opsi Pengembang (Lebar Terkecil)",
                                color = GoldAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        try {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Aktifkan Opsi Pengembang di Pengaturan Android", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // 5. PREVIEW CONFIGURATION SUMMARY BEFORE APPLY
            SectionHeader(
                title = "RINGKASAN KONFIGURASI",
                subtitle = "Konfigurasi yang akan diterapkan saat tombol APPLY ditekan"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF161B26))
                    .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tuning Mode", color = TextMuted, fontSize = 12.sp)
                        Text(
                            text = if (selectedMode == "PERFORMANCE") "Performance Mode" else "Smooth Mode",
                            color = if (selectedMode == "PERFORMANCE") Color(0xFFEF4444) else GreenAccent,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Target Refresh Rate", color = TextMuted, fontSize = 12.sp)
                        Text(
                            text = if (selectedHz == 0) "Auto / Bawaan" else "$selectedHz Hz",
                            color = GoldAccent,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Target Resolusi", color = TextMuted, fontSize = 12.sp)
                        Text(
                            text = manualResolutionText.ifBlank { "${deviceInfo.resolutionWidth}x${deviceInfo.resolutionHeight}" },
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Target DPI", color = TextMuted, fontSize = 12.sp)
                        Text(
                            text = "${manualDpiText.ifBlank { deviceInfo.densityDpi.toString() }} DPI",
                            color = CyanAccent,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Profil Touch & Haptic", color = TextMuted, fontSize = 12.sp)
                        Text(
                            text = "$selectedTouchProfile • $selectedHaptic",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 6. BOTTOM ACTION BAR (APPLY & RESET TO DEFAULT)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0F17))
                .border(0.5.dp, BorderColor, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // RESET TO DEFAULT BUTTON
                OutlinedButton(
                    onClick = {
                        val def = smoothnessManager.resetToDefault(activity)
                        selectedMode = def.mode
                        selectedHz = def.targetRefreshRate
                        selectedTouchProfile = def.touchProfile
                        selectedHaptic = def.hapticIntensity
                        selectedDpiPreset = def.dpiPreset
                        manualDpiText = def.customDpi.toString()
                        manualResolutionText = def.customResolution
                        minimalPostProcessing = def.minimalPostProcessing
                        importedZipInfo = null
                        zipErrorMessage = null
                        Toast.makeText(context, "Pengaturan di-reset ke default perangkat", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_reset_default"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RESET DEFAULT",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // APPLY BUTTON (TRIGGERS 5-SECOND COUNTDOWN)
                Button(
                    onClick = {
                        val parsedDpi = manualDpiText.toIntOrNull() ?: deviceInfo.densityDpi
                        val res = manualResolutionText.ifBlank { "${deviceInfo.resolutionWidth}x${deviceInfo.resolutionHeight}" }

                        // Start 5 second countdown
                        countdownSeconds = 5
                        showCountdownDialog = true

                        countdownJob?.cancel()
                        countdownJob = scope.launch {
                            for (sec in 5 downTo 1) {
                                countdownSeconds = sec
                                delay(1000)
                            }
                            // Countdown completed: Apply configuration
                            showCountdownDialog = false

                            val targetConfig = SmoothnessConfig(
                                mode = selectedMode,
                                targetRefreshRate = selectedHz,
                                touchProfile = selectedTouchProfile,
                                hapticIntensity = selectedHaptic,
                                dpiPreset = selectedDpiPreset,
                                customDpi = parsedDpi,
                                customResolution = res,
                                minimalPostProcessing = minimalPostProcessing
                            )

                            val result = smoothnessManager.applyConfigurationOfficial(activity, targetConfig)
                            applyResult = result
                            showResultDialog = true
                            smoothnessManager.triggerHapticFeedback(selectedHaptic)
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("btn_apply_smoothness"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "APPLY",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }

    // 7. COUNTDOWN MODAL DIALOG (5 -> 4 -> 3 -> 2 -> 1 with CANCEL button)
    if (showCountdownDialog) {
        Dialog(
            onDismissRequest = {
                countdownJob?.cancel()
                showCountdownDialog = false
                Toast.makeText(context, "Penerapan dibatalkan", Toast.LENGTH_SHORT).show()
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF131722))
                    .border(1.5.dp, GoldAccent, RoundedCornerShape(18.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "MEMPERSIAPKAN PENERAPAN",
                        color = GoldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Big Countdown Number
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFB300))
                            .border(2.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = countdownSeconds.toString(),
                            color = GoldAccent,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    LinearProgressIndicator(
                        progress = { countdownSeconds / 5f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = GoldAccent,
                        trackColor = Color(0xFF262C3E)
                    )

                    Text(
                        text = "Menerapkan DPI: ${manualDpiText.ifBlank { deviceInfo.densityDpi.toString() }} • Resolusi: ${manualResolutionText.ifBlank { "${deviceInfo.resolutionWidth}x${deviceInfo.resolutionHeight}" }}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Tekan CANCEL kapan saja untuk membatalkan tanpa mengubah pengaturan.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    // CANCEL BUTTON
                    Button(
                        onClick = {
                            countdownJob?.cancel()
                            showCountdownDialog = false
                            Toast.makeText(context, "Penerapan dibatalkan", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF334155),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "CANCEL (BATALKAN)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 8. RESULT & TRANSPARENCY DIALOG
    if (showResultDialog && applyResult != null) {
        val res = applyResult!!
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            containerColor = Color(0xFF131722),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (res.globalOsDpiApplied) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (res.globalOsDpiApplied) GreenAccent else CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Status Penerapan",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = res.message,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2333))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "• Window & Overlay Rendering: AKTIF",
                                color = GreenAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Target DPI: ${res.appliedDpi} DPI",
                                color = Color.White,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = "• Target Resolusi: ${res.appliedResolution}",
                                color = Color.White,
                                fontSize = 11.5.sp
                            )
                            Text(
                                text = "• Global OS System Display: ${if (res.globalOsDpiApplied) "BERHASIL DIUBAH" else "DIBATASI OLEH ANDROID"}",
                                color = if (res.globalOsDpiApplied) GreenAccent else GoldAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = res.reasonExplanation,
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showResultDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                ) {
                    Text(text = "MENGERTI", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (!res.globalOsDpiApplied) {
                    TextButton(
                        onClick = {
                            showResultDialog = false
                            try {
                                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    ) {
                        Text(text = "Opsi Pengembang", color = CyanAccent)
                    }
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            color = GoldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 11.5.sp
        )
    }
}

@Composable
private fun ModeSelectionCard(
    modifier: Modifier = Modifier,
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.15f) else DarkCardBg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = description,
                color = if (isSelected) Color(0xFFE2E8F0) else TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun RateChip(
    label: String,
    subLabel: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) GoldAccent.copy(alpha = 0.2f) else Color(0xFF1E2333))
            .border(1.dp, if (isSelected) GoldAccent else BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isSelected) GoldAccent else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subLabel,
                color = if (isSelected) GoldAccent.copy(alpha = 0.8f) else TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun TouchProfileOption(
    label: String,
    desc: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyanAccent.copy(alpha = 0.15f) else Color(0xFF1E2333))
            .border(1.dp, if (isSelected) CyanAccent else BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isSelected) CyanAccent else Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = desc,
                color = if (isSelected) CyanAccent.copy(alpha = 0.8f) else TextMuted,
                fontSize = 9.5.sp
            )
        }
    }
}

@Composable
private fun DpiChip(
    label: String,
    dpi: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) GoldAccent.copy(alpha = 0.2f) else Color(0xFF1E2333))
            .border(1.dp, if (isSelected) GoldAccent else BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isSelected) GoldAccent else Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$dpi DPI",
                color = if (isSelected) GoldAccent.copy(alpha = 0.8f) else TextMuted,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
