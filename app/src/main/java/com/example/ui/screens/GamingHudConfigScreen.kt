package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.service.CrosshairService
import com.example.service.OverlayService
import com.example.ui.theme.LocalAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamingHudConfigScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit = {},
    onNavigateToPage: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val accentColor = LocalAccentColor.current

    val isHudRunning by OverlayService.isServiceRunning.collectAsState()
    val isCrosshairRunning by CrosshairService.isServiceRunning.collectAsState()

    val hudPosition by settingsManager.hudPosition.collectAsState()
    val showFps by settingsManager.showFps.collectAsState()
    val showRam by settingsManager.showRam.collectAsState()
    val showTemp by settingsManager.showTemp.collectAsState()
    val showBattery by settingsManager.showBattery.collectAsState()
    val showPing by settingsManager.showPing.collectAsState()

    val crosshairStyle by settingsManager.crosshairStyle.collectAsState()
    val crosshairSize by settingsManager.crosshairSize.collectAsState()
    val crosshairColorLong by settingsManager.crosshairColor.collectAsState()
    val crosshairOpacity by settingsManager.crosshairOpacity.collectAsState()

    val crosshairColor = Color(crosshairColorLong)

    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0D))
            .testTag("gaming_hud_config_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "OVERLAY & AIM",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Gaming Telemetry & Crosshair Assist",
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp
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

        // Horizontal Page Quick Buttons (Smooth navigation without getting trapped)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToPage(2) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("← Game Space", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(0) },
                modifier = Modifier.weight(0.8f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(4) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Pengaturan →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // CARD 1: GAMING HUD TELEMETRY OVERLAY
            OverlayConfigCard {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                Text("Monitor status performa mengambang di atas game", color = Color(0xFF8E8E98), fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = isHudRunning,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (!Settings.canDrawOverlays(context)) {
                                        Toast.makeText(context, "Izin Tampilkan di Atas Aplikasi Diperlukan", Toast.LENGTH_SHORT).show()
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

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF22242E), thickness = 0.6.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Posisi Default Layar:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    val positions = listOf("TOP_LEFT", "TOP_CENTER", "TOP_RIGHT", "BOTTOM_CENTER")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        positions.forEach { pos ->
                            val label = when (pos) {
                                "TOP_LEFT" -> "Kiri Atas"
                                "TOP_CENTER" -> "Tengah Atas"
                                "TOP_RIGHT" -> "Kanan Atas"
                                else -> "Bawah"
                            }
                            val isSel = hudPosition == pos
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFFFFB300).copy(alpha = 0.2f) else Color(0xFF171A24))
                                    .border(0.8.dp, if (isSel) Color(0xFFFFB300) else Color(0xFF262A3B), RoundedCornerShape(8.dp))
                                    .clickable { settingsManager.setHudPosition(pos) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color(0xFFFFB300) else Color(0xFF9E9EA8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Elemen Telemetri Aktif:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Metric toggles
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MetricTogglePill("FPS", showFps, Color.White, modifier = Modifier.weight(1f)) { settingsManager.setShowFps(it) }
                        MetricTogglePill("RAM", showRam, Color(0xFFFFB300), modifier = Modifier.weight(1f)) { settingsManager.setShowRam(it) }
                        MetricTogglePill("Suhu", showTemp, Color(0xFF67E8F9), modifier = Modifier.weight(1f)) { settingsManager.setShowTemp(it) }
                        MetricTogglePill("Baterai", showBattery, Color(0xFF86EFAC), modifier = Modifier.weight(1f)) { settingsManager.setShowBattery(it) }
                        MetricTogglePill("Ping", showPing, Color(0xFFA78BFA), modifier = Modifier.weight(1f)) { settingsManager.setShowPing(it) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 2: CROSSHAIR AIM OVERLAY
            OverlayConfigCard {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                Text("Titik bidik presisi permanen di tengah layar", color = Color(0xFF8E8E98), fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = isCrosshairRunning,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (!Settings.canDrawOverlays(context)) {
                                        Toast.makeText(context, "Izin Tampilkan di Atas Aplikasi Diperlukan", Toast.LENGTH_SHORT).show()
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

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF22242E), thickness = 0.6.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Crosshair Preview Canvas Box
                    Text("Pratinjau Reticle:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C0E14))
                            .border(0.8.dp, Color(0xFF1E2232), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(90.dp)) {
                            val c = center
                            val effectiveColor = crosshairColor.copy(alpha = crosshairOpacity)
                            val sizePx = (crosshairSize * density).coerceAtLeast(14f)
                            val half = sizePx / 2f

                            when (crosshairStyle) {
                                "DOT" -> {
                                    drawCircle(color = effectiveColor, radius = half.coerceIn(4f, 18f), center = c)
                                }
                                "CIRCLE_DOT" -> {
                                    drawCircle(color = effectiveColor, radius = half, center = c, style = Stroke(width = 2.5f))
                                    drawCircle(color = effectiveColor, radius = 3.5f, center = c)
                                }
                                "T_SHAPE" -> {
                                    drawLine(effectiveColor, Offset(c.x, c.y - half), Offset(c.x, c.y + 4f), strokeWidth = 3f)
                                    drawLine(effectiveColor, Offset(c.x - half, c.y), Offset(c.x + half, c.y), strokeWidth = 3f)
                                }
                                "SNIPER" -> {
                                    drawCircle(effectiveColor, half, c, style = Stroke(2f))
                                    drawCircle(effectiveColor, half * 0.45f, c, style = Stroke(1.5f))
                                    drawLine(effectiveColor, Offset(c.x - half * 1.3f, c.y), Offset(c.x + half * 1.3f, c.y), 2f)
                                    drawLine(effectiveColor, Offset(c.x, c.y - half * 1.3f), Offset(c.x, c.y + half * 1.3f), 2f)
                                    drawCircle(effectiveColor, 3f, c)
                                }
                                else -> {
                                    // CLASSIC
                                    drawLine(effectiveColor, Offset(c.x - half, c.y), Offset(c.x - 4f, c.y), 3f)
                                    drawLine(effectiveColor, Offset(c.x + 4f, c.y), Offset(c.x + half, c.y), 3f)
                                    drawLine(effectiveColor, Offset(c.x, c.y - half), Offset(c.x, c.y - 4f), 3f)
                                    drawLine(effectiveColor, Offset(c.x, c.y + 4f), Offset(c.x, c.y + half), 3f)
                                    drawCircle(effectiveColor, 2f, c)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Style selector
                    Text("Gaya Bidikan:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    val styles = listOf(
                        "CLASSIC" to "Classic",
                        "DOT" to "Dot",
                        "CIRCLE_DOT" to "Circle Dot",
                        "T_SHAPE" to "T-Shape",
                        "SNIPER" to "Sniper"
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        styles.forEach { (stKey, label) ->
                            val isSel = crosshairStyle == stKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFFFF2A2A).copy(alpha = 0.2f) else Color(0xFF171A24))
                                    .border(0.8.dp, if (isSel) Color(0xFFFF2A2A) else Color(0xFF262A3B), RoundedCornerShape(8.dp))
                                    .clickable { settingsManager.setCrosshairStyle(stKey) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color(0xFFFF2A2A) else Color(0xFF9E9EA8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Size Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ukuran Reticle", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("${crosshairSize}dp", color = Color(0xFFFF2A2A), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = crosshairSize.toFloat(),
                        onValueChange = { settingsManager.setCrosshairSize(it.toInt()) },
                        valueRange = 16f..64f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF2A2A),
                            activeTrackColor = Color(0xFFFF2A2A),
                            inactiveTrackColor = Color(0xFF262A3B)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Opacity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transparansi (Opacity)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("${(crosshairOpacity * 100).toInt()}%", color = Color(0xFFFF2A2A), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = crosshairOpacity,
                        onValueChange = { settingsManager.setCrosshairOpacity(it) },
                        valueRange = 0.2f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF2A2A),
                            activeTrackColor = Color(0xFFFF2A2A),
                            inactiveTrackColor = Color(0xFF262A3B)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Palette
                    Text("Warna Reticle:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    val palette = listOf(
                        0xFFFF2A2AL to "Merah",
                        0xFF00FF66L to "Hijau",
                        0xFF00E5FFL to "Cyan",
                        0xFFFFD700L to "Kuning",
                        0xFFFFFFFFL to "Putih",
                        0xFFA855F7L to "Ungu"
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        palette.forEach { (colorVal, _) ->
                            val isSel = crosshairColorLong == colorVal
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .border(
                                        width = if (isSel) 2.5.dp else 1.dp,
                                        color = if (isSel) Color.White else Color(0xFF262A3B),
                                        shape = CircleShape
                                    )
                                    .clickable { settingsManager.setCrosshairColor(colorVal) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (colorVal == 0xFFFFFFFFL || colorVal == 0xFFFFD700L) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun OverlayConfigCard(content: @Composable () -> Unit) {
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

@Composable
private fun MetricTogglePill(
    label: String,
    checked: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) color.copy(alpha = 0.2f) else Color(0xFF171A24))
            .border(0.8.dp, if (checked) color else Color(0xFF262A3B), RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (checked) color else Color(0xFF8E8E98),
            fontSize = 11.sp,
            fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
        )
    }
}
