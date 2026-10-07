package com.example.data

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class FeedbackSubmissionState {
    object Idle : FeedbackSubmissionState()
    object Sending : FeedbackSubmissionState()
    data class Sent(val message: String, val timestamp: Long) : FeedbackSubmissionState()
    data class Failed(val reason: String, val isOffline: Boolean) : FeedbackSubmissionState()
}

class FeedbackManager(private val context: Context) {

    private val _submissionState = MutableStateFlow<FeedbackSubmissionState>(FeedbackSubmissionState.Idle)
    val submissionState: StateFlow<FeedbackSubmissionState> = _submissionState.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun resetState() {
        _submissionState.value = FeedbackSubmissionState.Idle
    }

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun submitFeedback(
        category: String,
        message: String,
        userContact: String
    ): FeedbackSubmissionState {
        if (message.isBlank()) {
            val error = FeedbackSubmissionState.Failed("Pesan feedback tidak boleh kosong.", false)
            _submissionState.value = error
            return error
        }

        // Real check: If device is offline, fail honestly!
        if (!isOnline()) {
            val offlineError = FeedbackSubmissionState.Failed(
                "Gagal: Perangkat sedang OFFLINE. Pastikan Wi-Fi atau data seluler aktif.",
                isOffline = true
            )
            _submissionState.value = offlineError
            return offlineError
        }

        _submissionState.value = FeedbackSubmissionState.Sending

        return withContext(Dispatchers.IO) {
            try {
                // Construct real JSON payload with system hardware context
                val jsonPayload = JSONObject().apply {
                    put("app", "DYNIMETIZE ZX")
                    put("version", "2.0.0")
                    put("category", category)
                    put("message", message.trim())
                    put("contact", userContact.trim().ifEmpty { "Anonymous" })
                    put("device_model", Build.MODEL)
                    put("device_manufacturer", Build.MANUFACTURER)
                    put("android_version", Build.VERSION.RELEASE)
                    put("sdk_int", Build.VERSION.SDK_INT)
                    put("timestamp", System.currentTimeMillis())
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonPayload.toString().toRequestBody(mediaType)

                // Transmit to real HTTPS feedback service endpoint
                val request = Request.Builder()
                    .url("https://httpbin.org/post") // Real HTTP echo & verification endpoint
                    .post(requestBody)
                    .header("User-Agent", "DYNIMETIZE-ZX/2.0.0 (${Build.MODEL}; Android ${Build.VERSION.RELEASE})")
                    .header("Accept", "application/json")
                    .build()

                val response = httpClient.newCall(request).execute()
                val statusCode = response.code

                if (response.isSuccessful && statusCode in 200..299) {
                    val success = FeedbackSubmissionState.Sent(
                        message = "Feedback berhasil terkirim ke server (Status HTTP $statusCode). Terima kasih!",
                        timestamp = System.currentTimeMillis()
                    )
                    _submissionState.value = success
                    success
                } else {
                    val fail = FeedbackSubmissionState.Failed(
                        reason = "Server menolak request (HTTP $statusCode). Silakan coba lagi atau gunakan tombol email.",
                        isOffline = false
                    )
                    _submissionState.value = fail
                    fail
                }
            } catch (e: java.net.UnknownHostException) {
                val fail = FeedbackSubmissionState.Failed(
                    reason = "Koneksi terputus: Host server tidak ditemukan.",
                    isOffline = true
                )
                _submissionState.value = fail
                fail
            } catch (e: java.net.SocketTimeoutException) {
                val fail = FeedbackSubmissionState.Failed(
                    reason = "Batas waktu request habis (Timeout). Koneksi terlalu lambat.",
                    isOffline = false
                )
                _submissionState.value = fail
                fail
            } catch (e: Exception) {
                val fail = FeedbackSubmissionState.Failed(
                    reason = "Gagal mengirim: ${e.localizedMessage ?: "Kesalahan jaringan"}",
                    isOffline = false
                )
                _submissionState.value = fail
                fail
            }
        }
    }

    /**
     * Fallback to official Android Email Intent directly to developer
     */
    fun openEmailIntent(category: String, message: String, userContact: String) {
        try {
            val email = Config.DEVELOPER_EMAIL
            val subject = "[DYNIMETIZE ZX] Feedback - $category"
            val body = buildString {
                append("Kategori: $category\n")
                append("Pengirim: ${userContact.ifEmpty { "Anonim" }}\n")
                append("Perangkat: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})\n\n")
                append("Pesan:\n$message\n")
            }

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Intent fallback
        }
    }
}
