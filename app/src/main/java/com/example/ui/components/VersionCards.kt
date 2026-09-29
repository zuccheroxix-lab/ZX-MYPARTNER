package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Config

@Composable
fun VersionCards(
    appVersion: String = Config.DEFAULT_VERSION_NAME,
    developerName: String = Config.DEVELOPER_NAME,
    onDeveloperClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Card: VERSION APP
        VersionCardItem(
            modifier = Modifier
                .weight(1f)
                .testTag("version_app_card"),
            icon = Icons.Default.Info,
            title = "VERSION APP",
            value = "Version $appVersion",
            onClick = {
                Toast.makeText(
                    context,
                    "${Config.APP_NAME} (Version $appVersion)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        // Right Card: DEVELOPER APP
        VersionCardItem(
            modifier = Modifier
                .weight(1f)
                .testTag("developer_app_card"),
            icon = Icons.Default.Person,
            title = "DEVELOPER APP",
            value = developerName,
            onClick = {
                if (onDeveloperClick != null) {
                    onDeveloperClick()
                } else {
                    Toast.makeText(
                        context,
                        "Developer: ${Config.DEVELOPER_FULL} (ID: ${Config.DEVELOPER_ID})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
private fun VersionCardItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141416))
            .border(
                width = 0.8.dp,
                color = Color(0xFF282830),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.2.dp, Color(0xFF8E8E98), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF8E8E98),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1
            )
        }
    }
}
