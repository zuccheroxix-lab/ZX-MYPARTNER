package com.example.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameProfile
import com.example.data.GameProfileManager
import com.example.data.SettingsManager

private val GoldAccent = Color(0xFFFFB300)
private val DarkCardBg = Color(0xFF141722)
private val BorderColor = Color(0xFF262C3E)
private val TextMuted = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileManager = remember { GameProfileManager(context) }
    val allSavedProfiles by profileManager.profilesFlow.collectAsState()

    var editingProfile by remember { mutableStateOf<GameProfile?>(null) }
    val defaultGames = remember {
        listOf(
            "com.dts.freefireth" to "Free Fire",
            "com.dts.freefiremax" to "Free Fire MAX",
            "com.mobile.legends" to "Mobile Legends: Bang Bang",
            "com.tencent.ig" to "PUBG Mobile",
            "com.miHoYo.GenshinImpact" to "Genshin Impact",
            "com.activision.callofduty.shooter" to "Call of Duty Mobile"
        )
    }

    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0C))
            .testTag("game_profile_screen")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "GAME PROFILES",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Pengaturan Khusus Per Game (Terisolasi)",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0C))
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x2238BDF8))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Setiap game memiliki profile konfigurasi terpisah. Mengubah mode performa atau posisi HUD pada satu game tidak akan memengaruhi game lain.",
                        color = Color(0xFFBAE6FD),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "DAFTAR PROFILE GAME",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // List of Games
            items(defaultGames) { (pkg, name) ->
                val profile = allSavedProfiles[pkg] ?: profileManager.getProfile(pkg, name)
                GameProfileCard(
                    profile = profile,
                    onEdit = { editingProfile = profile }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Custom user profiles if any
            val customProfiles = allSavedProfiles.filter { (pkg, _) ->
                defaultGames.none { it.first == pkg }
            }
            if (customProfiles.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "PROFILE APLIKASI TAMBAHAN",
                        color = GoldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                items(customProfiles.values.toList()) { profile ->
                    GameProfileCard(
                        profile = profile,
                        onEdit = { editingProfile = profile }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Modal Profile Editor Dialog
    if (editingProfile != null) {
        GameProfileEditorDialog(
            initialProfile = editingProfile!!,
            onDismiss = { editingProfile = null },
            onSave = { updated ->
                profileManager.saveProfile(updated)
                Toast.makeText(context, "Profile ${updated.appName} disimpan!", Toast.LENGTH_SHORT).show()
                editingProfile = null
            }
        )
    }
}

@Composable
private fun GameProfileCard(
    profile: GameProfile,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardBg)
            .border(0.8.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable { onEdit() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF221A0E))
                        .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = profile.appName,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProfileBadge(profile.performanceMode, GoldAccent)
                        ProfileBadge("HUD: ${profile.hudPosition}", Color(0xFF38BDF8))
                        if (profile.autoLaunchHud) {
                            ProfileBadge("AUTO-HUD", Color(0xFF10B981))
                        }
                    }
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Edit Profile",
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.6.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun GameProfileEditorDialog(
    initialProfile: GameProfile,
    onDismiss: () -> Unit,
    onSave: (GameProfile) -> Unit
) {
    var mode by remember { mutableStateOf(initialProfile.performanceMode) }
    var autoHud by remember { mutableStateOf(initialProfile.autoLaunchHud) }
    var position by remember { mutableStateOf(initialProfile.hudPosition) }
    var keepScreenOn by remember { mutableStateOf(initialProfile.keepScreenOn) }
    var touchSens by remember { mutableStateOf(initialProfile.touchSensitivity) }
    var notes by remember { mutableStateOf(initialProfile.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Profile: ${initialProfile.appName}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = initialProfile.packageName,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Mode Performa", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("PERFORMANCE", "BALANCED", "BATTERY_SAVER").forEach { m ->
                        val selected = mode == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) GoldAccent else Color(0xFF1E2332))
                                .clickable { mode = m }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (m) {
                                    "PERFORMANCE" -> "Performa"
                                    "BALANCED" -> "Balanced"
                                    else -> "Saver"
                                },
                                color = if (selected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Posisi HUD Layar", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("TOP_LEFT" to "Kiri Atas", "TOP_RIGHT" to "Kanan Atas", "BOTTOM_RIGHT" to "Kanan Bawah").forEach { (pos, label) ->
                        val selected = position == pos
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFF38BDF8) else Color(0xFF1E2332))
                                .clickable { position = pos }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.Black else Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Start Gaming HUD", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = autoHud,
                        onCheckedChange = { autoHud = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = GoldAccent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Layar Tetap Aktif (Keep Awake)", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { keepScreenOn = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = GoldAccent)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Sensitivitas / Game", fontSize = 11.sp) },
                    placeholder = { Text("Contoh: Sensibilitas DPI 90", fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialProfile.copy(
                            performanceMode = mode,
                            autoLaunchHud = autoHud,
                            hudPosition = position,
                            keepScreenOn = keepScreenOn,
                            notes = notes
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text("Simpan Profile", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted)
            }
        },
        containerColor = Color(0xFF141722),
        shape = RoundedCornerShape(18.dp)
    )
}
