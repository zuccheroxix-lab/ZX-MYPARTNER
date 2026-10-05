package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.io.File

data class CacheStats(
    val internalCacheBytes: Long,
    val externalCacheBytes: Long,
    val codeCacheBytes: Long,
    val totalCacheBytes: Long,
    val formattedTotal: String
)

/**
 * Handles safe, non-destructive app cache calculation and cleaning.
 * Uses official Android file APIs.
 * Does NOT delete app data, accounts, logins, photos, videos, or user files.
 */
object CacheManager {

    fun getAppCacheStats(context: Context): CacheStats {
        val internal = getDirectorySize(context.cacheDir)
        val external = context.externalCacheDir?.let { getDirectorySize(it) } ?: 0L
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getDirectorySize(context.codeCacheDir)
        } else {
            0L
        }
        val total = internal + external + code

        return CacheStats(
            internalCacheBytes = internal,
            externalCacheBytes = external,
            codeCacheBytes = code,
            totalCacheBytes = total,
            formattedTotal = formatBytes(total)
        )
    }

    /**
     * Safely clears temporary cache files of DYNIMETIZE ZX.
     * Leaves core files, databases, shared_prefs, and authentication intact.
     * Returns total bytes freed.
     */
    fun clearAppCache(context: Context): Long {
        val before = getAppCacheStats(context).totalCacheBytes

        cleanDirectoryChildren(context.cacheDir)
        context.externalCacheDir?.let { cleanDirectoryChildren(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cleanDirectoryChildren(context.codeCacheDir)
        }

        val after = getAppCacheStats(context).totalCacheBytes
        return (before - after).coerceAtLeast(0L)
    }

    private fun getDirectorySize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val children = dir.listFiles() ?: return 0L
        for (child in children) {
            size += if (child.isDirectory) {
                getDirectorySize(child)
            } else {
                child.length()
            }
        }
        return size
    }

    private fun cleanDirectoryChildren(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return false
        var success = true
        val children = dir.listFiles() ?: return true
        for (child in children) {
            if (child.isDirectory) {
                cleanDirectoryChildren(child)
            }
            if (!child.delete()) {
                success = false
            }
        }
        return success
    }

    fun openSystemStorageSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun openAppDetailsSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes.toDouble() / (1024 * 1024 * 1024))
            bytes >= 1024 * 1024 -> "%.2f MB".format(bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> "%.1f KB".format(bytes.toDouble() / 1024)
            else -> "$bytes Bytes"
        }
    }
}
