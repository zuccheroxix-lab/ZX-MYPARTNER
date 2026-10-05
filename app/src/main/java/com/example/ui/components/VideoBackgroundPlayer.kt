package com.example.ui.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * High-performance, crash-safe, full-screen video background player using native TextureView and MediaPlayer.
 * Plays 1000487036.mp4 in an endless loop, muted, center-cropped to fill 100% of the display.
 * Includes defensive state management to prevent native MediaPlayer state errors.
 */
@Composable
fun VideoBackgroundPlayer(
    modifier: Modifier = Modifier,
    fallbackImageRes: Int = R.drawable.img_gojo_banner
) {
    val context = LocalContext.current
    var isVideoReady by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var videoFileReady by remember { mutableStateOf(false) }

    // Pre-extract video file in background thread to avoid ANR and ensure safe direct file descriptor
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val file = getOrExtractVideoFile(context)
            if (file != null && file.exists() && file.length() > 0) {
                withContext(Dispatchers.Main) {
                    videoFileReady = true
                }
            } else {
                withContext(Dispatchers.Main) {
                    hasError = true
                }
            }
        }
    }

    val videoAlpha by animateFloatAsState(
        targetValue = if (isVideoReady && !hasError) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "VideoBackgroundAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // High quality fallback/poster underneath the video - always visible until video is ready
        Image(
            painter = painterResource(id = fallbackImageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (!hasError && videoFileReady) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(videoAlpha),
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            private var mediaPlayer: MediaPlayer? = null

                            override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                                if (hasError) return
                                try {
                                    val s = Surface(surfaceTexture)
                                    val mp = MediaPlayer()
                                    mediaPlayer = mp

                                    mp.setSurface(s)
                                    mp.isLooping = true
                                    mp.setVolume(0f, 0f) // strictly muted

                                    mp.setOnPreparedListener { player ->
                                        try {
                                            adjustAspectRatio(width, height, player.videoWidth, player.videoHeight)
                                            player.start()
                                            isVideoReady = true
                                        } catch (_: Exception) {
                                            hasError = true
                                        }
                                    }

                                    mp.setOnErrorListener { player, _, _ ->
                                        hasError = true
                                        try {
                                            player.reset()
                                            player.release()
                                        } catch (_: Exception) {}
                                        if (mediaPlayer === player) {
                                            mediaPlayer = null
                                        }
                                        true // consume error to prevent native framework crash
                                    }

                                    val videoFile = getOrExtractVideoFile(ctx)
                                    if (videoFile != null && videoFile.exists()) {
                                        mp.setDataSource(videoFile.absolutePath)
                                        mp.prepareAsync()
                                    } else {
                                        hasError = true
                                        safeReleasePlayer(mp)
                                        mediaPlayer = null
                                    }
                                } catch (_: Exception) {
                                    hasError = true
                                    safeReleasePlayer(mediaPlayer)
                                    mediaPlayer = null
                                }
                            }

                            override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                                mediaPlayer?.let { player ->
                                    try {
                                        if (player.videoWidth > 0 && player.videoHeight > 0) {
                                            adjustAspectRatio(width, height, player.videoWidth, player.videoHeight)
                                        }
                                    } catch (_: Exception) {}
                                }
                            }

                            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                                safeReleasePlayer(mediaPlayer)
                                mediaPlayer = null
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}

                            private fun adjustAspectRatio(viewWidth: Int, viewHeight: Int, videoWidth: Int, videoHeight: Int) {
                                if (viewWidth <= 0 || viewHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) return
                                val viewRatio = viewWidth.toFloat() / viewHeight
                                val videoRatio = videoWidth.toFloat() / videoHeight
                                val scaleX: Float
                                val scaleY: Float
                                if (videoRatio > viewRatio) {
                                    scaleX = videoRatio / viewRatio
                                    scaleY = 1.0f
                                } else {
                                    scaleX = 1.0f
                                    scaleY = viewRatio / videoRatio
                                }
                                val matrix = Matrix()
                                matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                                setTransform(matrix)
                            }
                        }
                    }
                },
                update = { /* no dynamic update needed */ }
            )
        }
    }
}

/**
 * Safely releases MediaPlayer without triggering "stop called in state 0" or error (-38, 0).
 */
private fun safeReleasePlayer(player: MediaPlayer?) {
    if (player == null) return
    try {
        try {
            if (player.isPlaying) {
                player.stop()
            }
        } catch (_: Exception) {}
        try {
            player.reset()
        } catch (_: Exception) {}
        try {
            player.release()
        } catch (_: Exception) {}
    } catch (_: Exception) {}
}

/**
 * Extracts raw/asset video into internal cache for rock-solid MediaPlayer playback.
 */
private fun getOrExtractVideoFile(context: Context): File? {
    return try {
        val target = File(context.cacheDir, "bg_video_1000487036.mp4")
        if (target.exists() && target.length() >= 2_000_000L) {
            return target
        }
        val temp = File(context.cacheDir, "bg_video_1000487036.tmp")
        val inputStream = try {
            context.resources.openRawResource(R.raw.video_1000487036)
        } catch (_: Exception) {
            context.assets.open("1000487036.mp4")
        }
        inputStream.use { input ->
            FileOutputStream(temp).use { output ->
                input.copyTo(output)
            }
        }
        if (temp.renameTo(target) || target.exists()) {
            target
        } else {
            temp
        }
    } catch (_: Exception) {
        null
    }
}
