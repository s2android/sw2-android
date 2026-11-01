package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SnapdragonChipset
import com.example.model.SnapdragonOptimizationConfig
import com.example.model.VulkanDriver
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
fun VulkanDriversScreen(
    drivers: List<VulkanDriver>,
    selectedDriverId: String,
    snapdragonConfig: SnapdragonOptimizationConfig,
    isFetchingOnline: Boolean,
    onSelectDriver: (String) -> Unit,
    onApplySnapdragonChipset: (SnapdragonChipset) -> Unit,
    onToggleGmemBypass: (Boolean) -> Unit,
    onToggleUbwc: (Boolean) -> Unit,
    onToggleTurbo: (Boolean) -> Unit,
    onToggleKgsl: (Boolean) -> Unit,
    onFetchOnlineDrivers: () -> Unit,
    onInstallCustomDriver: (name: String, version: String, desc: String) -> Unit
) {
    var showCustomDriverDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vulkan_drivers_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP INTRO CARD: SNAPDRAGON & MESA TURNIP ENGINE
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vulkan_drivers_header_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x6600E5FF))
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EglantineCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                        }
                        Column {
                            Text(
                                text = "Snapdragon Vulkan Engine",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mesa Turnip + Freedreno Adreno Optimizations",
                                color = EglantineCyan,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "Turnip provides tailor-made Vulkan 1.3 compilers for Qualcomm Snapdragon Adreno GPUs, leveraging GMEM bypass, UBWC memory compression, and KGSL zero-copy ioctl dispatch to eliminate stutters in Switch and NX2 titles.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    // Action buttons (Fetch Online & Install Custom ZIP)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onFetchOnlineDrivers,
                            enabled = !isFetchingOnline,
                            colors = ButtonDefaults.buttonColors(containerColor = EglantineCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_fetch_turnip")
                        ) {
                            if (isFetchingOnline) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Fetching...", color = Color.Black, fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Fetch Nightly", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { showCustomDriverDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EglantineMagenta),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_install_driver_zip")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = EglantineMagenta, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Install ZIP", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 1. SNAPDRAGON SOC TARGET PROFILES
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = EglantineMagenta, modifier = Modifier.size(16.dp))
                    Text(
                        text = "SNAPDRAGON CHIPSET PROFILE",
                        color = EglantineMagenta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SnapdragonChipset.entries) { chipset ->
                        val isSelected = snapdragonConfig.targetChipset == chipset
                        FilterChip(
                            selected = isSelected,
                            onClick = { onApplySnapdragonChipset(chipset) },
                            label = {
                                Text(
                                    text = chipset.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EglantineMagenta.copy(alpha = 0.25f),
                                selectedLabelColor = Color.White,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0x33FFFFFF),
                                selectedBorderColor = EglantineMagenta
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // 2. QUALCOMM ADRENO ACCELERATION TOGGLES
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("adreno_acceleration_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x667C4DFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "QUALCOMM ADRENO HARDWARE ACCELERATION",
                        color = EglantineViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // GMEM Bypass
                    AdrenoSwitchRow(
                        title = "Adreno GMEM Bypass",
                        description = "Direct DDR rendering: removes tile resolve stalls on Adreno 7xx/8xx",
                        checked = snapdragonConfig.gmemBypassEnabled,
                        onChecked = onToggleGmemBypass
                    )

                    // UBWC Compression
                    AdrenoSwitchRow(
                        title = "Qualcomm UBWC 4.0 Compression",
                        description = "Lossless universal bandwidth compression across LPDDR5X (saves ~35% bus traffic)",
                        checked = snapdragonConfig.ubwcCompression,
                        onChecked = onToggleUbwc
                    )

                    // Adreno Turbo Boost
                    AdrenoSwitchRow(
                        title = "Adreno Turbo Boost (903 MHz)",
                        description = "Locks GPU governor to peak sustained clocks to prevent sudden frame drops",
                        checked = snapdragonConfig.adrenoTurboBoost,
                        onChecked = onToggleTurbo
                    )

                    // KGSL Fast Path
                    AdrenoSwitchRow(
                        title = "KGSL Direct Driver Submission",
                        description = "Zero-copy ioctl command streaming via Qualcomm Graphics System Layer",
                        checked = snapdragonConfig.kgslFastPath,
                        onChecked = onToggleKgsl
                    )
                }
            }
        }

        // 3. AVAILABLE VULKAN DRIVERS LIST
        item {
            Text(
                text = "AVAILABLE SNAPDRAGON VULKAN DRIVERS (${drivers.size})",
                color = EglantineCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(drivers, key = { it.id }) { driver ->
            val isSelected = driver.id == selectedDriverId
            DriverItemCard(
                driver = driver,
                isSelected = isSelected,
                onSelect = { onSelectDriver(driver.id) }
            )
        }

        // 4. HARDWARE TELEMETRY CARD
        item {
            SnapdragonTelemetryCard(config = snapdragonConfig)
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showCustomDriverDialog) {
        CustomDriverImportDialog(
            onDismiss = { showCustomDriverDialog = false },
            onInstall = { name, ver, desc ->
                showCustomDriverDialog = false
                onInstallCustomDriver(name, ver, desc)
            }
        )
    }
}

@Composable
fun AdrenoSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EglantineCyan,
                uncheckedTrackColor = DarkSurfaceHighlight
            )
        )
    }
}

