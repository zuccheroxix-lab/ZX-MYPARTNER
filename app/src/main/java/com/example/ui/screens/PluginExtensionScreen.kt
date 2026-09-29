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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.DeviceInfoProvider
import com.example.ui.components.DashboardHeader
import kotlinx.coroutines.launch

data class ZenixPlugin(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val icon: ImageVector,
    var isEnabled: Boolean
)

@Composable
fun PluginExtensionScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPlugin by remember { mutableStateOf<ZenixPlugin?>(null) }
    var pingLatency by remember { mutableStateOf<Long?>(null) }
    var isPinging by remember { mutableStateOf(false) }

    val plugins = remember {
        mutableStateListOf(
            ZenixPlugin(
                id = "shizuku_bridge",
                name = "Shizuku ADB Privilege Bridge",
                version = "v1.2.0",
                description = "Menjembatani akses shell ADB tanpa root untuk memodifikasi parameter sistem Android secara runtime.",
                icon = Icons.Default.Security,
                isEnabled = false
            ),
            ZenixPlugin(
                id = "touch_boost",
                name = "DYNIMETIZE Touch Driver Overclock",
                version = "v2.4.1",
                description = "Mengurangi input latency sentuhan jari pada layar hingga 240Hz dengan mem-bypass touch queue filter.",
                icon = Icons.Default.TouchApp,
                isEnabled = true
            ),
            ZenixPlugin(
                id = "game_turbo_fps",
                name = "Game Turbo FPS Stabilizer",
                version = "v3.1.0",
                description = "Mengunci frame rate game pada 60/90 FPS stabil dan mencegah drop frame saat render pertempuran berat.",
                icon = Icons.Default.Speed,
                isEnabled = true
            ),
            ZenixPlugin(
                id = "ping_optimizer",
                name = "Network Latency & Ping Optimizer",
                version = "v1.8.0",
                description = "Mengoptimalkan DNS paket socket game dan mengaktifkan Wi-Fi High Performance Lock.",
                icon = Icons.Default.NetworkCheck,
                isEnabled = true
            ),
            ZenixPlugin(
                id = "thermal_guard",
                name = "Thermal Throttling Bypass Guard",
                version = "v2.0.4",
                description = "Memantau suhu baterai & SoC untuk mencegah penurunan performa agresif dari thermal manager.",
                icon = Icons.Default.ElectricBolt,
                isEnabled = false
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("plugin_extension_screen")
    ) {
        // Top Header matching video: Plugin Extension | plugin Extension Menu ZENIX | Dev : ZyrulFIVE
        DashboardHeader(
            title = "Plugin Extension",
            subtitle = "Plugin Extension Menu ZX",
            developerName = Config.DEVELOPER_NAME
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INSTALLED EXTENSIONS (${plugins.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF8E8E98),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    Text(
                        text = "DYNIMETIZE ZX Core 2.0",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF6E6E78),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            items(plugins, key = { it.id }) { plugin ->
                PluginCardItem(
                    plugin = plugin,
                    onToggle = { newState ->
                        val index = plugins.indexOfFirst { it.id == plugin.id }
                        if (index != -1) {
                            plugins[index] = plugins[index].copy(isEnabled = newState)
                        }
                        // Haptic feedback
                        try {
                            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                                vm?.defaultVibrator
                            } else {
                                @Suppress("DEPRECATION")
                                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vib?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vib?.vibrate(40)
                            }
                        } catch (_: Exception) {}

                        Toast.makeText(
                            context,
                            "${plugin.name}: ${if (newState) "AKTIF" else "NONAKTIF"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onClick = {
                        selectedPlugin = plugin
                        if (plugin.id == "ping_optimizer") {
                            isPinging = true
                            coroutineScope.launch {
                                pingLatency = DeviceInfoProvider.pingHost("8.8.8.8", 1500)
                                isPinging = false
                            }
                        }
                    }
                )
            }

            // Add Plugin Action Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141416))
                        .border(
                            width = 0.8.dp,
                            color = Color(0xFF282830),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            Toast.makeText(
                                context,
                                "Semua modul resmi DYNIMETIZE ZX sudah terpasang dan siap digunakan!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .padding(vertical = 14.dp)
                        .testTag("btn_add_plugin"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add New Extension Module",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Plugin Detail Dialog
    selectedPlugin?.let { plugin ->
        AlertDialog(
            onDismissRequest = { selectedPlugin = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = plugin.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Versi: ${plugin.version} • Status: ${if (plugin.isEnabled) "AKTIF" else "NONAKTIF"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (plugin.isEnabled) Color(0xFF4ADE80) else Color(0xFFF87171),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = plugin.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCCCCCC),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )

                    if (plugin.id == "ping_optimizer") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isPinging) {
                                "Mengukur latensi jaringan real-time..."
                            } else if (pingLatency != null && pingLatency!! >= 0) {
                                "Real Ping: ${pingLatency} ms (Koneksi Stabil)"
                            } else {
                                "Ping: Siap diuji"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            },
            containerColor = Color(0xFF18181C),
            confirmButton = {
                Button(
                    onClick = { selectedPlugin = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun PluginCardItem(
    plugin: ZenixPlugin,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141416))
            .border(
                width = 0.8.dp,
                color = Color(0xFF282830),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("plugin_${plugin.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F1F24)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = plugin.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plugin.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${plugin.version} • Tap untuk detail",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF8E8E98),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Switch(
                checked = plugin.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF3B82F6),
                    uncheckedThumbColor = Color(0xFF8E8E98),
                    uncheckedTrackColor = Color(0xFF282830)
                )
            )
        }
    }
}
