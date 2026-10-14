package com.example.data

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class ShellPrivilegeStatus(
    val isRootAvailable: Boolean,
    val isShizukuInstalled: Boolean,
    val hasWriteSecureSettings: Boolean,
    val shellTypeDescription: String
)

data class TerminalCommandResult(
    val command: String,
    val output: String,
    val error: String,
    val exitCode: Int,
    val executionTimeMs: Long,
    val isSuccess: Boolean,
    val privilegeLevel: String
)

data class PresetCommand(
    val id: String,
    val name: String,
    val command: String,
    val description: String,
    val requiresElevation: Boolean
)

class ExecTermManager(private val context: Context) {

    fun getPrivilegeStatus(): ShellPrivilegeStatus {
        val root = checkRootAvailable()
        val shizuku = checkShizukuInstalled()
        val secure = checkSecureSettingsPermission()

        val desc = when {
            root -> "Root SuperUser (UID 0) • Hak akses sistem penuh"
            shizuku -> "Shizuku ADB Bridge • Siap hak istimewa ADB"
            secure -> "Izin WRITE_SECURE_SETTINGS aktif via ADB"
            else -> "Shell Standar Sandbox (Non-Root / Unprivileged)"
        }

        return ShellPrivilegeStatus(
            isRootAvailable = root,
            isShizukuInstalled = shizuku,
            hasWriteSecureSettings = secure,
            shellTypeDescription = desc
        )
    }

