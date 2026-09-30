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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class OverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "zx_gaming_hud_channel"
        private const val NOTIFICATION_ID = 1009
        const val ACTION_STOP_OVERLAY = "com.example.service.ACTION_STOP_OVERLAY"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.stopService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var telemetryTracker: HudTelemetryTracker? = null

    private var isExpanded by mutableStateOf(false)
    private var currentMetrics by mutableStateOf(HudMetrics())

    private var triggerX = 20
    private var triggerY = 300

    private val mainHandler = Handler(Looper.getMainLooper())
    private val periodicMetricsRunnable = object : Runnable {
        override fun run() {
            telemetryTracker?.sampleMetrics()
            mainHandler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isServiceRunning.value = true

        startAsForeground()

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Permission SYSTEM_ALERT_WINDOW diperlukan", Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }

        setupTelemetry()
        setupOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_OVERLAY) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DYNIMETIZE ZX Gaming HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running in-game floating telemetry HUD & quick control panel"
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

        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DYNIMETIZE ZX Gaming HUD")
            .setContentText("Gaming floating telemetry & quick controls active")
            .setSmallIcon(R.drawable.img_app_icon)
            .setContentIntent(pendingAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close HUD", pendingStopIntent)
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

    private fun setupTelemetry() {
        telemetryTracker = HudTelemetryTracker(this) { metrics ->
            currentMetrics = metrics
        }.apply {
            start()
        }
        mainHandler.post(periodicMetricsRunnable)
    }

    private fun setupOverlayView() {
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
                GamingOverlayRoot(
                    isExpanded = isExpanded,
                    metrics = currentMetrics,
                    onExpandToggled = { expanded ->
                        toggleExpandState(expanded)
                    },
                    onCloseService = {
                        stopSelf()
                    },
                    onTriggerDrag = { dx, dy ->
                        updateTriggerPosition(dx, dy)
                    },
                    onVolumeChanged = { pct ->
                        telemetryTracker?.setMusicVolume(pct)
                        telemetryTracker?.sampleMetrics()
                    },
                    onBrightnessChanged = { pct ->
                        updateBrightness(pct)
                    },
                    onActionRamBoost = {
                        val freed = telemetryTracker?.triggerRamTrim() ?: 250
                        Toast.makeText(this@OverlayService, "RAM Freed: +${freed}MB Cached Memory Cleaned", Toast.LENGTH_SHORT).show()
                        telemetryTracker?.sampleMetrics()
                    },
                    onToggleExtremeGov = {
                        val state = !(telemetryTracker?.isExtremeGovOn ?: false)
                        telemetryTracker?.isExtremeGovOn = state
                        Toast.makeText(this@OverlayService, if (state) "Governor: EXTREME" else "Governor: BALANCED", Toast.LENGTH_SHORT).show()
                        telemetryTracker?.sampleMetrics()
                    },
                    onToggleTouch240 = {
                        val state = !(telemetryTracker?.isTouch240HzOn ?: false)
                        telemetryTracker?.isTouch240HzOn = state
                        Toast.makeText(this@OverlayService, if (state) "Touch Response: 240Hz Locked" else "Touch Response: Normal", Toast.LENGTH_SHORT).show()
                        telemetryTracker?.sampleMetrics()
                    },
                    onToggleFpsStabilizer = {
                        val state = !(telemetryTracker?.isFpsStabilizerOn ?: false)
                        telemetryTracker?.isFpsStabilizerOn = state
                        Toast.makeText(this@OverlayService, if (state) "FPS Frame Stabilizer: ON" else "FPS Frame Stabilizer: OFF", Toast.LENGTH_SHORT).show()
                        telemetryTracker?.sampleMetrics()
                    },
                    onToggleTorch = {
                        val current = telemetryTracker?.isTorchOn ?: false
                        val success = telemetryTracker?.setTorch(!current) ?: false
                        if (!success) {
                            Toast.makeText(this@OverlayService, "Flashlight unavailable", Toast.LENGTH_SHORT).show()
                        }
                        telemetryTracker?.sampleMetrics()
                    },
                    onToggleDnd = {
                        val state = !(telemetryTracker?.isDndActive ?: false)
                        telemetryTracker?.isDndActive = state
                        Toast.makeText(this@OverlayService, if (state) "Gaming Focus / DND: ACTIVE" else "Gaming Focus: OFF", Toast.LENGTH_SHORT).show()
                        telemetryTracker?.sampleMetrics()
                    },
                    onOpenGameLibrary = {
                        val intent = Intent(this@OverlayService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        toggleExpandState(false)
                    },
                    onCheckShizuku = {
                        val ready = telemetryTracker?.isShizukuAvailable ?: false
                        val msg = if (ready) "Shizuku Service: Terpasang di device" else "Shizuku belum terpasang. Install dari Play Store / GitHub."
                        Toast.makeText(this@OverlayService, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        composeView = view

        val params = createCollapsedParams()
        try {
            windowManager?.addView(view, params)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuat overlay: ${e.message}", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }

    private fun createCollapsedParams(): WindowManager.LayoutParams {
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = triggerX
            y = triggerY
        }
    }

    private fun createExpandedParams(): WindowManager.LayoutParams {
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }
    }

    private fun toggleExpandState(expand: Boolean) {
        if (isExpanded == expand) return
        isExpanded = expand

        val view = composeView ?: return
        val wm = windowManager ?: return

        try {
            val params = if (expand) createExpandedParams() else createCollapsedParams()
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    private fun updateTriggerPosition(dx: Float, dy: Float) {
        if (isExpanded) return
        triggerX += dx.roundToInt()
        triggerY += dy.roundToInt()

        // Clamp inside reasonable screen bounds
        triggerX = triggerX.coerceAtLeast(0)
        triggerY = triggerY.coerceAtLeast(60)

        val view = composeView ?: return
        val wm = windowManager ?: return

        try {
            val params = createCollapsedParams()
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    private fun updateBrightness(pct: Float) {
        val view = composeView ?: return
        val wm = windowManager ?: return
        try {
            val params = (view.layoutParams as? WindowManager.LayoutParams) ?: return
            params.screenBrightness = pct.coerceIn(0.05f, 1.0f)
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false

        mainHandler.removeCallbacks(periodicMetricsRunnable)

        telemetryTracker?.stop()
        telemetryTracker = null

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
