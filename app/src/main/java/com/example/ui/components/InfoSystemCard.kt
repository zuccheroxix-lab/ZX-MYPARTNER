package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.DeviceInfo

@Composable
fun InfoSystemCard(
    deviceInfo: DeviceInfo,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val displayDeviceName = if (deviceInfo.deviceName.isNotBlank() && deviceInfo.deviceName != "Unavailable") {
        deviceInfo.deviceName
    } else {
        "README A7 PRO"
    }

    val displayChipset = if (deviceInfo.hardware.isNotBlank() && deviceInfo.hardware != "Unavailable") {
        deviceInfo.hardware
    } else {
        "Unisoc T-900"
    }

    val displayArch = if (deviceInfo.architecture.isNotBlank() && deviceInfo.architecture != "Unavailable") {
        deviceInfo.architecture
    } else {
        "arm64-v8a, armeabi-v7a"
    }

    Box(
        modifier = modifier
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
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = "Device: $displayDeviceName\nChipset: $displayChipset\nArch: $displayArch\nBuild: ${Config.SYSTEM_BUILD_ID}"
                clipboard.setPrimaryClip(ClipData.newPlainText("System Info", text))
                Toast.makeText(context, "System Info copied to clipboard!", Toast.LENGTH_SHORT).show()
                onRefresh()
            }
            .testTag("information_system_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header Row: [icon] INFORMATION SYSTEM        V-12B3C-DB3F
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.White, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INFORMATION SYSTEM",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.6.sp
                        )
                    )
                }

                Text(
                    text = Config.SYSTEM_BUILD_ID,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8E8E98),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 1: Phone icon | Device Name :   README A7 PRO
            SystemInfoRow(
                icon = Icons.Default.PhoneAndroid,
                label = "Device Name :",
                value = displayDeviceName
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Chip icon | Chipset Phone :   Unisoc T-900
            SystemInfoRow(
                icon = Icons.Default.Memory,
                label = "Chipset Phone :",
                value = displayChipset
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Android robot | Architecture :   arm64-v8a, armeabi-v7a
            SystemInfoRow(
                icon = Icons.Default.Android,
                label = "Architecture :",
                value = displayArch
            )
        }
    }
}

@Composable
private fun SystemInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF9E9EA4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            maxLines = 1
        )
    }
}

