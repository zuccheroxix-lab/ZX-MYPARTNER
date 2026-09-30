package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity

// Gaming HUD Color Theme
private val HudBg = Color(0xF2090B10)
private val HudBorder = Color(0xFFD97706)
private val GoldAccent = Color(0xFFFFB300)
private val GoldGlow = Color(0xFFFFC107)
private val AmberActive = Color(0xFFF59E0B)
private val DarkTileBg = Color(0xFF141720)
private val DarkTileBorder = Color(0xFF262C3D)
private val TextMuted = Color(0xFF94A3B8)

@Composable
fun GamingOverlayRoot(
    isExpanded: Boolean,
    metrics: HudMetrics,
    onExpandToggled: (Boolean) -> Unit,
    onCloseService: () -> Unit,
    onTriggerDrag: (Float, Float) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onBrightnessChanged: (Float) -> Unit,
    onActionRamBoost: () -> Unit,
    onToggleExtremeGov: () -> Unit,
    onToggleTouch240: () -> Unit,
    onToggleFpsStabilizer: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleDnd: () -> Unit,
    onOpenGameLibrary: () -> Unit,
    onCheckShizuku: () -> Unit
) {
    val context = LocalContext.current

    if (isExpanded) {
        // Full Gaming HUD Expanded Panel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x80000000))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onExpandToggled(false)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Catch clicks inside HUD so it doesn't dismiss */ }
            ) {
                GamingHudPanel(
                    metrics = metrics,
                    onMinimize = { onExpandToggled(false) },
                    onCloseService = onCloseService,
                    onVolumeChanged = onVolumeChanged,
                    onBrightnessChanged = onBrightnessChanged,
                    onActionRamBoost = onActionRamBoost,
                    onToggleExtremeGov = onToggleExtremeGov,
                    onToggleTouch240 = onToggleTouch240,
                    onToggleFpsStabilizer = onToggleFpsStabilizer,
                    onToggleTorch = onToggleTorch,
                    onToggleDnd = onToggleDnd,
                    onOpenGameLibrary = onOpenGameLibrary,
                    onCheckShizuku = onCheckShizuku
                )
            }
        }
    } else {
        // Compact Floating Trigger (Bubble on screen edge)
        FloatingTriggerBubble(
            fpsText = metrics.fpsText,
            ramText = metrics.ramText,
            onClick = { onExpandToggled(true) },
            onDrag = onTriggerDrag
        )
    }
}

/**
 * Compact Floating Trigger Button.
 * Draggable along screen edges, displays live FPS and gaming logo.
 */
