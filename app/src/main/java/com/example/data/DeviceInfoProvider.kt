package com.example.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import java.io.File
import java.net.InetAddress
import java.util.Locale

data class DeviceInfo(
    val deviceName: String,
    val androidVersion: String,
    val sdkVersion: String,
    val architecture: String,
    val manufacturer: String,
    val model: String,
    val cpuAbi: String,
    val screenResolution: String,
    val brand: String,
    val board: String,
    val hardware: String,
    val bootloader: String,
    val securityPatch: String,
    val appVersion: String
)

data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val chargingSource: String,
    val status: String,
    val health: String,
    val voltageMv: Int,
    val temperatureCelsius: Float,
    val technology: String
)

data class MemoryInfoData(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val usedRamMb: Long,
    val usedPercent: Int,
    val isLowMemory: Boolean
)

data class StorageInfoData(
    val totalStorageGb: Double,
    val freeStorageGb: Double,
    val usedStorageGb: Double,
    val usedPercent: Int
)

data class NetworkDiagnosticInfo(
    val isConnected: Boolean,
    val networkType: String,
    val isMetered: Boolean,
    val isInternetValidated: Boolean,
    val linkDownSpeedMbps: Int,
    val linkUpSpeedMbps: Int
)

object DeviceInfoProvider {

    private const val UNAVAILABLE = "Unavailable"

    /**
     * Reads real hardware and software specifications using official Android APIs.
     * No hardcoding or simulated values.
     */
    fun getDeviceInfo(context: Context): DeviceInfo {
        // 1. Device Name
        val deviceName = try {
            val globalName = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
            if (!globalName.isNullOrBlank()) {
                globalName
            } else {
                "${Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }} ${Build.MODEL}"
            }
        } catch (_: Exception) {
            Build.MODEL.ifEmpty { UNAVAILABLE }
        }

        // 2. Android Version
        val androidVersion = Build.VERSION.RELEASE.ifEmpty { UNAVAILABLE }

        // 3. SDK Version
        val sdkVersion = Build.VERSION.SDK_INT.toString()

        // 4. Architecture
        val architecture = if (Build.SUPPORTED_ABIS.isNotEmpty()) {
            Build.SUPPORTED_ABIS.joinToString(", ")
        } else {
            System.getProperty("os.arch") ?: UNAVAILABLE
        }

        // 5. Manufacturer
        val manufacturer = Build.MANUFACTURER.ifEmpty { UNAVAILABLE }
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        // 6. Model
        val model = Build.MODEL.ifEmpty { UNAVAILABLE }

        // 7. CPU ABI (Primary)
        val cpuAbi = if (Build.SUPPORTED_ABIS.isNotEmpty()) {
            Build.SUPPORTED_ABIS[0]
        } else {
            @Suppress("DEPRECATION")
            Build.CPU_ABI.ifEmpty { UNAVAILABLE }
        }

        // 8. Screen Resolution
        val screenResolution = getScreenResolution(context)

        // Additional Specs
        val brand = Build.BRAND.ifEmpty { UNAVAILABLE }
        val board = Build.BOARD.ifEmpty { UNAVAILABLE }
        val hardware = Build.HARDWARE.ifEmpty { UNAVAILABLE }
        val bootloader = Build.BOOTLOADER.ifEmpty { UNAVAILABLE }
        val securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH.ifEmpty { UNAVAILABLE }
        } else {
            UNAVAILABLE
        }

        // App Version
        val appVersion = getAppVersion(context)

