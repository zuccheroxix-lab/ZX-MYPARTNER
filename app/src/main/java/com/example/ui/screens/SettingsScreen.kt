package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.Config
import com.example.data.SettingsManager
import com.example.ui.components.DashboardHeader
import com.example.ui.components.isShizukuInstalled

@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    isDarkMode: Boolean,
    animationsEnabled: Boolean,
    hapticsEnabled: Boolean,
    appVersion: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var extremeModeEnabled by remember { mutableStateOf(true) }
    var touchSampling240Hz by remember { mutableStateOf(true) }
    var fpsStabilizer by remember { mutableStateOf(true) }
    var showShizukuDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .verticalScroll(scrollState)
            .testTag("settings_screen")
    ) {
        // Top Header matching video: Settings INOX | Settings Menu ZENIX | Dev : ZyrulFIVE
        DashboardHeader(
            title = "Settings ZX",
            subtitle = "Settings Menu ZX",
            developerName = Config.DEVELOPER_NAME
        )

        // Section 1: Shizuku Service Privileged Access
        SettingsSectionTitle(title = "SHIZUKU PRIVILEGED SERVICE")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF141416))
                .border(0.8.dp, Color(0xFF282830), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                val installed = isShizukuInstalled(context)
                SettingsActionTile(
                    icon = Icons.Default.Security,
                    title = "Shizuku ADB Service",
                    subtitle = if (installed) "Terpasang (Service Not Running)" else "Belum Terpasang (Tap untuk download)",
                    badge = if (installed) "Inactive" else "Install",
                    badgeColor = Color(0xFFF97316),
                    onClick = { showShizukuDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Game Space & ZX Booster Engine
        SettingsSectionTitle(title = "GAME SPACE & ZX ENGINE")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF141416))
                .border(0.8.dp, Color(0xFF282830), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsSwitchTile(
                    icon = Icons.Default.Speed,
                    title = "Extreme Performance Governor",
                    subtitle = "Prioritaskan frekuensi CPU/GPU untuk game berat",
                    checked = extremeModeEnabled,
                    onCheckedChange = {
                        extremeModeEnabled = it
                        Toast.makeText(context, if (it) "Performance Governor: AKTIF" else "Performance Governor: ECO", Toast.LENGTH_SHORT).show()
                    }
                )

                HorizontalDivider(color = Color(0xFF222228), thickness = 0.6.dp)

                SettingsSwitchTile(
                    icon = Icons.Default.TouchApp,
                    title = "Ultra Touch Sampling (240Hz)",
                    subtitle = "Tingkatkan responsivitas sentuhan jari saat bermain",
                    checked = touchSampling240Hz,
                    onCheckedChange = {
                        touchSampling240Hz = it
                        Toast.makeText(context, if (it) "Sampling Rate: 240Hz Dikunci" else "Sampling Rate: Default", Toast.LENGTH_SHORT).show()
                    }
                )

                HorizontalDivider(color = Color(0xFF222228), thickness = 0.6.dp)

                SettingsSwitchTile(
                    icon = Icons.Default.ElectricBolt,
                    title = "FPS Frame Stabilizer",
                    subtitle = "Mencegah thermal throttling mendadak",
                    checked = fpsStabilizer,
                    onCheckedChange = {
                        fpsStabilizer = it
                        Toast.makeText(context, if (it) "FPS Stabilizer: AKTIF" else "FPS Stabilizer: NONAKTIF", Toast.LENGTH_SHORT).show()
                    }
                )

                HorizontalDivider(color = Color(0xFF222228), thickness = 0.6.dp)

                val isHudActive by com.example.service.OverlayService.isServiceRunning.collectAsState()
                SettingsSwitchTile(
                    icon = androidx.compose.material.icons.Icons.Default.SportsEsports,
                    title = "Floating Gaming HUD Overlay",
                    subtitle = "Tampilkan floating HUD game di atas Free Fire, MLBB, PUBG",
                    checked = isHudActive,
                    onCheckedChange = { enable ->
                        if (enable) {
                            if (!android.provider.Settings.canDrawOverlays(context)) {
                                Toast.makeText(context, "Izin Overlay diperlukan", Toast.LENGTH_SHORT).show()
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    android.net.Uri.parse("package:${context.packageName}")
                                ).apply { flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK }
                                context.startActivity(intent)
                            } else {
                                com.example.service.OverlayService.start(context)
                            }
                        } else {
                            com.example.service.OverlayService.stop(context)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: Tactile & Haptic Feedback
        SettingsSectionTitle(title = "HAPTIC & SYSTEM FEEDBACK")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF141416))
                .border(0.8.dp, Color(0xFF282830), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsSwitchTile(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Tactile Feedback",
                    subtitle = "Respon getaran saat menekan tombol dashboard & booster",
                    checked = hapticsEnabled,
                    onCheckedChange = { enabled ->
                        settingsManager.setHapticsEnabled(enabled)
                        if (enabled) {
                            try {
                                val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                                    vm?.defaultVibrator
                                } else {
                                    @Suppress("DEPRECATION")
                                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vib?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vib?.vibrate(50)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 4: DYNIMETIZE ZX System Info
        SettingsSectionTitle(title = "DYNIMETIZE ZX BUILD & ABOUT")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF141416))
                .border(0.8.dp, Color(0xFF282830), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                SettingsActionTile(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = Config.DEFAULT_VERSION_NAME,
                    badge = Config.SYSTEM_BUILD_ID,
                    badgeColor = Color(0xFF8E8E98),
                    onClick = {
                        Toast.makeText(context, "${Config.APP_NAME} (${Config.DEFAULT_VERSION_NAME})", Toast.LENGTH_SHORT).show()
                    }
                )

                HorizontalDivider(color = Color(0xFF222228), thickness = 0.6.dp)

                SettingsActionTile(
                    icon = Icons.Default.Info,
                    title = "Developer & Core",
                    subtitle = Config.DEVELOPER_FULL,
                    badge = Config.DEVELOPER_ROLE,
                    badgeColor = Color.White,
                    onClick = {
                        Toast.makeText(context, "Lead Developer: ${Config.DEVELOPER_NAME}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showShizukuDialog) {
        AlertDialog(
            onDismissRequest = { showShizukuDialog = false },
            title = {
                Text(
                    text = "Shizuku Service Configuration",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Shizuku memungkinkan DYNIMETIZE ZX mengeksekusi perintah shell tingkat lanjut (seperti penguncian refresh rate dan prioritas proses) tanpa memerlukan root.",
                    color = Color(0xFFCCCCCC),
                    fontSize = 13.sp
                )
            },
            containerColor = Color(0xFF18181C),
            confirmButton = {
                TextButton(onClick = { showShizukuDialog = false }) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = Color(0xFF8E8E98),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp
        ),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingsSwitchTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F24)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp
                    )
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF3B82F6),
                uncheckedThumbColor = Color(0xFF8E8E98),
                uncheckedTrackColor = Color(0xFF282830)
            )
        )
    }
}

@Composable
private fun SettingsActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1F24)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF222228))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = badgeColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
