package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Config
import com.example.data.DeviceInfo
import com.example.ui.components.BannerCard
import com.example.ui.components.DashboardHeader
import com.example.ui.components.DeveloperProfileCard
import com.example.ui.components.DeveloperProfileSheet
import com.example.ui.components.GamingHudLauncherCard
import com.example.ui.components.InfoSystemCard
import com.example.ui.components.OfficialContactsCard
import com.example.ui.components.PartnersSectionCard
import com.example.ui.components.ShizukuStatusBanner
import com.example.ui.components.VersionCards

private val GoldAccent = Color(0xFFFFB300)
private val DarkCardBg = Color(0xFF141722)
private val BorderColor = Color(0xFF262C3E)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    deviceInfo: DeviceInfo,
    onRefreshDeviceInfo: () -> Unit,
    onOpenPerformance: () -> Unit = {},
    onOpenGamingHud: () -> Unit = {},
    onOpenGameProfile: () -> Unit = {},
    onOpenSystemStatus: () -> Unit = {},
    onOpenClearCache: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showProfileSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .testTag("home_screen_content")
    ) {
        // 1. HEADER (Title: Dashboard, Subtitle: Dashboard Menu ZX, Dev: ZUCCHERO XANN)
        DashboardHeader(
            title = "Dashboard",
            subtitle = Config.APP_SUBTITLE,
            developerName = Config.DEVELOPER_NAME,
            onDevClick = { showProfileSheet = true }
        )

        // 2. BANNER
        BannerCard()

        // 2.2 NEW: ZX ENGINE 5 CORE MENUS SECTION
        ZxEngineMenusSection(
            onOpenPerformance = onOpenPerformance,
            onOpenGamingHud = onOpenGamingHud,
            onOpenGameProfile = onOpenGameProfile,
            onOpenSystemStatus = onOpenSystemStatus,
            onOpenClearCache = onOpenClearCache
        )

        // 2.5 FLOATING GAMING HUD OVERLAY LAUNCHER
        GamingHudLauncherCard()

        // 3. VERSION CARDS (Version APP & Developer APP)
        VersionCards(
            appVersion = deviceInfo.appVersion,
            developerName = Config.DEVELOPER_NAME,
            onDeveloperClick = { showProfileSheet = true }
        )

        // 4. INFORMATION SYSTEM
        InfoSystemCard(
            deviceInfo = deviceInfo,
            onRefresh = onRefreshDeviceInfo
        )

        // 5. DEVELOPER PROFILE SECTION (DYNIMETIZE ZX, ZUCCHERO XANN, ZX-DEV-001, [ COPY ])
        DeveloperProfileCard(
            onOpenFullProfile = { showProfileSheet = true }
        )

        // 6. OFFICIAL CONTACTS & CHANNELS (NO WA 1, NO WA 2, WHATSAPP CHANNEL, SOCIABUZZ)
        OfficialContactsCard()

        // 7. PARTNERS DIRECTORY (LALZ, VAXXY with WhatsApp CONTACT buttons & IDs)
        PartnersSectionCard()

        // 8. SHIZUKU STATUS WARNING BANNER
        ShizukuStatusBanner()

        // Bottom spacing so floating nav bar does not overlap content
        Spacer(modifier = Modifier.height(110.dp))
    }

    // Modal Bottom Sheet for Developer Profile & Contacts
    if (showProfileSheet) {
        DeveloperProfileSheet(
            onDismissRequest = { showProfileSheet = false },
            sheetState = sheetState
        )
    }
}

@Composable
private fun ZxEngineMenusSection(
    onOpenPerformance: () -> Unit,
    onOpenGamingHud: () -> Unit,
    onOpenGameProfile: () -> Unit,
    onOpenSystemStatus: () -> Unit,
    onOpenClearCache: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(
            text = "ZX ENGINE CONTROL CENTER",
            color = GoldAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: PERFORMANCE & GAMING HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EngineMenuTile(
                modifier = Modifier.weight(1f),
                title = "PERFORMANCE",
                subtitle = "Perf, Balanced, Saver",
                icon = Icons.Default.ElectricBolt,
                accentColor = Color(0xFFEF4444),
                onClick = onOpenPerformance
            )
            EngineMenuTile(
                modifier = Modifier.weight(1f),
                title = "GAMING HUD",
                subtitle = "FPS, RAM, Temp, Ping",
                icon = Icons.Default.SportsEsports,
                accentColor = GoldAccent,
                onClick = onOpenGamingHud
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: GAME PROFILE & SYSTEM STATUS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EngineMenuTile(
                modifier = Modifier.weight(1f),
                title = "GAME PROFILE",
                subtitle = "Per-game isolated settings",
                icon = Icons.Default.Tune,
                accentColor = Color(0xFF38BDF8),
                onClick = onOpenGameProfile
            )
            EngineMenuTile(
                modifier = Modifier.weight(1f),
                title = "SYSTEM STATUS",
                subtitle = "OS, RAM, Storage, Sensor",
                icon = Icons.Default.Info,
                accentColor = Color(0xFFA78BFA),
                onClick = onOpenSystemStatus
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: CLEAR CACHE (Full width)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkCardBg)
                .border(0.8.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .clickable { onOpenClearCache() }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x2210B981))
                            .border(1.dp, Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CLEAR CACHE",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Pembersihan aman & terukur tanpa hapus data/akun",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x2210B981))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SAFE",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineMenuTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardBg)
            .border(0.8.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 10.5.sp,
                maxLines = 1
            )
        }
    }
}
