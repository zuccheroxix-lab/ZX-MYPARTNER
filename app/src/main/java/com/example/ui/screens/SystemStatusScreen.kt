package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeviceInfoProvider

private val GoldAccent = Color(0xFFFFB300)
private val DarkCardBg = Color(0xFF141722)
private val BorderColor = Color(0xFF262C3E)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemStatusScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var memInfo by remember { mutableStateOf(DeviceInfoProvider.getMemoryInfo(context)) }
    var storageInfo by remember { mutableStateOf(DeviceInfoProvider.getStorageInfo()) }
    var batteryInfo by remember { mutableStateOf(DeviceInfoProvider.getBatteryInfo(context)) }
    val deviceInfo = remember { DeviceInfoProvider.getDeviceInfo(context) }

    fun refreshAll() {
        memInfo = DeviceInfoProvider.getMemoryInfo(context)
        storageInfo = DeviceInfoProvider.getStorageInfo()
        batteryInfo = DeviceInfoProvider.getBatteryInfo(context)
    }

    BackHandler { onBack() }

    // Biometric Status Check via official BiometricManager
    val biometricStatusStr = remember {
        try {
            val bm = BiometricManager.from(context)
            val auths = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            when (bm.canAuthenticate(auths)) {
                BiometricManager.BIOMETRIC_SUCCESS -> "Tersedia & Terdaftar (Siap Digunakan)"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Sensor Ada • Belum Didaftarkan di Pengaturan"
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Perangkat Tidak Memiliki Sensor Biometrik"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Sensor Biometrik Sedang Sibuk / Tidak Tersedia"
                else -> "Status Biometrik Tidak Diketahui"
            }
        } catch (_: Exception) {
            "Tidak dapat diperiksa"
        }
    }

    // Permission status checks
    val hasOverlayPerm = remember { Settings.canDrawOverlays(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("system_status_screen")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "SYSTEM STATUS",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Real Hardware & OS Specifications",
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
                IconButton(onClick = { refreshAll() }) {
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // 1. ANDROID VERSION & OS SPECS
            StatusCategorySection(
                title = "1. ANDROID OS & VERSION",
                icon = Icons.Default.Android,
                iconColor = Color(0xFF34D399)
            ) {
                StatusItemRow("Android Version", "Android ${deviceInfo.androidVersion}")
                StatusItemRow("API Level (SDK)", "API ${deviceInfo.sdkVersion}")
                StatusItemRow("Security Patch", deviceInfo.securityPatch)
                StatusItemRow("Brand & Model", "${deviceInfo.brand} • ${deviceInfo.model}")
                StatusItemRow("CPU ABI Arch", deviceInfo.cpuAbi)
                StatusItemRow("Bootloader", deviceInfo.bootloader)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. RAM HARDWARE SPECIFICATION
            val totalGb = String.format("%.2f GB", memInfo.totalRamMb / 1024.0)
            val usedGb = String.format("%.2f GB", memInfo.usedRamMb / 1024.0)
            val availGb = String.format("%.2f GB", memInfo.availableRamMb / 1024.0)

            StatusCategorySection(
                title = "2. MEMORY (RAM) STATUS",
                icon = Icons.Default.Memory,
                iconColor = GoldAccent
            ) {
                StatusItemRow("Total RAM Fisik", totalGb)
                StatusItemRow("RAM Digunakan", "$usedGb (${memInfo.usedPercent}%)")
                StatusItemRow("RAM Tersedia", availGb)
                StatusItemRow(
                    "Low Memory Alert",
                    if (memInfo.isLowMemory) "PERINGATAN: Memori Rendah" else "Normal (Optimal)"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. STORAGE STATUS
            StatusCategorySection(
                title = "3. INTERNAL STORAGE STATUS",
                icon = Icons.Default.SdStorage,
                iconColor = Color(0xFF38BDF8)
            ) {
                StatusItemRow("Total Penyimpanan", "${storageInfo.totalStorageGb} GB")
                StatusItemRow("Penyimpanan Terpakai", "${storageInfo.usedStorageGb} GB (${storageInfo.usedPercent}%)")
                StatusItemRow("Penyimpanan Tersedia", "${storageInfo.freeStorageGb} GB")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. BATTERY STATUS
            StatusCategorySection(
                title = "4. BATTERY HEALTH & POWER",
                icon = Icons.Default.BatteryChargingFull,
                iconColor = Color(0xFF4ADE80)
            ) {
                StatusItemRow("Level Baterai", "${batteryInfo.level}%")
                StatusItemRow("Sumber Pengisian", batteryInfo.chargingSource)
                StatusItemRow("Tegangan (Voltage)", "${batteryInfo.voltageMv} mV")
                StatusItemRow(
                    "Suhu Baterai",
                    if (batteryInfo.temperatureCelsius > 0) "%.1f °C".format(batteryInfo.temperatureCelsius) else "Sensor tidak tersedia"
                )
                StatusItemRow("Kesehatan Baterai", batteryInfo.health)
                StatusItemRow("Teknologi", batteryInfo.technology)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. BIOMETRIC HARDWARE STATUS
            StatusCategorySection(
                title = "5. BIOMETRIC STATUS (FINGERPRINT)",
                icon = Icons.Default.Fingerprint,
                iconColor = Color(0xFFA78BFA)
            ) {
                StatusItemRow("Status Sensor", biometricStatusStr)
                StatusItemRow("API Standard", "AndroidX BiometricPrompt 1.2.0")
                StatusItemRow("Authenticators", "BIOMETRIC_STRONG | BIOMETRIC_WEAK")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. PERMISSION STATUS
            StatusCategorySection(
                title = "6. PERMISSION STATUS",
                icon = Icons.Default.Security,
                iconColor = Color(0xFFF472B6)
            ) {
                StatusItemRow(
                    "Overlay (SYSTEM_ALERT_WINDOW)",
                    if (hasOverlayPerm) "DIIZINKAN (Granted)" else "BELUM DIIZINKAN"
                )
                StatusItemRow("Internet Access", "DIIZINKAN (Normal Permission)")
                StatusItemRow("Vibrate Haptic", "DIIZINKAN (Normal Permission)")
                StatusItemRow("Biometric Hardware Access", "DIIZINKAN (Normal Permission)")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun StatusCategorySection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardBg)
            .border(0.8.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun StatusItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
