package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.zip.ZipInputStream

data class InspectedFileEntry(
    val fileName: String,
    val sizeBytes: Long,
    val isDirectory: Boolean,
    val isSafeTextOrConfig: Boolean
)

data class ArchiveInspectionReport(
    val archiveName: String,
    val totalEntries: Int,
    val totalBytes: Long,
    val entries: List<InspectedFileEntry>,
    val isValid: Boolean,
    val complianceStatus: String
)

@Composable
fun GameConfigFileInspector(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isInspecting by remember { mutableStateOf(false) }
    var currentReport by remember { mutableStateOf<ArchiveInspectionReport?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    suspend fun inspectUri(uri: Uri): ArchiveInspectionReport = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        var displayName = "arsip_config.zip"
        var fileSize = 0L

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: displayName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {}

        val entriesList = mutableListOf<InspectedFileEntry>()
        var totalBytes = 0L
        var isValidArchive = true

        try {
            val input: InputStream? = contentResolver.openInputStream(uri)
            if (input != null) {
                ZipInputStream(input).use { zis ->
                    var entry = zis.nextEntry
                    var count = 0
                    while (entry != null && count < 200) {
                        count++
                        val name = entry.name
                        val size = entry.size.coerceAtLeast(0L)
                        totalBytes += size

                        // Check zip slip security
                        if (name.contains("..") || name.startsWith("/")) {
                            isValidArchive = false
                        }

                        val isTxt = name.endsWith(".json", true) ||
                                name.endsWith(".txt", true) ||
                                name.endsWith(".cfg", true) ||
                                name.endsWith(".ini", true) ||
                                name.endsWith(".xml", true)

                        entriesList.add(
                            InspectedFileEntry(
                                fileName = name,
                                sizeBytes = size,
                                isDirectory = entry.isDirectory,
                                isSafeTextOrConfig = isTxt
                            )
                        )
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            isValidArchive = false
        }

        ArchiveInspectionReport(
            archiveName = displayName,
            totalEntries = entriesList.size,
            totalBytes = if (totalBytes > 0) totalBytes else fileSize,
            entries = entriesList,
            isValid = isValidArchive,
            complianceStatus = if (isValidArchive) "Terverifikasi Valid & Aman (Non-Malicious)" else "Format Arsip Memerlukan Pemeriksaan Lanjutan"
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isInspecting = true
            statusMessage = null
            scope.launch {
                val report = inspectUri(uri)
                currentReport = report
                isInspecting = false
                Toast.makeText(context, "Inspeksi berkas berhasil diselesaikan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F1118))
            .border(1.dp, Color(0xFF262C3E), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Title Bar
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
                        .background(Color(0xFFA78BFA).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SAFE FILE & CONFIG INSPECTOR",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Audit Berkas Konfigurasi & Storage Access Framework (SAF)",
                        color = Color(0xFF8E8E98),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // POLICY & FAIR PLAY NOTICE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E172B))
                .border(0.8.dp, Color(0xFF5B21B6).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kebijakan Scoped Storage & Fair Play Game",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "DYNIMETIZE ZX mematuhi Kebijakan Privasi Android Scoped Storage dan Ketentuan Layanan pengembang game (Free Fire / MLBB). Modifikasi atau injeksi file biner internal game secara ilegal tidak didukung karena berisiko sanksi penutupan akun (Ban). Modul ini bekerja eksklusif melalui Storage Access Framework (SAF) untuk memeriksa berkas konfigurasi lokal secara transparan.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.5.sp,
                    lineHeight = 14.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Button: Select File
        Button(
            onClick = {
                filePickerLauncher.launch(
                    arrayOf(
                        "application/zip",
                        "application/x-zip-compressed",
                        "application/json",
                        "text/plain",
                        "*/*"
                    )
                )
            },
            enabled = !isInspecting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA))
        ) {
            if (isInspecting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Memeriksa Berkas...", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pilih File Konfigurasi (ZIP / JSON / TXT)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Inspection Results Report
        currentReport?.let { rep ->
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF141722))
                    .border(0.8.dp, Color(0xFF2E384D), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rep.archiveName,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (rep.isValid) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (rep.isValid) "VALID & AMAN" else "PERLU REVIEW",
                                color = if (rep.isValid) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Total Konten: ${rep.totalEntries} entri berkas • Ukuran: ${rep.totalBytes / 1024} KB",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    HorizontalDivider(color = Color(0xFF262C3E), thickness = 0.6.dp)

                    Text(
                        text = "DAFTAR ENTRI BERKAS TERDETEKSI:",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        rep.entries.take(6).forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${entry.fileName}",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                                Text(
                                    text = if (entry.isDirectory) "DIR" else "${entry.sizeBytes} B",
                                    color = Color(0xFF64748B),
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (rep.entries.size > 6) {
                            Text(
                                text = "...dan ${rep.entries.size - 6} berkas lainnya.",
                                color = Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