@Composable
fun FloatingTriggerBubble(
    fpsText: String,
    ramText: String,
    onClick: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 66.dp, height = 48.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF131722), Color(0xFF1E2436))
                )
            )
            .border(1.2.dp, GoldAccent, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = "Expand HUD",
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = fpsText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "FPS",
                    color = GoldAccent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Expanded Gaming HUD Dashboard with:
 * Left: 4 feature buttons (BOOST, GOVERNOR, TOUCH, FPS STAB)
 * Center: RAM, FPS (Prominent), CPU
 * Right: 4 feature buttons (TORCH, GAME SPACE, DND, SHIZUKU)
 * Bottom: Brightness & Volume sliders + Action buttons
 */
@Composable
fun GamingHudPanel(
    metrics: HudMetrics,
    onMinimize: () -> Unit,
    onCloseService: () -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onBrightnessChanged: (Float) -> Unit,
    onActionRamBoost: () -> Unit,
    onToggleExtremeGov: () -> Unit,
    onToggleTouch240: () -> Unit,
    onToggleFpsStabilizer: () -> Unit,
    onToggleTorch: () -> Unit,
    onToggleDnd: () -> Unit,
    onOpenGameLibrary: () -> Unit,
    onCheckShizuku: () -> Unit
) {
    val context = LocalContext.current
    var brightnessValue by remember { mutableFloatStateOf(0.8f) }
    var volumeValue by remember { mutableFloatStateOf(metrics.volumePercent / 100f) }

    fun vibrate() {
        try {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.96f)
            .widthIn(max = 640.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(22.dp))
            .background(HudBg)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(GoldAccent, Color(0xFF78350F), GoldAccent.copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Title + Minimize + Exit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DYNIMETIZE ZX",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GAMING HUD",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Open Main App Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2230))
                            .clickable {
                                vibrate()
                                val intent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                context.startActivity(intent)
                                onMinimize()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open App",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Minimize Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2230))
                            .clickable {
                                vibrate()
                                onMinimize()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Minimize HUD",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Layout: [Left 4 Features] [Center Telemetry: RAM, FPS 60, CPU] [Right 4 Features]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT 4 FEATURES (2x2 Grid)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HudFeatureTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.RocketLaunch,
                            label = "RAM BOOST",
                            isActive = true,
                            onClick = {
                                vibrate()
                                onActionRamBoost()
                            }
                        )
                        HudFeatureTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Speed,
                            label = "EXTREME",
                            isActive = metrics.isExtremeGovOn,
                            onClick = {
                                vibrate()
                                onToggleExtremeGov()
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HudFeatureTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.TouchApp,
                            label = "240Hz",
                            isActive = metrics.isTouch240HzOn,
                            onClick = {
                                vibrate()
                                onToggleTouch240()
                            }
                        )
                        HudFeatureTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ElectricBolt,
                            label = "FPS LOCK",
                            isActive = metrics.isFpsStabilizerOn,
                            onClick = {
                                vibrate()
                                onToggleFpsStabilizer()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // CENTER TELEMETRY: RAM | FPS 60 (Prominent) | CPU
                Box(
                    modifier = Modifier
                        .weight(1.35f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF101420), Color(0xFF0C0E16))
                            )
                        )
                        .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Top Telemetry Stats (RAM & CPU)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "RAM",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = metrics.ramText,
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "CPU",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = metrics.cpuText,
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Large Center FPS Display (e.g. 60)
                        Text(
                            text = metrics.fpsText,
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = (-1).sp
                        )

                        Text(
                            text = "REAL-TIME FPS",
                            color = GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = metrics.ramDetail,
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // RIGHT 4 FEATURES (2x2 Grid)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HudFeatureTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FlashlightOn,
                            label = "TORCH",
                            isActive = metrics.isTorchOn,
                            onClick = {
                                vibrate()
                                onToggleTorch()
                            }
                        )
                        HudFeatureTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.SportsEsports,
                            label = "GAMES",
                            isActive = true,
                            onClick = {
                                vibrate()
                                onOpenGameLibrary()
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HudFeatureTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.NotificationsOff,
                            label = "DND",
                            isActive = metrics.isDndActive,
                            onClick = {
                                vibrate()
                                onToggleDnd()
                            }
                        )
                        HudFeatureTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Security,
                            label = "SHIZUKU",
                            isActive = metrics.isShizukuAvailable,
                            onClick = {
                                vibrate()
                                onCheckShizuku()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // BOTTOM CONTROLS: Brightness & Volume Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brightness Slider
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Brightness",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Slider(
                        value = brightnessValue,
                        onValueChange = {
                            brightnessValue = it
                            onBrightnessChanged(it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = Color(0xFF262C3D)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Volume Slider
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Slider(
                        value = volumeValue,
                        onValueChange = {
                            volumeValue = it
                            onVolumeChanged(it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = GoldAccent,
                            activeTrackColor = GoldAccent,
                            inactiveTrackColor = Color(0xFF262C3D)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS: ACTIVE • HUD ENGINE ZX",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Minimize button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2332))
                            .clickable {
                                vibrate()
                                onMinimize()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "MINIMIZE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Exit Overlay Service button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF3B1218))
                            .border(0.8.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                            .clickable {
                                vibrate()
                                onCloseService()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "EXIT HUD",
                            color = Color(0xFFFCA5A5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern Futuristically-Styled HUD Feature Tile.
 * Distinctive glowing gaming aesthetics with icon & label.
 */
@Composable
fun HudFeatureTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isActive) GoldAccent.copy(alpha = 0.8f) else DarkTileBorder
    val iconTint = if (isActive) GoldAccent else TextMuted
    val bg = if (isActive) Color(0xFF1A1F2C) else DarkTileBg

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isActive) Color.White else TextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