    fun checkRootAvailable(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/system/sd/xbin/su"
        )
        for (p in paths) {
            try {
                if (File(p).exists()) return true
            } catch (_: Exception) {}
        }
        return false
    }

    fun checkShizukuInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun checkSecureSettingsPermission(): Boolean {
        return try {
            context.checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    fun getPresetCommands(): List<PresetCommand> {
        return listOf(
            PresetCommand(
                id = "wm_size",
                name = "Cek Resolusi Layar",
                command = "wm size",
                description = "Menampilkan resolusi fisik layar dan override aktif.",
                requiresElevation = false
            ),
            PresetCommand(
                id = "wm_density",
                name = "Cek DPI Layar",
                command = "wm density",
                description = "Menampilkan physical density dan overridden DPI sistem.",
                requiresElevation = false
            ),
            PresetCommand(
                id = "dumpsys_battery",
                name = "Diagnostik Baterai",
                command = "dumpsys battery",
                description = "Membaca telemetry real-time daya, voltase, status, dan suhu baterai.",
                requiresElevation = false
            ),
            PresetCommand(
                id = "dumpsys_mem",
                name = "Diagnostik Memori Proses",
                command = "dumpsys meminfo ${context.packageName}",
                description = "Membaca heap PSS/RSS dan alokasi RAM proses aplikasi saat ini.",
                requiresElevation = false
            ),
            PresetCommand(
                id = "device_model",
                name = "Model Perangkat",
                command = "getprop ro.product.model",
                description = "Mengambil nama model perangkat dari build prop Android.",
                requiresElevation = false
            ),
            PresetCommand(
                id = "android_ver",
                name = "Versi Android OS",
                command = "getprop ro.build.version.release",
                description = "Mengambil versi rilis Android dari build prop.",
                requiresElevation = false
            ),
            PresetCommand(
                id = "ping_dns",
                name = "Uji Latensi Jaringan",
                command = "ping -c 3 8.8.8.8",
                description = "Menghitung Round Trip Time (RTT) ICMP ke DNS Google.",
                requiresElevation = false
            ),
            PresetCommand(
                id = "recent_logs",
                name = "Buffer Logcat Terkini",
                command = "logcat -d -t 20",
                description = "Membaca 20 baris log buffer sistem terbaru.",
                requiresElevation = false
            ),
            PresetCommand(
                id = "reset_wm_size",
                name = "Reset Resolusi (wm size reset)",
                command = "wm size reset",
                description = "Mengembalikan resolusi layar ke nilai default bawaan pabrik.",
                requiresElevation = true
            ),
            PresetCommand(
                id = "reset_wm_density",
                name = "Reset DPI (wm density reset)",
                command = "wm density reset",
                description = "Mengembalikan densitas DPI layar ke nilai bawaan pabrik.",
                requiresElevation = true
            )
        )
    }

    suspend fun executeCommand(inputCommand: String): TerminalCommandResult = withContext(Dispatchers.IO) {
        val trimmed = inputCommand.trim()
        val startTime = System.currentTimeMillis()

        // 1. Safety validation: Block destructive commands
        val dangerousTokens = listOf("rm -rf", "mkfs", "dd if=", ":(){ :|:& };:", "wipe", "format", "reboot bootloader")
        for (token in dangerousTokens) {
            if (trimmed.contains(token, ignoreCase = true)) {
                return@withContext TerminalCommandResult(
                    command = trimmed,
                    output = "",
                    error = "Perintah diblokir oleh sistem keamanan DYNIMETIZE ZX demi keselamatan integritas perangkat Anda ($token).",
                    exitCode = 126,
                    executionTimeMs = System.currentTimeMillis() - startTime,
                    isSuccess = false,
                    privilegeLevel = "SECURITY_BLOCK"
                )
            }
        }

        // 2. Select execution binary: root (su) if available, otherwise sandbox shell (sh)
        val isRoot = checkRootAvailable()
        val shellBinary = if (isRoot) "su" else "sh"
        val privilegeLabel = if (isRoot) "ROOT (su)" else "UNPRIVILEGED (sh)"

        try {
            val process = ProcessBuilder(shellBinary, "-c", trimmed)
                .redirectErrorStream(false)
                .start()

            val stdoutBuilder = StringBuilder()
            val stderrBuilder = StringBuilder()

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            var line: String?
            while (stdoutReader.readLine().also { line = it } != null) {
                stdoutBuilder.append(line).append("\n")
            }
            while (stderrReader.readLine().also { line = it } != null) {
                stderrBuilder.append(line).append("\n")
            }

            val finished = process.waitFor(6, TimeUnit.SECONDS)
            if (!finished) {
                process.destroy()
                return@withContext TerminalCommandResult(
                    command = trimmed,
                    output = stdoutBuilder.toString().trim(),
                    error = "Proses melebihi batas waktu (Timeout 6s) dan dihentikan secara otomatis.",
                    exitCode = 124,
                    executionTimeMs = System.currentTimeMillis() - startTime,
                    isSuccess = false,
                    privilegeLevel = privilegeLabel
                )
            }

            val exitVal = process.exitValue()
            val elapsed = System.currentTimeMillis() - startTime

            val stdOutStr = stdoutBuilder.toString().trim()
            val stdErrStr = stderrBuilder.toString().trim()

            // If exit code is non-zero and user ran a privileged wm command on standard shell, give clear honest guidance
            val adjustedError = if (exitVal != 0 && (trimmed.startsWith("wm ") || trimmed.startsWith("pm "))) {
                if (stdErrStr.isNotEmpty()) {
                    "$stdErrStr\n[Info] Android membatasi perintah ini untuk aplikasi standar tanpa ADB / Shizuku / Root. Silakan gunakan Shizuku atau jalankan via 'adb shell $trimmed' dari PC."
                } else {
                    "Exit Code $exitVal: Perintah memerlukan izin istimewa ADB atau Root."
                }
            } else {
                stdErrStr
            }

            TerminalCommandResult(
                command = trimmed,
                output = stdOutStr,
                error = adjustedError,
                exitCode = exitVal,
                executionTimeMs = elapsed,
                isSuccess = exitVal == 0,
                privilegeLevel = privilegeLabel
            )
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            TerminalCommandResult(
                command = trimmed,
                output = "",
                error = "Kegagalan eksekusi proses shell: ${e.localizedMessage ?: e.javaClass.simpleName}",
                exitCode = -1,
                executionTimeMs = elapsed,
                isSuccess = false,
                privilegeLevel = privilegeLabel
            )
        }
    }
}