@Composable
fun SnapdragonTelemetryCard(config: SnapdragonOptimizationConfig) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = EglantineCyan, modifier = Modifier.size(18.dp))
                Text("Qualcomm Hardware Telemetry & Drivers", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text("• SoC: ${config.detectedSoC}", color = EglantineCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("• Kernel Interface: ${config.detectedKernel}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("• Instruction Architecture: ARMv8.2-A / ARMv9 (AArch64 64-bit)", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("• Vulkan Memory Model: Timeline semaphores & Float16 ALUs active", color = AccentSuccess, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun DriverItemCard(
    driver: VulkanDriver,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_card_${driver.id}")
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceHighlight else DarkSurfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) EglantineCyan else Color(0x22FFFFFF)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) EglantineCyan else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = driver.name,
                        color = if (isSelected) EglantineCyan else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (driver.isOpenSource) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentSuccess.copy(alpha = 0.2f))
                            .border(1.dp, AccentSuccess, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OPEN SOURCE",
                            color = AccentSuccess,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33757575))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PROPRIETARY",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "${driver.vendor} • ${driver.version}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Text(
                text = driver.description,
                color = Color(0xFFC7C2D6),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CapabilityTag(text = "Vulkan ${driver.vulkanApiVersion}", active = true)
                CapabilityTag(text = "Float16", active = driver.hasFloat16Support)
                CapabilityTag(text = "UBWC", active = driver.ubwcSupported)
                CapabilityTag(text = "GMEM Bypass", active = driver.gmemTilingBypass)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EglantineCyan)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ACTIVE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CapabilityTag(text: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) Color(0x3300E5FF) else Color(0x22757575))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = if (active) EglantineCyan else TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CustomDriverImportDialog(
    onDismiss: () -> Unit,
    onInstall: (name: String, version: String, desc: String) -> Unit
) {
    var driverName by remember { mutableStateOf("Mesa Turnip v24.3-snapdragon") }
    var driverVersion by remember { mutableStateOf("v24.3.0-adreno-custom") }
    var driverDesc by remember { mutableStateOf("Custom Mesa Turnip driver with aggressive Adreno 7xx/8xx optimizations.") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Install Custom Snapdragon Vulkan Driver ZIP", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select or provide package details for the custom Open-Source Vulkan driver (Turnip/Freedreno).",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Driver Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EglantineCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = driverVersion,
                    onValueChange = { driverVersion = it },
                    label = { Text("Version") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EglantineCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = driverDesc,
                    onValueChange = { driverDesc = it },
                    label = { Text("Description") },
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
                onClick = { onInstall(driverName, driverVersion, driverDesc) },
                colors = ButtonDefaults.buttonColors(containerColor = EglantineCyan),
                modifier = Modifier.testTag("btn_confirm_custom_driver")
            ) {
                Text("Install Package", color = Color.Black, fontWeight = FontWeight.Bold)
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

