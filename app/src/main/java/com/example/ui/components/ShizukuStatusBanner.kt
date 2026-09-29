package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Config

/**
 * Checks if Shizuku app is installed on the user device
 */
fun isShizukuInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo(Config.SHIZUKU_PACKAGE_NAME, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}

/**
 * Floating Warning Banner matching video reference:
 * "🛡️ Layanan Shizuku Tidak Berjalan!"
 */
@Composable
fun ShizukuStatusBanner(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1C1917).copy(alpha = 0.95f))
            .border(
                width = 0.8.dp,
                color = Color(0xFFF97316).copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { showDialog = true }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("shizuku_warning_banner"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFFF97316),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRunning) "Layanan Shizuku Berjalan" else "Layanan Shizuku Tidak Berjalan!",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isRunning) Color(0xFF4ADE80) else Color(0xFFFDE047),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.2.sp
                )
            )
        }
    }

    if (showDialog) {
        val installed = isShizukuInstalled(context)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Status Layanan Shizuku",
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
                        text = if (installed) {
                            "Aplikasi Shizuku terpasang di perangkat Anda, namun layanannya belum aktif via ADB atau Wireless Debugging."
                        } else {
                            "Aplikasi Shizuku belum terpasang. Shizuku diperlukan agar DYNIMETIZE ZX dapat mengoptimalkan touch sampling, governor CPU, dan booster FPS secara penuh."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCCCCCC),
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Package: ${Config.SHIZUKU_PACKAGE_NAME}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF888888),
                            fontSize = 11.sp
                        )
                    )
                }
            },
            containerColor = Color(0xFF1E1E24),
            confirmButton = {
                Button(
                    onClick = {
                        if (installed) {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(Config.SHIZUKU_PACKAGE_NAME)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            } else {
                                Toast.makeText(context, "Membuka Shizuku...", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=${Config.SHIZUKU_PACKAGE_NAME}")
                            )
                            context.startActivity(intent)
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF97316)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (installed) "Buka Shizuku" else "Download Shizuku",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isRunning = isShizukuInstalled(context)
                        Toast.makeText(
                            context,
                            if (isRunning) "Mendeteksi layanan Shizuku..." else "Layanan Shizuku tetap tidak berjalan",
                            Toast.LENGTH_SHORT
                        ).show()
                        showDialog = false
                    }
                ) {
                    Text("Tutup", color = Color(0xFFAAAAAA))
                }
            }
        )
    }
}