        return DeviceInfo(
            deviceName = deviceName,
            androidVersion = androidVersion,
            sdkVersion = sdkVersion,
            architecture = architecture,
            manufacturer = manufacturer,
            model = model,
            cpuAbi = cpuAbi,
            screenResolution = screenResolution,
            brand = brand,
            board = board,
            hardware = hardware,
            bootloader = bootloader,
            securityPatch = securityPatch,
            appVersion = appVersion
        )
    }

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: Config.DEFAULT_VERSION_NAME
        } catch (_: Exception) {
            Config.DEFAULT_VERSION_NAME
        }
    }

    private fun getScreenResolution(context: Context): String {
        return try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            if (windowManager != null) {
                val metrics = DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(metrics)
                "${metrics.widthPixels} x ${metrics.heightPixels} px (${metrics.densityDpi} dpi)"
            } else {
                val displayMetrics = context.resources.displayMetrics
                "${displayMetrics.widthPixels} x ${displayMetrics.heightPixels} px"
            }
        } catch (_: Exception) {
            UNAVAILABLE
        }
    }

    fun getBatteryInfo(context: Context): BatteryInfo {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct: Int = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 0

            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val chargePlug: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val chargingSource = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Charger"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
                else -> if (isCharging) "Charging" else "On Battery"
            }

            val statusStr = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Fully Charged"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                else -> "Normal"
            }

            val healthInt: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
            val health = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Good"
            }

            val voltageMv = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
            val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val temperatureCelsius = tempTenths / 10f
            val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

            BatteryInfo(
                level = batteryPct,
                isCharging = isCharging,
                chargingSource = chargingSource,
                status = statusStr,
                health = health,
                voltageMv = voltageMv,
                temperatureCelsius = temperatureCelsius,
                technology = technology
            )
        } catch (_: Exception) {
            BatteryInfo(
                level = 0,
                isCharging = false,
                chargingSource = UNAVAILABLE,
                status = UNAVAILABLE,
                health = UNAVAILABLE,
                voltageMv = 0,
                temperatureCelsius = 0f,
                technology = UNAVAILABLE
            )
        }
    }

    fun getMemoryInfo(context: Context): MemoryInfoData {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)

            val totalMb = memInfo.totalMem / (1024 * 1024)
            val availMb = memInfo.availMem / (1024 * 1024)
            val usedMb = totalMb - availMb
            val percent = if (totalMb > 0) ((usedMb.toDouble() / totalMb) * 100).toInt() else 0

            MemoryInfoData(
                totalRamMb = totalMb,
                availableRamMb = availMb,
                usedRamMb = usedMb,
                usedPercent = percent,
                isLowMemory = memInfo.lowMemory
            )
        } catch (_: Exception) {
            MemoryInfoData(0, 0, 0, 0, false)
        }
    }

    fun getStorageInfo(): StorageInfoData {
        return try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - freeBytes

            val bytesInGb = 1024.0 * 1024.0 * 1024.0
            val totalGb = String.format(Locale.US, "%.1f", totalBytes / bytesInGb).toDouble()
            val freeGb = String.format(Locale.US, "%.1f", freeBytes / bytesInGb).toDouble()
            val usedGb = String.format(Locale.US, "%.1f", usedBytes / bytesInGb).toDouble()
            val percent = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes) * 100).toInt() else 0

            StorageInfoData(
                totalStorageGb = totalGb,
                freeStorageGb = freeGb,
                usedStorageGb = usedGb,
                usedPercent = percent
            )
        } catch (_: Exception) {
            StorageInfoData(0.0, 0.0, 0.0, 0)
        }
    }

    fun getNetworkDiagnostic(context: Context): NetworkDiagnosticInfo {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(activeNetwork)

            if (caps == null) {
                return NetworkDiagnosticInfo(
                    isConnected = false,
                    networkType = "Disconnected",
                    isMetered = false,
                    isInternetValidated = false,
                    linkDownSpeedMbps = 0,
                    linkUpSpeedMbps = 0
                )
            }

            val networkType = when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular Mobile"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN Active"
                else -> "Connected"
            }

            val isMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            val downSpeed = caps.linkDownstreamBandwidthKbps / 1000
            val upSpeed = caps.linkUpstreamBandwidthKbps / 1000

            NetworkDiagnosticInfo(
                isConnected = true,
                networkType = networkType,
                isMetered = isMetered,
                isInternetValidated = isValidated,
                linkDownSpeedMbps = downSpeed,
                linkUpSpeedMbps = upSpeed
            )
        } catch (_: Exception) {
            NetworkDiagnosticInfo(false, UNAVAILABLE, false, false, 0, 0)
        }
    }

    suspend fun pingHost(host: String = "8.8.8.8", timeoutMs: Int = 2000): Long {
        return try {
            val startTime = System.currentTimeMillis()
            val address = InetAddress.getByName(host)
            val reachable = address.isReachable(timeoutMs)
            val latency = System.currentTimeMillis() - startTime
            if (reachable) latency else -1L
        } catch (_: Exception) {
            -1L
        }
    }

    fun getDeviceSensors(context: Context): List<Sensor> {
        return try {
            val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            sm?.getSensorList(Sensor.TYPE_ALL) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun hasFlashlight(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return false
            for (id in cm.cameraIdList) {
                val chars = cm.getCameraCharacteristics(id)
                val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (flashAvailable) return true
            }
            false
        } catch (_: Exception) {
            false
        }
    }
}
