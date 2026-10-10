package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CrosshairProfileManager
import com.example.data.SettingsManager
import com.example.service.CrosshairCanvas
import com.example.service.CrosshairService
import kotlin.math.roundToInt

private val ColorPresets = listOf(
    Pair("Merah", 0xFFFF2A2AL),
    Pair("Hijau", 0xFF10B981L),
    Pair("Cyan", 0xFF00E5FFL),
    Pair("Emas", 0xFFFFB300L),
    Pair("Putih", 0xFFFFFFFFL),
    Pair("Magenta", 0xFFE040FBL),
    Pair("Oranye", 0xFFFF6D00L),
    Pair("Kuning", 0xFFAEEA00L),
    Pair("Biru", 0xFF2979FFL),
    Pair("Ungu", 0xFF7C4DFFL)
)

private val StylePresets = listOf(
    "CLASSIC_CROSS" to "Classic",
    "DOT" to "Dot",
    "CIRCLE_DOT" to "Circle Dot",
    "T_SHAPE" to "T-Shape",
    "SNIPER" to "Sniper",
    "CHEVRON" to "Chevron",
    "BOX_DOT" to "Box Dot",
    "DIAMOND" to "Diamond"
)

private fun getRealScreenSize(context: Context): Pair<Int, Int> {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = wm.currentWindowMetrics.bounds
        Pair(bounds.width(), bounds.height())
    } else {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        Pair(metrics.widthPixels, metrics.heightPixels)
    }
}

