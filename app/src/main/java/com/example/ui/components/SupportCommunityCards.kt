package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Config
import kotlinx.coroutines.delay

/**
 * Universal Link Handler for DYNIMETIZE ZX.
 * Supports WhatsApp direct package launch with browser fallback,
 * HTTPS URL opening, and clipboard management.
 */
object LinkHandler {
    fun openWhatsApp(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openUrl(context, url)
        }
    }

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open link: $url", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String, onCopied: (() -> Unit)? = null) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
            onCopied?.invoke()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

/** Legacy alias for existing call sites **/
fun launchExternalUrl(context: Context, url: String) {
    LinkHandler.openUrl(context, url)
}

/**
 * SECTION 6. STRUKTUR PROFILE: DEVELOPER IDENTITY CARD
 * Shows:
 * DYNIMETIZE ZX
 * ZUCCHERO XANN - DEVELOPER
 * ID: ZX-DEV-001 [ COPY ]
 */
@Composable
fun DeveloperProfileCard(
    modifier: Modifier = Modifier,
    onOpenFullProfile: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141416))
            .border(
                width = 0.8.dp,
                color = Color(0xFF282830),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(enabled = onOpenFullProfile != null) { onOpenFullProfile?.invoke() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("developer_profile_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: App Name & Developer Badge
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
                            .background(Color(0xFF1F1F26))
                            .border(1.dp, Color(0xFF383842), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = Config.APP_NAME,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "OFFICIAL PROFILE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF8E8E98),
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }

                // DEVELOPER Role Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E3A8A).copy(alpha = 0.4f))
                        .border(0.6.dp, Color(0xFF3B82F6), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Config.DEVELOPER_ROLE,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF93C5FD),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF222228), thickness = 0.6.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Developer Name Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LEAD DEVELOPER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF8E8E98),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Config.DEVELOPER_NAME,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 0.2.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ID Row with [ COPY ] button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F12))
                    .border(0.8.dp, Color(0xFF282830), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ID: ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF8E8E98),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = Config.DEVELOPER_ID,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF60A5FA),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }

                // Copy Action Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (copied) Color(0xFF14532D) else Color(0xFF1E293B))
                        .border(
                            width = 0.8.dp,
                            color = if (copied) Color(0xFF22C55E) else Color(0xFF475569),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            LinkHandler.copyToClipboard(context, "Developer ID", Config.DEVELOPER_ID) {
                                copied = true
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("btn_copy_dev_id")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = if (copied) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (copied) "ID COPIED" else "COPY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (copied) Color(0xFF4ADE80) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.4.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * SECTION 3 & 6: OFFICIAL CONTACTS & CHANNEL & SUPPORT
 * Shows:
 * CONTACT: NO WA 1, NO WA 2
 * CHANNEL: WHATSAPP CHANNEL
 * SUPPORT: SOCIABUZZ
 */
@Composable
fun OfficialContactsCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141416))
            .border(0.8.dp, Color(0xFF282830), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("official_contacts_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OFFICIAL CONTACT & CHANNEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-section: CONTACT
            Text(
                text = "CONTACT",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF8E8E98),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            // NO WA 1
            ContactItemRow(
                title = "NO WA 1",
                subtitle = Config.WA_1_DISPLAY,
                buttonText = "CHAT WA",
                iconTint = Color(0xFF25D366),
                onClick = { LinkHandler.openWhatsApp(context, Config.WA_1) },
                testTag = "btn_wa_1"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // NO WA 2
            ContactItemRow(
                title = "NO WA 2",
                subtitle = Config.WA_2_DISPLAY,
                buttonText = "CHAT WA",
                iconTint = Color(0xFF25D366),
                onClick = { LinkHandler.openWhatsApp(context, Config.WA_2) },
                testTag = "btn_wa_2"
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF222228), thickness = 0.6.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Sub-section: CHANNEL
            Text(
                text = "CHANNEL",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF8E8E98),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            ContactItemRow(
                title = Config.CHANNEL_NAME,
                subtitle = "Official Announcements & Releases",
                buttonText = "JOIN CHANNEL",
                iconTint = Color(0xFF22C55E),
                onClick = { LinkHandler.openWhatsApp(context, Config.CHANNEL) },
                testTag = "btn_wa_channel"
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF222228), thickness = 0.6.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Sub-section: SUPPORT
            Text(
                text = "SUPPORT",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF8E8E98),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            ContactItemRow(
                title = Config.SUPPORT_NAME,
                subtitle = "Support ZUCCHERO XANN on Sociabuzz",
                buttonText = "DONATE",
                iconTint = Color(0xFFFF5252),
                icon = Icons.Default.Favorite,
                onClick = { LinkHandler.openUrl(context, Config.SUPPORT) },
                testTag = "btn_support_sociabuzz"
            )
        }
    }
}

