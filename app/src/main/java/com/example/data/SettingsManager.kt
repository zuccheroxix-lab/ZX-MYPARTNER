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
    }
}
