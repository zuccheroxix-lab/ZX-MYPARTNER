package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.math.roundToInt

/**
 * Verification status for sensitivity settings.
 * Strict honest indicator: NEVER displays APPLY_VERIFIED unless actual system confirmation is established.
 */
enum class SensitivityApplyStatus(val label: String, val description: String) {
    CONFIG_SAVED(
        "CONFIG SAVED",
        "Konfigurasi berhasil disimpan di penyimpanan internal DYNIMETIZE ZX."
    ),
    PRESET_LOADED(
        "PRESET LOADED",
        "Preset berhasil dimuat ke editor sensitivitas aktif."
    ),
    MANUAL_CONFIGURATION(
        "MANUAL CONFIGURATION",
        "Preset tersimpan sebagai panduan. Silakan terapkan angka X/Y ke menu sensitivitas di dalam game."
    ),
    APPLY_VERIFIED(
        "APPLY VERIFIED",
        "Penerapan berhasil diverifikasi langsung melalui API kecepatan penunjuk (Pointer Speed) sistem Android."
    ),
    UNSUPPORTED(
        "UNSUPPORTED",
        "Modifikasi sensitivitas internal game secara otomatis dari aplikasi pihak ketiga tidak didukung oleh sandbox Android & anti-cheat."
    )
}

/**
 * Model preset sensitivitas X / Y untuk DYNIMETIZE ZX.
 * Rentang X & Y: 0.1x hingga 5.0x (default 1.00x).
 * Scope: 0-100 (Red Dot, 2x, 4x, Sniper).
 */
data class SensitivityPreset(
    val id: String,
    val name: String,
    val targetGamePackage: String = "ALL_GAMES",
    val targetGameName: String = "Universal (Semua Game)",
    val sensX: Float = 1.00f,
    val sensY: Float = 1.00f,
    val generalSens: Int = 100,
    val redDotSens: Int = 95,
    val scope2xSens: Int = 88,
    val scope4xSens: Int = 82,
    val sniperSens: Int = 68,
    val freeLookSens: Int = 75,
    val notes: String = "Preset seimbang kontrol recoil dan flick shot"
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("targetGamePackage", targetGamePackage)
            put("targetGameName", targetGameName)
            put("sensX", sensX.toDouble())
            put("sensY", sensY.toDouble())
            put("generalSens", generalSens)
            put("redDotSens", redDotSens)
            put("scope2xSens", scope2xSens)
            put("scope4xSens", scope4xSens)
            put("sniperSens", sniperSens)
            put("freeLookSens", freeLookSens)
            put("notes", notes)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): SensitivityPreset {
            val id = json.optString("id", "preset_${System.currentTimeMillis()}")
            val name = json.optString("name", "Custom Preset").trim().ifEmpty { "Custom Preset" }
            val targetGamePackage = json.optString("targetGamePackage", "ALL_GAMES")
            val targetGameName = json.optString("targetGameName", "Universal")
            
            val sensX = json.optDouble("sensX", 1.0).toFloat().coerceIn(0.1f, 5.0f)
            val sensY = json.optDouble("sensY", 1.0).toFloat().coerceIn(0.1f, 5.0f)
            val generalSens = json.optInt("generalSens", 100).coerceIn(0, 100)
            val redDotSens = json.optInt("redDotSens", 95).coerceIn(0, 100)
            val scope2xSens = json.optInt("scope2xSens", 88).coerceIn(0, 100)
            val scope4xSens = json.optInt("scope4xSens", 82).coerceIn(0, 100)
            val sniperSens = json.optInt("sniperSens", 68).coerceIn(0, 100)
            val freeLookSens = json.optInt("freeLookSens", 75).coerceIn(0, 100)
            val notes = json.optString("notes", "")

            return SensitivityPreset(
                id = id,
                name = name,
                targetGamePackage = targetGamePackage,
                targetGameName = targetGameName,
                sensX = (sensX * 100).roundToInt() / 100f,
                sensY = (sensY * 100).roundToInt() / 100f,
                generalSens = generalSens,
                redDotSens = redDotSens,
                scope2xSens = scope2xSens,
                scope4xSens = scope4xSens,
                sniperSens = sniperSens,
                freeLookSens = freeLookSens,
                notes = notes
            )
        }
    }
}

