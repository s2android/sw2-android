package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.DarkSurface
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
                                text = "NX2 Architecture Diagnostics",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nintendo Switch 2 Emulation Research (Eden/Eglantine)",
                                color = EglantineMagenta,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "Eglantine investigates running Switch 2 binaries natively with ARMv8.2-A pointer authentication, LSE atomic instructions, 39-bit address spaces, and Ampere GPU pushbuffer pipeline splitting.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
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
                    Nx2SpecItem(label = "Graphics Subsystem", value = "NVN2 / Ampere Architecture with Pushbuffer Thread Split")
                    Nx2SpecItem(label = "Atomic Decoders", value = "LDAPR, CASP, RETAA/B, STG, XPAC, PACGA")
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
                            color = if (log.contains("SVC")) EglantineCyan
                            else if (log.contains("Pushbuffer")) EglantineMagenta
                            else if (log.contains("Dynarmic")) Color(0xFFFFD54F)
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
