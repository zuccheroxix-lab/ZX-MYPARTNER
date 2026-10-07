package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.data.DeviceInfoProvider
import com.example.data.GameSmoothnessManager
import com.example.data.ImportedZipConfig
import com.example.data.SettingsManager
import com.example.service.CrosshairService
import com.example.service.OverlayService
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateToPage: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accentColor = LocalAccentColor.current
    val scrollState = rememberScrollState()

    val settingsManager = remember { SettingsManager(context) }
    val smoothnessManager = remember { GameSmoothnessManager(context) }
    val savedSmoothnessConfig by smoothnessManager.config.collectAsState()

    // Sub-Tabs: 0 = DPI & Resolution, 1 = DNS Kustom, 2 = Crosshair & HUD, 3 = Render Selection
    var selectedTab by remember { mutableIntStateOf(0) }

    // DPI & Resolution States
    var manualDpiText by remember { mutableStateOf(savedSmoothnessConfig.customDpi.toString()) }
    var manualResolutionText by remember { mutableStateOf(savedSmoothnessConfig.customResolution) }
    var isImportingZip by remember { mutableStateOf(false) }
    var importedZipInfo by remember { mutableStateOf<ImportedZipConfig?>(null) }
    var zipErrorMessage by remember { mutableStateOf<String?>(null) }

    // Countdown & Apply states
    var showCountdownDialog by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }
    var applyResult by remember { mutableStateOf<ApplyResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    // DNS State
    val savedDns by settingsManager.customDns.collectAsState()
    var dnsInput by remember { mutableStateOf(savedDns) }
    var dnsPingResult by remember { mutableStateOf<String?>(null) }
    var isPingingDns by remember { mutableStateOf(false) }

    // Crosshair State
    val isCrosshairRunning by CrosshairService.isServiceRunning.collectAsState()
    val isHudRunning by OverlayService.isServiceRunning.collectAsState()
    val crosshairStyle by settingsManager.crosshairStyle.collectAsState()
    val crosshairSize by settingsManager.crosshairSize.collectAsState()
    val crosshairColorLong by settingsManager.crosshairColor.collectAsState()
    val crosshairOpacity by settingsManager.crosshairOpacity.collectAsState()
    val crosshairColor = Color(crosshairColorLong)

    // Render Selection
    val renderMode by settingsManager.renderMode.collectAsState()

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
                        config.dpi?.let { manualDpiText = it.toString() }
                        config.resolution?.let { manualResolutionText = it }
                        Toast.makeText(context, "Konfigurasi ZIP terbaca dengan aman!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        zipErrorMessage = error.localizedMessage ?: "File ZIP tidak valid atau tidak kompatibel."
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
            onNavigateToPage(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0D))
            .testTag("tools_screen_root")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "GAMING TOOLS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "DPI, Resolution, DNS, Crosshair & Render Engine",
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { onNavigateToPage(0) }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = accentColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0C))
        )

        // Navigation Jump Bar (Smooth transition across the 4 main pages)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToPage(1) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("← Game", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(3) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Settings →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented Sub-Category Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                "DPI & Res" to Icons.Default.DisplaySettings,
                "DNS" to Icons.Default.Dns,
                "Crosshair & HUD" to Icons.Default.Layers,
                "Render" to Icons.Default.Speed
            )

            tabs.forEachIndexed { idx, (label, icon) ->
                val isSel = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) Color(0xFFFFB300).copy(alpha = 0.2f) else Color(0xFF13151D))
                        .border(0.8.dp, if (isSel) Color(0xFFFFB300) else Color(0xFF222533), RoundedCornerShape(12.dp))
                        .clickable { selectedTab = idx }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSel) Color(0xFFFFB300) else Color(0xFF8E8E98),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = label,
                            color = if (isSel) Color(0xFFFFB300) else Color(0xFF8E8E98),
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB CONTENTS
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: DPI & RESOLUTION ENGINE
                    ToolsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFB300).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DisplaySettings, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("DPI & Resolution Engine", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Input manual, Import ZIP aman & Countdown 5s", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Manual DPI Input
                            Text("Input DPI Manual:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = manualDpiText,
                                onValueChange = { manualDpiText = it.filter { ch -> ch.isDigit() } },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Contoh: 360, 420, 480", color = Color(0xFF6E7285), fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB300),
                                    unfocusedBorderColor = Color(0xFF262A3B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Manual Resolution Input
                            Text("Input Resolusi Manual (Lebar x Tinggi):", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = manualResolutionText,
                                onValueChange = { manualResolutionText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Contoh: 1080x2400", color = Color(0xFF6E7285), fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB300),
                                    unfocusedBorderColor = Color(0xFF262A3B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // IMPORT ZIP BUTTON
                            OutlinedButton(
                                onClick = {
                                    try {
                                        zipPickerLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream"))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Tidak dapat membuka pemilih file", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.FolderZip, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("IMPORT ZIP KONFIGURASI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (importedZipInfo != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Konfigurasi ZIP Siap: DPI=${importedZipInfo?.dpi ?: "-"} • Res=${importedZipInfo?.resolution ?: "-"}",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (zipErrorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Error ZIP: $zipErrorMessage",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons (Apply with 5s countdown + Reset)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            smoothnessManager.resetToDefault(context as? Activity)
                                            manualDpiText = "420"
                                            manualResolutionText = "1080x2400"
                                            Toast.makeText(context, "DPI & Resolusi direset ke default sistem", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Default", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        // Start real 5s countdown dialog with Cancel button
                                        countdownSeconds = 5
                                        showCountdownDialog = true
                                        countdownJob?.cancel()
                                        countdownJob = scope.launch {
                                            for (i in 5 downTo 1) {
                                                countdownSeconds = i
                                                delay(1000)
                                            }
                                            showCountdownDialog = false
                                            // Apply via Shizuku / Shell / Settings
                                            val dpiVal = manualDpiText.toIntOrNull() ?: 420
                                            val config = com.example.data.SmoothnessConfig(
                                                mode = "BALANCED",
                                                targetRefreshRate = 60,
                                                touchProfile = "STANDARD",
                                                hapticIntensity = "MEDIUM",
                                                dpiPreset = "CUSTOM",
                                                customDpi = dpiVal,
                                                customResolution = manualResolutionText,
                                                minimalPostProcessing = false
                                            )
                                            val result = smoothnessManager.applyConfigurationOfficial(context as? Activity, config)
                                            applyResult = result
                                            showResultDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                                ) {
                                    Text("APPLY (5s Timer)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: DNS KUSTOM
                    ToolsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Dns, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("DNS Kustom Gaming", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Input manual, preset & tes ping jaringan nyata", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Alamat DNS Provider:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = dnsInput,
                                onValueChange = { dnsInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Contoh: 1dot1dot1dot1.cloudflare-dns.com", color = Color(0xFF6E7285), fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF262A3B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Presets
                            Text("Preset DNS Populer:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))

                            val presets = listOf(
                                "Cloudflare" to "1dot1dot1dot1.cloudflare-dns.com",
                                "Google DNS" to "dns.google",
                                "AdGuard" to "dns.adguard.com"
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                presets.forEach { (label, host) ->
                                    val isSel = dnsInput == host
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF171A24))
                                            .border(0.8.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF262A3B), RoundedCornerShape(8.dp))
                                            .clickable { dnsInput = host }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, color = if (isSel) Color(0xFF38BDF8) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: Ping test & Apply
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        isPingingDns = true
                                        dnsPingResult = null
                                        scope.launch {
                                            val res = withContext(Dispatchers.IO) {
                                                try {
                                                    val hostToPing = if (dnsInput.contains(".")) dnsInput.trim() else "1.1.1.1"
                                                    val start = System.currentTimeMillis()
                                                    val addr = InetAddress.getByName(hostToPing)
                                                    val reachable = addr.isReachable(3000)
                                                    val elapsed = System.currentTimeMillis() - start
                                                    if (reachable) "Sukses: ${elapsed}ms (${addr.hostAddress})"
                                                    else "DNS dapat di-resolve: ${addr.hostAddress} (${elapsed}ms)"
                                                } catch (e: Exception) {
                                                    "Gagal ping: ${e.localizedMessage}"
                                                }
                                            }
                                            isPingingDns = false
                                            dnsPingResult = res
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                                ) {
                                    if (isPingingDns) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF38BDF8))
                                    } else {
                                        Text("Tes Ping DNS", fontSize = 11.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        settingsManager.setCustomDns(dnsInput.trim())
                                        Toast.makeText(context, "DNS Tersimpan: $dnsInput", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                                ) {
                                    Text("Simpan DNS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            if (dnsPingResult != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Hasil Ping: $dnsPingResult",
                                    color = if (dnsPingResult?.startsWith("Gagal") == true) Color(0xFFEF4444) else Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: CROSSHAIR RETICLE & GAMING HUD OVERLAY
                    ToolsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Crosshair Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF2A2A).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFFF2A2A), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Crosshair Aim Reticle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Titik bidik presisi permanen", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                    }
                                }

                                Switch(
                                    checked = isCrosshairRunning,
                                    onCheckedChange = { enable ->
                                        if (enable) {
                                            if (!Settings.canDrawOverlays(context)) {
                                                Toast.makeText(context, "Izin Overlay Diperlukan", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            } else {
                                                settingsManager.setCrosshairEnabled(true)
                                                CrosshairService.start(context)
                                            }
                                        } else {
                                            settingsManager.setCrosshairEnabled(false)
                                            CrosshairService.stop(context)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF2A2A))
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Reticle Preview Canvas Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0C0E14))
                                    .border(0.8.dp, Color(0xFF1E2232), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(80.dp)) {
                                    val c = center
                                    val effColor = crosshairColor.copy(alpha = crosshairOpacity)
                                    val sizePx = (crosshairSize * density).coerceAtLeast(14f)
                                    val half = sizePx / 2f

                                    when (crosshairStyle) {
                                        "DOT" -> drawCircle(color = effColor, radius = half.coerceIn(4f, 18f), center = c)
                                        "CIRCLE_DOT" -> {
                                            drawCircle(color = effColor, radius = half, center = c, style = Stroke(width = 2.5f))
                                            drawCircle(color = effColor, radius = 3.5f, center = c)
                                        }
                                        "T_SHAPE" -> {
                                            drawLine(effColor, Offset(c.x, c.y - half), Offset(c.x, c.y + 4f), strokeWidth = 3f)
                                            drawLine(effColor, Offset(c.x - half, c.y), Offset(c.x + half, c.y), strokeWidth = 3f)
                                        }
                                        "SNIPER" -> {
                                            drawCircle(effColor, half, c, style = Stroke(2f))
                                            drawLine(effColor, Offset(c.x - half * 1.3f, c.y), Offset(c.x + half * 1.3f, c.y), 2f)
                                            drawLine(effColor, Offset(c.x, c.y - half * 1.3f), Offset(c.x, c.y + half * 1.3f), 2f)
                                            drawCircle(effColor, 3f, c)
                                        }
                                        else -> {
                                            // Classic Cross
                                            drawLine(effColor, Offset(c.x - half, c.y), Offset(c.x - 4f, c.y), 3f)
                                            drawLine(effColor, Offset(c.x + 4f, c.y), Offset(c.x + half, c.y), 3f)
                                            drawLine(effColor, Offset(c.x, c.y - half), Offset(c.x, c.y - 4f), 3f)
                                            drawLine(effColor, Offset(c.x, c.y + 4f), Offset(c.x, c.y + half), 3f)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Crosshair Styles
                            val styles = listOf("CLASSIC", "DOT", "CIRCLE_DOT", "T_SHAPE", "SNIPER")
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                styles.forEach { st ->
                                    val isSel = crosshairStyle == st
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) Color(0xFFFF2A2A).copy(alpha = 0.2f) else Color(0xFF171A24))
                                            .border(0.8.dp, if (isSel) Color(0xFFFF2A2A) else Color(0xFF262A3B), RoundedCornerShape(8.dp))
                                            .clickable { settingsManager.setCrosshairStyle(st) }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(st.replace("_", " "), color = if (isSel) Color(0xFFFF2A2A) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFF22242E), thickness = 0.6.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Gaming HUD Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFB300).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Gaming HUD Telemetry", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Floating monitor FPS, RAM, Temp & Baterai", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                    }
                                }

                                Switch(
                                    checked = isHudRunning,
                                    onCheckedChange = { enable ->
                                        if (enable) {
                                            if (!Settings.canDrawOverlays(context)) {
                                                Toast.makeText(context, "Izin Overlay Diperlukan", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            } else {
                                                OverlayService.start(context)
                                            }
                                        } else {
                                            OverlayService.stop(context)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFFB300))
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: RENDER SELECTION
                    ToolsCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFA78BFA).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Pilihan Backend Render", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Deteksi dukungan driver grafis GPU perangkat", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val vulkanDesc = DeviceInfoProvider.getVulkanInfo(context)
                            val openGlDesc = DeviceInfoProvider.getOpenGlEsVersion(context)
                            val hasVulkan = !vulkanDesc.contains("Not Supported", ignoreCase = true)

                            val renderEngines = listOf(
                                Triple("DEFAULT", "Default Driver OS", "Driver rendering default yang ditentukan sistem"),
                                Triple("VULKAN", "Vulkan API", vulkanDesc),
                                Triple("OPENGL_ES", "OpenGL ES", openGlDesc)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                renderEngines.forEach { (modeKey, title, desc) ->
                                    val isSel = renderMode == modeKey
                                    val isSupported = modeKey != "VULKAN" || hasVulkan
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) Color(0xFFA78BFA).copy(alpha = 0.2f) else Color(0xFF171A24))
                                            .border(0.8.dp, if (isSel) Color(0xFFA78BFA) else Color(0xFF262A3B), RoundedCornerShape(10.dp))
                                            .clickable(enabled = isSupported) {
                                                settingsManager.setRenderMode(modeKey)
                                                Toast.makeText(context, "Render Mode: $title", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(title, color = if (isSel) Color(0xFFA78BFA) else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(desc, color = if (isSupported) Color(0xFF8E8E98) else Color(0xFFEF4444), fontSize = 10.sp)
                                            }
                                            if (isSel) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // 5-SECOND COUNTDOWN DIALOG WITH CANCEL BUTTON
    if (showCountdownDialog) {
        Dialog(
            onDismissRequest = {
                countdownJob?.cancel()
                showCountdownDialog = false
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF13151D))
                    .border(1.2.dp, Color(0xFFFFB300), RoundedCornerShape(18.dp))
                    .padding(22.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Menerapkan Konfigurasi Layar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Konfigurasi akan diterapkan dalam $countdownSeconds detik...",
                        color = Color(0xFF8E8E98),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "$countdownSeconds",
                        color = Color(0xFFFFB300),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            countdownJob?.cancel()
                            showCountdownDialog = false
                            Toast.makeText(context, "Dibatalkan oleh pengguna", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("BATALKAN SEKARANG", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // RESULT DIALOG
    if (showResultDialog && applyResult != null) {
        val res = applyResult!!
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = {
                val isOk = res.windowApplied || res.globalOsDpiApplied
                Text(
                    text = if (isOk) "Konfigurasi Berhasil Diproses" else "Izin / Dukungan Sistem Diperlukan",
                    color = if (isOk) Color(0xFF10B981) else Color(0xFFEF4444),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(res.message + if (res.reasonExplanation.isNotBlank()) "\n\n${res.reasonExplanation}" else "", color = Color.White, fontSize = 12.sp)
            },
            confirmButton = {
                Button(
                    onClick = { showResultDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF13151D)
        )
    }
}

@Composable
private fun ToolsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF13151D))
            .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(18.dp))
    ) {
        content()
    }
}
