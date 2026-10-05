package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.data.AuthManager
import com.example.data.DeviceInfo
import com.example.data.DeviceInfoProvider
import com.example.data.SettingsManager
import com.example.ui.navigation.BottomNavBar
import com.example.ui.navigation.NavScreen

enum class SubScreen {
    PERFORMANCE,
    GAMING_HUD,
    GAME_PROFILE,
    SYSTEM_STATUS,
    CLEAR_CACHE,
    GAME_SMOOTHNESS
}

@Composable
fun MainScreen(
    settingsManager: SettingsManager,
    authManager: AuthManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(NavScreen.HOME) }
    var activeSubScreen by remember { mutableStateOf<SubScreen?>(null) }

    val isDarkMode by settingsManager.isDarkMode.collectAsState()
    val animationsEnabled by settingsManager.animationsEnabled.collectAsState()
    val hapticsEnabled by settingsManager.hapticsEnabled.collectAsState()

    var deviceInfo by remember { mutableStateOf(DeviceInfoProvider.getDeviceInfo(context)) }

    fun refreshDeviceInfo() {
        deviceInfo = DeviceInfoProvider.getDeviceInfo(context)
    }

    // System Back button handling
    BackHandler(enabled = activeSubScreen != null) {
        activeSubScreen = null
    }

    BackHandler(enabled = activeSubScreen == null && currentScreen != NavScreen.HOME) {
        currentScreen = NavScreen.HOME
    }

    LaunchedEffect(Unit) {
        refreshDeviceInfo()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .statusBarsPadding()
            .testTag("main_screen_root")
    ) {
        if (activeSubScreen != null) {
            // Sub-screen destination
            when (activeSubScreen) {
                SubScreen.PERFORMANCE -> {
                    PerformanceScreen(
                        settingsManager = settingsManager,
                        onBack = { activeSubScreen = null }
                    )
                }
                SubScreen.GAMING_HUD -> {
                    GamingHudConfigScreen(
                        settingsManager = settingsManager,
                        onBack = { activeSubScreen = null }
                    )
                }
                SubScreen.GAME_PROFILE -> {
                    GameProfileScreen(
                        onBack = { activeSubScreen = null }
                    )
                }
                SubScreen.SYSTEM_STATUS -> {
                    SystemStatusScreen(
                        onBack = { activeSubScreen = null }
                    )
                }
                SubScreen.CLEAR_CACHE -> {
                    ClearCacheScreen(
                        onBack = { activeSubScreen = null }
                    )
                }
                SubScreen.GAME_SMOOTHNESS -> {
                    GameSmoothnessScreen(
                        onBack = { activeSubScreen = null }
                    )
                }
                null -> {}
            }
        } else {
            // Main Bottom Tab Screens with fluid transition
            if (animationsEnabled) {
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 200),
                    label = "ScreenTransition"
                ) { screen ->
                    ScreenRouter(
                        screen = screen,
                        deviceInfo = deviceInfo,
                        onRefreshDeviceInfo = { refreshDeviceInfo() },
                        settingsManager = settingsManager,
                        authManager = authManager,
                        isDarkMode = isDarkMode,
                        animationsEnabled = animationsEnabled,
                        hapticsEnabled = hapticsEnabled,
                        onOpenPerformance = { activeSubScreen = SubScreen.PERFORMANCE },
                        onOpenGamingHud = { activeSubScreen = SubScreen.GAMING_HUD },
                        onOpenGameProfile = { activeSubScreen = SubScreen.GAME_PROFILE },
                        onOpenSystemStatus = { activeSubScreen = SubScreen.SYSTEM_STATUS },
                        onOpenClearCache = { activeSubScreen = SubScreen.CLEAR_CACHE },
                        onOpenGameSmoothness = { activeSubScreen = SubScreen.GAME_SMOOTHNESS }
                    )
                }
            } else {
                ScreenRouter(
                    screen = currentScreen,
                    deviceInfo = deviceInfo,
                    onRefreshDeviceInfo = { refreshDeviceInfo() },
                    settingsManager = settingsManager,
                    authManager = authManager,
                    isDarkMode = isDarkMode,
                    animationsEnabled = animationsEnabled,
                    hapticsEnabled = hapticsEnabled,
                    onOpenPerformance = { activeSubScreen = SubScreen.PERFORMANCE },
                    onOpenGamingHud = { activeSubScreen = SubScreen.GAMING_HUD },
                    onOpenGameProfile = { activeSubScreen = SubScreen.GAME_PROFILE },
                    onOpenSystemStatus = { activeSubScreen = SubScreen.SYSTEM_STATUS },
                    onOpenClearCache = { activeSubScreen = SubScreen.CLEAR_CACHE },
                    onOpenGameSmoothness = { activeSubScreen = SubScreen.GAME_SMOOTHNESS }
                )
            }

            // Floating Bottom Navigation Bar (Home, Library, Settings, Plugin)
            BottomNavBar(
                currentScreen = currentScreen,
                onSelectScreen = { screen ->
                    currentScreen = screen
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ScreenRouter(
    screen: NavScreen,
    deviceInfo: DeviceInfo,
    onRefreshDeviceInfo: () -> Unit,
    settingsManager: SettingsManager,
    authManager: AuthManager?,
    isDarkMode: Boolean,
    animationsEnabled: Boolean,
    hapticsEnabled: Boolean,
    onOpenPerformance: () -> Unit,
    onOpenGamingHud: () -> Unit,
    onOpenGameProfile: () -> Unit,
    onOpenSystemStatus: () -> Unit,
    onOpenClearCache: () -> Unit,
    onOpenGameSmoothness: () -> Unit
) {
    when (screen) {
        NavScreen.HOME -> {
            HomeScreen(
                deviceInfo = deviceInfo,
                onRefreshDeviceInfo = onRefreshDeviceInfo,
                onOpenPerformance = onOpenPerformance,
                onOpenGamingHud = onOpenGamingHud,
                onOpenGameProfile = onOpenGameProfile,
                onOpenSystemStatus = onOpenSystemStatus,
                onOpenClearCache = onOpenClearCache,
                onOpenGameSmoothness = onOpenGameSmoothness
            )
        }
        NavScreen.GAME_LIBRARY -> {
            GameLibraryScreen()
        }
        NavScreen.SETTINGS -> {
            SettingsScreen(
                settingsManager = settingsManager,
                authManager = authManager,
                isDarkMode = isDarkMode,
                animationsEnabled = animationsEnabled,
                hapticsEnabled = hapticsEnabled,
                appVersion = deviceInfo.appVersion,
                onOpenPerformance = onOpenPerformance,
                onOpenGamingHud = onOpenGamingHud,
                onOpenGameProfile = onOpenGameProfile,
                onOpenSystemStatus = onOpenSystemStatus,
                onOpenClearCache = onOpenClearCache,
                onOpenGameSmoothness = onOpenGameSmoothness
            )
        }
        NavScreen.PLUGINS -> {
            PluginExtensionScreen()
        }
    }
}
