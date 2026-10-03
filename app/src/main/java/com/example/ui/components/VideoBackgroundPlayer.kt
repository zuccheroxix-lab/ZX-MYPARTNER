package com.example.ui.components

import android.content.res.AssetFileDescriptor
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R

/**
 * High-performance, crash-safe, full-screen video background player using native TextureView and MediaPlayer.
 * Plays 1000487036.mp4 in an endless loop, muted, center-cropped to fill 100% of the display.
 */
@Composable
fun VideoBackgroundPlayer(
    modifier: Modifier = Modifier,
    fallbackImageRes: Int = R.drawable.img_gojo_banner
) {
    val context = LocalContext.current
    var isVideoReady by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // High quality fallback/poster underneath the video
        Image(
            painter = painterResource(id = fallbackImageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (!hasError) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    TextureView(ctx).apply {
                        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                            private var mediaPlayer: MediaPlayer? = null

                            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                try {
                                    val s = Surface(surface)
                                    val mp = MediaPlayer().apply {
                                        setSurface(s)
                                        isLooping = true
                                        setVolume(0f, 0f) // strictly muted as required
                                        setOnPreparedListener { player ->
                                            isVideoReady = true
                                            adjustAspectRatio(width, height, player.videoWidth, player.videoHeight)
                                            try {
                                                player.start()
                                            } catch (_: Exception) {}
                                        }
                                        setOnErrorListener { _, _, _ ->
                                            hasError = true
                                            true
                                        }

                                        // 1. Try raw resource first
                                        var loaded = false
                                        try {
                                            val uri = Uri.parse("android.resource://" + ctx.packageName + "/" + R.raw.video_1000487036)
                                            setDataSource(ctx, uri)
                                            loaded = true
                                        } catch (_: Exception) {
                                            loaded = false
                                        }

                                        // 2. Fallback to asset 1000487036.mp4
                                        if (!loaded) {
                                            val afd: AssetFileDescriptor = ctx.assets.openFd("1000487036.mp4")
                                            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                            afd.close()
                                        }

                                        prepareAsync()
                                    }
                                    mediaPlayer = mp
                                    tag = mp
                                } catch (_: Exception) {
                                    hasError = true
                                }
                            }

                            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                                mediaPlayer?.let { player ->
                                    try {
                                        if (player.videoWidth > 0 && player.videoHeight > 0) {
                                            adjustAspectRatio(width, height, player.videoWidth, player.videoHeight)
                                        }
                                    } catch (_: Exception) {}
                                }
                            }

                            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                try {
                                    mediaPlayer?.stop()
                                    mediaPlayer?.release()
                                    mediaPlayer = null
                                } catch (_: Exception) {}
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

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
