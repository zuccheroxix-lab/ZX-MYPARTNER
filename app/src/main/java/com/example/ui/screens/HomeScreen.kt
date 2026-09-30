package com.example.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.Config
import com.example.data.DeviceInfo
import com.example.ui.components.BannerCard
import com.example.ui.components.DashboardHeader
import com.example.ui.components.DeveloperProfileCard
import com.example.ui.components.DeveloperProfileSheet
import com.example.ui.components.InfoSystemCard
import com.example.ui.components.OfficialContactsCard
import com.example.ui.components.PartnersSectionCard
import com.example.ui.components.ShizukuStatusBanner
import com.example.ui.components.VersionCards

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    deviceInfo: DeviceInfo,
    onRefreshDeviceInfo: () -> Unit,
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

        // 2.5 FLOATING GAMING HUD OVERLAY LAUNCHER
        com.example.ui.components.GamingHudLauncherCard()

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
