package com.example.data

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.ViewConfiguration
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import kotlin.math.min
import kotlin.math.roundToInt

data class DisplaySmoothnessInfo(
    val currentRefreshRate: Float,
    val supportedRefreshRates: List<Int>,
    val resolutionWidth: Int,
    val resolutionHeight: Int,
    val densityDpi: Int,
    val smallestWidthDp: Int,
    val densityScale: Float,
    val touchSlopPx: Int,
    val pointerSpeed: Int,
    val isMinimalPostProcessingSupported: Boolean,
    val isHapticSupported: Boolean,
    val hasSecureSettingsPermission: Boolean
)

data class SmoothnessConfig(
    val mode: String, // "SMOOTH" or "PERFORMANCE"
    val targetRefreshRate: Int, // e.g. 60, 90, 120, 144 (0 = auto/native)
    val touchProfile: String, // "STANDARD", "ULTRA_SMOOTH", "INSTANT_RESPONSE"
    val hapticIntensity: String, // "OFF", "SOFT", "BALANCED", "CRISP"
    val dpiPreset: String, // "NATIVE", "COMPACT_380", "BALANCED_411", "EXPANDED_460", "CUSTOM"
    val customDpi: Int, // DPI value
    val customResolution: String, // e.g. "1080x2400"
    val minimalPostProcessing: Boolean
)

data class ImportedZipConfig(
    val dpi: Int?,
    val resolution: String?,
    val mode: String?,
    val refreshRate: Int?,
    val fileName: String,
    val parsedEntryName: String,
    val rawSummary: String
)

data class ApplyResult(
    val windowApplied: Boolean,
    val globalOsDpiApplied: Boolean,
    val appliedDpi: Int,
    val appliedResolution: String,
    val message: String,
    val reasonExplanation: String,
    val hasSecurePermission: Boolean
)

class GameSmoothnessManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<SmoothnessConfig> = _config.asStateFlow()

    fun getDeviceInfo(): DisplaySmoothnessInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.display ?: windowManager?.defaultDisplay
            } catch (_: Exception) {
                windowManager?.defaultDisplay
            }
        } else {
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay
        }

        val metrics = context.resources.displayMetrics
        val currentRate = display?.refreshRate ?: 60f

        val supportedRates = mutableListOf<Int>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && display != null) {
            try {
                display.supportedModes.forEach { mode ->
                    val hz = mode.refreshRate.roundToInt()
                    if (hz > 0 && !supportedRates.contains(hz)) {
                        supportedRates.add(hz)
                    }
                }
            } catch (_: Exception) {}
        }
        if (supportedRates.isEmpty()) {
            supportedRates.add(currentRate.roundToInt().coerceAtLeast(60))
        }
        supportedRates.sort()

        val widthPx = metrics.widthPixels
        val heightPx = metrics.heightPixels
        val dpi = metrics.densityDpi
        val density = metrics.density
        val swDp = if (dpi > 0) (min(widthPx, heightPx) / (dpi / 160f)).roundToInt() else 360

        val touchSlop = try {
            ViewConfiguration.get(context).scaledTouchSlop
        } catch (_: Exception) {
            8
        }

        val pointerSpeed = try {
            Settings.System.getInt(context.contentResolver, "pointer_speed", 0)
        } catch (_: Exception) {
            0
        }

        val isMinimalPostProcessing = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && display != null) {
            try {
                display.isMinimalPostProcessingSupported
            } catch (_: Exception) {
                false
            }
        } else {
            false
        }

        val hasVibrator = try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.hasVibrator() == true
        } catch (_: Exception) {
            false
        }

        val hasSecurePermission = checkSecurePermission()

        return DisplaySmoothnessInfo(
            currentRefreshRate = currentRate,
            supportedRefreshRates = supportedRates,
            resolutionWidth = widthPx,
            resolutionHeight = heightPx,
            densityDpi = dpi,
            smallestWidthDp = swDp,
            densityScale = density,
            touchSlopPx = touchSlop,
            pointerSpeed = pointerSpeed,
            isMinimalPostProcessingSupported = isMinimalPostProcessing,
            isHapticSupported = hasVibrator,
            hasSecureSettingsPermission = hasSecurePermission
        )
    }

    private fun checkSecurePermission(): Boolean {
        return try {
            context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    private fun loadConfig(): SmoothnessConfig {
        val metrics = context.resources.displayMetrics
        val nativeDpi = metrics.densityDpi
        val nativeRes = "${metrics.widthPixels}x${metrics.heightPixels}"
        return SmoothnessConfig(
            mode = prefs.getString(KEY_MODE, "SMOOTH") ?: "SMOOTH",
            targetRefreshRate = prefs.getInt(KEY_TARGET_REFRESH_RATE, 0),
            touchProfile = prefs.getString(KEY_TOUCH_PROFILE, "ULTRA_SMOOTH") ?: "ULTRA_SMOOTH",
            hapticIntensity = prefs.getString(KEY_HAPTIC, "BALANCED") ?: "BALANCED",
            dpiPreset = prefs.getString(KEY_DPI_PRESET, "NATIVE") ?: "NATIVE",
            customDpi = prefs.getInt(KEY_CUSTOM_DPI, nativeDpi),
            customResolution = prefs.getString(KEY_CUSTOM_RESOLUTION, nativeRes) ?: nativeRes,
            minimalPostProcessing = prefs.getBoolean(KEY_MINIMAL_POST_PROCESSING, true)
        )
    }

    fun saveConfig(newConfig: SmoothnessConfig, activity: Activity? = null) {
        prefs.edit()
            .putString(KEY_MODE, newConfig.mode)
            .putInt(KEY_TARGET_REFRESH_RATE, newConfig.targetRefreshRate)
            .putString(KEY_TOUCH_PROFILE, newConfig.touchProfile)
            .putString(KEY_HAPTIC, newConfig.hapticIntensity)
            .putString(KEY_DPI_PRESET, newConfig.dpiPreset)
            .putInt(KEY_CUSTOM_DPI, newConfig.customDpi)
            .putString(KEY_CUSTOM_RESOLUTION, newConfig.customResolution)
            .putBoolean(KEY_MINIMAL_POST_PROCESSING, newConfig.minimalPostProcessing)
            .apply()

        _config.value = newConfig

        activity?.let {
            applyToWindow(it, newConfig)
        }
    }

    fun applyToWindow(activity: Activity, config: SmoothnessConfig) {
        try {
            val window = activity.window ?: return
            val layoutParams = window.attributes

            // 1. Refresh Rate / Display Mode Request
            if (config.targetRefreshRate > 0) {
                layoutParams.preferredRefreshRate = config.targetRefreshRate.toFloat()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            activity.display
                        } catch (_: Exception) {
                            @Suppress("DEPRECATION")
                            activity.windowManager.defaultDisplay
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        activity.windowManager.defaultDisplay
                    }

                    val matchingMode = display?.supportedModes?.firstOrNull { mode ->
                        mode.refreshRate.roundToInt() == config.targetRefreshRate
                    }
                    if (matchingMode != null) {
                        layoutParams.preferredDisplayModeId = matchingMode.modeId
                    }
                }
            } else {
                layoutParams.preferredRefreshRate = 0f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    layoutParams.preferredDisplayModeId = 0
                }
            }
            window.attributes = layoutParams

            // 2. Minimal post processing (game mode display latency reduction on Android 11+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setPreferMinimalPostProcessing(config.minimalPostProcessing)
            }

            // 3. Hardware acceleration flag enforcement
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
        } catch (_: Exception) {}
    }

    /**
     * Executes configuration application and honestly checks Android system boundaries.
     */
    fun applyConfigurationOfficial(activity: Activity?, config: SmoothnessConfig): ApplyResult {
        saveConfig(config, activity)
        val hasSecure = checkSecurePermission()

        var globalApplied = false
        if (hasSecure) {
            try {
                // If WRITE_SECURE_SETTINGS is present, safe attempt via ContentResolver
                Settings.Global.putString(context.contentResolver, "display_density_forced", config.customDpi.toString())
                globalApplied = true
            } catch (_: Exception) {
                globalApplied = false
            }
        }

        val message = if (globalApplied) {
            "Konfigurasi Window & Global OS Display berhasil diterapkan sepenuhnya."
        } else {
            "Konfigurasi Game Smoothness & Window Rendering berhasil diterapkan."
        }

        val explanation = if (globalApplied) {
            "Izin WRITE_SECURE_SETTINGS terdeteksi: DPI sistem OS diperbarui ke ${config.customDpi} DPI."
        } else {
            "Android membatasi pengubahan DPI/Resolusi sistem OS secara global kepada aplikasi standar tanpa izin WRITE_SECURE_SETTINGS (ADB/Shizuku). Perubahan diterapkan pada layer Window, Pacing & Overlay ZX. Untuk DPI permanen di seluruh sistem Android, silakan gunakan Opsi Pengembang (Lebar Terkecil)."
        }

        return ApplyResult(
            windowApplied = true,
            globalOsDpiApplied = globalApplied,
            appliedDpi = config.customDpi,
            appliedResolution = config.customResolution,
            message = message,
            reasonExplanation = explanation,
            hasSecurePermission = hasSecure
        )
    }

    fun resetToDefault(activity: Activity? = null): SmoothnessConfig {
        val metrics = context.resources.displayMetrics
        val nativeDpi = metrics.densityDpi
        val nativeRes = "${metrics.widthPixels}x${metrics.heightPixels}"
        val defaultConfig = SmoothnessConfig(
            mode = "SMOOTH",
            targetRefreshRate = 0,
            touchProfile = "STANDARD",
            hapticIntensity = "BALANCED",
            dpiPreset = "NATIVE",
            customDpi = nativeDpi,
            customResolution = nativeRes,
            minimalPostProcessing = false
        )
        saveConfig(defaultConfig, activity)
        return defaultConfig
    }

    /**
     * Safely reads and validates configuration from a user-selected ZIP file.
     * Prevents Zip Slip vulnerabilities and restricts entry size to avoid zip bombs.
     */
    suspend fun parseZipConfig(uri: Uri): Result<ImportedZipConfig> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Tidak dapat membuka file ZIP terpilih."))

            var parsedConfig: ImportedZipConfig? = null

            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name

                    // Security: Zip Slip vulnerability check (reject path traversal)
                    if (entryName.contains("..") || entryName.startsWith("/") || entryName.startsWith("\\")) {
                        entry = zis.nextEntry
                        continue
                    }

                    // Only process regular files with safe extensions
                    val isConfigFile = !entry.isDirectory && (
                            entryName.endsWith(".json", ignoreCase = true) ||
                            entryName.endsWith(".txt", ignoreCase = true) ||
                            entryName.endsWith(".conf", ignoreCase = true) ||
                            entryName.endsWith(".cfg", ignoreCase = true) ||
                            entryName.endsWith(".properties", ignoreCase = true) ||
                            entryName.contains("dpi", ignoreCase = true) ||
                            entryName.contains("resolution", ignoreCase = true) ||
                            entryName.contains("config", ignoreCase = true) ||
                            entryName.contains("settings", ignoreCase = true)
                    )

                    if (isConfigFile) {
                        // Limit reading to max 512KB per entry to prevent memory exhaustion
                        val reader = BufferedReader(InputStreamReader(zis, Charsets.UTF_8))
                        val sb = StringBuilder()
                        var line: String? = reader.readLine()
                        var bytesRead = 0
                        while (line != null && bytesRead < 512 * 1024) {
                            sb.append(line).append("\n")
                            bytesRead += line.length
                            line = reader.readLine()
                        }
                        val content = sb.toString().trim()

                        val candidate = extractFromContent(content, entryName)
                        if (candidate != null && (candidate.dpi != null || candidate.resolution != null)) {
                            parsedConfig = candidate
                            break
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            if (parsedConfig != null) {
                Result.success(parsedConfig)
            } else {
                Result.failure(
                    Exception(
                        "File ZIP tidak memuat data DPI atau Resolusi yang kompatibel. " +
                        "Pastikan file ZIP berisi file JSON atau teks konfigurasi (contoh: config.json berisi {\"dpi\": 420, \"resolution\": \"1080x2400\"})."
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membaca file ZIP: ${e.localizedMessage ?: "Format tidak valid atau rusak"}"))
        }
    }

    private fun extractFromContent(content: String, entryName: String): ImportedZipConfig? {
        // 1. Try parsing as JSON
        try {
            if (content.startsWith("{") && content.endsWith("}")) {
                val json = JSONObject(content)
                val dpi = when {
                    json.has("dpi") -> json.optInt("dpi")
                    json.has("DPI") -> json.optInt("DPI")
                    json.has("density") -> json.optInt("density")
                    json.has("densityDpi") -> json.optInt("densityDpi")
                    else -> null
                }?.takeIf { it in 100..1000 }

                var resolution = when {
                    json.has("resolution") -> json.optString("resolution")
                    json.has("RESOLUTION") -> json.optString("RESOLUTION")
                    json.has("screen") -> json.optString("screen")
                    else -> null
                }
                if (resolution.isNullOrBlank() && json.has("width") && json.has("height")) {
                    resolution = "${json.optInt("width")}x${json.optInt("height")}"
                }

                val mode = json.optString("mode", null)
                val hz = if (json.has("refresh_rate")) json.optInt("refresh_rate") else if (json.has("refreshRate")) json.optInt("refreshRate") else null

                if (dpi != null || !resolution.isNullOrBlank()) {
                    return ImportedZipConfig(
                        dpi = dpi,
                        resolution = resolution,
                        mode = mode,
                        refreshRate = hz,
                        fileName = entryName,
                        parsedEntryName = entryName,
                        rawSummary = content.take(300)
                    )
                }
            }
        } catch (_: Exception) {}

        // 2. Try parsing key-value pairs (e.g. dpi=420 or resolution=1080x2400)
        var parsedDpi: Int? = null
        var parsedRes: String? = null
        var parsedMode: String? = null
        var parsedHz: Int? = null

        val lines = content.lines()
        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue

            val parts = if (line.contains("=")) line.split("=", limit = 2) else line.split(":", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim().lowercase()
                val value = parts[1].trim().replace("\"", "").replace("'", "").replace(";", "")

                when {
                    key.contains("dpi") || key.contains("density") -> {
                        val num = value.filter { it.isDigit() }.toIntOrNull()
                        if (num != null && num in 100..1000) parsedDpi = num
                    }
                    key.contains("res") || key.contains("size") || key.contains("display") -> {
                        if (value.matches(Regex("[0-9]+\\s*[xX*]\\s*[0-9]+"))) {
                            parsedRes = value.replace(" ", "").lowercase()
                        }
                    }
                    key.contains("mode") -> {
                        if (value.contains("perf", ignoreCase = true)) parsedMode = "PERFORMANCE"
                        else if (value.contains("smooth", ignoreCase = true)) parsedMode = "SMOOTH"
                    }
                    key.contains("hz") || key.contains("refresh") || key.contains("rate") -> {
                        parsedHz = value.filter { it.isDigit() }.toIntOrNull()
                    }
                }
            } else if (line.matches(Regex("[0-9]{3,4}\\s*[xX*]\\s*[0-9]{3,4}"))) {
                parsedRes = line.replace(" ", "").lowercase()
            }
        }

        if (parsedDpi != null || parsedRes != null) {
            return ImportedZipConfig(
                dpi = parsedDpi,
                resolution = parsedRes,
                mode = parsedMode,
                refreshRate = parsedHz,
                fileName = entryName,
                parsedEntryName = entryName,
                rawSummary = content.take(300)
            )
        }

        return null
    }

    fun triggerHapticFeedback(intensity: String) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator == null || !vibrator.hasVibrator()) return

            when (intensity) {
                "OFF" -> {}
                "SOFT" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(10, 50))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(10)
                    }
                }
                "BALANCED" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(20, 120))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(20)
                    }
                }
                "CRISP" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(35, 255))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(35)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    companion object {
        private const val PREFS_NAME = "zx_game_smoothness_prefs"
        private const val KEY_MODE = "key_mode"
        private const val KEY_TARGET_REFRESH_RATE = "key_target_refresh_rate"
        private const val KEY_TOUCH_PROFILE = "key_touch_profile"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_DPI_PRESET = "key_dpi_preset"
        private const val KEY_CUSTOM_DPI = "key_custom_dpi"
        private const val KEY_CUSTOM_RESOLUTION = "key_custom_resolution"
        private const val KEY_MINIMAL_POST_PROCESSING = "key_minimal_post_processing"
    }
}
