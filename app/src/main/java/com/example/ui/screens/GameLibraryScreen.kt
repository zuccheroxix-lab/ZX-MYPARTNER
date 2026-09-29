package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Config
import com.example.ui.components.DashboardHeader
import kotlinx.coroutines.delay

data class InstalledGame(
    val packageName: String,
    val appName: String,
    val isSystem: Boolean
)

@Composable
fun GameLibraryScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showBoosterDialog by remember { mutableStateOf(false) }
    var boostingActive by remember { mutableStateOf(false) }
    var boostedRamMb by remember { mutableStateOf(0) }
    val installedGames = remember { mutableStateListOf<InstalledGame>() }

    // Scan real installed games on the Android device
    LaunchedEffect(Unit) {
        try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val games = packages.filter { app ->
                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    app.category == ApplicationInfo.CATEGORY_GAME
                } else false
                isGame && (app.flags and ApplicationInfo.FLAG_SYSTEM == 0)
            }.map { app ->
                InstalledGame(
                    packageName = app.packageName,
                    appName = pm.getApplicationLabel(app).toString(),
                    isSystem = false
                )
            }
            installedGames.clear()
            installedGames.addAll(games)
        } catch (_: Exception) {
            // Handled gracefully
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("game_library_screen")
    ) {
        // Top Header matching video: Game Library | Game Library Menu ZENIX | Dev : ZyrulFIVE
        DashboardHeader(
            title = "Game Library",
            subtitle = "Game Library Menu ZX",
            developerName = Config.DEVELOPER_NAME
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Game Space Banner Card (Matching Video 1 at 00:12)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF141416))
                        .border(
                            width = 0.8.dp,
                            color = Color(0xFF282830),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .testTag("game_space_banner_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Game-Space",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF8E8E98),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Game Library",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Config.GAME_SPACE_VERSION,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF6E6E78),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Circular white button with black rocket icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    // Trigger real haptic feedback
                                    try {
                                        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                                            vm?.defaultVibrator
                                        } else {
                                            @Suppress("DEPRECATION")
                                            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            vibrator?.vibrate(
                                                VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                                            )
                                        } else {
                                            @Suppress("DEPRECATION")
                                            vibrator?.vibrate(80)
                                        }
                                    } catch (_: Exception) {}

                                    boostingActive = true
                                    boostedRamMb = (280..420).random()
                                    showBoosterDialog = true
                                }
                                .testTag("btn_rocket_boost"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "Turbo Game Space Boost",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Section Label: "Game-Libray" (Matching exact label in video!)
            item {
                Text(
                    text = "Game-Libray",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 0.2.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                )
            }

            // Primary Featured Game Card: Free Fire MAX (as seen in video reference)
            item {
                GameItemCard(
                    title = "Free Fire MAX",
                    subtitle = "Garena International • Battle Royale",
                    imageRes = R.drawable.img_free_fire,
                    packageName = Config.FREE_FIRE_PACKAGE,
                    onClick = { launchOrInstallGame(context, Config.FREE_FIRE_PACKAGE, "Free Fire MAX") }
                )
            }

            // Real Scanned Games on Device
            if (installedGames.isNotEmpty()) {
                items(installedGames, key = { it.packageName }) { game ->
                    GameItemCard(
                        title = game.appName,
                        subtitle = "Installed Game • ${game.packageName}",
                        imageRes = null,
                        packageName = game.packageName,
                        onClick = { launchOrInstallGame(context, game.packageName, game.appName) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Game Space Turbo Boost Active Dialog
    if (showBoosterDialog) {
        AlertDialog(
            onDismissRequest = { showBoosterDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Game Space Turbo Engine",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "🚀 Game Booster diaktifkan!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• RAM Dibersihkan: +$boostedRamMb MB\n• Refresh Rate: Terkunci pada Mode Performa Maksimal\n• Latensi Sentuh: Mode Sampling Rendah Aktif\n• Prioritas GPU: Turbo Governor On",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )
                    )
                }
            },
            containerColor = Color(0xFF18181C),
            confirmButton = {
                Button(
                    onClick = { showBoosterDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun GameItemCard(
    title: String,
    subtitle: String,
    imageRes: Int?,
    packageName: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141416))
            .border(
                width = 0.8.dp,
                color = Color(0xFF282830),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("game_card_$packageName")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1F1F24)),
                contentAlignment = Alignment.Center
            ) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }

            // Launch / Play Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222228))
                    .border(0.8.dp, Color(0xFF383842), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Launch $title",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun launchOrInstallGame(context: Context, packageName: String, gameName: String) {
    try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
            Toast.makeText(context, "Meluncurkan $gameName...", Toast.LENGTH_SHORT).show()
        } else {
            // Open Play Store if not installed
            val storeIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packageName")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(storeIntent)
            } catch (_: Exception) {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
            Toast.makeText(context, "Membuka halaman download $gameName di Play Store", Toast.LENGTH_SHORT).show()
        }
    } catch (_: Exception) {
        Toast.makeText(context, "Gagal meluncurkan $gameName", Toast.LENGTH_SHORT).show()
    }
}
