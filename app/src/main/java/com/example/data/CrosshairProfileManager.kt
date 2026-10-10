package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class CrosshairProfile(
    val id: String,
    val name: String,
    val targetGame: String = "",
    val style: String = "CLASSIC_CROSS",
    val size: Int = 28,
    val color: Long = 0xFFFF2A2AL,
    val opacity: Float = 0.9f,
    val thickness: Float = 2.5f,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val autoCenter: Boolean = true
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("targetGame", targetGame)
            put("style", style)
            put("size", size)
            put("color", color)
            put("opacity", opacity.toDouble())
            put("thickness", thickness.toDouble())
            put("offsetX", offsetX)
            put("offsetY", offsetY)
            put("autoCenter", autoCenter)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): CrosshairProfile {
            return CrosshairProfile(
                id = json.optString("id", "default"),
                name = json.optString("name", "Default"),
                targetGame = json.optString("targetGame", ""),
                style = json.optString("style", "CLASSIC_CROSS"),
                size = json.optInt("size", 28),
                color = json.optLong("color", 0xFFFF2A2AL),
                opacity = json.optDouble("opacity", 0.9).toFloat(),
                thickness = json.optDouble("thickness", 2.5).toFloat(),
                offsetX = json.optInt("offsetX", 0),
                offsetY = json.optInt("offsetY", 0),
                autoCenter = json.optBoolean("autoCenter", true)
            )
        }
    }
}

