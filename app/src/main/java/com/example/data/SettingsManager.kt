package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("zx_dashboard_prefs", Context.MODE_PRIVATE)

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

    // Tampilan: Custom UI Accent Color
    private val _accentColor = MutableStateFlow(prefs.getLong(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR))
    val accentColor: StateFlow<Long> = _accentColor.asStateFlow()

    // Custom DNS
    private val _customDns = MutableStateFlow(
        prefs.getString(KEY_CUSTOM_DNS, "1dot1dot1dot1.cloudflare-dns.com") ?: "1dot1dot1dot1.cloudflare-dns.com"
    )
    val customDns: StateFlow<String> = _customDns.asStateFlow()

    // Resolution & DPI
    private val _customResolution = MutableStateFlow(
        prefs.getString(KEY_CUSTOM_RESOLUTION, "1080x2400") ?: "1080x2400"
    )
    val customResolution: StateFlow<String> = _customResolution.asStateFlow()

    private val _customDpi = MutableStateFlow(prefs.getInt(KEY_CUSTOM_DPI, 411))
    val customDpi: StateFlow<Int> = _customDpi.asStateFlow()

    // Crosshair Overlay Configuration
    private val _crosshairEnabled = MutableStateFlow(prefs.getBoolean(KEY_CROSSHAIR_ENABLED, false))
    val crosshairEnabled: StateFlow<Boolean> = _crosshairEnabled.asStateFlow()

    private val _crosshairStyle = MutableStateFlow(
        prefs.getString(KEY_CROSSHAIR_STYLE, "CLASSIC_CROSS") ?: "CLASSIC_CROSS"
    )
    val crosshairStyle: StateFlow<String> = _crosshairStyle.asStateFlow()

    private val _crosshairSize = MutableStateFlow(prefs.getInt(KEY_CROSSHAIR_SIZE, 26))
    val crosshairSize: StateFlow<Int> = _crosshairSize.asStateFlow()

    private val _crosshairColor = MutableStateFlow(prefs.getLong(KEY_CROSSHAIR_COLOR, 0xFFFF2A2AL))
    val crosshairColor: StateFlow<Long> = _crosshairColor.asStateFlow()

    private val _crosshairOpacity = MutableStateFlow(prefs.getFloat(KEY_CROSSHAIR_OPACITY, 0.9f))
    val crosshairOpacity: StateFlow<Float> = _crosshairOpacity.asStateFlow()

    private val _crosshairOffsetX = MutableStateFlow(prefs.getInt(KEY_CROSSHAIR_OFFSET_X, 0))
    val crosshairOffsetX: StateFlow<Int> = _crosshairOffsetX.asStateFlow()

    private val _crosshairOffsetY = MutableStateFlow(prefs.getInt(KEY_CROSSHAIR_OFFSET_Y, 0))
    val crosshairOffsetY: StateFlow<Int> = _crosshairOffsetY.asStateFlow()

    private val _crosshairThickness = MutableStateFlow(prefs.getFloat(KEY_CROSSHAIR_THICKNESS, 2.5f))
    val crosshairThickness: StateFlow<Float> = _crosshairThickness.asStateFlow()

    private val _crosshairAutoCenter = MutableStateFlow(prefs.getBoolean(KEY_CROSSHAIR_AUTO_CENTER, true))
    val crosshairAutoCenter: StateFlow<Boolean> = _crosshairAutoCenter.asStateFlow()

    private val _crosshairDragMode = MutableStateFlow(prefs.getBoolean(KEY_CROSSHAIR_DRAG_MODE, false))
    val crosshairDragMode: StateFlow<Boolean> = _crosshairDragMode.asStateFlow()

    // Sensitivity GameLab Profile
    private val _sensitivityProfile = MutableStateFlow(
        prefs.getString(KEY_SENSITIVITY_PROFILE, "ESPORTS_PRO") ?: "ESPORTS_PRO"
    )
    val sensitivityProfile: StateFlow<String> = _sensitivityProfile.asStateFlow()

    private val _calibrationScore = MutableStateFlow(prefs.getInt(KEY_CALIBRATION_SCORE, 96))
    val calibrationScore: StateFlow<Int> = _calibrationScore.asStateFlow()

    // Touch & Swipe Optimizations
    private val _touchOptimization = MutableStateFlow(prefs.getBoolean(KEY_TOUCH_OPTIMIZATION, true))
    val touchOptimization: StateFlow<Boolean> = _touchOptimization.asStateFlow()

    private val _swipeOptimization = MutableStateFlow(prefs.getBoolean(KEY_SWIPE_OPTIMIZATION, true))
    val swipeOptimization: StateFlow<Boolean> = _swipeOptimization.asStateFlow()

    // Render Mode Selection
    private val _renderMode = MutableStateFlow(
        prefs.getString(KEY_RENDER_MODE, "AUTO") ?: "AUTO"
    )
    val renderMode: StateFlow<String> = _renderMode.asStateFlow()

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_DARK_MODE -> _isDarkMode.value = prefs.getBoolean(KEY_DARK_MODE, true)
            KEY_ANIMATIONS -> _animationsEnabled.value = prefs.getBoolean(KEY_ANIMATIONS, true)
            KEY_HAPTICS -> _hapticsEnabled.value = prefs.getBoolean(KEY_HAPTICS, true)
            KEY_PERFORMANCE_MODE -> _performanceMode.value = prefs.getString(KEY_PERFORMANCE_MODE, "BALANCED") ?: "BALANCED"
            KEY_ACCENT_COLOR -> _accentColor.value = prefs.getLong(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR)
            KEY_CROSSHAIR_ENABLED -> _crosshairEnabled.value = prefs.getBoolean(KEY_CROSSHAIR_ENABLED, false)
            KEY_CROSSHAIR_STYLE -> _crosshairStyle.value = prefs.getString(KEY_CROSSHAIR_STYLE, "CLASSIC_CROSS") ?: "CLASSIC_CROSS"
            KEY_CROSSHAIR_SIZE -> _crosshairSize.value = prefs.getInt(KEY_CROSSHAIR_SIZE, 28)
            KEY_CROSSHAIR_COLOR -> _crosshairColor.value = prefs.getLong(KEY_CROSSHAIR_COLOR, 0xFFFF2A2AL)
            KEY_CROSSHAIR_OPACITY -> _crosshairOpacity.value = prefs.getFloat(KEY_CROSSHAIR_OPACITY, 0.9f)
            KEY_CROSSHAIR_OFFSET_X -> _crosshairOffsetX.value = prefs.getInt(KEY_CROSSHAIR_OFFSET_X, 0)
            KEY_CROSSHAIR_OFFSET_Y -> _crosshairOffsetY.value = prefs.getInt(KEY_CROSSHAIR_OFFSET_Y, 0)
            KEY_CROSSHAIR_THICKNESS -> _crosshairThickness.value = prefs.getFloat(KEY_CROSSHAIR_THICKNESS, 2.5f)
            KEY_CROSSHAIR_AUTO_CENTER -> _crosshairAutoCenter.value = prefs.getBoolean(KEY_CROSSHAIR_AUTO_CENTER, true)
            KEY_CROSSHAIR_DRAG_MODE -> _crosshairDragMode.value = prefs.getBoolean(KEY_CROSSHAIR_DRAG_MODE, false)
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    // Mutator Methods
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

    fun setAccentColor(color: Long) {
        prefs.edit().putLong(KEY_ACCENT_COLOR, color).apply()
        _accentColor.value = color
    }

    fun setCustomDns(dns: String) {
        prefs.edit().putString(KEY_CUSTOM_DNS, dns).apply()
        _customDns.value = dns
    }

    fun setCustomResolution(resolution: String) {
        prefs.edit().putString(KEY_CUSTOM_RESOLUTION, resolution).apply()
        _customResolution.value = resolution
    }

    fun setCustomDpi(dpi: Int) {
        prefs.edit().putInt(KEY_CUSTOM_DPI, dpi).apply()
        _customDpi.value = dpi
    }

    fun setCrosshairEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CROSSHAIR_ENABLED, enabled).apply()
        _crosshairEnabled.value = enabled
    }

    fun setCrosshairStyle(style: String) {
        prefs.edit().putString(KEY_CROSSHAIR_STYLE, style).apply()
        _crosshairStyle.value = style
    }

    fun setCrosshairSize(size: Int) {
        prefs.edit().putInt(KEY_CROSSHAIR_SIZE, size).apply()
        _crosshairSize.value = size
    }

    fun setCrosshairColor(color: Long) {
        prefs.edit().putLong(KEY_CROSSHAIR_COLOR, color).apply()
        _crosshairColor.value = color
    }

    fun setCrosshairOpacity(opacity: Float) {
        prefs.edit().putFloat(KEY_CROSSHAIR_OPACITY, opacity).apply()
        _crosshairOpacity.value = opacity
    }

    fun setCrosshairOffset(x: Int, y: Int) {
        prefs.edit().putInt(KEY_CROSSHAIR_OFFSET_X, x).putInt(KEY_CROSSHAIR_OFFSET_Y, y).apply()
        _crosshairOffsetX.value = x
        _crosshairOffsetY.value = y
    }

    fun setCrosshairThickness(thickness: Float) {
        prefs.edit().putFloat(KEY_CROSSHAIR_THICKNESS, thickness).apply()
        _crosshairThickness.value = thickness
    }

    fun setCrosshairAutoCenter(autoCenter: Boolean) {
        prefs.edit().putBoolean(KEY_CROSSHAIR_AUTO_CENTER, autoCenter).apply()
        _crosshairAutoCenter.value = autoCenter
        if (autoCenter) {
            setCrosshairOffset(0, 0)
        }
    }

    fun setCrosshairDragMode(dragMode: Boolean) {
        prefs.edit().putBoolean(KEY_CROSSHAIR_DRAG_MODE, dragMode).apply()
        _crosshairDragMode.value = dragMode
    }

    fun resetCrosshairPosition() {
        setCrosshairAutoCenter(true)
        setCrosshairOffset(0, 0)
        setCrosshairDragMode(false)
    }

    fun resetCrosshairStyle() {
        setCrosshairStyle("CLASSIC_CROSS")
        setCrosshairSize(28)
        setCrosshairColor(0xFFFF2A2AL)
        setCrosshairOpacity(0.9f)
        setCrosshairThickness(2.5f)
    }

    fun resetCrosshair() {
        resetCrosshairStyle()
        resetCrosshairPosition()
    }

    fun setSensitivityProfile(profile: String) {
        prefs.edit().putString(KEY_SENSITIVITY_PROFILE, profile).apply()
        _sensitivityProfile.value = profile
    }

    fun setCalibrationScore(score: Int) {
        prefs.edit().putInt(KEY_CALIBRATION_SCORE, score).apply()
        _calibrationScore.value = score
    }

    fun setTouchOptimization(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TOUCH_OPTIMIZATION, enabled).apply()
        _touchOptimization.value = enabled
    }

    fun setSwipeOptimization(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SWIPE_OPTIMIZATION, enabled).apply()
        _swipeOptimization.value = enabled
    }

    fun setRenderMode(mode: String) {
        prefs.edit().putString(KEY_RENDER_MODE, mode).apply()
        _renderMode.value = mode
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
        @Volatile
        private var instance: SettingsManager? = null

        operator fun invoke(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context.applicationContext).also { instance = it }
            }
        }

        fun getInstance(context: Context): SettingsManager = invoke(context)

        const val DEFAULT_ACCENT_COLOR = 0xFF00D2FFL

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

        private const val KEY_ACCENT_COLOR = "pref_accent_color"
        private const val KEY_CUSTOM_DNS = "pref_custom_dns"
        private const val KEY_CUSTOM_RESOLUTION = "pref_custom_resolution"
        private const val KEY_CUSTOM_DPI = "pref_custom_dpi"
        private const val KEY_CROSSHAIR_ENABLED = "pref_crosshair_enabled"
        private const val KEY_CROSSHAIR_STYLE = "pref_crosshair_style"
        private const val KEY_CROSSHAIR_SIZE = "pref_crosshair_size"
        private const val KEY_CROSSHAIR_COLOR = "pref_crosshair_color"
        private const val KEY_CROSSHAIR_OPACITY = "pref_crosshair_opacity"
        private const val KEY_CROSSHAIR_OFFSET_X = "pref_crosshair_offset_x"
        private const val KEY_CROSSHAIR_OFFSET_Y = "pref_crosshair_offset_y"
        private const val KEY_CROSSHAIR_THICKNESS = "pref_crosshair_thickness"
        private const val KEY_CROSSHAIR_AUTO_CENTER = "pref_crosshair_auto_center"
        private const val KEY_CROSSHAIR_DRAG_MODE = "pref_crosshair_drag_mode"
        private const val KEY_SENSITIVITY_PROFILE = "pref_sensitivity_profile"
        private const val KEY_CALIBRATION_SCORE = "pref_calibration_score"
        private const val KEY_TOUCH_OPTIMIZATION = "pref_touch_optimization"
        private const val KEY_SWIPE_OPTIMIZATION = "pref_swipe_optimization"
        private const val KEY_RENDER_MODE = "pref_render_mode"
    }
}
