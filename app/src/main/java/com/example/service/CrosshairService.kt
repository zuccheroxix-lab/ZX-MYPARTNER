package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrosshairService : Service() {

    companion object {
        private const val CHANNEL_ID = "zx_crosshair_channel"
        private const val NOTIFICATION_ID = 2009
        const val ACTION_STOP_CROSSHAIR = "com.example.service.ACTION_STOP_CROSSHAIR"
        const val ACTION_REFRESH_CROSSHAIR = "com.example.service.ACTION_REFRESH_CROSSHAIR"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, CrosshairService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CrosshairService::class.java)
            context.stopService(intent)
        }

        fun refresh(context: Context) {
            val intent = Intent(context, CrosshairService::class.java).apply {
                action = ACTION_REFRESH_CROSSHAIR
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private lateinit var settingsManager: SettingsManager
    private var currentParams: WindowManager.LayoutParams? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isServiceRunning.value = true
        settingsManager = SettingsManager(applicationContext)

        startAsForeground()

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Izin Overlay Diperlukan untuk Crosshair", Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }

        setupCrosshairView()
        observeSettings()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_CROSSHAIR -> stopSelf()
            ACTION_REFRESH_CROSSHAIR -> updateViewLayout()
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DYNIMETIZE ZX Crosshair Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active floating crosshair target overlay for games"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }

        val appIntent = Intent(this, MainActivity::class.java)
        val pendingAppIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, CrosshairService::class.java).apply {
            action = ACTION_STOP_CROSSHAIR
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DYNIMETIZE ZX Crosshair")
            .setContentText("Overlay crosshair presisi aktif di layar")
            .setSmallIcon(R.drawable.img_app_icon)
            .setContentIntent(pendingAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Matikan", pendingStopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCrosshairView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Ensure single active overlay instance: remove any previous view if present
        composeView?.let { oldView ->
            try {
                windowManager?.removeViewImmediate(oldView)
            } catch (_: Exception) {}
            composeView = null
        }

        val owner = OverlayLifecycleOwner()
        owner.onCreate()
        lifecycleOwner = owner

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)

            setContent {
                val style by settingsManager.crosshairStyle.collectAsState()
                val sizeDp by settingsManager.crosshairSize.collectAsState()
                val colorLong by settingsManager.crosshairColor.collectAsState()
                val opacity by settingsManager.crosshairOpacity.collectAsState()
                val thickness by settingsManager.crosshairThickness.collectAsState()
                val dragMode by settingsManager.crosshairDragMode.collectAsState()
                val offsetX by settingsManager.crosshairOffsetX.collectAsState()
                val offsetY by settingsManager.crosshairOffsetY.collectAsState()

                CrosshairCanvas(
                    style = style,
                    sizeDp = sizeDp,
                    color = Color(colorLong).copy(alpha = opacity),
                    thicknessDp = thickness,
                    isDragMode = dragMode,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    onDragMove = { dx, dy ->
                        val params = currentParams ?: return@CrosshairCanvas
                        params.x += dx
                        params.y += dy
                        try {
                            windowManager?.updateViewLayout(this@apply, params)
                        } catch (_: Exception) {}
                    },
                    onDragEnd = {
                        val params = currentParams ?: return@CrosshairCanvas
                        settingsManager.setCrosshairOffset(params.x, params.y)
                        settingsManager.setCrosshairAutoCenter(false)
                    },
                    onCenterClick = {
                        settingsManager.setCrosshairAutoCenter(true)
                        settingsManager.setCrosshairOffset(0, 0)
                    },
                    onLockClick = {
                        settingsManager.setCrosshairDragMode(false)
                    }
                )
            }
        }

        composeView = view

        val params = createParams()
        currentParams = params
        try {
            windowManager?.addView(view, params)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal menambahkan crosshair overlay: ${e.message}", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }

    private fun createParams(): WindowManager.LayoutParams {
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val offsetX = settingsManager.crosshairOffsetX.value
        val offsetY = settingsManager.crosshairOffsetY.value
        val isDragMode = settingsManager.crosshairDragMode.value

        // When dragMode is FALSE (normal gaming), FLAG_NOT_TOUCHABLE ensures zero game interference.
        // When dragMode is TRUE, touches allow user to drag the reticle freely on screen.
        val flags = if (isDragMode) {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        } else {
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = offsetX
            y = offsetY
        }
    }

    private fun updateViewLayout() {
        val view = composeView ?: return
        val wm = windowManager ?: return
        try {
            val params = currentParams ?: createParams().also { currentParams = it }
            val offsetX = settingsManager.crosshairOffsetX.value
            val offsetY = settingsManager.crosshairOffsetY.value
            val isDragMode = settingsManager.crosshairDragMode.value

            params.flags = if (isDragMode) {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            } else {
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            }
            params.gravity = Gravity.CENTER
            params.x = offsetX
            params.y = offsetY
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    private fun observeSettings() {
        serviceScope.launch {
            settingsManager.crosshairOffsetX.collect { updateViewLayout() }
        }
        serviceScope.launch {
            settingsManager.crosshairOffsetY.collect { updateViewLayout() }
        }
        serviceScope.launch {
            settingsManager.crosshairDragMode.collect { updateViewLayout() }
        }
        serviceScope.launch {
            settingsManager.crosshairAutoCenter.collect { updateViewLayout() }
        }
        serviceScope.launch {
            settingsManager.crosshairEnabled.collect { enabled ->
                if (!enabled) {
                    stopSelf()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        updateViewLayout()
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        settingsManager.setCrosshairEnabled(false)

        composeView?.let { view ->
            try {
                windowManager?.removeViewImmediate(view)
            } catch (_: Exception) {
                try {
                    windowManager?.removeView(view)
                } catch (_: Exception) {}
            }
        }
        composeView = null

        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        windowManager = null
    }
}

@Composable
fun CrosshairCanvas(
    style: String,
    sizeDp: Int,
    color: Color,
    thicknessDp: Float = 2.5f,
    isDragMode: Boolean = false,
    offsetX: Int = 0,
    offsetY: Int = 0,
    onDragMove: (Int, Int) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    onCenterClick: () -> Unit = {},
    onLockClick: () -> Unit = {}
) {
    val totalSize = (sizeDp * 2.4f).coerceAtLeast(if (isDragMode) 68f else 44f).dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(totalSize)
                .pointerInput(isDragMode) {
                    if (isDragMode) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDragMove(dragAmount.x.toInt(), dragAmount.y.toInt())
                            },
                            onDragEnd = {
                                onDragEnd()
                            }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(totalSize)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val halfRadius = sizeDp.dp.toPx() / 2f
                val strokeWidth = (thicknessDp.dp.toPx()).coerceAtLeast(1.5f)
                val outlineStroke = strokeWidth + 2.dp.toPx()

                // Reposition guide ring when drag mode is active
                if (isDragMode) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = halfRadius * 1.5f,
                        center = center,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )
                }

                when (style) {
                    "DOT" -> {
                        // High contrast outer shadow ring + inner precision dot
                        drawCircle(Color.Black.copy(alpha = 0.6f), radius = halfRadius * 0.45f + 1.8f, center = center)
                        drawCircle(color, radius = (halfRadius * 0.45f).coerceAtLeast(3f), center = center)
                    }
                    "CIRCLE_DOT" -> {
                        // Tactical outer ring
                        drawCircle(Color.Black.copy(alpha = 0.5f), radius = halfRadius, center = center, style = Stroke(outlineStroke))
                        drawCircle(color, radius = halfRadius, center = center, style = Stroke(strokeWidth))
                        // Center dot
                        drawCircle(Color.Black.copy(alpha = 0.6f), radius = (strokeWidth * 1.2f) + 1.2f, center = center)
                        drawCircle(color, radius = (strokeWidth * 1.2f).coerceAtLeast(2.5f), center = center)
                    }
                    "T_SHAPE" -> {
                        val gap = halfRadius * 0.25f
                        val length = halfRadius * 0.9f
                        // Left bar
                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x - gap - length, center.y), Offset(center.x - gap, center.y), outlineStroke)
                        drawLine(color, Offset(center.x - gap - length, center.y), Offset(center.x - gap, center.y), strokeWidth)
                        // Right bar
                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x + gap, center.y), Offset(center.x + gap + length, center.y), outlineStroke)
                        drawLine(color, Offset(center.x + gap, center.y), Offset(center.x + gap + length, center.y), strokeWidth)
                        // Bottom bar
                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + length), outlineStroke)
                        drawLine(color, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + length), strokeWidth)
                        // Center dot
                        drawCircle(color, radius = (strokeWidth * 0.8f).coerceAtLeast(2f), center = center)
                    }
                    "SNIPER" -> {
                        // Outer Ring with notch markers
                        drawCircle(Color.Black.copy(alpha = 0.4f), radius = halfRadius, center = center, style = Stroke(outlineStroke))
                        drawCircle(color, radius = halfRadius, center = center, style = Stroke(strokeWidth * 0.85f))
                        // Cross lines
                        val gap = halfRadius * 0.35f
                        val len = halfRadius * 1.2f
                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x - len, center.y), Offset(center.x - gap, center.y), outlineStroke)
                        drawLine(color, Offset(center.x - len, center.y), Offset(center.x - gap, center.y), strokeWidth)

                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x + gap, center.y), Offset(center.x + len, center.y), outlineStroke)
                        drawLine(color, Offset(center.x + gap, center.y), Offset(center.x + len, center.y), strokeWidth)

                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x, center.y - len), Offset(center.x, center.y - gap), outlineStroke)
                        drawLine(color, Offset(center.x, center.y - len), Offset(center.x, center.y - gap), strokeWidth)

                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x, center.y + gap), Offset(center.x, center.y + len), outlineStroke)
                        drawLine(color, Offset(center.x, center.y + gap), Offset(center.x, center.y + len), strokeWidth)
                        // Center dot
                        drawCircle(color, radius = 2.2.dp.toPx(), center = center)
                    }
                    "CHEVRON" -> {
                        // Tactical Chevron (Inverted V arrow)
                        val halfW = halfRadius * 0.7f
                        val height = halfRadius * 0.75f
                        val path = Path().apply {
                            moveTo(center.x - halfW, center.y + height / 2f)
                            lineTo(center.x, center.y - height / 2f)
                            lineTo(center.x + halfW, center.y + height / 2f)
                        }
                        drawPath(path, Color.Black.copy(alpha = 0.5f), style = Stroke(outlineStroke))
                        drawPath(path, color, style = Stroke(strokeWidth))
                        drawCircle(color, radius = 2.dp.toPx(), center = Offset(center.x, center.y - height / 2f))
                    }
                    "BOX_DOT" -> {
                        // Square box reticle with center dot
                        val boxSide = halfRadius * 1.2f
                        val topLeft = Offset(center.x - boxSide / 2f, center.y - boxSide / 2f)
                        drawRect(Color.Black.copy(alpha = 0.5f), topLeft, Size(boxSide, boxSide), style = Stroke(outlineStroke))
                        drawRect(color, topLeft, Size(boxSide, boxSide), style = Stroke(strokeWidth))
                        drawCircle(color, radius = (strokeWidth * 0.9f).coerceAtLeast(2.2f), center = center)
                    }
                    "DIAMOND" -> {
                        val path = Path().apply {
                            moveTo(center.x, center.y - halfRadius)
                            lineTo(center.x + halfRadius, center.y)
                            lineTo(center.x, center.y + halfRadius)
                            lineTo(center.x - halfRadius, center.y)
                            close()
                        }
                        drawPath(path, Color.Black.copy(alpha = 0.5f), style = Stroke(outlineStroke))
                        drawPath(path, color, style = Stroke(strokeWidth))
                        drawCircle(color, radius = (strokeWidth * 0.8f).coerceAtLeast(2f), center = center)
                    }
                    else -> { // "CLASSIC_CROSS"
                        val gap = halfRadius * 0.28f
                        val length = halfRadius * 0.85f

                        // Top
                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x, center.y - gap - length), Offset(center.x, center.y - gap), outlineStroke)
                        drawLine(color, Offset(center.x, center.y - gap - length), Offset(center.x, center.y - gap), strokeWidth)
                        // Bottom
                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + length), outlineStroke)
                        drawLine(color, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + length), strokeWidth)
                        // Left
                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x - gap - length, center.y), Offset(center.x - gap, center.y), outlineStroke)
                        drawLine(color, Offset(center.x - gap - length, center.y), Offset(center.x - gap, center.y), strokeWidth)
                        // Right
                        drawLine(Color.Black.copy(alpha = 0.5f), Offset(center.x + gap, center.y), Offset(center.x + gap + length, center.y), outlineStroke)
                        drawLine(color, Offset(center.x + gap, center.y), Offset(center.x + gap + length, center.y), strokeWidth)
                        // Tiny Center dot
                        drawCircle(color, radius = (strokeWidth * 0.7f).coerceAtLeast(1.8f), center = center)
                    }
                }
            }
        }

        // Overlay drag indicator and quick lock button when drag mode is active
        if (isDragMode) {
            Spacer(modifier = Modifier.size(4.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F121C).copy(alpha = 0.92f))
                    .border(0.8.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "X: $offsetX, Y: $offsetY",
                    color = Color(0xFFFFB300),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                        .clickable { onCenterClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🎯 Center",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .clickable { onLockClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔒 Kunci",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
