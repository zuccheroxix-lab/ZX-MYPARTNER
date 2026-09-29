package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.BatteryInfo
import com.example.data.DeviceInfoProvider
import com.example.data.MemoryInfoData
import com.example.data.NetworkDiagnosticInfo
import com.example.data.StorageInfoData
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Real-time State
    var batteryInfo by remember { mutableStateOf(DeviceInfoProvider.getBatteryInfo(context)) }
    var memoryInfo by remember { mutableStateOf(DeviceInfoProvider.getMemoryInfo(context)) }
    var storageInfo by remember { mutableStateOf(DeviceInfoProvider.getStorageInfo()) }
    var networkInfo by remember { mutableStateOf(DeviceInfoProvider.getNetworkDiagnostic(context)) }
    var sensorsList by remember { mutableStateOf<List<Sensor>>(emptyList()) }

    // Ping State
    var isPinging by remember { mutableStateOf(false) }
    var pingResultMs by remember { mutableStateOf<Long?>(null) }

    // Flashlight State
    var isFlashlightOn by remember { mutableStateOf(false) }
    val hasFlashlight = remember { DeviceInfoProvider.hasFlashlight(context) }

    // Screen Test State
    var isScreenTestOpen by remember { mutableStateOf(false) }
    val testColors = listOf(Color.Red, Color.Green, Color.Blue, Color.White, Color.Black)
    var currentColorIndex by remember { mutableStateOf(0) }

    // Load initial sensors list
    LaunchedEffect(Unit) {
        sensorsList = DeviceInfoProvider.getDeviceSensors(context)
    }

    // Safety: turn off flashlight when leaving composable
    DisposableEffect(Unit) {
        onDispose {
            if (isFlashlightOn) {
                try {
                    val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                    val cameraId = cm?.cameraIdList?.firstOrNull()
                    if (cameraId != null) {
                        cm.setTorchMode(cameraId, false)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun vibrateDevice(pattern: Int) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    when (pattern) {
                        1 -> vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                        2 -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 100, 120), -1))
                        else -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 150, 150, 150, 300), -1))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(80)
                }
                Toast.makeText(context, "Haptic pulse emitted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Vibrator hardware not available on this device", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Vibrator execution error", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFlashlight() {
        if (!hasFlashlight) {
            Toast.makeText(context, "Flashlight hardware unavailable on this device", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cm?.cameraIdList?.firstOrNull()
            if (cameraId != null) {
                val nextState = !isFlashlightOn
                cm.setTorchMode(cameraId, nextState)
                isFlashlightOn = nextState
                Toast.makeText(context, if (nextState) "Flashlight turned ON" else "Flashlight turned OFF", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot toggle torch: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tools_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyanAccent)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DIAGNOSTIC & HARDWARE SUITE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CyanAccent,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tools",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                )
                Text(
                    text = "Functional hardware diagnostics powered by official Android APIs",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // TOOL 1: BATTERY TELEMETRY
        item {
            ToolCardContainer(
                title = "BATTERY TELEMETRY",
                subtitle = "BatteryManager Broadcast Receiver",
                icon = Icons.Default.BatteryChargingFull,
                onRefresh = {
                    batteryInfo = DeviceInfoProvider.getBatteryInfo(context)
                    Toast.makeText(context, "Battery data refreshed", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${batteryInfo.level}%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = if (batteryInfo.level > 20) StatusGreen else StatusRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        )
                        Text(
                            text = "${batteryInfo.status} • ${batteryInfo.chargingSource}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Temp: ${batteryInfo.temperatureCelsius}°C",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (batteryInfo.temperatureCelsius > 42f) StatusRed else TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Voltage: ${batteryInfo.voltageMv} mV",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                        Text(
                            text = "Health: ${batteryInfo.health}",
                            style = MaterialTheme.typography.bodySmall.copy(color = StatusGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (batteryInfo.level.coerceIn(0, 100) / 100f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (batteryInfo.level > 20) CyanAccent else StatusRed,
                    trackColor = SurfaceElevated
                )
            }
        }

        // TOOL 2: RAM & MEMORY INSPECTOR
        item {
            ToolCardContainer(
                title = "RAM & MEMORY USAGE",
                subtitle = "ActivityManager.MemoryInfo API",
                icon = Icons.Default.Memory,
                onRefresh = {
                    memoryInfo = DeviceInfoProvider.getMemoryInfo(context)
                    Toast.makeText(context, "Memory stats refreshed", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${memoryInfo.usedPercent}% Used",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (memoryInfo.usedPercent > 85) StatusRed else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${memoryInfo.usedRamMb} MB used of ${memoryInfo.totalRamMb} MB",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Free: ${memoryInfo.availableRamMb} MB",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (memoryInfo.isLowMemory) "Low Memory: YES" else "Memory State: Optimal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (memoryInfo.isLowMemory) StatusRed else StatusGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (memoryInfo.usedPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (memoryInfo.usedPercent > 85) StatusRed else CyanAccent,
                    trackColor = SurfaceElevated
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        val before = memoryInfo.availableRamMb
                        System.gc()
                        memoryInfo = DeviceInfoProvider.getMemoryInfo(context)
                        val freed = (memoryInfo.availableRamMb - before).coerceAtLeast(0)
                        Toast.makeText(context, "GC triggered. Memory inspected ($freed MB reclaimed)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, CardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Trigger System Garbage Collection Hint",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // TOOL 3: INTERNAL STORAGE ANALYZER
        item {
            ToolCardContainer(
                title = "INTERNAL STORAGE",
                subtitle = "StatFs Partition Query",
                icon = Icons.Default.SdStorage,
                onRefresh = {
                    storageInfo = DeviceInfoProvider.getStorageInfo()
                    Toast.makeText(context, "Storage calculated", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${storageInfo.usedStorageGb} GB / ${storageInfo.totalStorageGb} GB",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${storageInfo.usedPercent}% Capacity Occupied",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Free: ${storageInfo.freeStorageGb} GB",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = StatusGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (storageInfo.usedPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CyanAccent,
                    trackColor = SurfaceElevated
                )
            }
        }

        // TOOL 4: NETWORK DIAGNOSTIC & PING TEST
        item {
            ToolCardContainer(
                title = "NETWORK & PING DIAGNOSTIC",
                subtitle = "ConnectivityManager & Socket Latency",
                icon = Icons.Default.NetworkCheck,
                onRefresh = {
                    networkInfo = DeviceInfoProvider.getNetworkDiagnostic(context)
                    Toast.makeText(context, "Network diagnostic refreshed", Toast.LENGTH_SHORT).show()
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Type: ${networkInfo.networkType}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = if (networkInfo.isConnected) "Status: Connected" else "Status: Disconnected",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (networkInfo.isConnected) StatusGreen else StatusRed
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Down: ~${networkInfo.linkDownSpeedMbps} Mbps",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = if (networkInfo.isMetered) "Metered Connection" else "Unmetered (High Bandwidth)",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (!isPinging) {
                                isPinging = true
                                pingResultMs = null
                                scope.launch {
                                    val latency = withContext(Dispatchers.IO) {
                                        DeviceInfoProvider.pingHost("8.8.8.8", 3000)
                                    }
                                    pingResultMs = latency
                                    isPinging = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = Color(0xFF001E2E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_run_ping")
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF001E2E),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ping DNS (8.8.8.8)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceElevated)
                            .border(0.6.dp, CardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                isPinging -> "Measuring RTT..."
                                pingResultMs == null -> "Ping not run"
                                pingResultMs!! >= 0 -> "${pingResultMs} ms (OK)"
                                else -> "Host Unreachable"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = when {
                                    pingResultMs == null -> TextMuted
                                    pingResultMs!! in 0..150 -> StatusGreen
                                    pingResultMs!! > 150 -> StatusAmber
                                    else -> StatusRed
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // TOOL 5: HARDWARE SENSORS SCANNER
        item {
            ToolCardContainer(
                title = "HARDWARE SENSORS SCANNER",
                subtitle = "SensorManager Query",
                icon = Icons.Default.Sensors,
                onRefresh = {
                    sensorsList = DeviceInfoProvider.getDeviceSensors(context)
                    Toast.makeText(context, "${sensorsList.size} sensors detected", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(
                    text = "Total Hardware Sensors Detected: ${sensorsList.size}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CyanGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (sensorsList.isEmpty()) {
                    Text(
                        text = "No physical sensors exposed in this hardware environment.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        sensorsList.take(6).forEach { sensor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceElevated)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sensor.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${sensor.vendor} • Power: ${sensor.power} mA",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x2200D2FF))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Type ${sensor.type}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CyanAccent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        if (sensorsList.size > 6) {
                            Text(
                                text = "+ ${sensorsList.size - 6} additional sensors operational on device",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // TOOL 6: FLASHLIGHT / TORCH
        item {
            ToolCardContainer(
                title = "FLASHLIGHT / TORCH CONTROLLER",
                subtitle = "CameraManager.setTorchMode API",
                icon = Icons.Default.FlashlightOn
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasFlashlight) {
                                if (isFlashlightOn) "Torch Active (ON)" else "Torch Standby (OFF)"
                            } else {
                                "Hardware Flash Unavailable"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isFlashlightOn) CyanGlow else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (hasFlashlight) "Toggles device camera LED flash" else "Device has no rear camera flash unit",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Button(
                        onClick = { toggleFlashlight() },
                        enabled = hasFlashlight,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFlashlightOn) Color(0xFFEF4444) else CyanAccent,
                            contentColor = if (isFlashlightOn) Color.White else Color(0xFF001E2E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_toggle_flashlight")
                    ) {
                        Text(
                            text = if (isFlashlightOn) "TURN OFF" else "TURN ON",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // TOOL 7: HAPTIC ENGINE TESTER
        item {
            ToolCardContainer(
                title = "HAPTIC MOTOR TESTER",
                subtitle = "Vibrator Service Waveforms",
                icon = Icons.Default.Vibration
            ) {
                Text(
                    text = "Test physical device vibration actuator with distinct tactile effects:",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { vibrateDevice(1) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, CardBorder)
                    ) {
                        Text("Click (50ms)", fontSize = 11.sp, color = TextPrimary)
                    }

                    OutlinedButton(
                        onClick = { vibrateDevice(2) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, CardBorder)
                    ) {
                        Text("Double Pulse", fontSize = 11.sp, color = CyanAccent)
                    }

                    OutlinedButton(
                        onClick = { vibrateDevice(3) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, CardBorder)
                    ) {
                        Text("Alarm Wave", fontSize = 11.sp, color = Color(0xFFFBBF24))
                    }
                }
            }
        }

        // TOOL 8: SCREEN TEST PATTERN
        item {
            ToolCardContainer(
                title = "SCREEN & DEAD PIXEL TESTER",
                subtitle = "Display Panel Color Calibration",
                icon = Icons.Default.Visibility
            ) {
                Text(
                    text = "Launches full-screen solid RGB, White and Black frames to visually inspect display panel uniformity and dead pixels.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        currentColorIndex = 0
                        isScreenTestOpen = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = CyanAccent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, CyanAccent.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_screen_test")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Fullscreen Display Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    // Fullscreen Screen Pixel Test Dialog
    if (isScreenTestOpen) {
        Dialog(
            onDismissRequest = { isScreenTestOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(testColors[currentColorIndex])
                    .clickable {
                        if (currentColorIndex < testColors.size - 1) {
                            currentColorIndex++
                        } else {
                            isScreenTestOpen = false
                            Toast.makeText(context, "Display test complete", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xAA000000))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Tap to next color (${currentColorIndex + 1}/${testColors.size}) • Double tap outside to exit",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolCardContainer(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onRefresh: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceCard)
            .border(0.8.dp, CardBorder, RoundedCornerShape(22.dp))
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CyanAccent.copy(alpha = 0.12f))
                            .border(0.6.dp, CyanAccent.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                if (onRefresh != null) {
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorderSubtle, thickness = 0.6.dp)
            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