/**
 * Pengelola Sensitivitas X/Y untuk DYNIMETIZE ZX.
 * Mendukung penyimpanan multi-preset, per-game profile, export/import JSON ber-validasi ketat,
 * backup & pemulihan snapshot otomatis sebelum overwrite.
 */
class SensitivityManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _presets = MutableStateFlow<List<SensitivityPreset>>(emptyList())
    val presets: StateFlow<List<SensitivityPreset>> = _presets.asStateFlow()

    private val _activePreset = MutableStateFlow(getDefaultPreset())
    val activePreset: StateFlow<SensitivityPreset> = _activePreset.asStateFlow()

    private val _selectedGamePackage = MutableStateFlow("ALL_GAMES")
    val selectedGamePackage: StateFlow<String> = _selectedGamePackage.asStateFlow()

    private val _status = MutableStateFlow(SensitivityApplyStatus.MANUAL_CONFIGURATION)
    val status: StateFlow<SensitivityApplyStatus> = _status.asStateFlow()

    private val _lastBackupJson = MutableStateFlow<String?>(prefs.getString(KEY_LAST_BACKUP, null))
    val lastBackupJson: StateFlow<String?> = _lastBackupJson.asStateFlow()

    init {
        loadPresets()
    }

    private fun getDefaultPreset(): SensitivityPreset {
        return SensitivityPreset(
            id = "preset_default_universal",
            name = "Default Balanced (Universal)",
            targetGamePackage = "ALL_GAMES",
            targetGameName = "Universal (Semua Game)",
            sensX = 1.00f,
            sensY = 1.00f,
            generalSens = 98,
            redDotSens = 92,
            scope2xSens = 86,
            scope4xSens = 78,
            sniperSens = 65,
            freeLookSens = 70,
            notes = "Setelan dasar seimbang untuk pergerakan horizontal dan recoil vertikal"
        )
    }

    private fun getInitialBuiltInPresets(): List<SensitivityPreset> {
        return listOf(
            getDefaultPreset(),
            SensitivityPreset(
                id = "preset_ff_headshot_drag",
                name = "FF Drag Shoot (Pro Headshot)",
                targetGamePackage = "com.dts.freefireth",
                targetGameName = "Free Fire / MAX",
                sensX = 1.65f,
                sensY = 2.10f,
                generalSens = 100,
                redDotSens = 96,
                scope2xSens = 90,
                scope4xSens = 84,
                sniperSens = 70,
                freeLookSens = 80,
                notes = "Sumbu Y ditingkatkan 2.10x agar tarikan tombol tembak ke atas lebih licin dan responsif"
            ),
            SensitivityPreset(
                id = "preset_ff_gyro_fast",
                name = "FF Flick & Fast Reflex",
                targetGamePackage = "com.dts.freefireth",
                targetGameName = "Free Fire / MAX",
                sensX = 1.85f,
                sensY = 1.75f,
                generalSens = 100,
                redDotSens = 98,
                scope2xSens = 92,
                scope4xSens = 86,
                sniperSens = 75,
                freeLookSens = 85,
                notes = "Optimal untuk pertempuran jarak dekat (Close Combat / Shotgun flick)"
            ),
            SensitivityPreset(
                id = "preset_pubg_anti_recoil",
                name = "PUBG Vertical Anti-Recoil",
                targetGamePackage = "com.tencent.ig",
                targetGameName = "PUBG Mobile",
                sensX = 1.15f,
                sensY = 1.55f,
                generalSens = 90,
                redDotSens = 82,
                scope2xSens = 75,
                scope4xSens = 60,
                sniperSens = 45,
                freeLookSens = 70,
                notes = "Rasio Y lebih tinggi membantu menstabilkan semprotan M416 / Beryl"
            ),
            SensitivityPreset(
                id = "preset_codm_speed_accel",
                name = "CODM Aggressive SMG",
                targetGamePackage = "com.activision.callofduty.shooter",
                targetGameName = "Call of Duty: Mobile",
                sensX = 1.40f,
                sensY = 1.35f,
                generalSens = 95,
                redDotSens = 90,
                scope2xSens = 82,
                scope4xSens = 72,
                sniperSens = 55,
                freeLookSens = 75,
                notes = "Akselerasi rotasi geser cepat untuk sliding dan hipfire"
            ),
            SensitivityPreset(
                id = "preset_mlbb_panning",
                name = "MLBB Fast Camera Panning",
                targetGamePackage = "com.mobile.legends",
                targetGameName = "Mobile Legends: Bang Bang",
                sensX = 1.30f,
                sensY = 1.10f,
                generalSens = 90,
                redDotSens = 90,
                scope2xSens = 90,
                scope4xSens = 90,
                sniperSens = 90,
                freeLookSens = 90,
                notes = "Kecepatan geser kamera minimap horizontal lebih leluasa"
            )
        )
    }

    private fun loadPresets() {
        val jsonStr = prefs.getString(KEY_PRESETS_JSON, null)
        val list = mutableListOf<SensitivityPreset>()
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    list.add(SensitivityPreset.fromJsonObject(array.getJSONObject(i)))
                }
            } catch (_: Exception) {
                list.clear()
            }
        }
        if (list.isEmpty()) {
            list.addAll(getInitialBuiltInPresets())
            savePresetsInternal(list)
        }
        _presets.value = list

        val activeId = prefs.getString(KEY_ACTIVE_PRESET_ID, list.first().id)
        val found = list.find { it.id == activeId } ?: list.first()
        _activePreset.value = found
    }

    private fun savePresetsInternal(list: List<SensitivityPreset>) {
        val array = JSONArray()
        list.forEach { array.put(it.toJsonObject()) }
        prefs.edit().putString(KEY_PRESETS_JSON, array.toString()).apply()
        _presets.value = list
    }

    fun setSelectedGamePackage(pkg: String) {
        _selectedGamePackage.value = pkg
    }

    /**
     * Memperbarui preset aktif saat ini di memori dan persistensi.
     */
    fun updateActivePreset(updated: SensitivityPreset) {
        val currentList = _presets.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            currentList[index] = updated
        } else {
            currentList.add(updated)
        }
        _activePreset.value = updated
        savePresetsInternal(currentList)
        prefs.edit().putString(KEY_ACTIVE_PRESET_ID, updated.id).apply()
        _status.value = SensitivityApplyStatus.CONFIG_SAVED
    }

    /**
     * Memilih preset lain untuk menjadi preset aktif.
     */
    fun selectPreset(preset: SensitivityPreset) {
        _activePreset.value = preset
        prefs.edit().putString(KEY_ACTIVE_PRESET_ID, preset.id).apply()
        _status.value = SensitivityApplyStatus.PRESET_LOADED
    }

    /**
     * Menyimpan preset baru dengan nama kustom.
     */
    fun createNewPreset(
        name: String,
        targetPkg: String,
        targetName: String,
        sensX: Float,
        sensY: Float
    ): SensitivityPreset {
        val newId = "preset_${System.currentTimeMillis()}"
        val preset = SensitivityPreset(
            id = newId,
            name = name.trim().ifEmpty { "Preset #${_presets.value.size + 1}" },
            targetGamePackage = targetPkg,
            targetGameName = targetName,
            sensX = (sensX * 100).roundToInt() / 100f,
            sensY = (sensY * 100).roundToInt() / 100f
        )
        val currentList = _presets.value.toMutableList()
        currentList.add(preset)
        savePresetsInternal(currentList)
        selectPreset(preset)
        _status.value = SensitivityApplyStatus.CONFIG_SAVED
        return preset
    }

    /**
     * Menggandakan (duplicate) preset yang ada.
     */
    fun duplicatePreset(source: SensitivityPreset): SensitivityPreset {
        val copy = source.copy(
            id = "preset_${System.currentTimeMillis()}",
            name = "${source.name} (Copy)"
        )
        val currentList = _presets.value.toMutableList()
        currentList.add(copy)
        savePresetsInternal(currentList)
        selectPreset(copy)
        _status.value = SensitivityApplyStatus.CONFIG_SAVED
        return copy
    }

    /**
     * Mengganti nama preset.
     */
    fun renamePreset(presetId: String, newName: String) {
        val currentList = _presets.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == presetId }
        if (index >= 0) {
            val updated = currentList[index].copy(name = newName.trim().ifEmpty { currentList[index].name })
            currentList[index] = updated
            savePresetsInternal(currentList)
            if (_activePreset.value.id == presetId) {
                _activePreset.value = updated
            }
            _status.value = SensitivityApplyStatus.CONFIG_SAVED
        }
    }

    /**
     * Menghapus preset. Menyisakan minimal 1 preset.
     */
    fun deletePreset(presetId: String): Boolean {
        val currentList = _presets.value.toMutableList()
        if (currentList.size <= 1) return false
        val removed = currentList.removeAll { it.id == presetId }
        if (removed) {
            savePresetsInternal(currentList)
            if (_activePreset.value.id == presetId) {
                _activePreset.value = currentList.first()
                prefs.edit().putString(KEY_ACTIVE_PRESET_ID, currentList.first().id).apply()
            }
            _status.value = SensitivityApplyStatus.CONFIG_SAVED
            return true
        }
        return false
    }

    /**
     * Reset preset aktif ke nilai default (1.00x).
     */
    fun resetActivePresetToDefault() {
        val active = _activePreset.value
        val reset = active.copy(
            sensX = 1.00f,
            sensY = 1.00f,
            generalSens = 100,
            redDotSens = 95,
            scope2xSens = 88,
            scope4xSens = 82,
            sniperSens = 68,
            freeLookSens = 75
        )
        updateActivePreset(reset)
    }

    /**
     * Memeriksa dan menerapkan ke Pointer Speed OS jika diizinkan oleh sistem Android.
     * Android pointer speed berkisar dari -7 hingga +7 (default 0).
     * Jika izin WRITE_SECURE_SETTINGS tidak diberikan (Android sandbox), metode ini dengan jujur
     * mengembalikan status MANUAL_CONFIGURATION atau UNSUPPORTED tanpa klaim palsu.
     */
    fun attemptSystemPointerSpeedCalibration(sensX: Float): SensitivityApplyStatus {
        return try {
            // Skala 0.1x .. 5.0x dipetakan ke pointer_speed -7 .. +7
            val targetPointerSpeed = ((sensX - 1.0f) * 3.5f).roundToInt().coerceIn(-7, 7)
            val resolver = context.contentResolver
            
            // Coba tulis ke Settings.System.POINTER_SPEED
            val success = Settings.System.putInt(resolver, "pointer_speed", targetPointerSpeed)
            if (success) {
                // Verifikasi kembali dari sistem
                val current = Settings.System.getInt(resolver, "pointer_speed", 0)
                if (current == targetPointerSpeed) {
                    _status.value = SensitivityApplyStatus.APPLY_VERIFIED
                    SensitivityApplyStatus.APPLY_VERIFIED
                } else {
                    _status.value = SensitivityApplyStatus.MANUAL_CONFIGURATION
                    SensitivityApplyStatus.MANUAL_CONFIGURATION
                }
            } else {
                _status.value = SensitivityApplyStatus.MANUAL_CONFIGURATION
                SensitivityApplyStatus.MANUAL_CONFIGURATION
            }
        } catch (_: SecurityException) {
            // Wajar pada Android non-root tanpa izin WRITE_SECURE_SETTINGS via ADB/Shizuku
            _status.value = SensitivityApplyStatus.MANUAL_CONFIGURATION
            SensitivityApplyStatus.MANUAL_CONFIGURATION
        } catch (_: Exception) {
            _status.value = SensitivityApplyStatus.UNSUPPORTED
            SensitivityApplyStatus.UNSUPPORTED
        }
    }

    /**
     * Export seluruh konfigurasi preset ke String JSON terformat.
     */
    fun exportPresetsJson(): String {
        val root = JSONObject()
        root.put("app", "DYNIMETIZE ZX")
        root.put("version", "2.2.0")
        root.put("timestamp", System.currentTimeMillis())
        root.put("activePresetId", _activePreset.value.id)

        val array = JSONArray()
        _presets.value.forEach { array.put(it.toJsonObject()) }
        root.put("presets", array)

        return root.toString(2)
    }

    /**
     * Validasi dan Import string JSON.
     * Mengamankan data lama dengan snapshot backup otomatis sebelum ditimpa.
     * Tangani file rusak tanpa crash.
     */
    sealed class ImportResult {
        data class Success(val count: Int, val message: String) : ImportResult()
        data class Error(val reason: String) : ImportResult()
    }

    fun importPresetsJson(jsonString: String): ImportResult {
        if (jsonString.isBlank()) {
            return ImportResult.Error("Data JSON kosong atau tidak dapat dibaca.")
        }

        try {
            val root = JSONObject(jsonString)

            if (!root.has("presets")) {
                return ImportResult.Error("Format JSON tidak valid: kunci 'presets' tidak ditemukan.")
            }

            val array = root.getJSONArray("presets")
            if (array.length() == 0) {
                return ImportResult.Error("Daftar preset dalam file JSON kosong.")
            }

            val parsedList = mutableListOf<SensitivityPreset>()
            for (i in 0 until array.length()) {
                val itemObj = array.getJSONObject(i)
                parsedList.add(SensitivityPreset.fromJsonObject(itemObj))
            }

            if (parsedList.isEmpty()) {
                return ImportResult.Error("Tidak ada preset yang valid untuk diimpor.")
            }

            // Simpan snapshot backup konfigurasi lama sebelum menimpa
            val currentBackup = exportPresetsJson()
            prefs.edit().putString(KEY_LAST_BACKUP, currentBackup).apply()
            _lastBackupJson.value = currentBackup

            // Simpan konfigurasi baru
            savePresetsInternal(parsedList)
            val newActiveId = root.optString("activePresetId", parsedList.first().id)
            val newActive = parsedList.find { it.id == newActiveId } ?: parsedList.first()
            selectPreset(newActive)

            return ImportResult.Success(
                parsedList.size,
                "Berhasil mengimpor ${parsedList.size} preset DYNIMETIZE ZX. Backup konfigurasi sebelumnya telah disimpan."
            )
        } catch (e: Exception) {
            return ImportResult.Error("Gagal membaca file JSON: ${e.message ?: "Format sintaks tidak valid"}")
        }
    }

    /**
     * Memulihkan snapshot konfigurasi sebelumnya (Undo / Restore Backup).
     */
    fun restorePreviousBackup(): Boolean {
        val backup = _lastBackupJson.value ?: return false
        val result = importPresetsJson(backup)
        return result is ImportResult.Success
    }

    companion object {
        private const val PREFS_NAME = "zx_sensitivity_prefs"
        private const val KEY_PRESETS_JSON = "key_sensitivity_presets_json"
        private const val KEY_ACTIVE_PRESET_ID = "key_active_preset_id"
        private const val KEY_LAST_BACKUP = "key_last_backup_snapshot"
    }
}
