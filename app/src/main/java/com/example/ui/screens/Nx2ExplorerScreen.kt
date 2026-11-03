package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EglantineCyan
import com.example.ui.theme.EglantineMagenta
import com.example.ui.theme.EglantineViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun Nx2ExplorerScreen(
    logs: List<String>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("nx2_explorer_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66E040FB))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = Color.Black)
                        }
                        Column {
                            Text(
                                text = "NX2 Core Diagnostics (Build 104 / Lichii)",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Synced with upstream zortos293/Eglantine (commit 29571ea9)",
                                color = EglantineMagenta,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "Eglantine executes Switch 2 (NX2) binaries natively with ARMv8.2-A pointer authentication, LSE atomics, 39-bit address spaces, NX2 GSV sparse tile pre-free, and DLSS spatial upscale stand-in shaders.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // UPSTREAM ZORTOS293/EGLANTINE BUILD 104 / LICHII NX2 SUBSYSTEMS
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = EglantineCyan, modifier = Modifier.size(16.dp))
                Text(
                    text = "UPSTREAM BUILD 104 / LICHII NX2 MODULES",
                    color = EglantineCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UpstreamModuleItem(
                        module = "nx2_gsv.h • Sparse Tile Pre-Free & EOS Drain",
                        commit = "29571ea9 / aef27988",
                        detail = "Frees non-resident sparse page tiles before GPU handoff and orders end-of-stream (EOS) frames in GSV presenter."
                    )
                    UpstreamModuleItem(
                        module = "fire_emblem_upscale.h • DLSS Spatial Stand-In",
                        commit = "e7a2835b / 3f0110e8",
                        detail = "Replaces NX2 DLSS output pass (14b4222bbe23abee) & strips fractional camera jitter in prepass (4cbf6eff32525aec) for Fire Emblem: Fortune's Weave (04007EF01F8AE000)."
                    )
                    UpstreamModuleItem(
                        module = "exclusive_monitor.cpp • ARM64 CAS Pre-Check",
                        commit = "b408de16",
                        detail = "Pre-checks exclusive monitor reservation before compare-and-swap, reducing multi-core cache-line contention from 10% to 2%."
                    )
                    UpstreamModuleItem(
                        module = "vk_scheduler.cpp & pipeline_cache_files.h",
                        commit = "2ce2fd4c / 780f1ca5",
                        detail = "Isolates fork pipeline cache (vulkan.fork-v39.bin), widens 8-bit 0xFF->0xFFFF primitive restart indices, and guards VK_EXT_mesh_shader."
                    )
                    UpstreamModuleItem(
                        module = "texture_cache_base.h • VMA Block Eviction",
                        commit = "a5fbba67 / e7b16a6c",
                        detail = "Tracks physical VMA block occupancy to evict sparse textures before Adreno VRAM fragmentation occurs."
                    )
                    UpstreamModuleItem(
                        module = "host_memory.cpp & command_fetch_order.h",
                        commit = "e4756178 / 6aa29909",
                        detail = "Direct-map relative page-state indexing, >4KiB page Fastmem guard, and DMA pusher macro parameter snapshot guard."
                    )
                }
            }
        }

        // SPECIFICATIONS BREAKDOWN
        item {
            Text(
                text = "NX2 HARDWARE & KERNEL PROFILE",
                color = EglantineCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Nx2SpecItem(label = "Instruction Set", value = "ARMv8.2-A / ARMv9 (AArch64)")
                    Nx2SpecItem(label = "CPU Cores", value = "6 CPU Cores (Mask 0x3F, Cores 0..5)")
                    Nx2SpecItem(label = "Virtual Address Space", value = "39-bit (512 GB Virtual Range)")
                    Nx2SpecItem(label = "Kernel Version Cap", value = "9.1 (SVC 0x01-0x36 + 0x80)")
                    Nx2SpecItem(label = "Graphics Subsystem", value = "NVN2 / Ampere + GSV Sparse Pre-Free")
                    Nx2SpecItem(label = "Atomic Decoders", value = "LDAPR, CASP, RETAA/B, STG, XPAC, PACGA")
                    Nx2SpecItem(label = "Audio Subsystem", value = "AAudio / AudioTrack 44.1kHz 16-bit PCM")
                }
            }
        }

        // REAL-TIME SVC / IPC / PUSHBUFFER MONITOR LOGS
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = EglantineViolet, modifier = Modifier.size(16.dp))
                Text(
                    text = "LIVE EXECUTION TRACE LOGS (SVC / IPC / NVN2)",
                    color = EglantineViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nx2_log_terminal"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09080F)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x337C4DFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    logs.forEach { log ->
                        Text(
                            text = log,
                            color = if (log.contains("SVC") || log.contains("[nx2_gsv]") || log.contains("[FE_Upscale]")) EglantineCyan
                            else if (log.contains("Pushbuffer") || log.contains("[Pipeline]")) EglantineMagenta
                            else if (log.contains("Dynarmic") || log.contains("[Audio]")) Color(0xFFFFD54F)
                            else Color(0xFFB0BEC5),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun UpstreamModuleItem(
    module: String,
    commit: String,
    detail: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceHighlight)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = module,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x2200E676))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = commit,
                    color = AccentSuccess,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = detail,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun Nx2SpecItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
