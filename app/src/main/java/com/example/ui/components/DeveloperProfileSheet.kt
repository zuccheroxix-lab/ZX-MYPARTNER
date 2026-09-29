package com.example.ui.components

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay

/**
 * Section 6: STRUKTUR PROFILE
 * Modal Bottom Sheet presenting the complete developer identity, official contacts,
 * channel, support, and partner directory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperProfileSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var copiedDevId by remember { mutableStateOf(false) }
    var copiedPartner1Id by remember { mutableStateOf(false) }
    var copiedPartner2Id by remember { mutableStateOf(false) }

    LaunchedEffect(copiedDevId) {
        if (copiedDevId) {
            delay(2000)
            copiedDevId = false
        }
    }
    LaunchedEffect(copiedPartner1Id) {
        if (copiedPartner1Id) {
            delay(2000)
            copiedPartner1Id = false
        }
    }
    LaunchedEffect(copiedPartner2Id) {
        if (copiedPartner2Id) {
            delay(2000)
            copiedPartner2Id = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF101014),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier.testTag("developer_profile_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .verticalScroll(scrollState)
                .padding(bottom = 36.dp)
        ) {
            // Header: DYNIMETIZE ZX + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Config.APP_NAME,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "DEVELOPER PROFILE & DIRECTORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF8E8E98),
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("btn_close_profile_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF8E8E98)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF222228), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 1. DEVELOPER PROFILE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF18181D))
                    .border(0.8.dp, Color(0xFF2E2E38), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Config.DEVELOPER_NAME,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Config.DEVELOPER_ROLE,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF60A5FA),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF3B82F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ID Box: ID: ZX-DEV-001 [ COPY ]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF101014))
                            .border(0.6.dp, Color(0xFF33333F), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ID: ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF8E8E98),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = Config.DEVELOPER_ID,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF60A5FA),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (copiedDevId) Color(0xFF15803D) else Color(0xFF22222E))
                                .border(0.6.dp, if (copiedDevId) Color(0xFF4ADE80) else Color(0xFF3F3F52), RoundedCornerShape(6.dp))
                                .clickable {
                                    LinkHandler.copyToClipboard(context, "Developer ID", Config.DEVELOPER_ID) {
                                        copiedDevId = true
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("sheet_copy_dev_id")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (copiedDevId) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = if (copiedDevId) Color.White else Color(0xFF9E9EA4),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (copiedDevId) "ID COPIED" else "COPY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. CONTACT
            Text(
                text = "CONTACT",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF8E8E98),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            ContactRow(
                title = "NO WA 1",
                subtitle = Config.WA_1_DISPLAY,
                buttonText = "CHAT WA",
                onClick = { LinkHandler.openWhatsApp(context, Config.WA_1) },
                iconTint = Color(0xFF25D366)
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContactRow(
                title = "NO WA 2",
                subtitle = Config.WA_2_DISPLAY,
                buttonText = "CHAT WA",
                onClick = { LinkHandler.openWhatsApp(context, Config.WA_2) },
                iconTint = Color(0xFF25D366)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. CHANNEL
            Text(
                text = "CHANNEL",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF8E8E98),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            ContactRow(
                title = Config.CHANNEL_NAME,
                subtitle = "Official WhatsApp Channel Updates",
                buttonText = "JOIN CHANNEL",
                onClick = { LinkHandler.openWhatsApp(context, Config.CHANNEL) },
                iconTint = Color(0xFF22C55E)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. SUPPORT
            Text(
                text = "SUPPORT",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF8E8E98),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            ContactRow(
                title = Config.SUPPORT_NAME,
                subtitle = "sociabuzz.com/zucchero_xann",
                buttonText = "SUPPORT",
                onClick = { LinkHandler.openUrl(context, Config.SUPPORT) },
                iconTint = Color(0xFFFF5252)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 5. PARTNER
            Text(
                text = "PARTNER",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(0xFF8E8E98),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Partner 1: LALZ
            PartnerRow(
                name = Config.PARTNER_1_NAME,
                partnerId = Config.PARTNER_1_ID,
                displayPhone = Config.PARTNER_1_DISPLAY,
                isCopied = copiedPartner1Id,
                onCopyId = {
                    LinkHandler.copyToClipboard(context, "Partner ID", Config.PARTNER_1_ID) {
                        copiedPartner1Id = true
                    }
                },
                onContactClick = { LinkHandler.openWhatsApp(context, Config.PARTNER_1_WA) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Partner 2: VAXXY
            PartnerRow(
                name = Config.PARTNER_2_NAME,
                partnerId = Config.PARTNER_2_ID,
                displayPhone = Config.PARTNER_2_DISPLAY,
                isCopied = copiedPartner2Id,
                onCopyId = {
                    LinkHandler.copyToClipboard(context, "Partner ID", Config.PARTNER_2_ID) {
                        copiedPartner2Id = true
                    }
                },
                onContactClick = { LinkHandler.openWhatsApp(context, Config.PARTNER_2_WA) }
            )
        }
    }
}

@Composable
private fun ContactRow(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF18181D))
            .border(0.6.dp, Color(0xFF2E2E38), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22222A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
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
                .background(Color(0xFF262632))
                .border(0.6.dp, Color(0xFF3F3F50), RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun PartnerRow(
    name: String,
    partnerId: String,
    displayPhone: String,
    isCopied: Boolean,
    onCopyId: () -> Unit,
    onContactClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF18181D))
            .border(0.6.dp, Color(0xFF2E2E38), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262633)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF101014))
                            .border(0.5.dp, Color(0xFF383848), RoundedCornerShape(6.dp))
                            .clickable(onClick = onCopyId)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isCopied) "ID COPIED" else partnerId,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCopied) Color(0xFF4ADE80) else Color(0xFF9E9EA4),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Text(
                    text = displayPhone,
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
                .background(Color(0xFF15803D))
                .clickable(onClick = onContactClick)
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Text(
                text = "CONTACT",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