/**
 * SECTION 4 & 6: PARTNER SECTION
 * Shows:
 * PARTNER
 * LALZ [CONTACT] (ID: ZX-PARTNER-001)
 * VAXXY [CONTACT] (ID: ZX-PARTNER-002)
 */
@Composable
fun PartnersSectionCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copiedPartnerId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(copiedPartnerId) {
        if (copiedPartnerId != null) {
            delay(2000)
            copiedPartnerId = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141416))
            .border(0.8.dp, Color(0xFF282830), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("partners_section_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PARTNER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Partner 1: LALZ
            PartnerItemRow(
                name = Config.PARTNER_1_NAME,
                partnerId = Config.PARTNER_1_ID,
                phoneDisplay = Config.PARTNER_1_DISPLAY,
                isCopied = copiedPartnerId == Config.PARTNER_1_ID,
                onCopyId = {
                    LinkHandler.copyToClipboard(context, "Partner ID", Config.PARTNER_1_ID) {
                        copiedPartnerId = Config.PARTNER_1_ID
                    }
                },
                onContactClick = {
                    LinkHandler.openWhatsApp(context, Config.PARTNER_1_WA)
                },
                testTag = "btn_partner_1"
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF222228), thickness = 0.6.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Partner 2: VAXXY
            PartnerItemRow(
                name = Config.PARTNER_2_NAME,
                partnerId = Config.PARTNER_2_ID,
                phoneDisplay = Config.PARTNER_2_DISPLAY,
                isCopied = copiedPartnerId == Config.PARTNER_2_ID,
                onCopyId = {
                    LinkHandler.copyToClipboard(context, "Partner ID", Config.PARTNER_2_ID) {
                        copiedPartnerId = Config.PARTNER_2_ID
                    }
                },
                onContactClick = {
                    LinkHandler.openWhatsApp(context, Config.PARTNER_2_WA)
                },
                testTag = "btn_partner_2"
            )
        }
    }
}

@Composable
private fun ContactItemRow(
    title: String,
    subtitle: String,
    buttonText: String,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String,
    icon: ImageVector = Icons.AutoMirrored.Filled.Chat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1E))
            .border(0.6.dp, Color(0xFF2A2A32), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22222A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
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
                        color = Color(0xFF9E9EA4),
                        fontSize = 11.sp
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF262630))
                .border(0.6.dp, Color(0xFF3E3E4C), RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag(testTag)
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
private fun PartnerItemRow(
    name: String,
    partnerId: String,
    phoneDisplay: String,
    isCopied: Boolean,
    onCopyId: () -> Unit,
    onContactClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1E))
            .border(0.6.dp, Color(0xFF2A2A32), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF252530))
                    .border(0.8.dp, Color(0xFF3F3F50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = name,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF121216))
                            .border(0.5.dp, Color(0xFF33333F), RoundedCornerShape(6.dp))
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = phoneDisplay,
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
                .testTag(testTag)
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

/** Backward compatibility wrappers for previous composables **/
@Composable
fun SupportCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    OfficialContactsCard(modifier = modifier)
}

@Composable
fun CommunityCard(modifier: Modifier = Modifier) {
    PartnersSectionCard(modifier = modifier)
}

@Composable
fun CommunityPublicCard(modifier: Modifier = Modifier) {
    // Retained for layout compatibility; content handled in OfficialContactsCard
}
