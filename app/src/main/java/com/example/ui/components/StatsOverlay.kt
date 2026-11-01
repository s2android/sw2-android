package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.EglantineCyan
import com.example.ui.theme.EglantineMagenta

@Composable
fun StatsOverlay(
    modifier: Modifier = Modifier,
    fps: Float,
    frametimeMs: Float,
    driverName: String,
    shadersBuilding: Int,
    appRamMb: Int,
    sysRamMb: Int,
    batteryTempC: Float,
    socPowerWatts: Float,
    isDocked: Boolean,
    isNx2: Boolean,
    adrenoClocksMhz: Int = 903,
    cpuBackend: String = "NCE",
    fastmem: Boolean = true,
    dramPoolGb: Int = 6
) {
    Box(
        modifier = modifier
            .testTag("stats_overlay_hud")
            .background(Color(0xDD0E0D14), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0x6600E5FF), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            // First row: Driver & Mode badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = driverName.take(24),
                    color = EglantineCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isDocked) "DOCKED (1080p)" else "HANDHELD (720p)",
                    color = Color(0xFFFFD54F),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                if (isNx2) {
                    Text(
                        text = "NX2",
                        color = EglantineMagenta,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Second row: FPS, Frametime, Shaders
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val fpsColor = if (fps >= 55f) AccentSuccess else if (fps >= 30f) AccentWarning else Color.Red
                Text(
                    text = String.format("%.1f FPS", fps),
                    color = fpsColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format("%.1f ms", frametimeMs),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (shadersBuilding > 0) "Building $shadersBuilding" else "Shaders: Cached",
                    color = if (shadersBuilding > 0) Color(0xFFFF9100) else Color(0xFFB0BEC5),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Third row: Snapdragon Adreno Turbo & Telemetry
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Adreno: ${adrenoClocksMhz}MHz [UBWC]",
                    color = Color(0xFF69F0AE),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${appRamMb}M RAM",
                    color = Color(0xFFCFD8DC),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format("%.1f°C | %.1fW", batteryTempC, socPowerWatts),
                    color = Color(0xFF80DEEA),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Fourth row: AAA CPU backend & Memory Engine
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CPU: ${if (cpuBackend.contains("NCE")) "NCE Direct" else "Dynarmic"}${if (fastmem) " [Fastmem]" else ""}",
                    color = Color(0xFFE040FB),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "DRAM: ${dramPoolGb}GB Pool",
                    color = Color(0xFFFFD54F),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
