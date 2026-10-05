package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.data.SettingsManager
import com.example.service.HudMetrics
import com.example.service.HudTelemetryTracker
import com.example.service.OverlayService

private val GoldAccent = Color(0xFFFFB300)
private val DarkCardBg = Color(0xFF141722)
private val BorderColor = Color(0xFF262C3E)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamingHudConfigScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isServiceRunning by OverlayService.isServiceRunning.collectAsState()
    val hudPosition by settingsManager.hudPosition.collectAsState()
    val isHudShown by settingsManager.isHudShown.collectAsState()

    val showPing by settingsManager.showPing.collectAsState()
    val showFps by settingsManager.showFps.collectAsState()
    val showRam by settingsManager.showRam.collectAsState()
    val showTemp by settingsManager.showTemp.collectAsState()
    val showBattery by settingsManager.showBattery.collectAsState()

    var liveMetrics by remember { mutableStateOf(HudMetrics()) }

    BackHandler { onBack() }

    // Start in-app telemetry while viewing this configuration screen
    DisposableEffect(Unit) {
        val tracker = HudTelemetryTracker(context) { metrics ->
            liveMetrics = metrics
        }
        tracker.start()
        onDispose {
            tracker.stop()
        }
    }

    fun toggleOverlayService() {
        val hasPermission = Settings.canDrawOverlays(context)
        if (!hasPermission) {
            Toast.makeText(context, "Izin Overlay Diperlukan. Membuka Pengaturan...", Toast.LENGTH_SHORT).show()
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
            } catch (_: Exception) {}
            return
        }

        if (isServiceRunning) {
            OverlayService.stop(context)
            Toast.makeText(context, "Gaming HUD Dihentikan", Toast.LENGTH_SHORT).show()
        } else {
            OverlayService.start(context)
            Toast.makeText(context, "Gaming HUD Aktif di Layar", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("gaming_hud_config_screen")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "GAMING HUD OVERLAY",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Real-time Floating Telemetry & Controls",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0C))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Section 1: LIVE IN-APP HUD PREVIEW
            Text(
                text = "LIVE HUD PREVIEW",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Interactive HUD Preview Box
            AnimatedVisibility(visible = isHudShown) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF101420), Color(0xFF0C0E16))
                            )
                        )
                        .border(1.2.dp, GoldAccent, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE TELEMETRY HUD",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Text(
                                text = "POS: $hudPosition",
                                color = GoldAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live metrics display in HUD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showFps) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = liveMetrics.fpsText, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                    Text(text = "FPS", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (showRam) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = liveMetrics.ramText, color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(text = "RAM", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (showTemp) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = liveMetrics.tempText, color = Color(0xFF67E8F9), fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(text = "SUHU", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (showBattery) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = liveMetrics.batteryText, color = Color(0xFF86EFAC), fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(text = "BATERAI", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (showPing) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = liveMetrics.pingText, color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(text = "PING", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Show/Hide Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardBg)
                    .border(0.8.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isHudShown) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Show / Hide Gaming HUD",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isHudShown) "HUD aktif dan terlihat di game" else "HUD disembunyikan",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = isHudShown,
                        onCheckedChange = { settingsManager.setHudShown(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = GoldAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: HUD POSITION SELECTOR
            Text(
                text = "HUD SCREEN POSITION",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PositionOptionButton(
                    modifier = Modifier.weight(1f),
                    label = "Top Left",
                    isSelected = hudPosition == "TOP_LEFT",
                    onClick = { settingsManager.setHudPosition("TOP_LEFT") }
                )
                PositionOptionButton(
                    modifier = Modifier.weight(1f),
                    label = "Top Right",
                    isSelected = hudPosition == "TOP_RIGHT",
                    onClick = { settingsManager.setHudPosition("TOP_RIGHT") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PositionOptionButton(
                    modifier = Modifier.weight(1f),
                    label = "Bottom Left",
                    isSelected = hudPosition == "BOTTOM_LEFT",
                    onClick = { settingsManager.setHudPosition("BOTTOM_LEFT") }
                )
                PositionOptionButton(
                    modifier = Modifier.weight(1f),
                    label = "Bottom Right",
                    isSelected = hudPosition == "BOTTOM_RIGHT",
                    onClick = { settingsManager.setHudPosition("BOTTOM_RIGHT") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: METRIC VISIBILITY TOGGLES
            Text(
                text = "TAMPILKAN INDIKATOR HUD",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardBg)
                    .border(0.8.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                MetricToggleRow("Frame Rate (FPS)", showFps) { settingsManager.setShowFps(it) }
                MetricToggleRow("Memory Usage (RAM)", showRam) { settingsManager.setShowRam(it) }
                MetricToggleRow("Suhu Baterai (Temp)", showTemp) { settingsManager.setShowTemp(it) }
                MetricToggleRow("Level Baterai (Battery)", showBattery) { settingsManager.setShowBattery(it) }
                MetricToggleRow("Jaringan Latensi (Ping ms)", showPing) { settingsManager.setShowPing(it) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 4: FLOATING WINDOW OVERLAY SERVICE TRIGGER
            Button(
                onClick = { toggleOverlayService() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isServiceRunning) Color(0xFFEF4444) else GoldAccent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = if (isServiceRunning) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isServiceRunning) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isServiceRunning) "STOP FLOATING OVERLAY HUD" else "START FLOATING OVERLAY HUD",
                    color = if (isServiceRunning) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PositionOptionButton(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF221A0E) else DarkCardBg)
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = if (isSelected) GoldAccent else BorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = if (isSelected) GoldAccent else Color.White,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MetricToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = GoldAccent
            )
        )
    }
}
