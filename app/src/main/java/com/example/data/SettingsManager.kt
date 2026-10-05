package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("zx_dashboard_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _animationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_ANIMATIONS, true))
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTICS, true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _customGamePackages = MutableStateFlow(
        prefs.getStringSet(KEY_CUSTOM_GAMES, emptySet())?.toSet() ?: emptySet()
    )
    val customGamePackages: StateFlow<Set<String>> = _customGamePackages.asStateFlow()

    // Performance Mode: "PERFORMANCE", "BALANCED", "BATTERY_SAVER"
    private val _performanceMode = MutableStateFlow(
        prefs.getString(KEY_PERFORMANCE_MODE, "BALANCED") ?: "BALANCED"
    )
    val performanceMode: StateFlow<String> = _performanceMode.asStateFlow()

    // HUD Configuration
    private val _hudPosition = MutableStateFlow(
        prefs.getString(KEY_HUD_POSITION, "TOP_RIGHT") ?: "TOP_RIGHT"
    )
    val hudPosition: StateFlow<String> = _hudPosition.asStateFlow()

    private val _isHudShown = MutableStateFlow(prefs.getBoolean(KEY_HUD_SHOWN, true))
    val isHudShown: StateFlow<Boolean> = _isHudShown.asStateFlow()

    private val _showPing = MutableStateFlow(prefs.getBoolean(KEY_SHOW_PING, true))
    val showPing: StateFlow<Boolean> = _showPing.asStateFlow()

    private val _showFps = MutableStateFlow(prefs.getBoolean(KEY_SHOW_FPS, true))
    val showFps: StateFlow<Boolean> = _showFps.asStateFlow()

    private val _showRam = MutableStateFlow(prefs.getBoolean(KEY_SHOW_RAM, true))
    val showRam: StateFlow<Boolean> = _showRam.asStateFlow()

    private val _showTemp = MutableStateFlow(prefs.getBoolean(KEY_SHOW_TEMP, true))
    val showTemp: StateFlow<Boolean> = _showTemp.asStateFlow()

    private val _showBattery = MutableStateFlow(prefs.getBoolean(KEY_SHOW_BATTERY, true))
    val showBattery: StateFlow<Boolean> = _showBattery.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _animationsEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _hapticsEnabled.value = enabled
    }

    fun setPerformanceMode(mode: String) {
        prefs.edit().putString(KEY_PERFORMANCE_MODE, mode).apply()
        _performanceMode.value = mode
    }

    fun setHudPosition(position: String) {
        prefs.edit().putString(KEY_HUD_POSITION, position).apply()
        _hudPosition.value = position
    }

    fun setHudShown(shown: Boolean) {
        prefs.edit().putBoolean(KEY_HUD_SHOWN, shown).apply()
        _isHudShown.value = shown
    }

    fun setShowPing(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_PING, show).apply()
        _showPing.value = show
    }

    fun setShowFps(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_FPS, show).apply()
        _showFps.value = show
    }

    fun setShowRam(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_RAM, show).apply()
        _showRam.value = show
    }

    fun setShowTemp(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_TEMP, show).apply()
        _showTemp.value = show
    }

    fun setShowBattery(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_BATTERY, show).apply()
        _showBattery.value = show
    }

    fun addCustomGamePackage(packageName: String) {
        val updated = _customGamePackages.value.toMutableSet().apply { add(packageName) }
        prefs.edit().putStringSet(KEY_CUSTOM_GAMES, updated).apply()
        _customGamePackages.value = updated
    }

    fun removeCustomGamePackage(packageName: String) {
        val updated = _customGamePackages.value.toMutableSet().apply { remove(packageName) }
        prefs.edit().putStringSet(KEY_CUSTOM_GAMES, updated).apply()
        _customGamePackages.value = updated
    }

    fun toggleCustomGamePackage(packageName: String) {
        if (_customGamePackages.value.contains(packageName)) {
            removeCustomGamePackage(packageName)
        } else {
            addCustomGamePackage(packageName)
        }
    }

    companion object {
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_ANIMATIONS = "pref_animations"
        private const val KEY_HAPTICS = "pref_haptics"
        private const val KEY_CUSTOM_GAMES = "pref_custom_games"
        private const val KEY_PERFORMANCE_MODE = "pref_performance_mode"
        private const val KEY_HUD_POSITION = "pref_hud_position"
        private const val KEY_HUD_SHOWN = "pref_hud_shown"
        private const val KEY_SHOW_PING = "pref_show_ping"
        private const val KEY_SHOW_FPS = "pref_show_fps"
        private const val KEY_SHOW_RAM = "pref_show_ram"
        private const val KEY_SHOW_TEMP = "pref_show_temp"
        private const val KEY_SHOW_BATTERY = "pref_show_battery"
    }
}
