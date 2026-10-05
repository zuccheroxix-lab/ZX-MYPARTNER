package com.example.ui.screens

import android.content.Context
import android.os.Build
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryInfo
import com.example.data.DeviceInfoProvider
import com.example.data.MemoryInfoData
import com.example.data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val GoldAccent = Color(0xFFFFB300)
private val DarkCardBg = Color(0xFF141722)
private val BorderColor = Color(0xFF262C3E)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    settingsManager: SettingsManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val activeMode by settingsManager.performanceMode.collectAsState()

    var memInfo by remember { mutableStateOf(DeviceInfoProvider.getMemoryInfo(context)) }
    var batteryInfo by remember { mutableStateOf(DeviceInfoProvider.getBatteryInfo(context)) }
    var isRefreshing by remember { mutableStateOf(false) }

    val cpuCores = remember { Runtime.getRuntime().availableProcessors() }
    val cpuArch = remember {
        if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else System.getProperty("os.arch") ?: "ARM"
    }

    BackHandler { onBack() }

    fun refreshStats() {
        memInfo = DeviceInfoProvider.getMemoryInfo(context)
        batteryInfo = DeviceInfoProvider.getBatteryInfo(context)
    }

    // Refresh telemetry periodically while on screen
    LaunchedEffect(Unit) {
        while (isActive) {
            refreshStats()
            delay(2000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("performance_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "PERFORMANCE TUNER",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Hardware Governor & Telemetry",
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
            actions = {
                IconButton(onClick = { refreshStats() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = GoldAccent
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
            // Section 1: MODE SELECTOR
            Text(
                text = "OPERATING PROFILE",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 1. PERFORMANCE MODE
            ModeSelectionCard(
                title = "Performance Mode",
                subtitle = "Mengutamakan framerate & respon sentuh tercepat. Mempertahankan sampling rate tinggi dan scheduler agresif.",
                icon = Icons.Default.ElectricBolt,
                accentColor = Color(0xFFEF4444),
                isSelected = activeMode == "PERFORMANCE",
                onClick = { settingsManager.setPerformanceMode("PERFORMANCE") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. BALANCED MODE
            ModeSelectionCard(
                title = "Balanced Mode",
                subtitle = "Keseimbangan ideal antara framerate stabil, efisiensi termal, dan daya tahan baterai harian.",
                icon = Icons.Default.Speed,
                accentColor = GoldAccent,
                isSelected = activeMode == "BALANCED",
                onClick = { settingsManager.setPerformanceMode("BALANCED") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. BATTERY SAVER
            ModeSelectionCard(
                title = "Battery Saver",
                subtitle = "Mengurangi polling latar belakang dan konsumsi daya untuk memperpanjang durasi gaming saat baterai menipis.",
                icon = Icons.Default.BatteryChargingFull,
                accentColor = Color(0xFF10B981),
                isSelected = activeMode == "BATTERY_SAVER",
                onClick = { settingsManager.setPerformanceMode("BATTERY_SAVER") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: REAL HARDWARE TELEMETRY
            Text(
                text = "REAL-TIME HARDWARE METRICS",
                color = GoldAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Data langsung dari API Android resmi (ActivityManager, BatteryManager, Hardware Sensors).",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Grid of 4 Real Telemetry Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CPU METRIC CARD
                TelemetryCard(
                    modifier = Modifier.weight(1f),
                    title = "CPU HARDWARE",
                    primaryVal = "$cpuCores Cores",
                    secondaryVal = cpuArch,
                    detailVal = "Governor: Adaptive",
                    icon = Icons.Default.Memory,
                    accentColor = Color(0xFF38BDF8)
                )

                // RAM METRIC CARD
                val totalGb = String.format("%.1f GB", memInfo.totalRamMb / 1024.0)
                val usedGb = String.format("%.1f GB", memInfo.usedRamMb / 1024.0)
                TelemetryCard(
                    modifier = Modifier.weight(1f),
                    title = "PHYSICAL RAM",
                    primaryVal = "${memInfo.usedPercent}%",
                    secondaryVal = "$usedGb / $totalGb",
                    detailVal = if (memInfo.isLowMemory) "Status: Low Memory" else "Status: Optimal",
                    icon = Icons.Default.Speed,
                    accentColor = GoldAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TEMPERATURE CARD
                val tempStr = if (batteryInfo.temperatureCelsius > 0) {
                    "%.1f°C".format(batteryInfo.temperatureCelsius)
                } else {
                    "Tidak tersedia"
                }
                TelemetryCard(
                    modifier = Modifier.weight(1f),
                    title = "BATTERY TEMP",
                    primaryVal = tempStr,
                    secondaryVal = "Kondisi: ${batteryInfo.health}",
                    detailVal = if (batteryInfo.temperatureCelsius > 42) "Thermal: Hangat" else "Thermal: Aman",
                    icon = Icons.Default.DeviceThermostat,
                    accentColor = Color(0xFFFB923C)
                )

                // BATTERY LEVEL CARD
                TelemetryCard(
                    modifier = Modifier.weight(1f),
                    title = "BATTERY LEVEL",
                    primaryVal = "${batteryInfo.level}%",
                    secondaryVal = if (batteryInfo.voltageMv > 0) "${batteryInfo.voltageMv} mV" else "Li-ion",
                    detailVal = batteryInfo.chargingSource,
                    icon = Icons.Default.BatteryChargingFull,
                    accentColor = Color(0xFF4ADE80)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ModeSelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF1B2030) else DarkCardBg)
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = if (isSelected) accentColor else BorderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun TelemetryCard(
    modifier: Modifier = Modifier,
    title: String,
    primaryVal: String,
    secondaryVal: String,
    detailVal: String,
    icon: ImageVector,
    accentColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardBg)
            .border(0.8.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = primaryVal,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = secondaryVal,
                color = accentColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = detailVal,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}
