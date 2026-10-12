package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SensitivityApplyStatus
import com.example.data.SensitivityManager
import com.example.data.SensitivityPreset
import com.example.ui.theme.BurgundyBright
import com.example.ui.theme.BurgundyDeep
import com.example.ui.theme.BurgundyGlow
import com.example.ui.theme.BurgundyPrimary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun XYSensitivitySection(
    sensitivityManager: SensitivityManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val presets by sensitivityManager.presets.collectAsState()
    val activePreset by sensitivityManager.activePreset.collectAsState()
    val selectedGamePackage by sensitivityManager.selectedGamePackage.collectAsState()
    val status by sensitivityManager.status.collectAsState()
    val lastBackupJson by sensitivityManager.lastBackupJson.collectAsState()

    // Dialog states
    var showNewPresetDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showManualGuideDialog by remember { mutableStateOf(false) }

    var newPresetName by remember { mutableStateOf("") }
    var renamePresetText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    // Game filters list
    val supportedGames = listOf(
        Pair("ALL_GAMES", "Universal"),
        Pair("com.dts.freefireth", "Free Fire / MAX"),
        Pair("com.tencent.ig", "PUBG Mobile"),
        Pair("com.activision.callofduty.shooter", "CODM"),
        Pair("com.mobile.legends", "Mobile Legends")
    )

    // Filtered presets by game
    val filteredPresets = if (selectedGamePackage == "ALL_GAMES") {
        presets
    } else {
        presets.filter { it.targetGamePackage == selectedGamePackage || it.targetGamePackage == "ALL_GAMES" }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("xy_sensitivity_section")
    ) {
        // 1. HEADER CARD WITH BURGUNDY ACCENT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E0A12), Color(0xFF13151D))
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(BurgundyGlow, BurgundyDeep, Color(0xFF222533))
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BurgundyDeep)
                                .border(1.dp, BurgundyGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = BurgundyGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "X/Y SENSITIVITY SYSTEM",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BurgundyPrimary)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = Color.White,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Pengaturan Rasio Sensitivitas Sumbu Horizontal & Vertikal",
                                color = Color(0xFF9E8E98),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Manual Guide Button
                    IconButton(
                        onClick = { showManualGuideDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E212B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Panduan",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. STATUS BANNER (HONEST, TRANSPARENT)
                StatusBanner(status = status)

                Spacer(modifier = Modifier.height(14.dp))

                // 3. TARGET GAME FILTER CHIPS
                Text(
                    text = "TARGET GAME:",
                    color = Color(0xFF9E8E98),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    supportedGames.forEach { (pkg, label) ->
                        val isSelected = selectedGamePackage == pkg
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BurgundyPrimary else Color(0xFF171A24))
                                .border(
                                    0.8.dp,
                                    if (isSelected) BurgundyGlow else Color(0xFF262A3B),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { sensitivityManager.setSelectedGamePackage(pkg) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF9E8E98),
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. PRESET SELECTOR & ACTIONS ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAFTAR PRESET (${filteredPresets.size}):",
                        color = Color(0xFF9E8E98),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Add preset button
                        IconButton(
                            onClick = {
                                newPresetName = "Preset #${presets.size + 1}"
                                showNewPresetDialog = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Preset", tint = BurgundyGlow, modifier = Modifier.size(16.dp))
                        }
                        // Duplicate active preset button
                        IconButton(
                            onClick = {
                                val dup = sensitivityManager.duplicatePreset(activePreset)
                                Toast.makeText(context, "Preset digandakan: ${dup.name}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Gandakan", tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                        }
                        // Rename active preset button
                        IconButton(
                            onClick = {
                                renamePresetText = activePreset.name
                                showRenameDialog = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Ganti Nama", tint = Color(0xFFFFB300), modifier = Modifier.size(15.dp))
                        }
                        // Delete active preset button (if > 1)
                        if (presets.size > 1) {
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Horizontal Preset Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredPresets.forEach { preset ->
                        val isCurrent = preset.id == activePreset.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCurrent) BurgundyDeep else Color(0xFF171A24))
                                .border(
                                    width = if (isCurrent) 1.2.dp else 0.8.dp,
                                    color = if (isCurrent) BurgundyGlow else Color(0xFF262A3B),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { sensitivityManager.selectPreset(preset) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCurrent) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = BurgundyGlow,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = preset.name,
                                        color = if (isCurrent) Color.White else Color(0xFF9E8E98),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "X: ${String.format("%.2f", preset.sensX)}x • Y: ${String.format("%.2f", preset.sensY)}x",
                                    color = if (isCurrent) BurgundyGlow else Color(0xFF6E7285),
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF252938), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 5. SLIDER KONTROL SENSITIVITAS X (HORIZONTAL)
                SensitivitySliderControl(
                    label = "SENSITIVITAS X (HORIZONTAL / PANNING)",
                    sublabel = "Pergerakan kamera kiri-kanan & rotasi 360°",
                    value = activePreset.sensX,
                    minValue = 0.10f,
                    maxValue = 5.00f,
                    step = 0.05f,
                    color = Color(0xFF38BDF8),
                    onValueChange = { newVal ->
                        val rounded = (newVal * 100).roundToInt() / 100f
                        sensitivityManager.updateActivePreset(activePreset.copy(sensX = rounded))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 6. SLIDER KONTROL SENSITIVITAS Y (VERTIKAL / RECOIL & DRAG)
                SensitivitySliderControl(
                    label = "SENSITIVITAS Y (VERTIKAL / DRAG & RECOIL)",
                    sublabel = "Tarikan tombol tembak ke atas (drag shot) & kontrol hentakan",
                    value = activePreset.sensY,
                    minValue = 0.10f,
                    maxValue = 5.00f,
                    step = 0.05f,
                    color = BurgundyGlow,
                    onValueChange = { newVal ->
                        val rounded = (newVal * 100).roundToInt() / 100f
                        sensitivityManager.updateActivePreset(activePreset.copy(sensY = rounded))
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Ratio Summary Box
                val ratio = if (activePreset.sensX > 0f) activePreset.sensY / activePreset.sensX else 1.0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF151824))
                        .border(0.6.dp, Color(0xFF262A3B), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rasio Y / X:",
                            color = Color(0xFF9E8E98),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${String.format("%.2f", ratio)}x ${if (ratio > 1.0f) "(Vertikal Lebih Cepat)" else if (ratio < 1.0f) "(Horizontal Lebih Cepat)" else "(Seimbang 1:1)"}",
                            color = if (ratio > 1.0f) BurgundyGlow else Color(0xFF38BDF8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF252938), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 7. IN-GAME SCOPES REFERENCE (0 - 100 SLIDERS)
                Text(
                    text = "PANDUAN NILAI SENSI IN-GAME (0 - 100):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Nilai yang dapat Anda masukkan ke menu pengaturan sensitivitas dalam game",
                    color = Color(0xFF8E8E98),
                    fontSize = 10.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                ScopeSensRow(
                    name = "Lihat Sekeliling (General)",
                    value = activePreset.generalSens,
                    onValueChange = { sensitivityManager.updateActivePreset(activePreset.copy(generalSens = it)) }
                )
                ScopeSensRow(
                    name = "Red Dot Sight",
                    value = activePreset.redDotSens,
                    onValueChange = { sensitivityManager.updateActivePreset(activePreset.copy(redDotSens = it)) }
                )
                ScopeSensRow(
                    name = "2x Scope",
                    value = activePreset.scope2xSens,
                    onValueChange = { sensitivityManager.updateActivePreset(activePreset.copy(scope2xSens = it)) }
                )
                ScopeSensRow(
                    name = "4x Scope",
                    value = activePreset.scope4xSens,
                    onValueChange = { sensitivityManager.updateActivePreset(activePreset.copy(scope4xSens = it)) }
                )
                ScopeSensRow(
                    name = "Sniper Scope",
                    value = activePreset.sniperSens,
                    onValueChange = { sensitivityManager.updateActivePreset(activePreset.copy(sniperSens = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 8. PRIMARY ACTION BUTTONS (RESET, SAVE, APPLY OS POINTER, BACKUP/RESTORE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            sensitivityManager.resetActivePresetToDefault()
                            Toast.makeText(context, "Preset direset ke 1.00x", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            sensitivityManager.updateActivePreset(activePreset)
                            Toast.makeText(context, "Preset '${activePreset.name}' berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            // Coba kalibrasi via Pointer Speed OS jika diizinkan
                            val res = sensitivityManager.attemptSystemPointerSpeedCalibration(activePreset.sensX)
                            if (res == SensitivityApplyStatus.APPLY_VERIFIED) {
                                Toast.makeText(context, "Pointer Speed Android berhasil disinkronkan & terverifikasi!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Disimpan sebagai preset manual. Buka game untuk mencocokkan angka sensitivitas.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BurgundyGlow,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kalibrasi OS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 9. EXPORT & IMPORT JSON / BACKUP ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export JSON", fontSize = 10.5.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            importJsonText = ""
                            importErrorMessage = null
                            showImportDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB300))
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import JSON", fontSize = 10.5.sp)
                    }

                    if (lastBackupJson != null) {
                        OutlinedButton(
                            onClick = {
                                val ok = sensitivityManager.restorePreviousBackup()
                                if (ok) {
                                    Toast.makeText(context, "Konfigurasi sebelumnya berhasil dipulihkan!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Gagal memulihkan backup", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pulihkan", fontSize = 10.5.sp)
                        }
                    }
                }
            }
        }
    }

    // DIALOG: BUAT PRESET BARU
    if (showNewPresetDialog) {
        AlertDialog(
            onDismissRequest = { showNewPresetDialog = false },
            title = {
                Text(
                    text = "Buat Preset Sensitivitas Baru",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan nama profil preset untuk game target Anda:",
                        color = Color(0xFF9E8E98),
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        label = { Text("Nama Preset") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BurgundyGlow,
                            unfocusedBorderColor = Color(0xFF2E3245),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val created = sensitivityManager.createNewPreset(
                            name = newPresetName,
                            targetPkg = selectedGamePackage,
                            targetName = if (selectedGamePackage == "ALL_GAMES") "Universal" else selectedGamePackage,
                            sensX = activePreset.sensX,
                            sensY = activePreset.sensY
                        )
                        showNewPresetDialog = false
                        Toast.makeText(context, "Preset '${created.name}' dibuat!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyGlow)
                ) {
                    Text("Buat", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPresetDialog = false }) {
                    Text("Batal", color = Color(0xFF9E8E98))
                }
            },
            containerColor = Color(0xFF13151D)
        )
    }

    // DIALOG: RENAME PRESET
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = {
                Text(
                    text = "Ganti Nama Preset",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = renamePresetText,
                    onValueChange = { renamePresetText = it },
                    label = { Text("Nama Baru") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BurgundyGlow,
                        unfocusedBorderColor = Color(0xFF2E3245),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        sensitivityManager.renamePreset(activePreset.id, renamePresetText)
                        showRenameDialog = false
                        Toast.makeText(context, "Nama preset diperbarui", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyGlow)
                ) {
                    Text("Simpan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Batal", color = Color(0xFF9E8E98))
                }
            },
            containerColor = Color(0xFF13151D)
        )
    }

    // DIALOG: HAPUS PRESET
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text("Hapus Preset?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus preset '${activePreset.name}'? Tindakan ini tidak dapat dibatalkan kecuali dari snapshot backup.",
                    color = Color(0xFF9E8E98),
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        sensitivityManager.deletePreset(activePreset.id)
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Preset dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal", color = Color(0xFF9E8E98))
                }
            },
            containerColor = Color(0xFF13151D)
        )
    }

    // DIALOG: EXPORT JSON
    if (showExportDialog) {
        val exportedJson = sensitivityManager.exportPresetsJson()
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text("Export Konfigurasi JSON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Salin teks konfigurasi di bawah ini untuk backup atau bagikan ke perangkat lain:",
                        color = Color(0xFF9E8E98),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090A0E))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = exportedJson,
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("DYNIMETIZE ZX Presets", exportedJson))
                        showExportDialog = false
                        Toast.makeText(context, "JSON disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyGlow)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin Clipboard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Tutup", color = Color(0xFF9E8E98))
                }
            },
            containerColor = Color(0xFF13151D)
        )
    }

    // DIALOG: IMPORT JSON (WITH STRICT VALIDATION)
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text("Import Konfigurasi JSON", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Tempel teks JSON konfigurasi preset DYNIMETIZE ZX di sini. Sistem akan memvalidasi rentang nilai dan format sebelum menyimpan.",
                        color = Color(0xFF9E8E98),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = {
                            importJsonText = it
                            importErrorMessage = null
                        },
                        placeholder = { Text("{\n  \"presets\": [...] \n}") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BurgundyGlow,
                            unfocusedBorderColor = Color(0xFF2E3245),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )

                    if (importErrorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Error: $importErrorMessage",
                            color = Color(0xFFEF4444),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = sensitivityManager.importPresetsJson(importJsonText)
                        when (res) {
                            is SensitivityManager.ImportResult.Success -> {
                                showImportDialog = false
                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            }
                            is SensitivityManager.ImportResult.Error -> {
                                importErrorMessage = res.reason
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyGlow)
                ) {
                    Text("Validasi & Impor", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Batal", color = Color(0xFF9E8E98))
                }
            },
            containerColor = Color(0xFF13151D)
        )
    }

    // DIALOG: PANDUAN MANUAL & TRANSPARANSI KEAMANAN
    if (showManualGuideDialog) {
        AlertDialog(
            onDismissRequest = { showManualGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transparansi Sensitivitas Android", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "1. Tidak Ada Injeksi Memori / Cheat:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "DYNIMETIZE ZX mematuhi kebijakan keamanan Android. Kami tidak melakukan modifikasi paket game, injeksi memori, atau hook anti-cheat sehingga akun Anda 100% aman.",
                        color = Color(0xFF9E8E98),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "2. Mengapa Perlu Panduan Manual?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Android sandbox melarang aplikasi pihak ketiga mengubah file internal game lain secara otomatis. Gunakan angka rekomendasi X/Y dan Scope dari DYNIMETIZE ZX ke menu sensitivitas Free Fire / PUBG / CODM Anda.",
                        color = Color(0xFF9E8E98),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "3. Apa Fungsi Kalibrasi OS?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Kalibrasi OS mengatur kecepatan kursor penunjuk layar (Pointer Speed) Android secara resmi untuk mempercepat respons sentuhan tanpa bypass keamanan.",
                        color = Color(0xFF9E8E98),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showManualGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary)
                ) {
                    Text("Saya Mengerti", color = Color.White)
                }
            },
            containerColor = Color(0xFF13151D)
        )
    }
}

/**
 * Banner status yang jujur dan transparan berdasarkan hasil verifikasi nyata.
 */
@Composable
private fun StatusBanner(status: SensitivityApplyStatus) {
    val (bgColor, borderColor, textColor, icon) = when (status) {
        SensitivityApplyStatus.APPLY_VERIFIED -> Quadruple(
            Color(0xEE092918),
            Color(0xFF10B981),
            Color(0xFF34D399),
            Icons.Default.Check
        )
        SensitivityApplyStatus.CONFIG_SAVED -> Quadruple(
            Color(0xEE1E0710),
            BurgundyGlow,
            Color(0xFFFB7185),
            Icons.Default.Save
        )
        SensitivityApplyStatus.PRESET_LOADED -> Quadruple(
            Color(0xEE081D29),
            Color(0xFF38BDF8),
            Color(0xFF7DD3FC),
            Icons.Default.TouchApp
        )
        SensitivityApplyStatus.MANUAL_CONFIGURATION -> Quadruple(
            Color(0xEE2A1D08),
            Color(0xFFFFB300),
            Color(0xFFFDE68A),
            Icons.Default.Info
        )
        SensitivityApplyStatus.UNSUPPORTED -> Quadruple(
            Color(0xEE2B0A0E),
            Color(0xFFEF4444),
            Color(0xFFFCA5A5),
            Icons.Default.Warning
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(0.8.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
            .testTag("sensitivity_status_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "STATUS: ${status.label}",
                    color = borderColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = status.description,
                    color = textColor,
                    fontSize = 9.5.sp
                )
            }
        }
    }
}

/**
 * Kontrol Slider X / Y yang presisi dengan tombol kurangi (-0.05) & tambah (+0.05).
 */
@Composable
private fun SensitivitySliderControl(
    label: String,
    sublabel: String,
    value: Float,
    minValue: Float,
    maxValue: Float,
    step: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.3.sp
                )
                Text(
                    text = sublabel,
                    color = Color(0xFF8E8E98),
                    fontSize = 10.sp
                )
            }

            // Accurate Numeric Display Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(0.8.dp, color, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${String.format("%.2f", value)}x",
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decrement Button
            IconButton(
                onClick = {
                    val next = (value - step).coerceIn(minValue, maxValue)
                    onValueChange(next)
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF171A24))
            ) {
                Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = minValue..maxValue,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = Color(0xFF222533)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // Increment Button
            IconButton(
                onClick = {
                    val next = (value + step).coerceIn(minValue, maxValue)
                    onValueChange(next)
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF171A24))
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${String.format("%.2f", minValue)}x Min", color = Color(0xFF6E7285), fontSize = 9.sp)
            Text("1.00x Default", color = Color(0xFF6E7285), fontSize = 9.sp)
            Text("${String.format("%.2f", maxValue)}x Max", color = Color(0xFF6E7285), fontSize = 9.sp)
        }
    }
}

/**
 * Baris pengaturan sensitivitas Scope (0 - 100).
 */
@Composable
private fun ScopeSensRow(
    name: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = Color(0xFFCFD3E2),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { onValueChange((value - 1).coerceIn(0, 100)) },
                modifier = Modifier.size(24.dp)
            ) {
                Text("-", color = Color(0xFF8E8E98), fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .width(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF171A24))
                    .border(0.6.dp, Color(0xFF262A3B), RoundedCornerShape(6.dp))
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = { onValueChange((value + 1).coerceIn(0, 100)) },
                modifier = Modifier.size(24.dp)
            ) {
                Text("+", color = Color(0xFF8E8E98), fontSize = 14.sp)
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
