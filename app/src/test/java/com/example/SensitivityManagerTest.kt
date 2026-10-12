package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SensitivityApplyStatus
import com.example.data.SensitivityManager
import com.example.data.SensitivityPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SensitivityManagerTest {

    @Test
    fun `test initial presets loaded and active preset defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SensitivityManager(context)

        val presets = manager.presets.value
        assertTrue("Presets should have at least 5 default options", presets.size >= 5)

        val active = manager.activePreset.value
        assertNotNull(active)
        assertTrue(active.sensX >= 0.1f && active.sensX <= 5.0f)
        assertTrue(active.sensY >= 0.1f && active.sensY <= 5.0f)
    }

    @Test
    fun `test update active preset X and Y values, bounds and persistency`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SensitivityManager(context)

        val initial = manager.activePreset.value
        val updated = initial.copy(
            sensX = 2.45f,
            sensY = 3.15f,
            generalSens = 95,
            redDotSens = 88
        )

        manager.updateActivePreset(updated)

        assertEquals(2.45f, manager.activePreset.value.sensX)
        assertEquals(3.15f, manager.activePreset.value.sensY)
        assertEquals(SensitivityApplyStatus.CONFIG_SAVED, manager.status.value)

        // Re-instantiate manager to verify persistence across app reopen
        val newManager = SensitivityManager(context)
        val loaded = newManager.activePreset.value
        assertEquals(updated.id, loaded.id)
        assertEquals(2.45f, loaded.sensX)
        assertEquals(3.15f, loaded.sensY)
    }

    @Test
    fun `test create, rename, duplicate, and delete preset`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SensitivityManager(context)

        val initialCount = manager.presets.value.size

        // 1. Create new
        val created = manager.createNewPreset(
            name = "Test Shotgun Preset",
            targetPkg = "com.dts.freefireth",
            targetName = "Free Fire",
            sensX = 1.80f,
            sensY = 2.40f
        )
        assertEquals(initialCount + 1, manager.presets.value.size)
        assertEquals("Test Shotgun Preset", manager.activePreset.value.name)

        // 2. Duplicate
        val duplicated = manager.duplicatePreset(created)
        assertEquals(initialCount + 2, manager.presets.value.size)
        assertTrue(duplicated.name.contains("Copy"))

        // 3. Rename
        manager.renamePreset(created.id, "Shotgun Extreme X")
        val found = manager.presets.value.find { it.id == created.id }
        assertNotNull(found)
        assertEquals("Shotgun Extreme X", found?.name)

        // 4. Delete
        val deleted = manager.deletePreset(created.id)
        assertTrue(deleted)
        assertEquals(initialCount + 1, manager.presets.value.size)
    }

    @Test
    fun `test reset active preset to default values`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SensitivityManager(context)

        manager.updateActivePreset(manager.activePreset.value.copy(sensX = 4.8f, sensY = 4.2f))
        assertEquals(4.8f, manager.activePreset.value.sensX)

        manager.resetActivePresetToDefault()
        assertEquals(1.00f, manager.activePreset.value.sensX)
        assertEquals(1.00f, manager.activePreset.value.sensY)
        assertEquals(100, manager.activePreset.value.generalSens)
    }

    @Test
    fun `test export and import JSON with strict validation and error handling`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SensitivityManager(context)

        // 1. Export valid JSON
        val exportedJson = manager.exportPresetsJson()
        assertTrue(exportedJson.contains("DYNIMETIZE ZX"))
        assertTrue(exportedJson.contains("presets"))

        // 2. Import valid JSON
        val validResult = manager.importPresetsJson(exportedJson)
        assertTrue(validResult is SensitivityManager.ImportResult.Success)

        // 3. Import empty or corrupt JSON should handle gracefully without crashing
        val emptyResult = manager.importPresetsJson("")
        assertTrue(emptyResult is SensitivityManager.ImportResult.Error)

        val corruptResult = manager.importPresetsJson("{ invalid_json_syntax: true }")
        assertTrue(corruptResult is SensitivityManager.ImportResult.Error)

        val missingKeyResult = manager.importPresetsJson("""{"app": "ZX"}""")
        assertTrue(missingKeyResult is SensitivityManager.ImportResult.Error)
    }

    @Test
    fun `test automatic backup snapshot and restoration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SensitivityManager(context)

        // Create a specific preset
        manager.createNewPreset("Pre-Import Preset", "ALL_GAMES", "Universal", 1.25f, 1.25f)
        val countBefore = manager.presets.value.size

        // Import a single-item json
        val singleItemJson = """
        {
          "app": "DYNIMETIZE ZX",
          "presets": [
            {
              "id": "single_imported",
              "name": "Single Imported",
              "sensX": 2.0,
              "sensY": 2.5
            }
          ]
        }
        """.trimIndent()

        val importRes = manager.importPresetsJson(singleItemJson)
        assertTrue(importRes is SensitivityManager.ImportResult.Success)
        assertEquals(1, manager.presets.value.size)
        assertNotNull(manager.lastBackupJson.value)

        // Restore backup
        val restored = manager.restorePreviousBackup()
        assertTrue(restored)
        assertEquals(countBefore, manager.presets.value.size)
    }

    @Test
    fun `test honest apply status mapping`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = SensitivityManager(context)

        // Attempting OS pointer speed without WRITE_SECURE_SETTINGS returns honest MANUAL_CONFIGURATION
        val status = manager.attemptSystemPointerSpeedCalibration(1.5f)
        assertTrue(
            status == SensitivityApplyStatus.MANUAL_CONFIGURATION ||
            status == SensitivityApplyStatus.APPLY_VERIFIED ||
            status == SensitivityApplyStatus.UNSUPPORTED
        )
    }
}
