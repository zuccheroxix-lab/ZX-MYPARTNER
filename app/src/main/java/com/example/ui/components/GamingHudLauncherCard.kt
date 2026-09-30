package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.OverlayService

private val GoldAccent = Color(0xFFFFB300)
private val DarkCardBg = Color(0xFF141722)
private val BorderColor = Color(0xFF262C3E)

@Composable
fun GamingHudLauncherCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRunning by OverlayService.isServiceRunning.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }

    fun checkAndToggleHud() {
        val hasPermission = Settings.canDrawOverlays(context)
        if (!hasPermission) {
            showPermissionDialog = true
            return
        }

        if (isRunning) {
            OverlayService.stop(context)
            Toast.makeText(context, "Gaming HUD dinonaktifkan", Toast.LENGTH_SHORT).show()
        } else {
            OverlayService.start(context)
            Toast.makeText(context, "Gaming HUD Aktif! Trigger floating muncul di layar", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCardBg)
            .border(
                width = 1.dp,
                brush = if (isRunning) {
                    Brush.horizontalGradient(listOf(GoldAccent, Color(0xFFF59E0B)))
                } else {
                    Brush.horizontalGradient(listOf(BorderColor, BorderColor))
                },
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
            .testTag("gaming_hud_launcher_card")
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isRunning) Color(0xFF2B2210) else Color(0xFF1B202D)
                            )
                            .border(
                                1.dp,
                                if (isRunning) GoldAccent else Color(0xFF333B50),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "HUD Overlay",
                            tint = if (isRunning) GoldAccent else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "FLOATING GAMING HUD",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isRunning) Color(0xFF10B981) else Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRunning) "ACTIVE • OVERLAY RUNNING" else "STANDBY • TAP TO LAUNCH",
                                color = if (isRunning) Color(0xFF10B981) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Launch / Stop Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isRunning) Color(0xFF3B1218) else GoldAccent
                        )
                        .clickable { checkAndToggleHud() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("btn_toggle_hud_overlay")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Stop" else "Launch",
                            tint = if (isRunning) Color(0xFFFCA5A5) else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRunning) "STOP" else "START",
                            color = if (isRunning) Color(0xFFFCA5A5) else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle Description & Feature Highlights
            Text(
                text = "Muncul di atas game (Free Fire, PUBG, MLBB) dengan real-time FPS, RAM, CPU telemetry, 8 kontrol aksi game, dan slider brightness/volume tanpa keluar dari game.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HudBadge(label = "REAL-TIME FPS", active = isRunning)
                HudBadge(label = "RAM CLEANER", active = isRunning)
                HudBadge(label = "240Hz TOUCH", active = isRunning)
                HudBadge(label = "HUD OVERLAY", active = true)
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Permission",
                    tint = GoldAccent,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Izin Display Overlay Diperlukan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Untuk menampilkan panel floating Gaming HUD di atas game atau aplikasi lain, Android memerlukan izin 'Tampilkan di atas aplikasi lain' (SYSTEM_ALERT_WINDOW).\n\nTekan tombol di bawah untuk membuka halaman pengaturan izin.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(fallbackIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Buka Pengaturan Izin", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Batal", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun HudBadge(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) Color(0xFF241D12) else Color(0xFF1E2332))
            .border(0.6.dp, if (active) GoldAccent.copy(alpha = 0.6f) else Color(0xFF2C344A), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = if (active) GoldAccent else Color(0xFF94A3B8),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
