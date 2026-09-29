package com.example.ui.screens

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
import com.example.data.DeviceInfo
import com.example.data.DeviceInfoProvider
import com.example.data.SettingsManager
import com.example.ui.navigation.BottomNavBar
import com.example.ui.navigation.NavScreen

@Composable
fun MainScreen(
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(NavScreen.HOME) }

    val isDarkMode by settingsManager.isDarkMode.collectAsState()
    val animationsEnabled by settingsManager.animationsEnabled.collectAsState()
    val hapticsEnabled by settingsManager.hapticsEnabled.collectAsState()

    var deviceInfo by remember { mutableStateOf(DeviceInfoProvider.getDeviceInfo(context)) }

    fun refreshDeviceInfo() {
        deviceInfo = DeviceInfoProvider.getDeviceInfo(context)
    }

    androidx.activity.compose.BackHandler(enabled = currentScreen != NavScreen.HOME) {
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
        // Screen Content with fluid Crossfade
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
                    isDarkMode = isDarkMode,
                    animationsEnabled = animationsEnabled,
                    hapticsEnabled = hapticsEnabled
                )
            }
        } else {
            ScreenRouter(
                screen = currentScreen,
                deviceInfo = deviceInfo,
                onRefreshDeviceInfo = { refreshDeviceInfo() },
                settingsManager = settingsManager,
                isDarkMode = isDarkMode,
                animationsEnabled = animationsEnabled,
                hapticsEnabled = hapticsEnabled
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

@Composable
private fun ScreenRouter(
    screen: NavScreen,
    deviceInfo: DeviceInfo,
    onRefreshDeviceInfo: () -> Unit,
    settingsManager: SettingsManager,
    isDarkMode: Boolean,
    animationsEnabled: Boolean,
    hapticsEnabled: Boolean
) {
    when (screen) {
        NavScreen.HOME -> {
            HomeScreen(
                deviceInfo = deviceInfo,
                onRefreshDeviceInfo = onRefreshDeviceInfo
            )
        }
        NavScreen.GAME_LIBRARY -> {
            GameLibraryScreen()
        }
        NavScreen.SETTINGS -> {
            SettingsScreen(
                settingsManager = settingsManager,
                isDarkMode = isDarkMode,
                animationsEnabled = animationsEnabled,
                hapticsEnabled = hapticsEnabled,
                appVersion = deviceInfo.appVersion
            )
        }
        NavScreen.PLUGINS -> {
            PluginExtensionScreen()
        }
    }
}
