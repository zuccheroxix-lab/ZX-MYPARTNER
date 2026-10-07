package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Config
import com.example.data.DeviceInfo
import com.example.data.DeviceInfoProvider
import com.example.data.SettingsManager
import com.example.service.CrosshairService
import com.example.service.OverlayService
import com.example.ui.components.BannerCard
import com.example.ui.components.DashboardHeader
import com.example.ui.components.DeveloperProfileSheet
import com.example.ui.components.isShizukuInstalled
import com.example.ui.theme.LocalAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    deviceInfo: DeviceInfo,
    onRefreshDeviceInfo: () -> Unit,
    onNavigateToPage: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val accentColor = LocalAccentColor.current

    val settingsManager = remember { SettingsManager(context) }
    val performanceMode by settingsManager.performanceMode.collectAsState()
    val isHudRunning by OverlayService.isServiceRunning.collectAsState()
    val isCrosshairRunning by CrosshairService.isServiceRunning.collectAsState()

    var memoryInfo by remember { mutableStateOf(DeviceInfoProvider.getMemoryInfo(context)) }
    var batteryInfo by remember { mutableStateOf(DeviceInfoProvider.getBatteryInfo(context)) }
    var showProfileSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun refreshStats() {
        onRefreshDeviceInfo()
        memoryInfo = DeviceInfoProvider.getMemoryInfo(context)
        batteryInfo = DeviceInfoProvider.getBatteryInfo(context)
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("home_screen_content")
    ) {
        // 1. Header (Clean & Professional)
        DashboardHeader(
            title = "DYNIMETIZE ZX",
            subtitle = "Gaming Performance & Control Center",
            developerName = Config.DEVELOPER_NAME,
            onDevClick = { showProfileSheet = true }
        )

        // 2. Original Anime / Visual Asset Banner (Retaining existing photo/asset)
        BannerCard(
            onBannerClick = { showProfileSheet = true }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Main 3 Navigation Shortcuts: GAME, TOOLS, SETTINGS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToPage(1) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("GAME →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(2) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("TOOLS →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(3) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SETTINGS →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Performance Mode Selector Card
        HomeSectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Mode Performa", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Alokasi daya sistem Android", color = Color(0xFF8E8E98), fontSize = 11.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (performanceMode) {
                                    "PERFORMANCE" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    "BATTERY_SAVER" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    else -> accentColor.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = performanceMode,
                            color = when (performanceMode) {
                                "PERFORMANCE" -> Color(0xFFEF4444)
                                "BATTERY_SAVER" -> Color(0xFF10B981)
                                else -> accentColor
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Buttons (Performance, Balanced, Eco Saver)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        Triple("PERFORMANCE", "Performance", Color(0xFFEF4444)),
                        Triple("BALANCED", "Balanced", Color(0xFF38BDF8)),
                        Triple("BATTERY_SAVER", "Eco Saver", Color(0xFF10B981))
                    )
                    modes.forEach { (modeKey, label, color) ->
                        val isSelected = performanceMode == modeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.22f) else Color(0xFF171A24))
                                .border(0.8.dp, if (isSelected) color else Color(0xFF262A3B), RoundedCornerShape(10.dp))
                                .clickable {
                                    settingsManager.setPerformanceMode(modeKey)
                                    val act = context as? Activity
                                    if (act != null) {
                                        if (modeKey == "PERFORMANCE") {
                                            act.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                        } else {
                                            act.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                        }
                                    }
                                    Toast.makeText(context, "Mode: $label", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) color else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Hardware Specifications Overview (Real Android System APIs)
        HomeSectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Status Hardware Perangkat", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Data resmi dari API sistem Android", color = Color(0xFF8E8E98), fontSize = 11.sp)
                        }
                    }

                    IconButton(onClick = { refreshStats() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = accentColor, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4-Grid Metrics
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HardwareMetricTile(
                        label = "RAM Sistem",
                        value = "${memoryInfo.availableRamMb} MB Bebas",
                        sub = "Total: ${memoryInfo.totalRamMb} MB (${memoryInfo.usedPercent}% used)",
                        color = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                    HardwareMetricTile(
                        label = "Suhu & Baterai",
                        value = "${batteryInfo.temperatureCelsius}°C",
                        sub = "${batteryInfo.level}% • ${batteryInfo.health}",
                        color = if (batteryInfo.temperatureCelsius > 42) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HardwareMetricTile(
                        label = "Prosesor & SoC",
                        value = Build.HARDWARE.ifEmpty { "ARM64" },
                        sub = "${DeviceInfoProvider.getCpuCores()} Cores • ${deviceInfo.cpuAbi}",
                        color = Color(0xFFA78BFA),
                        modifier = Modifier.weight(1f)
                    )
                    val refreshRates = DeviceInfoProvider.getSupportedRefreshRates(context)
                    HardwareMetricTile(
                        label = "Refresh Rate Layar",
                        value = "${refreshRates.lastOrNull() ?: 60}Hz",
                        sub = "Didukung: ${refreshRates.joinToString("Hz, ")}Hz",
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Floating Overlay Toggles (Gaming HUD & Crosshair)
        HomeSectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "OVERLAY MENGAMBANG",
                    color = Color(0xFF8E8E98),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gaming HUD Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Gaming HUD Telemetry", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Monitor terapung di atas game", color = Color(0xFF8E8E98), fontSize = 11.sp)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(0.6.dp).background(Color(0xFF222533)))
                Spacer(modifier = Modifier.height(10.dp))

                // Crosshair Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2A2A).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFFF2A2A), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Crosshair Aim Reticle", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Titik bidik presisi permanen di layar", color = Color(0xFF8E8E98), fontSize = 11.sp)
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
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7. Shizuku Status Overview
        HomeSectionCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val installed = isShizukuInstalled(context)
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (installed) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF97316).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (installed) Color(0xFF10B981) else Color(0xFFF97316),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Layanan Shizuku ADB", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = if (installed) "Terpasang (Akses Shell Diizinkan)" else "Belum Terpasang (Akses Terbatas)",
                            color = if (installed) Color(0xFF10B981) else Color(0xFFF97316),
                            fontSize = 11.sp
                        )
                    }
                }

                TextButton(onClick = { onNavigateToPage(3) }) {
                    Text("Pengaturan →", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }

    if (showProfileSheet) {
        DeveloperProfileSheet(
            onDismissRequest = { showProfileSheet = false },
            sheetState = sheetState
        )
    }
}

@Composable
private fun HomeSectionCard(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF13151D))
            .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(18.dp))
    ) {
        content()
    }
}

@Composable
private fun HardwareMetricTile(
    label: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF171A24))
            .border(0.6.dp, Color(0xFF262A3B), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, color = Color(0xFF8E8E98), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, color = Color(0xFF6E7285), fontSize = 9.sp, maxLines = 1)
        }
    }
}
