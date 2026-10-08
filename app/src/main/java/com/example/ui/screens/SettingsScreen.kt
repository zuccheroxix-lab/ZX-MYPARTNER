package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuthManager
import com.example.data.Config
import com.example.data.FeedbackManager
import com.example.data.FeedbackSubmissionState
import com.example.data.SettingsManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import com.example.ui.components.LinkHandler
import com.example.ui.components.isShizukuInstalled
import com.example.ui.theme.LocalAccentColor
import kotlinx.coroutines.launch

private val AccentPresets = listOf(
    Pair("Cyan ZX", 0xFF00D2FFL),
    Pair("ROG Red", 0xFFFF3344L),
    Pair("Cyber Gold", 0xFFFFB300L),
    Pair("Neon Green", 0xFF10B981L),
    Pair("Purple", 0xFFA855F7L),
    Pair("Sky Blue", 0xFF38BDF8L)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    authManager: AuthManager? = null,
    isDarkMode: Boolean = true,
    animationsEnabled: Boolean = true,
    hapticsEnabled: Boolean = true,
    appVersion: String = Config.DEFAULT_VERSION_NAME,
    onNavigateToPage: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val accentColorLong by settingsManager.accentColor.collectAsState()
    val accentColor = Color(accentColorLong)

    val feedbackManager = remember { FeedbackManager(context) }
    val feedbackState by feedbackManager.submissionState.collectAsState()

    // Feedback Form States
    var feedbackCategory by remember { mutableStateOf("Bug Report") }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackContact by remember { mutableStateOf("") }

    val shizukuInstalled = isShizukuInstalled(context)

    BackHandler {
        onNavigateToPage(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A0D))
            .testTag("settings_screen_root")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "SETTINGS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "UI Colors, Shizuku, Permissions & Feedback",
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

        // Navigation Quick Jump Bar (Across the 4 main pages)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToPage(2) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("← Tools", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { onNavigateToPage(0) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Home →", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            // CARD 1: TAMPILAN & WARNA UI (UI COLOR & ACCENT)
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Warna Aksen & Tampilan UI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Personalisasi tema & indikator aplikasi", color = Color(0xFF8E8E98), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Preset Warna Aksen:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AccentPresets.forEach { (name, colorLong) ->
                            val isSel = accentColorLong == colorLong
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorLong))
                                    .border(
                                        width = if (isSel) 2.5.dp else 1.dp,
                                        color = if (isSel) Color.White else Color(0xFF262A3B),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        settingsManager.setAccentColor(colorLong)
                                        Toast.makeText(context, "Aksen diatur: $name", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF22242E), thickness = 0.6.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dark Mode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tema Gelap (OLED Black)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Efisiensi baterai & kontras gaming", color = Color(0xFF8E8E98), fontSize = 11.sp)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { settingsManager.setDarkMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 2: SHIZUKU ADB INTEGRATION
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (shizukuInstalled) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF97316).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (shizukuInstalled) Color(0xFF10B981) else Color(0xFFF97316),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Layanan Shizuku ADB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = if (shizukuInstalled) "Terpasang & Siap Digunakan" else "Belum Terdeteksi di Perangkat",
                                    color = if (shizukuInstalled) Color(0xFF10B981) else Color(0xFFF97316),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (shizukuInstalled) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF97316).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (shizukuInstalled) "READY" else "NOT INSTALLED",
                                color = if (shizukuInstalled) Color(0xFF10B981) else Color(0xFFF97316),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Shizuku memungkinkan pengaturan resolusi, DPI, dan pengaturan sistem aman tanpa perlu melakukan root perangkat.",
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Kunjungi https://shizuku.rikka.app", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Panduan & Download Shizuku", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 3: PERMISSION MANAGER
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Manajer Izin Sistem (Permissions)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Status izin keamanan yang digunakan aplikasi", color = Color(0xFF8E8E98), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val canDraw = Settings.canDrawOverlays(context)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Izin Tampilkan di Atas Aplikasi", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Diperlukan untuk Floating HUD & Crosshair", color = Color(0xFF8E8E98), fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (canDraw) Color(0xFF10B981) else Color(0xFFEF4444))
                        ) {
                            Text(if (canDraw) "Aktif" else "Buka Izin", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Setelan Khusus Sistem (Write Settings)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Pengaturan penunjuk OS & layar", color = Color(0xFF8E8E98), fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Info Aplikasi", fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 4: REAL WORKING FEEDBACK SYSTEM (NO GIMMICKS)
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Feedback, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Kirim Masukan (Feedback Nyata)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Langsung terkirim ke server pengembang & email", color = Color(0xFF8E8E98), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Category Selector
                    Text("Kategori Masukan:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))

                    val categories = listOf("Bug Report", "Saran Fitur", "Game Baru", "Performa")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            val isSel = feedbackCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF171A24))
                                    .border(0.8.dp, if (isSel) Color(0xFF38BDF8) else Color(0xFF262A3B), RoundedCornerShape(8.dp))
                                    .clickable { feedbackCategory = cat }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) Color(0xFF38BDF8) else Color(0xFF8E8E98),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact / Email field (optional)
                    Text("Kontak / Email Pengirim (Opsional):", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = feedbackContact,
                        onValueChange = { feedbackContact = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("email@contoh.com atau nama Anda", color = Color(0xFF6E7285), fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF262A3B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Message field
                    Text("Isi Pesan Masukan:", color = Color(0xFF8E8E98), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = feedbackMessage,
                        onValueChange = { feedbackMessage = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Jelaskan masalah, masukan, atau usulan fitur Anda...", color = Color(0xFF6E7285), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF262A3B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // SUBMISSION STATUS BANNER (REAL STATUS)
                    when (val state = feedbackState) {
                        is FeedbackSubmissionState.Sending -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F2B48))
                                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF38BDF8), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Status: Mengirim data ke server...", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        is FeedbackSubmissionState.Sent -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF064E3B))
                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(state.message, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        is FeedbackSubmissionState.Failed -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF451A1A))
                                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (state.isOffline) Icons.Default.WifiOff else Icons.Default.Error, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(state.reason, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        FeedbackSubmissionState.Idle -> {}
                    }

                    // Submission Buttons
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    feedbackManager.submitFeedback(
                                        category = feedbackCategory,
                                        message = feedbackMessage,
                                        userContact = feedbackContact
                                    )
                                }
                            },
                            enabled = feedbackState !is FeedbackSubmissionState.Sending,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kirim ke Server", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                feedbackManager.openEmailIntent(
                                    category = feedbackCategory,
                                    message = feedbackMessage,
                                    userContact = feedbackContact
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Mail, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Via Email Langsung", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 5: UPDATE & DOWNLOAD APK TERBARU (Official Release)
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(Color(0xFFFFB300).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Update & Release APK",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Versi Terbaru: v${Config.DEFAULT_VERSION_NAME.replace(" (release)", "")} (Build ${Config.VERSION_CODE})",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "READY",
                                color = Color(0xFF10B981),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "File instalasi resmi ${Config.APK_FILENAME}. Berisi update 4 navigasi utama (Home, Game, Tools, Settings), feedback nyata, dan zero gimmick.",
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            LinkHandler.openUrl(context, Config.APK_DOWNLOAD_URL)
                            Toast.makeText(context, "Membuka download ${Config.APK_FILENAME}...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_download_latest_apk"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300),
                            contentColor = Color(0xFF0A0B0F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DOWNLOAD APK TERBARU", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                LinkHandler.openUrl(context, Config.GITHUB_RELEASES_URL)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_open_github_release"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, Color(0xFF2E3244))
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GitHub Release", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                LinkHandler.copyToClipboard(context, "APK Download URL", Config.APK_DOWNLOAD_URL) {
                                    Toast.makeText(context, "Link APK berhasil disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_copy_download_link"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, Color(0xFF2E3244))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin Link", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CARD 6: PENGATURAN & INFO APLIKASI (Preserving asset logo & identity)
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_zx_logo),
                            contentDescription = "ZX Logo Asset",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("DYNIMETIZE ZX Control Engine", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("Build Version $appVersion • Developer: ${Config.DEVELOPER_NAME}", color = Color(0xFF8E8E98), fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Aplikasi utilitas gaming performa tinggi dan alat diagnostik Android. Seluruh fitur berjalan sesuai batas izin resmi tanpa manipulasi sistem ilegal.",
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF13151D))
            .border(0.8.dp, Color(0xFF222533), RoundedCornerShape(18.dp))
    ) {
        content()
    }
}
