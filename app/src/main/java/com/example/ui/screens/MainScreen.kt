package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.AuthManager
import com.example.data.DeviceInfoProvider
import com.example.data.SettingsManager
import com.example.ui.navigation.BottomNavBar
import com.example.ui.navigation.NavScreen
import kotlinx.coroutines.launch

/**
 * MainScreen with strictly 4 primary navigation pages:
 * 0: HOME
 * 1: GAME
 * 2: TOOLS
 * 3: SETTINGS
 *
 * Fully supports smooth horizontal swiping across all 4 pages,
 * clear in-page navigation buttons, and reliable BackHandler.
 */
@Composable
fun MainScreen(
    settingsManager: SettingsManager,
    authManager: AuthManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 4 Primary Pages
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })

    val isDarkMode by settingsManager.isDarkMode.collectAsState()
    val animationsEnabled by settingsManager.animationsEnabled.collectAsState()
    val hapticsEnabled by settingsManager.hapticsEnabled.collectAsState()

    var deviceInfo by remember { mutableStateOf(DeviceInfoProvider.getDeviceInfo(context)) }

    fun refreshDeviceInfo() {
        deviceInfo = DeviceInfoProvider.getDeviceInfo(context)
    }

    // System Back button handling: returns smoothly to Home (0) from any page without trapping
    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    LaunchedEffect(Unit) {
        refreshDeviceInfo()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0D))
            .statusBarsPadding()
            .testTag("main_screen_root")
    ) {
        // Horizontal Pager: allows seamless, fluid swipe across ALL 4 primary pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> {
                    HomeScreen(
                        deviceInfo = deviceInfo,
                        onRefreshDeviceInfo = { refreshDeviceInfo() },
                        onNavigateToPage = { targetPage ->
                            scope.launch { pagerState.animateScrollToPage(targetPage) }
                        }
                    )
                }
                1 -> {
                    GameScreen(
                        onNavigateToPage = { targetPage ->
                            scope.launch { pagerState.animateScrollToPage(targetPage) }
                        }
                    )
                }
                2 -> {
                    ToolsScreen(
                        onNavigateToPage = { targetPage ->
                            scope.launch { pagerState.animateScrollToPage(targetPage) }
                        }
                    )
                }
                3 -> {
                    SettingsScreen(
                        settingsManager = settingsManager,
                        authManager = authManager,
                        isDarkMode = isDarkMode,
                        animationsEnabled = animationsEnabled,
                        hapticsEnabled = hapticsEnabled,
                        appVersion = deviceInfo.appVersion,
                        onNavigateToPage = { targetPage ->
                            scope.launch { pagerState.animateScrollToPage(targetPage) }
                        }
                    )
                }
            }
        }

        // Floating Bottom Navigation Bar (4 items: HOME, GAME, TOOLS, SETTINGS)
        BottomNavBar(
            currentScreen = NavScreen.fromIndex(pagerState.currentPage),
            onSelectScreen = { screen ->
                scope.launch { pagerState.animateScrollToPage(screen.pageIndex) }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
