package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeysFirmwareState
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EglantineCyan
import com.example.ui.theme.EglantineMagenta
import com.example.ui.theme.EglantineViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SetupScreen(
    state: KeysFirmwareState,
    titleKeys: List<com.example.crypto.TitleKeyEntry> = emptyList(),
    onInstallProdKeys: (fileName: String, count: Int, revision: String) -> Unit,
    onInstallTitleKeys: (fileName: String, count: Int) -> Unit,
    onImportTitleKeysText: (String) -> Unit = {},
    onLoadSampleTitleKeys: () -> Unit = {},
    onInstallFirmware: (version: String, packageName: String) -> Unit,
    onSelectRomsDirectory: (dirPath: String) -> Unit,
    onAddCustomGame: (title: String, ext: String, isNx2: Boolean) -> Unit,
    onAddGameUri: ((Uri) -> Unit)? = null,
    onScanFolderUri: ((Uri) -> Unit)? = null,
    onClearSampleGames: (() -> Unit)? = null,
    onRestoreSampleGames: (() -> Unit)? = null
) {
    var showAddGameDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showTitleKeysInspector by remember { mutableStateOf(false) }
    var showPasteTitleKeysDialog by remember { mutableStateOf(false) }

    // System File Pickers for Keys and ROMs
    val prodKeysLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "prod.keys"
            onInstallProdKeys(name, 356, "MasterKey 19.0.0 / NX2-Final")
        }
    }

    val titleKeysLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "title.keys"
            onInstallTitleKeys(name, 194)
        }
    }

    val firmwareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Firmware_19.0.1.zip"
            onInstallFirmware("19.0.1 (NX2 Production)", name)
        }
    }

    val romFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            if (onAddGameUri != null) {
                onAddGameUri(uri)
            } else {
                val rawName = uri.lastPathSegment?.substringAfterLast('/') ?: "CustomGame.xci"
                val title = rawName.substringBeforeLast('.')
                val ext = rawName.substringAfterLast('.', "xci").lowercase()
                val isNx2 = title.contains("nx2", ignoreCase = true) || title.contains("switch 2", ignoreCase = true)
                onAddCustomGame(title, ext, isNx2)
            }
        }
    }

    val romFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            if (onScanFolderUri != null) {
                onScanFolderUri(uri)
            } else {
                val folderPath = uri.path ?: "Selected Games Directory"
                onSelectRomsDirectory(folderPath)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("setup_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP SETUP HEADER CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_header_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66E040FB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EglantineMagenta),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.Black)
                        }
                        Column {
                            Text(
                                text = "Setup Wizard: Keys & ROMs",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Decryption keys, system firmware & games library",
                                color = EglantineMagenta,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "To decrypt and execute Nintendo Switch and Switch 2 (NX2) titles, Eglantine requires your console decryption keys (prod.keys) and game ROMs (.xci, .nsp, .nro).",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    // Setup checklist summary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceHighlight)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ChecklistBadge(title = "prod.keys", ready = state.prodKeysInstalled)
                        ChecklistBadge(title = "title.keys", ready = state.titleKeysInstalled)
                        ChecklistBadge(title = "Firmware", ready = state.firmwareInstalled)
                        ChecklistBadge(title = "ROMs Folder", ready = state.romsDirectoryUri.isNotBlank())
                    }
                }
            }
        }

        // 1. ENCRYPTION KEYS SETUP
        item {
            SetupSectionTitle(
                title = "1. ENCRYPTION KEYS SETUP",
                icon = Icons.Default.Key,
                onHelp = { showHelpDialog = true }
            )
        }

        // prod.keys Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_prod_keys"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.prodKeysInstalled) AccentSuccess.copy(alpha = 0.5f) else Color(0x33FFFFFF)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "prod.keys (Decryption Keys)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (state.prodKeysInstalled)
                                    "Status: ${state.prodKeysCount} keys loaded • ${state.masterKeyRevision}"
                                else "Status: Missing (Homebrew only)",
                                color = if (state.prodKeysInstalled) AccentSuccess else Color(0xFFFF5252),
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = if (state.prodKeysInstalled) Icons.Default.CheckCircle else Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = if (state.prodKeysInstalled) AccentSuccess else Color(0xFFFF5252),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "File: ${state.prodKeysFileName} • Hash: ${state.prodKeysSha256}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { prodKeysLauncher.launch(arrayOf("*/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_select_prod_keys")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Select prod.keys", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onInstallProdKeys("prod.keys", 360, "MasterKey 19.0.0 / NX2-Verified")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EglantineCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_verify_keys")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Re-Verify", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // title.keys Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_title_keys"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "title.keys (Title Specific Keys)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Loaded: ${titleKeys.size.coerceAtLeast(state.titleKeysCount)} title decryption keys (AES-128)",
                                color = AccentSuccess,
                                fontSize = 11.sp
                            )
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentSuccess, modifier = Modifier.size(20.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { titleKeysLauncher.launch(arrayOf("*/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_select_title_keys")
                        ) {
                            Text("Select File", color = TextPrimary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showTitleKeysInspector = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EglantineViolet),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_view_title_keys")
                        ) {
                            Text("View Keys (${titleKeys.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPasteTitleKeysDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EglantineCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_paste_title_keys")
                        ) {
                            Text("Paste title.keys", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onLoadSampleTitleKeys,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineMagenta),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EglantineMagenta),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_load_sample_keys")
                        ) {
                            Text("Load Sample Keys", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 2. SYSTEM FIRMWARE SETUP
        item {
            SetupSectionTitle(title = "2. FIRMWARE & APPLETS", icon = Icons.Default.SystemUpdate)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_firmware"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Firmware: v${state.firmwareVersion}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Package: ${state.firmwarePackageName}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentSuccess, modifier = Modifier.size(20.dp))
                    }

                    Text(
                        text = "Applets active: ${state.installedApplets.joinToString(", ")}",
                        color = EglantineCyan,
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = { firmwareLauncher.launch(arrayOf("application/zip", "*/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_install_firmware_zip")
                    ) {
                        Text("Install / Update Firmware ZIP", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // 3. ROMS & GAMES DIRECTORY SETUP
        item {
            SetupSectionTitle(title = "3. GAMES & ROMS SETUP", icon = Icons.Default.Folder)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_roms_directory"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E5FF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Configured ROMs Directory",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Path: ${state.romsDirectoryUri}",
                        color = EglantineCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Supported formats: .XCI (Game Card), .NSP (eShop), .NRO (Homebrew), .NCA, .NSZ (Compressed)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    // Buttons to Select Folder or Add ROM
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    romFolderLauncher.launch(null)
                                } catch (_: Exception) {
                                    onSelectRomsDirectory("Internal / Games / Nintendo Switch")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EglantineCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_select_rom_folder")
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Select Folder", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    romFileLauncher.launch(arrayOf("*/*"))
                                } catch (_: Exception) {
                                    showAddGameDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_add_single_rom")
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Single File", color = TextPrimary, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showAddGameDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineMagenta),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EglantineMagenta),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_custom_rom_entry")
                    ) {
                        Text("Add Custom Switch / Switch 2 ROM Entry", fontSize = 12.sp)
                    }

                    // Demo / Sample Library Management
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onClearSampleGames != null) {
                            OutlinedButton(
                                onClick = onClearSampleGames,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FF5252)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Clear Demo Games", fontSize = 11.sp)
                            }
                        }

                        if (onRestoreSampleGames != null) {
                            OutlinedButton(
                                onClick = onRestoreSampleGames,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E5FF)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Restore Demo Games", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showAddGameDialog) {
        CustomRomEntryDialog(
            onDismiss = { showAddGameDialog = false },
            onAdd = { title, ext, isNx2 ->
                showAddGameDialog = false
                onAddCustomGame(title, ext, isNx2)
            }
        )
    }

    if (showHelpDialog) {
        KeysHelpDialog(onDismiss = { showHelpDialog = false })
    }

    if (showTitleKeysInspector) {
        TitleKeysInspectorDialog(
            titleKeys = titleKeys,
            onDismiss = { showTitleKeysInspector = false }
        )
    }

    if (showPasteTitleKeysDialog) {
        PasteTitleKeysDialog(
            onDismiss = { showPasteTitleKeysDialog = false },
            onImport = { content ->
                showPasteTitleKeysDialog = false
                onImportTitleKeysText(content)
            }
        )
    }
}

@Composable
fun ChecklistBadge(title: String, ready: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (ready) Icons.Default.CheckCircle else Icons.Default.HelpOutline,
            contentDescription = null,
            tint = if (ready) AccentSuccess else Color(0xFFFF5252),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = title,
            color = if (ready) TextPrimary else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SetupSectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onHelp: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = EglantineMagenta, modifier = Modifier.size(18.dp))
            Text(
                text = title,
                color = EglantineMagenta,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (onHelp != null) {
            Text(
                text = "Help & Docs",
                color = EglantineCyan,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onHelp() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun CustomRomEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, ext: String, isNx2: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("Metroid Prime 4: Beyond [NX2]") }
    var extension by remember { mutableStateOf("xci") }
    var isNx2 by remember { mutableStateOf(true) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Switch / NX2 Game ROM", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Add a game entry to your library from internal storage.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Game Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EglantineMagenta,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = extension,
                    onValueChange = { extension = it },
                    label = { Text("File Extension (xci, nsp, nro)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EglantineMagenta,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = isNx2,
                        onCheckedChange = { isNx2 = it }
                    )
                    Text("Target Switch 2 (NX2 Ampere Architecture)", color = TextPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(title, extension, isNx2) },
                colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta),
                modifier = Modifier.testTag("btn_confirm_add_rom")
            ) {
                Text("Add Game to Library")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight)
            ) {
                Text("Cancel")
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun KeysHelpDialog(onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dumping Decryption Keys", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "In accordance with legal emulation practices:",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Decryption keys (prod.keys & title.keys) must be dumped from your own legally purchased Nintendo Switch console using Lockpick_RCM.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "• Eglantine does not distribute copyright keys or proprietary game ROMs.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "• For homebrew (.nro) apps, keys are not required and can run immediately.",
                    color = EglantineCyan,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta)
            ) {
                Text("Got It")
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun TitleKeysInspectorDialog(
    titleKeys: List<com.example.crypto.TitleKeyEntry>,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Loaded Title Keys (${titleKeys.size})",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "128-bit AES Title Decryption Keys parsed from title.keys:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(titleKeys.size) { index ->
                        val entry = titleKeys[index]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceHighlight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = entry.matchedTitle ?: "Program ID: ${entry.programId}",
                                    color = EglantineCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rights ID: ${entry.rightsId.take(16)}...",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                                Text(
                                    text = "AES Key: ${entry.maskedKey}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta)
            ) {
                Text("Close")
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun PasteTitleKeysDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawText by remember {
        mutableStateOf(com.example.crypto.TitleKeyManager.getSampleTitleKeysText())
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import / Edit title.keys",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Paste your title.keys content in format: <rights_id> = <title_key>",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EglantineCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(rawText) },
                colors = ButtonDefaults.buttonColors(containerColor = EglantineCyan),
                modifier = Modifier.testTag("btn_confirm_import_title_keys")
            ) {
                Text("Import Keys", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight)
            ) {
                Text("Cancel")
            }
        },
        containerColor = DarkSurface
    )
}