@Composable
fun CrosshairConfigSection(
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileManager = remember { CrosshairProfileManager(context, settingsManager) }

    val isRunning by CrosshairService.isServiceRunning.collectAsState()
    val crosshairStyle by settingsManager.crosshairStyle.collectAsState()
    val crosshairSize by settingsManager.crosshairSize.collectAsState()
    val crosshairColorLong by settingsManager.crosshairColor.collectAsState()
    val crosshairOpacity by settingsManager.crosshairOpacity.collectAsState()
    val crosshairThickness by settingsManager.crosshairThickness.collectAsState()
    val crosshairOffsetX by settingsManager.crosshairOffsetX.collectAsState()
    val crosshairOffsetY by settingsManager.crosshairOffsetY.collectAsState()
    val crosshairAutoCenter by settingsManager.crosshairAutoCenter.collectAsState()
    val crosshairDragMode by settingsManager.crosshairDragMode.collectAsState()

    val profiles by profileManager.profiles.collectAsState()
    val activeProfileId by profileManager.activeProfileId.collectAsState()

    val hasOverlayPermission = Settings.canDrawOverlays(context)
    val screenSize = remember { getRealScreenSize(context) }
    val screenWidth = screenSize.first
    val screenHeight = screenSize.second

    // Dialog state for new game profile
    var showNewProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newProfileGame by remember { mutableStateOf("") }

    // Dialog state for custom free color picker
    var showCustomColorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("crosshair_config_section")
    ) {
        // ==========================================
        // 0. MASTER SHOW / HIDE CROSSHAIR & PERMISSION
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) Color(0xFF10B981).copy(alpha = 0.18f) else Color(0xFFFF2A2A).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = null,
                        tint = if (isRunning) Color(0xFF10B981) else Color(0xFFFF2A2A),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Crosshair Aim Overlay",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                    Text(
                        text = if (isRunning) "Overlay Aktif di Layar (Real Service)" else "Overlay Tersembunyi (Nonaktif)",
                        color = if (isRunning) Color(0xFF10B981) else Color(0xFF8E8E98),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Switch(
                checked = isRunning,
                onCheckedChange = { enable ->
                    if (enable) {
                        if (!Settings.canDrawOverlays(context)) {
                            Toast.makeText(context, "Izin Overlay Diperlukan", Toast.LENGTH_SHORT).show()
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } else {
                            settingsManager.setCrosshairEnabled(true)
                            CrosshairService.start(context)
                            Toast.makeText(context, "Crosshair overlay aktif di layar", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        settingsManager.setCrosshairEnabled(false)
                        CrosshairService.stop(context)
                        Toast.makeText(context, "Crosshair overlay disembunyikan", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF10B981)
                ),
                modifier = Modifier.testTag("switch_crosshair_master")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DEDICATED SHOW / HIDE CROSSHAIR BUTTON (Explicit Requirement)
        Button(
            onClick = {
                if (!isRunning) {
                    if (!Settings.canDrawOverlays(context)) {
                        Toast.makeText(context, "Izin Overlay Diperlukan", Toast.LENGTH_SHORT).show()
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } else {
                        settingsManager.setCrosshairEnabled(true)
                        CrosshairService.start(context)
                        Toast.makeText(context, "Crosshair overlay aktif di layar", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    settingsManager.setCrosshairEnabled(false)
                    CrosshairService.stop(context)
                    Toast.makeText(context, "Crosshair overlay disembunyikan", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(if (isRunning) "btn_hide_crosshair" else "btn_show_crosshair"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Color(0xFFEF4444).copy(alpha = 0.85f) else Color(0xFF10B981),
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRunning) "SEMBUNYIKAN CROSSHAIR" else "TAMPILKAN CROSSHAIR",
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                letterSpacing = 0.5.sp
            )
        }

        // PERMISSION REQUIRED WARNING (if not granted)
        if (!hasOverlayPermission) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Overlay Permission Required",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Izin 'Tampilkan di atas aplikasi lain' (SYSTEM_ALERT_WINDOW) diperlukan agar crosshair overlay dapat muncul di atas game tanpa mengganggu input touch permainan.",
                        color = Color(0xFFD1D5DB),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_request_overlay_permission")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Pengaturan Izin Android", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 1. PREVIEW CROSSHAIR
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PREVIEW CROSSHAIR",
                color = Color(0xFFFFB300),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Sentuh & geser untuk atur posisi",
                color = Color(0xFF8E8E98),
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF07080C))
                .border(1.dp, Color(0xFF1E2232), RoundedCornerShape(14.dp))
                .pointerInput(Unit) {
                    // Allows interactive touch drag preview directly inside preview canvas
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = (settingsManager.crosshairOffsetX.value + dragAmount.x.toInt()).coerceIn(-screenWidth / 2, screenWidth / 2)
                        val newY = (settingsManager.crosshairOffsetY.value + dragAmount.y.toInt()).coerceIn(-screenHeight / 2, screenHeight / 2)
                        settingsManager.setCrosshairOffset(newX, newY)
                        settingsManager.setCrosshairAutoCenter(false)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Subtle alignment grid backdrop
            Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val gridColor = Color(0xFF181C28)
                val accentGrid = Color(0xFF283046)

                // Background grid lines
                val step = 20.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 0.6f)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 0.6f)
                    y += step
                }

                // Center cross lines
                drawLine(
                    accentGrid,
                    Offset(cx, 0f),
                    Offset(cx, size.height),
                    1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
                drawLine(
                    accentGrid,
                    Offset(0f, cy),
                    Offset(size.width, cy),
                    1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // Real reticle canvas preview
            CrosshairCanvas(
                style = crosshairStyle,
                sizeDp = crosshairSize,
                color = Color(crosshairColorLong).copy(alpha = crosshairOpacity),
                thicknessDp = crosshairThickness,
                isDragMode = false,
                offsetX = crosshairOffsetX,
                offsetY = crosshairOffsetY
            )

            // Top-left status badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F121C).copy(alpha = 0.88f))
                    .border(0.6.dp, if (crosshairAutoCenter) Color(0xFF10B981) else Color(0xFFFFB300), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (crosshairAutoCenter) "🎯 AUTO CENTER (Tengah Layar)" else "📐 MANUAL (X: $crosshairOffsetX, Y: $crosshairOffsetY)",
                    color = if (crosshairAutoCenter) Color(0xFF10B981) else Color(0xFFFFB300),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Bottom-right resolution badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F121C).copy(alpha = 0.88f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Layar Asli: ${screenWidth}x${screenHeight}px",
                    color = Color(0xFF9CA3AF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BENTUK CROSSHAIR (Style Selector)
        Text(
            text = "Bentuk Crosshair",
            color = Color(0xFFD1D5DB),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(StylePresets) { (key, label) ->
                val isSelected = crosshairStyle == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFFFF2A2A).copy(alpha = 0.2f) else Color(0xFF13151D))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFFFF2A2A) else Color(0xFF222533),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { settingsManager.setCrosshairStyle(key) }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFFFF2A2A) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 2. SIZE (Kecil → Sedang → Besar + Fine Slider)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Size",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${crosshairSize} dp",
                color = Color(0xFFFFB300),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Quick Preset Buttons: Kecil -> Sedang -> Besar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("Kecil", 18, crosshairSize <= 20),
                Triple("Sedang", 30, crosshairSize in 21..38),
                Triple("Besar", 48, crosshairSize >= 39)
            ).forEach { (label, value, isPres) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPres) Color(0xFFFFB300).copy(alpha = 0.2f) else Color(0xFF13151D))
                        .border(0.8.dp, if (isPres) Color(0xFFFFB300) else Color(0xFF222533), RoundedCornerShape(8.dp))
                        .clickable { settingsManager.setCrosshairSize(value) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$label (${value}dp)",
                        color = if (isPres) Color(0xFFFFB300) else Color(0xFFD1D5DB),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Slider(
            value = crosshairSize.toFloat(),
            onValueChange = { settingsManager.setCrosshairSize(it.roundToInt()) },
            valueRange = 14f..64f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFB300),
                activeTrackColor = Color(0xFFFFB300),
                inactiveTrackColor = Color(0xFF222533)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ==========================================
        // 3. COLOR (Bisa mengubah warna secara bebas)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Color",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(Color(crosshairColorLong))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "#%06X".format(0xFFFFFF and crosshairColorLong.toInt()),
                    color = Color(0xFFFFB300),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Palette presets
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(ColorPresets) { (name, colorValue) ->
                val isSelected = crosshairColorLong == colorValue
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(colorValue))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0xFF222533),
                            shape = CircleShape
                        )
                        .clickable { settingsManager.setCrosshairColor(colorValue) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = name,
                            tint = if (colorValue == 0xFFFFFFFFL) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Custom Free Color Picker Button
            item {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(Color(0xFF1E2232))
                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(17.dp))
                        .clickable { showCustomColorDialog = true }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kustom Bebas", color = Color(0xFFFFB300), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 4. OPACITY
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Opacity",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${(crosshairOpacity * 100).roundToInt()}%",
                color = Color(0xFFFFB300),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = crosshairOpacity,
            onValueChange = { settingsManager.setCrosshairOpacity(it) },
            valueRange = 0.1f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFB300),
                activeTrackColor = Color(0xFFFFB300),
                inactiveTrackColor = Color(0xFF222533)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ==========================================
        // 5. THICKNESS
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Thickness",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "%.1f dp".format(crosshairThickness),
                color = Color(0xFFFFB300),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = crosshairThickness,
            onValueChange = { settingsManager.setCrosshairThickness(it) },
            valueRange = 1.0f..7.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFB300),
                activeTrackColor = Color(0xFFFFB300),
                inactiveTrackColor = Color(0xFF222533)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 6. POSITION & AUTO CENTER
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Position",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Pusat Layar: (${screenWidth / 2}, ${screenHeight / 2}) px",
                color = Color(0xFF8E8E98),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        // Auto Center vs Manual Switch Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (crosshairAutoCenter) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF13151D))
                    .border(
                        0.8.dp,
                        if (crosshairAutoCenter) Color(0xFF10B981) else Color(0xFF222533),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        settingsManager.setCrosshairAutoCenter(true)
                        settingsManager.setCrosshairOffset(0, 0)
                        Toast.makeText(context, "Auto Center Aktif (Pusat Layar 0, 0)", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = null,
                        tint = if (crosshairAutoCenter) Color(0xFF10B981) else Color(0xFF8E8E98),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto Center",
                        color = if (crosshairAutoCenter) Color(0xFF10B981) else Color(0xFFD1D5DB),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!crosshairAutoCenter) Color(0xFFFFB300).copy(alpha = 0.2f) else Color(0xFF13151D))
                    .border(
                        0.8.dp,
                        if (!crosshairAutoCenter) Color(0xFFFFB300) else Color(0xFF222533),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        settingsManager.setCrosshairAutoCenter(false)
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = if (!crosshairAutoCenter) Color(0xFFFFB300) else Color(0xFF8E8E98),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Manual Position",
                        color = if (!crosshairAutoCenter) Color(0xFFFFB300) else Color(0xFFD1D5DB),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // On-Screen Drag Mode toggle (Drag freely on screen, then locks to prevent touching game)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF13151D))
                .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mode Drag di Layar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = if (crosshairDragMode) "Geser overlay di layar. Kunci setelah selesai agar touch game tidak terhalang." else "Terkunci (Touch tembus 100% langsung ke game)",
                    color = if (crosshairDragMode) Color(0xFFFFB300) else Color(0xFF8E8E98),
                    fontSize = 10.5.sp
                )
            }
            Switch(
                checked = crosshairDragMode,
                onCheckedChange = { enable ->
                    settingsManager.setCrosshairDragMode(enable)
                    if (enable) {
                        settingsManager.setCrosshairAutoCenter(false)
                        Toast.makeText(context, "Mode Drag Aktif: Geser crosshair di layar", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Posisi Terkunci: Aman untuk bermain game", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFFFB300)
                )
            )
        }

        // Manual sliders (visible if manual mode is enabled)
        if (!crosshairAutoCenter) {
            Spacer(modifier = Modifier.height(10.dp))

            // X Position
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Offset Horizontal (X)", color = Color(0xFFD1D5DB), fontSize = 11.5.sp)
                Text(text = "$crosshairOffsetX px", color = Color(0xFFFFB300), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = crosshairOffsetX.toFloat(),
                onValueChange = { settingsManager.setCrosshairOffset(it.roundToInt(), crosshairOffsetY) },
                valueRange = -250f..250f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFB300),
                    activeTrackColor = Color(0xFFFFB300),
                    inactiveTrackColor = Color(0xFF222533)
                )
            )

            // Y Position
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Offset Vertikal (Y)", color = Color(0xFFD1D5DB), fontSize = 11.5.sp)
                Text(text = "$crosshairOffsetY px", color = Color(0xFFFFB300), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = crosshairOffsetY.toFloat(),
                onValueChange = { settingsManager.setCrosshairOffset(crosshairOffsetX, it.roundToInt()) },
                valueRange = -250f..250f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFB300),
                    activeTrackColor = Color(0xFFFFB300),
                    inactiveTrackColor = Color(0xFF222533)
                )
            )

            // Fine tuning D-Pad buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("⬅️ -1 X", { settingsManager.setCrosshairOffset(crosshairOffsetX - 1, crosshairOffsetY) }),
                    Pair("➡️ +1 X", { settingsManager.setCrosshairOffset(crosshairOffsetX + 1, crosshairOffsetY) }),
                    Pair("⬆️ -1 Y", { settingsManager.setCrosshairOffset(crosshairOffsetX, crosshairOffsetY - 1) }),
                    Pair("⬇️ +1 Y", { settingsManager.setCrosshairOffset(crosshairOffsetX, crosshairOffsetY + 1) })
                ).forEach { (lbl, action) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF171A24))
                            .clickable { action() }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lbl, color = Color(0xFFD1D5DB), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Coarse tuning buttons (+10 / -10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("-10 X", { settingsManager.setCrosshairOffset(crosshairOffsetX - 10, crosshairOffsetY) }),
                    Pair("+10 X", { settingsManager.setCrosshairOffset(crosshairOffsetX + 10, crosshairOffsetY) }),
                    Pair("-10 Y", { settingsManager.setCrosshairOffset(crosshairOffsetX, crosshairOffsetY - 10) }),
                    Pair("+10 Y", { settingsManager.setCrosshairOffset(crosshairOffsetX, crosshairOffsetY + 10) })
                ).forEach { (lbl, action) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1A1F2C))
                            .clickable { action() }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lbl, color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==========================================
        // 7. PROFIL GAME (Diferensiasi profil per game)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PROFIL GAME",
                color = Color(0xFF8E8E98),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            TextButton(
                onClick = { showNewProfileDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Profil Baru", color = Color(0xFFFFB300), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(profiles) { prof ->
                val isSelected = activeProfileId == prof.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFFFFB300).copy(alpha = 0.2f) else Color(0xFF13151D))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFFFFB300) else Color(0xFF222533),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            profileManager.selectProfile(prof.id)
                            Toast.makeText(context, "Profil '${prof.name}' diterapkan", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = prof.name,
                            color = if (isSelected) Color(0xFFFFB300) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (prof.targetGame.isNotBlank()) {
                            Text(
                                text = prof.targetGame.substringAfterLast("."),
                                color = Color(0xFF8E8E98),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ==========================================
        // 8. PRIMARY ACTION BUTTONS:
        // [ AUTO CENTER ] [ SAVE PROFILE ]
        // [ RESET POSITION ] [ RESET STYLE ]
        // ==========================================
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // [ AUTO CENTER ]
                OutlinedButton(
                    onClick = {
                        settingsManager.setCrosshairAutoCenter(true)
                        settingsManager.setCrosshairOffset(0, 0)
                        Toast.makeText(context, "Crosshair otomatis diposisikan tepat di tengah layar!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_crosshair_autocenter"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AUTO CENTER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // [ SAVE PROFILE ]
                Button(
                    onClick = {
                        val activeProf = profiles.find { it.id == activeProfileId }
                        profileManager.saveCurrentAsProfile(
                            profileName = activeProf?.name ?: "Universal Profile",
                            gamePackage = activeProf?.targetGame ?: ""
                        )
                        Toast.makeText(context, "Profil crosshair berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_crosshair_save"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300),
                        contentColor = Color(0xFF0A0B0F)
                    )
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SAVE PROFILE", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // [ RESET POSITION ]
                OutlinedButton(
                    onClick = {
                        settingsManager.resetCrosshairPosition()
                        Toast.makeText(context, "Posisi crosshair di-reset ke tengah (0, 0)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_crosshair_reset_position"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESET POSITION", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // [ RESET STYLE ]
                OutlinedButton(
                    onClick = {
                        settingsManager.resetCrosshairStyle()
                        Toast.makeText(context, "Gaya tampilan crosshair di-reset ke default", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_crosshair_reset_style"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE040FB)),
                    border = BorderStroke(1.dp, Color(0xFFE040FB).copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RESET STYLE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // [ RESET ALL ]
                OutlinedButton(
                    onClick = {
                        settingsManager.resetCrosshair()
                        Toast.makeText(context, "Semua konfigurasi crosshair di-reset", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .height(42.dp)
                        .testTag("btn_crosshair_reset"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("RESET ALL", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ==========================================
    // DIALOG: CREATE NEW GAME PROFILE
    // ==========================================
    if (showNewProfileDialog) {
        AlertDialog(
            onDismissRequest = { showNewProfileDialog = false },
            title = {
                Text("Buat Profil Crosshair Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Simpan konfigurasi crosshair saat ini untuk game tertentu agar otomatis digunakan dan tidak berpindah.",
                        color = Color(0xFFD1D5DB),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Nama Profil (misal: Apex Mobile)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB300),
                            unfocusedBorderColor = Color(0xFF2E3244),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newProfileGame,
                        onValueChange = { newProfileGame = it },
                        label = { Text("Package Name / ID Game (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB300),
                            unfocusedBorderColor = Color(0xFF2E3244),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            profileManager.createNewProfile(newProfileName.trim(), newProfileGame.trim())
                            showNewProfileDialog = false
                            newProfileName = ""
                            newProfileGame = ""
                            Toast.makeText(context, "Profil baru berhasil dibuat!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300), contentColor = Color.Black)
                ) {
                    Text("Simpan Profil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProfileDialog = false }) {
                    Text("Batal", color = Color(0xFF8E8E98))
                }
            },
            containerColor = Color(0xFF13151D),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ==========================================
    // DIALOG: CUSTOM FREE COLOR PICKER
    // ==========================================
    if (showCustomColorDialog) {
        val initialRed = ((crosshairColorLong shr 16) and 0xFF).toFloat()
        val initialGreen = ((crosshairColorLong shr 8) and 0xFF).toFloat()
        val initialBlue = (crosshairColorLong and 0xFF).toFloat()

        var redVal by remember { mutableFloatStateOf(initialRed) }
        var greenVal by remember { mutableFloatStateOf(initialGreen) }
        var blueVal by remember { mutableFloatStateOf(initialBlue) }

        val computedColorLong = (0xFFL shl 24) or
                ((redVal.roundToInt().toLong() and 0xFF) shl 16) or
                ((greenVal.roundToInt().toLong() and 0xFF) shl 8) or
                (blueVal.roundToInt().toLong() and 0xFF)

        AlertDialog(
            onDismissRequest = { showCustomColorDialog = false },
            title = {
                Text("Pilih Warna Crosshair Bebas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Atur kombinasi warna Red, Green, Blue secara bebas sesuai kebutuhan visual game Anda.",
                        color = Color(0xFFD1D5DB),
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Preview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0B0D14))
                            .border(1.dp, Color(0xFF222533), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(computedColorLong))
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Kode Hex: #%06X".format(0xFFFFFF and computedColorLong.toInt()),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "RGB(${redVal.roundToInt()}, ${greenVal.roundToInt()}, ${blueVal.roundToInt()})",
                                color = Color(0xFFFFB300),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Red Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Merah (Red)", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${redVal.roundToInt()}", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = redVal,
                        onValueChange = { redVal = it },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFEF4444), activeTrackColor = Color(0xFFEF4444))
                    )

                    // Green Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Hijau (Green)", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${greenVal.roundToInt()}", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = greenVal,
                        onValueChange = { greenVal = it },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                    )

                    // Blue Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Biru (Blue)", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${blueVal.roundToInt()}", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = blueVal,
                        onValueChange = { blueVal = it },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsManager.setCrosshairColor(computedColorLong)
                        showCustomColorDialog = false
                        Toast.makeText(context, "Warna kustom berhasil diterapkan!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300), contentColor = Color.Black)
                ) {
                    Text("Terapkan Warna", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomColorDialog = false }) {
                    Text("Batal", color = Color(0xFF8E8E98))
                }
            },
            containerColor = Color(0xFF13151D),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
