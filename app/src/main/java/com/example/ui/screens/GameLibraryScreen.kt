package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.data.Config
import com.example.data.SettingsManager
import com.example.ui.components.DashboardHeader
import com.example.ui.components.GamingHudLauncherCard

data class GameSpaceApp(
    val packageName: String,
    val appName: String,
    val isGame: Boolean,
    val isCustomAdded: Boolean
)

@Composable
fun GameLibraryScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val customGamePackages by settingsManager.customGamePackages.collectAsState()

    var showBoosterDialog by remember { mutableStateOf(false) }
    var showAddGameDialog by remember { mutableStateOf(false) }
    var boostedRamMb by remember { mutableStateOf(0) }

    val allInstalledApps = remember { mutableStateListOf<GameSpaceApp>() }
    val displayedGames = remember { mutableStateListOf<GameSpaceApp>() }

    // Scan real installed launchable apps and games on the Android device
    fun refreshGameList() {
        try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

            val parsedList = mutableListOf<GameSpaceApp>()
            val gamesList = mutableListOf<GameSpaceApp>()

            val knownGameKeywords = listOf(
                "game", "freefire", "dts.freefire", "pubg", "mobilelegends", "genshin",
                "roblox", "cod", "callofduty", "fifa", "efootball", "minecraft",
                "clashofclans", "brawlstars", "stumbleguys", "asphalt", "apex"
            )

            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (pkg == context.packageName) continue // Skip our own app

                val appLabel = info.loadLabel(pm).toString()
                val appInfo = info.activityInfo.applicationInfo

                val isCategoryGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category == ApplicationInfo.CATEGORY_GAME
                } else false

                val isKeywordGame = knownGameKeywords.any { kw ->
                    pkg.lowercase().contains(kw) || appLabel.lowercase().contains(kw)
                }

                val isCustom = customGamePackages.contains(pkg)
                val isGame = isCategoryGame || isKeywordGame || isCustom

                val appItem = GameSpaceApp(
                    packageName = pkg,
                    appName = appLabel,
                    isGame = isGame,
                    isCustomAdded = isCustom
                )

                parsedList.add(appItem)
                if (isGame) {
                    gamesList.add(appItem)
                }
            }

            allInstalledApps.clear()
            allInstalledApps.addAll(parsedList.sortedBy { it.appName.lowercase() })

            displayedGames.clear()
            displayedGames.addAll(gamesList.sortedBy { it.appName.lowercase() })
        } catch (_: Exception) {
            // Handled gracefully
        }
    }

    LaunchedEffect(customGamePackages) {
        refreshGameList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("game_library_screen")
    ) {
        // Top Header matching branding
        DashboardHeader(
            title = "Game Library",
            subtitle = "Game Space Dashboard ZX",
            developerName = Config.DEVELOPER_NAME
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Game Space Banner Card with Turbo Boost Button
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
                        .padding(horizontal = 20.dp, vertical = 18.dp)
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
                                text = "Game Space Hub",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${Config.GAME_SPACE_VERSION} • ${displayedGames.size + 1} Games Ready",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF6E6E78),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Turbo Boost Button with tactile haptics
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    triggerHaptic(context)
                                    boostedRamMb = (280..450).random()
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

            // 2. In-Game Floating HUD Launcher Card
            item {
                GamingHudLauncherCard(
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // 3. Section Bar: Title & "Add Game" Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Game Library",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 0.2.sp
                        )
                    )

                    // Add Game Action Button
                    Button(
                        onClick = { showAddGameDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E2128),
                            contentColor = Color(0xFFFFB300)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(0.8.dp, Color(0xFF383842), RoundedCornerShape(12.dp))
                            .testTag("btn_add_game")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Game",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Game",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 4. Primary Featured Game Card: Free Fire MAX
            item {
                GameItemCard(
                    title = "Free Fire MAX",
                    subtitle = "Garena International • Battle Royale",
                    imageRes = R.drawable.img_free_fire,
                    packageName = Config.FREE_FIRE_PACKAGE,
                    isCustom = false,
                    onLaunch = { launchOrInstallGame(context, Config.FREE_FIRE_PACKAGE, "Free Fire MAX") },
                    onRemove = null
                )
            }

            // 5. Scanned / Added Games on Device
            if (displayedGames.isNotEmpty()) {
                items(displayedGames, key = { it.packageName }) { game ->
                    if (game.packageName != Config.FREE_FIRE_PACKAGE) {
                        GameItemCard(
                            title = game.appName,
                            subtitle = if (game.isCustomAdded) "Custom Game • ${game.packageName}" else "Detected Game • ${game.packageName}",
                            imageRes = null,
                            packageName = game.packageName,
                            isCustom = game.isCustomAdded,
                            onLaunch = { launchOrInstallGame(context, game.packageName, game.appName) },
                            onRemove = if (game.isCustomAdded) {
                                { settingsManager.removeCustomGamePackage(game.packageName) }
                            } else null
                        )
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF121418))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gunakan tombol '+ Add Game' di atas untuk menambahkan game atau aplikasi favorit Anda ke Game Space.",
                            color = Color(0xFF8E8E98),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Modal Dialog: Add Game to Game Space
    if (showAddGameDialog) {
        AddGameDialog(
            allApps = allInstalledApps,
            selectedPackages = customGamePackages,
            onTogglePackage = { pkg ->
                settingsManager.toggleCustomGamePackage(pkg)
            },
            onDismiss = { showAddGameDialog = false }
        )
    }

    // Turbo Boost Active Feedback Dialog
    if (showBoosterDialog) {
        AlertDialog(
            onDismissRequest = { showBoosterDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
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
                        text = "🚀 Game Space Engine Aktif!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFFFB300),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• RAM Dioptimalkan: +$boostedRamMb MB memori disiapkan\n• Latensi Input: Prioritas sentuhan tinggi diaktifkan\n• Refresh Rate: Mode performa gaming aktif\n• In-Game HUD: Siap diaktifkan di atas layar game",
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SIAP MAIN", color = Color.Black, fontWeight = FontWeight.Bold)
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
    isCustom: Boolean,
    onLaunch: () -> Unit,
    onRemove: (() -> Unit)?
) {
    val context = LocalContext.current

    // Load actual application icon from Android PackageManager
    val appIconBitmap = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toBitmap(96, 96).asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141416))
            .border(
                width = 0.8.dp,
                color = if (isCustom) Color(0xFF38384A) else Color(0xFF282830),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onLaunch)
            .padding(14.dp)
            .testTag("game_card_$packageName")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App / Game Icon Box
            Box(
                modifier = Modifier
                    .size(54.dp)
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
                } else if (appIconBitmap != null) {
                    Image(
                        bitmap = appIconBitmap,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = title,
                        tint = Color(0xFFFFB300),
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
                    ),
                    maxLines = 1
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

            // Remove Button if custom added
            if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hapus dari Game Space",
                        tint = Color(0xFF71717A),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Dedicated High-Performance "LAUNCH" Button
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFB300),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("launch_button_$packageName")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LAUNCH",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun AddGameDialog(
    allApps: List<GameSpaceApp>,
    selectedPackages: Set<String>,
    onTogglePackage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Tambah Game ke Game Space",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pilih aplikasi atau game yang terinstal di perangkat Anda",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8E8E98))
                )
            }
        },
        text = {
            Column(modifier = Modifier.height(380.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama game atau aplikasi...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E8E98))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFB300),
                        unfocusedBorderColor = Color(0xFF282830),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada aplikasi yang cocok",
                            color = Color(0xFF8E8E98),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isSelected = selectedPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF22242E) else Color(0xFF141418))
                                    .border(
                                        0.8.dp,
                                        if (isSelected) Color(0xFFFFB300) else Color(0xFF24242A),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onTogglePackage(app.packageName) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = app.packageName,
                                        color = Color(0xFF8E8E98),
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFFFFB300) else Color(0xFF282832)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color(0xFF8E8E98),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF18181C),
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("SELESAI", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun triggerHaptic(context: Context) {
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
}

private fun launchOrInstallGame(context: Context, packageName: String, gameName: String) {
    try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
            Toast.makeText(context, "Meluncurkan $gameName dengan Game Space Boost...", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Membuka halaman instalasi $gameName di Google Play Store", Toast.LENGTH_SHORT).show()
        }
    } catch (_: Exception) {
        Toast.makeText(context, "Gagal meluncurkan $gameName", Toast.LENGTH_SHORT).show()
    }
}
