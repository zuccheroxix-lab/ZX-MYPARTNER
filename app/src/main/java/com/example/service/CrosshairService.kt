package com.example.service

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
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
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

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isServiceRunning.value = true
        settingsManager = SettingsManager(applicationContext)

        startAsForeground()

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Izin Tampilkan di Atas Aplikasi Lain diperlukan untuk Crosshair", Toast.LENGTH_LONG).show()
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

    private fun setupCrosshairView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

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

                CrosshairCanvas(
                    style = style,
                    sizeDp = sizeDp,
                    color = Color(colorLong).copy(alpha = opacity)
                )
            }
        }
        composeView = view

        val params = createParams()
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

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
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
            val params = createParams()
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
            settingsManager.crosshairEnabled.collect { enabled ->
                if (!enabled) {
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        settingsManager.setCrosshairEnabled(false)

        composeView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {}
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
    color: Color
) {
    val totalSize = (sizeDp * 2.2f).coerceAtLeast(36f).dp

    Box(
        modifier = Modifier.size(totalSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(totalSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val halfRadius = sizeDp.dp.toPx() / 2f
            val strokeWidth = 2.5.dp.toPx()
            val outlineStroke = strokeWidth + 2.dp.toPx()

            when (style) {
                "DOT" -> {
                    // Outer subtle black outline for contrast
                    drawCircle(Color.Black.copy(alpha = 0.6f), radius = halfRadius * 0.45f + 1.5f, center = center)
                    drawCircle(color, radius = halfRadius * 0.45f, center = center)
                }
                "CIRCLE_DOT" -> {
                    // Outer Ring
                    drawCircle(Color.Black.copy(alpha = 0.5f), radius = halfRadius, center = center, style = Stroke(outlineStroke))
                    drawCircle(color, radius = halfRadius, center = center, style = Stroke(strokeWidth))
                    // Center Dot
                    drawCircle(Color.Black.copy(alpha = 0.6f), radius = 2.5.dp.toPx() + 1f, center = center)
                    drawCircle(color, radius = 2.5.dp.toPx(), center = center)
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
                    drawCircle(color, radius = 1.8.dp.toPx(), center = center)
                }
                "SNIPER" -> {
                    // Outer Ring with 4 notch markers
                    drawCircle(Color.Black.copy(alpha = 0.4f), radius = halfRadius, center = center, style = Stroke(outlineStroke))
                    drawCircle(color, radius = halfRadius, center = center, style = Stroke(strokeWidth * 0.8f))
                    // Cross lines
                    val gap = halfRadius * 0.35f
                    val len = halfRadius * 1.1f
                    drawLine(color, Offset(center.x - len, center.y), Offset(center.x - gap, center.y), strokeWidth)
                    drawLine(color, Offset(center.x + gap, center.y), Offset(center.x + len, center.y), strokeWidth)
                    drawLine(color, Offset(center.x, center.y - len), Offset(center.x, center.y - gap), strokeWidth)
                    drawLine(color, Offset(center.x, center.y + gap), Offset(center.x, center.y + len), strokeWidth)
                    // Tiny center dot
                    drawCircle(color, radius = 2.dp.toPx(), center = center)
                }
                else -> { // "CLASSIC_CROSS"
                    val gap = halfRadius * 0.3f
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
                    // Center dot
                    drawCircle(color, radius = 1.5.dp.toPx(), center = center)
                }
            }
        }
    }
}
