package com.example.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.Config
import com.example.data.SettingsManager
import com.example.ui.components.DashboardHeader
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GameItemEntry(
    val packageName: String,
    val appName: String,
    val iconBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    val isGame: Boolean,
    val isCustom: Boolean
)

@Composable
fun GameLibraryScreen(
    onNavigateToPage: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = LocalAccentColor.current
    val settingsManager = remember { SettingsManager(context) }
    val customGamePackages by settingsManager.customGamePackages.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    val gamesList = remember { mutableStateListOf<GameItemEntry>() }
    val allInstalledList = remember { mutableStateListOf<GameItemEntry>() }
    var isLoading by remember { mutableStateOf(true) }

    suspend fun loadApps() {
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)

                val tempAll = mutableListOf<GameItemEntry>()
                val tempGames = mutableListOf<GameItemEntry>()

                val gameKeywords = listOf(
                    "game", "freefire", "dts", "pubg", "mobilelegends", "genshin",
                    "roblox", "cod", "callofduty", "fifa", "efootball", "minecraft",
                    "stumbleguys", "asphalt", "apex", "honkai", "clash"
                )

                for (info in resolveInfos) {
                    val pkg = info.activityInfo.packageName
                    if (pkg == context.packageName) continue
                    val label = info.loadLabel(pm).toString()
                    val icon = try {
                        info.loadIcon(pm).toBitmap(64, 64).asImageBitmap()
                    } catch (_: Exception) { null }

                    val isCategory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            info.activityInfo.applicationInfo.category == ApplicationInfo.CATEGORY_GAME
                        } catch (_: Exception) { false }
                    } else false

                    val isKeyword = gameKeywords.any { pkg.lowercase().contains(it) || label.lowercase().contains(it) }
                    val isCustom = customGamePackages.contains(pkg)
                    val isGame = isCategory || isKeyword || isCustom

                    val entry = GameItemEntry(pkg, label, icon, isGame, isCustom)
                    tempAll.add(entry)
                    if (isGame) tempGames.add(entry)
                }

                withContext(Dispatchers.Main) {
                    allInstalledList.clear()
                    allInstalledList.addAll(tempAll.sortedBy { it.appName.lowercase() })
                    gamesList.clear()
                    gamesList.addAll(tempGames.sortedBy { it.appName.lowercase() })
                    isLoading = false
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    LaunchedEffect(customGamePackages) {
        loadApps()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0D))
            .testTag("game_library_screen")
    ) {
        // Header
        DashboardHeader(
            title = "Game Space",
            subtitle = "Daftar & Peluncur Game Terpasang",
            developerName = Config.DEVELOPER_NAME
        )

        // Navigation shortcuts (Horizontal Page Jump)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToPage(1) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("← Smooth", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(0) },
                modifier = Modifier.weight(0.9f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Beranda", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(3) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Overlay →", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF13151D))
                .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Game Terpasang", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${gamesList.size} Game terdeteksi siap diluncurkan", color = Color(0xFF8E8E98), fontSize = 11.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Games List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (gamesList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF13151D))
                            .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF6E7285), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ada game terdeteksi otomatis", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap tombol '+ Tambah' di atas untuk memasukkan game atau aplikasi favorit.", color = Color(0xFF8E8E98), fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(gamesList, key = { it.packageName }) { game ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF13151D))
                            .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                if (game.iconBitmap != null) {
                                    Image(
                                        bitmap = game.iconBitmap,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(accentColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(game.appName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(game.packageName, color = Color(0xFF8E8E98), fontSize = 10.sp, maxLines = 1)
                                }
                            }

                            Button(
                                onClick = {
                                    try {
                                        val launchIntent = context.packageManager.getLaunchIntentForPackage(game.packageName)
                                        if (launchIntent != null) {
                                            context.startActivity(launchIntent)
                                        } else {
                                            Toast.makeText(context, "Tidak dapat membuka game", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buka", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }

    // Modal: Add Custom Game
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Pilih Aplikasi / Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            },
            text = {
                Column(modifier = Modifier.height(340.dp).verticalScroll(rememberScrollState())) {
                    Text("Pilih aplikasi yang ingin ditambahkan ke Game Space:", color = Color(0xFF8E8E98), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    allInstalledList.forEach { appItem ->
                        val isAdded = customGamePackages.contains(appItem.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { settingsManager.toggleCustomGamePackage(appItem.packageName) }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                if (appItem.iconBitmap != null) {
                                    Image(bitmap = appItem.iconBitmap, contentDescription = null, modifier = Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)))
                                } else {
                                    Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(Color(0xFF262838)))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(appItem.appName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(appItem.packageName, color = Color(0xFF8E8E98), fontSize = 9.sp, maxLines = 1)
                                }
                            }
                            Switch(
                                checked = isAdded,
                                onCheckedChange = { settingsManager.toggleCustomGamePackage(appItem.packageName) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFF161822),
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Selesai", color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
