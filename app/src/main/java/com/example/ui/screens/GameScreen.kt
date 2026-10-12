package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.R
import com.example.data.SensitivityManager
import com.example.data.SettingsManager
import com.example.ui.components.XYSensitivitySection
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GameEntry(
    val packageName: String,
    val appName: String,
    val iconBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    val isGame: Boolean,
    val isCustom: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onNavigateToPage: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = LocalAccentColor.current
    val settingsManager = remember { SettingsManager(context) }
    val sensitivityManager = remember { SensitivityManager(context) }

    // Sub-sections: 0 = Game Space, 1 = Sensitivity Lab (X/Y Sensi System), 2 = Profile & Touch Opt
    var selectedTab by remember { mutableIntStateOf(1) } // Default to Sensitivity Lab
    val scrollState = rememberScrollState()

    // Sensitivity State
    val sensitivityProfile by settingsManager.sensitivityProfile.collectAsState()
    val touchOptEnabled by settingsManager.touchOptimization.collectAsState()
    val swipeOptEnabled by settingsManager.swipeOptimization.collectAsState()
    val customGamePackages by settingsManager.customGamePackages.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    val gamesList = remember { mutableStateListOf<GameEntry>() }
    val allInstalledList = remember { mutableStateListOf<GameEntry>() }
    var isLoadingGames by remember { mutableStateOf(true) }

    // Load actual installed games
    suspend fun loadInstalledApps() {
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                val tempAll = mutableListOf<GameEntry>()
                val tempGames = mutableListOf<GameEntry>()

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

                    val entry = GameEntry(pkg, label, icon, isGame, isCustom)
                    tempAll.add(entry)
                    if (isGame) tempGames.add(entry)
                }

                withContext(Dispatchers.Main) {
                    allInstalledList.clear()
                    allInstalledList.addAll(tempAll.sortedBy { it.appName.lowercase() })
                    gamesList.clear()
                    gamesList.addAll(tempGames.sortedBy { it.appName.lowercase() })
                    isLoadingGames = false
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { isLoadingGames = false }
            }
        }
    }

    LaunchedEffect(customGamePackages) {
        loadInstalledApps()
    }

    BackHandler {
        onNavigateToPage(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0D))
            .testTag("game_screen_root")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "GAME HUB",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Game Space, Sensitivity Lab & Input Tuning",
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { onNavigateToPage(0) }) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = accentColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0C))
        )

        // Navigation Jump Bar (Smooth transition across the 4 main pages)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToPage(0) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("← Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(2) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tools →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented Sub-Category Tabs (Game Space, Sensitivity, Profil & Sentuhan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "Game Space" to Icons.Default.SportsEsports,
                "Sensitivity Lab" to Icons.Default.TouchApp,
                "Input & Profil" to Icons.Default.Tune
            )

            tabs.forEachIndexed { idx, (label, icon) ->
                val isSel = selectedTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSel) accentColor.copy(alpha = 0.2f) else Color(0xFF13151D))
                        .border(0.8.dp, if (isSel) accentColor else Color(0xFF222533), RoundedCornerShape(12.dp))
                        .clickable { selectedTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSel) accentColor else Color(0xFF8E8E98),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            color = if (isSel) accentColor else Color(0xFF8E8E98),
                            fontSize = 10.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB CONTENT
        when (selectedTab) {
            0 -> {
                // GAME SPACE TAB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    // Header Summary & Add Game
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF13151D))
                            .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Koleksi Game Lokal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${gamesList.size} Game siap dimainkan", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = { showAddDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tambah", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Games List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (gamesList.isEmpty() && !isLoadingGames) {
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
                                        Text("Tap tombol '+ Tambah' untuk memilih game favorit Anda.", color = Color(0xFF8E8E98), fontSize = 11.sp)
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
                                                        Toast.makeText(context, "Tidak dapat membuka game ini", Toast.LENGTH_SHORT).show()
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Gagal meluncurkan: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Main", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
            }

            1 -> {
                // SENSITIVITY GAMELAB & X/Y SENSITIVITY SYSTEM TAB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                ) {
                    // 1. Primary Feature: DYNIMETIZE ZX X/Y Sensitivity System
                    XYSensitivitySection(
                        sensitivityManager = sensitivityManager,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Legacy / Secondary: Quick OS Pointer Profiles
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF13151D))
                            .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Profil Cepat OS Pointer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Kurva akselerasi sentuh Android standar", color = Color(0xFF8E8E98), fontSize = 10.5.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val profiles = listOf(
                                Triple("STANDARD", "Standar (1.0x)", "Kecepatan penunjuk default sistem Android"),
                                Triple("HIGH_PRECISION", "Presisi (1.25x)", "Bidikan stabil jarak jauh"),
                                Triple("ESPORTS_PRO", "Esports Pro (1.5x)", "Refleks kamera cepat"),
                                Triple("ULTRA_FLICK", "Ultra Flick (1.75x)", "Flick 180° maksimal")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                profiles.forEach { (key, label, desc) ->
                                    val isSelected = sensitivityProfile == key
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) accentColor.copy(alpha = 0.18f) else Color(0xFF171923))
                                            .border(0.8.dp, if (isSelected) accentColor else Color(0xFF262A3B), RoundedCornerShape(8.dp))
                                            .clickable {
                                                settingsManager.setSensitivityProfile(key)
                                                Toast.makeText(context, "Profil OS: $label", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(label, color = if (isSelected) accentColor else Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                                Text(desc, color = Color(0xFF8E8E98), fontSize = 9.5.sp)
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        settingsManager.setSensitivityProfile("STANDARD")
                                        Toast.makeText(context, "Direset ke Standar (1.0x)", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                                ) {
                                    Text("Reset OS", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent("android.settings.POINTER_SPEED_SETTINGS").apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            try {
                                                val fallback = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(fallback)
                                            } catch (_: Exception) {
                                                Toast.makeText(context, "Buka Pengaturan > Kecepatan Penunjuk", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                                ) {
                                    Text("Buka Pengaturan OS", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(110.dp))
                }
            }

            2 -> {
                // INPUT TUNING & PROFIL TAB
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF13151D))
                            .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Optimasi Sentuhan & Gesekan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Setelan latensi sentuhan jendela aplikasi", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Touch Opt Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Prioritas Event Sentuhan", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Prioritaskan input window rendah latensi", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                }
                                Switch(
                                    checked = touchOptEnabled,
                                    onCheckedChange = {
                                        settingsManager.setTouchOptimization(it)
                                        Toast.makeText(context, if (it) "Touch Opt: AKTIF" else "Touch Opt: DEFAULT", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFF22242E), thickness = 0.6.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Swipe Opt Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Optimasi Fisika Gesekan Swipe", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Perhalus friksi gulir navigasi layar", color = Color(0xFF8E8E98), fontSize = 11.sp)
                                }
                                Switch(
                                    checked = swipeOptEnabled,
                                    onCheckedChange = {
                                        settingsManager.setSwipeOptimization(it)
                                        Toast.makeText(context, if (it) "Swipe Opt: ULTRA SMOOTH" else "Swipe Opt: NORMAL", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
                            Text(
                                text = "Data Sistem: Touch Slop = ${touchSlop}px • Window Accelerated = Aktif",
                                color = Color(0xFF8E8E98),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(110.dp))
                }
            }
        }
    }

    // Modal dialog to add a game from installed apps
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Pilih Aplikasi atau Game", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(allInstalledList) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsManager.addCustomGamePackage(app.packageName)
                                    showAddDialog = false
                                    Toast.makeText(context, "${app.appName} ditambahkan ke Game Space", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (app.iconBitmap != null) {
                                Image(bitmap = app.iconBitmap, contentDescription = null, modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)))
                            } else {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = accentColor, modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(app.appName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(app.packageName, color = Color(0xFF8E8E98), fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Tutup", color = accentColor)
                }
            },
            containerColor = Color(0xFF13151D)
        )
    }
}