class CrosshairProfileManager(
    context: Context,
    private val settingsManager: SettingsManager
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _profiles = MutableStateFlow<List<CrosshairProfile>>(emptyList())
    val profiles: StateFlow<List<CrosshairProfile>> = _profiles.asStateFlow()

    private val _activeProfileId = MutableStateFlow(
        prefs.getString(KEY_ACTIVE_PROFILE_ID, "default") ?: "default"
    )
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    init {
        loadProfiles()
    }

    private fun getDefaultProfiles(): List<CrosshairProfile> {
        return listOf(
            CrosshairProfile(
                id = "default",
                name = "Universal (Umum)",
                targetGame = "Semua Game",
                style = "CLASSIC_CROSS",
                size = 28,
                color = 0xFFFF2A2AL, // Neon Red
                opacity = 0.90f,
                thickness = 2.5f,
                offsetX = 0,
                offsetY = 0,
                autoCenter = true
            ),
            CrosshairProfile(
                id = "free_fire",
                name = "Free Fire MAX",
                targetGame = "com.dts.freefiremax",
                style = "CIRCLE_DOT",
                size = 24,
                color = 0xFF00E5FFL, // Cyan ZX
                opacity = 0.95f,
                thickness = 2.2f,
                offsetX = 0,
                offsetY = 0,
                autoCenter = true
            ),
            CrosshairProfile(
                id = "pubg",
                name = "PUBG Mobile",
                targetGame = "com.tencent.ig",
                style = "DOT",
                size = 18,
                color = 0xFF10B981L, // Neon Green
                opacity = 1.0f,
                thickness = 3.0f,
                offsetX = 0,
                offsetY = 0,
                autoCenter = true
            ),
            CrosshairProfile(
                id = "mlbb",
                name = "Mobile Legends",
                targetGame = "com.mobile.legends",
                style = "T_SHAPE",
                size = 32,
                color = 0xFFFFB300L, // Cyber Gold
                opacity = 0.85f,
                thickness = 2.5f,
                offsetX = 0,
                offsetY = 0,
                autoCenter = true
            ),
            CrosshairProfile(
                id = "codm",
                name = "Call of Duty: Mobile",
                targetGame = "com.activision.callofduty.shooter",
                style = "SNIPER",
                size = 32,
                color = 0xFFE040FBL, // Vivid Magenta
                opacity = 0.90f,
                thickness = 2.8f,
                offsetX = 0,
                offsetY = 0,
                autoCenter = true
            )
        )
    }

    private fun loadProfiles() {
        val jsonStr = prefs.getString(KEY_PROFILES_JSON, null)
        if (jsonStr.isNullOrBlank()) {
            val defaults = getDefaultProfiles()
            _profiles.value = defaults
            saveProfilesToPrefs(defaults)
        } else {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<CrosshairProfile>()
                for (i in 0 until array.length()) {
                    list.add(CrosshairProfile.fromJsonObject(array.getJSONObject(i)))
                }
                _profiles.value = if (list.isNotEmpty()) list else getDefaultProfiles()
            } catch (_: Exception) {
                _profiles.value = getDefaultProfiles()
            }
        }
    }

    private fun saveProfilesToPrefs(list: List<CrosshairProfile>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJsonObject()) }
        prefs.edit().putString(KEY_PROFILES_JSON, array.toString()).apply()
    }

    fun selectProfile(profileId: String) {
        val profile = _profiles.value.find { it.id == profileId } ?: return
        _activeProfileId.value = profileId
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, profileId).apply()

        // Apply profile values into SettingsManager immediately
        settingsManager.setCrosshairStyle(profile.style)
        settingsManager.setCrosshairSize(profile.size)
        settingsManager.setCrosshairColor(profile.color)
        settingsManager.setCrosshairOpacity(profile.opacity)
        settingsManager.setCrosshairThickness(profile.thickness)
        settingsManager.setCrosshairAutoCenter(profile.autoCenter)
        settingsManager.setCrosshairOffset(profile.offsetX, profile.offsetY)
    }

    fun saveCurrentAsProfile(profileName: String, gamePackage: String = "") {
        val currentId = _activeProfileId.value
        val currentList = _profiles.value.toMutableList()

        val updatedProfile = CrosshairProfile(
            id = if (currentId.isNotBlank()) currentId else "custom_${System.currentTimeMillis()}",
            name = profileName.ifBlank { "Custom Profile" },
            targetGame = gamePackage,
            style = settingsManager.crosshairStyle.value,
            size = settingsManager.crosshairSize.value,
            color = settingsManager.crosshairColor.value,
            opacity = settingsManager.crosshairOpacity.value,
            thickness = settingsManager.crosshairThickness.value,
            offsetX = settingsManager.crosshairOffsetX.value,
            offsetY = settingsManager.crosshairOffsetY.value,
            autoCenter = settingsManager.crosshairAutoCenter.value
        )

        val existingIndex = currentList.indexOfFirst { it.id == updatedProfile.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = updatedProfile
        } else {
            currentList.add(updatedProfile)
        }

        _profiles.value = currentList
        saveProfilesToPrefs(currentList)
        _activeProfileId.value = updatedProfile.id
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, updatedProfile.id).apply()
    }

    fun createNewProfile(name: String, gamePackage: String = "") {
        val newId = "profile_${System.currentTimeMillis()}"
        val newProfile = CrosshairProfile(
            id = newId,
            name = name.ifBlank { "Profil Baru" },
            targetGame = gamePackage,
            style = settingsManager.crosshairStyle.value,
            size = settingsManager.crosshairSize.value,
            color = settingsManager.crosshairColor.value,
            opacity = settingsManager.crosshairOpacity.value,
            thickness = settingsManager.crosshairThickness.value,
            offsetX = settingsManager.crosshairOffsetX.value,
            offsetY = settingsManager.crosshairOffsetY.value,
            autoCenter = settingsManager.crosshairAutoCenter.value
        )
        val currentList = _profiles.value.toMutableList()
        currentList.add(newProfile)
        _profiles.value = currentList
        saveProfilesToPrefs(currentList)
        _activeProfileId.value = newId
        prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, newId).apply()
    }

    fun resetToDefaults() {
        val defaults = getDefaultProfiles()
        _profiles.value = defaults
        saveProfilesToPrefs(defaults)
        selectProfile("default")
    }

    companion object {
        private const val PREF_NAME = "zx_crosshair_profiles"
        private const val KEY_PROFILES_JSON = "profiles_json"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
    }
}
