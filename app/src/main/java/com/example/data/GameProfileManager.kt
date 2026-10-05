package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class GameProfile(
    val packageName: String,
    val appName: String,
    val performanceMode: String = "PERFORMANCE", // PERFORMANCE, BALANCED, BATTERY_SAVER
    val autoLaunchHud: Boolean = true,
    val hudPosition: String = "TOP_RIGHT", // TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    val keepScreenOn: Boolean = true,
    val touchSensitivity: String = "HIGH", // NORMAL, HIGH, EXTREME
    val notes: String = ""
)

/**
 * Manages per-game profiles independently.
 * Settings for one game are strictly isolated by package name and will NEVER affect another game.
 */
class GameProfileManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("zx_game_profiles_store", Context.MODE_PRIVATE)

    private val _profilesFlow = MutableStateFlow<Map<String, GameProfile>>(loadAllProfiles())
    val profilesFlow: StateFlow<Map<String, GameProfile>> = _profilesFlow.asStateFlow()

    private fun loadAllProfiles(): Map<String, GameProfile> {
        val map = mutableMapOf<String, GameProfile>()
        val allEntries = prefs.all
        for ((key, value) in allEntries) {
            if (key.startsWith(PREFIX_KEY) && value is String) {
                try {
                    val profile = parseProfile(value)
                    if (profile != null) {
                        map[profile.packageName] = profile
                    }
                } catch (_: Exception) {}
            }
        }
        return map
    }

    fun getProfile(packageName: String, appName: String = ""): GameProfile {
        val existing = _profilesFlow.value[packageName]
        if (existing != null) return existing

        val savedRaw = prefs.getString(PREFIX_KEY + packageName, null)
        if (savedRaw != null) {
            val parsed = parseProfile(savedRaw)
            if (parsed != null) return parsed
        }

        // Return default profile for this game
        return GameProfile(
            packageName = packageName,
            appName = if (appName.isNotBlank()) appName else packageName
        )
    }

    fun saveProfile(profile: GameProfile) {
        val json = JSONObject().apply {
            put("packageName", profile.packageName)
            put("appName", profile.appName)
            put("performanceMode", profile.performanceMode)
            put("autoLaunchHud", profile.autoLaunchHud)
            put("hudPosition", profile.hudPosition)
            put("keepScreenOn", profile.keepScreenOn)
            put("touchSensitivity", profile.touchSensitivity)
            put("notes", profile.notes)
        }

        prefs.edit().putString(PREFIX_KEY + profile.packageName, json.toString()).apply()

        val updatedMap = _profilesFlow.value.toMutableMap()
        updatedMap[profile.packageName] = profile
        _profilesFlow.value = updatedMap
    }

    fun deleteProfile(packageName: String) {
        prefs.edit().remove(PREFIX_KEY + packageName).apply()
        val updatedMap = _profilesFlow.value.toMutableMap()
        updatedMap.remove(packageName)
        _profilesFlow.value = updatedMap
    }

    private fun parseProfile(jsonStr: String): GameProfile? {
        return try {
            val obj = JSONObject(jsonStr)
            GameProfile(
                packageName = obj.optString("packageName", ""),
                appName = obj.optString("appName", ""),
                performanceMode = obj.optString("performanceMode", "PERFORMANCE"),
                autoLaunchHud = obj.optBoolean("autoLaunchHud", true),
                hudPosition = obj.optString("hudPosition", "TOP_RIGHT"),
                keepScreenOn = obj.optBoolean("keepScreenOn", true),
                touchSensitivity = obj.optString("touchSensitivity", "HIGH"),
                notes = obj.optString("notes", "")
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val PREFIX_KEY = "pkg_profile_"
    }
}
