package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.TvOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmulatorSettings
import com.example.model.VulkanDriver
import com.example.ui.components.GamepadOverlay
import com.example.ui.components.PlayableGameEngineViewport
import com.example.ui.components.StatsOverlay
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EglantineCyan
import com.example.ui.theme.EglantineMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EmulationRuntime
import kotlinx.coroutines.delay

@Composable
fun EmulationScreen(
    runtime: EmulationRuntime,
    activeDriver: VulkanDriver?,
    settings: EmulatorSettings,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onToggleDocked: () -> Unit,
    onSaveState: () -> Unit,
    onLoadState: (String) -> Unit,
    onScreenshot: () -> Unit,
    onExit: () -> Unit
) {
    // Intercept back button to safely exit emulation
    BackHandler {
        onExit()
    }

    var showMenu by remember { mutableStateOf(false) }
    var showStateDialog by remember { mutableStateOf(false) }
    var showLogsDialog by remember { mutableStateOf(false) }
    var userActionFeedback by remember { mutableStateOf<String?>(null) }

    // Live interactive controller state routed directly into the playable game engine
    var lastInputCommand by remember { mutableStateOf("") }
    var inputEventCounter by remember { mutableLongStateOf(0L) }
    var leftStickX by remember { mutableFloatStateOf(0f) }
    var leftStickY by remember { mutableFloatStateOf(0f) }
    var rightStickX by remember { mutableFloatStateOf(0f) }
    var rightStickY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(runtime.recentActionMessage) {
        runtime.recentActionMessage?.let { msg ->
            userActionFeedback = msg
            delay(2000)
            userActionFeedback = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("emulation_screen_view")
    ) {
        // 1. LIVE INTERACTIVE PLAYABLE GAME ENGINE VIEWPORT
        PlayableGameEngineViewport(
            gameTitle = runtime.game?.title ?: "Tetris Forever",
            programIdHex = runtime.game?.programIdHex ?: "01007EF00011E000",
            isNx2 = runtime.game?.isNx2 == true,
            isPaused = runtime.isPaused,
            isDocked = runtime.isDocked,
            lastInputCommand = lastInputCommand,
            inputEventCounter = inputEventCounter,
            leftStickX = leftStickX,
            leftStickY = leftStickY,
            rightStickX = rightStickX,
            rightStickY = rightStickY
        )

        // Safe-area container for all HUD & controller overlays so nothing overlaps the status bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 2. IN-GAME ACTION BAR (TOP)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xEE12111A), Color(0x5512111A), Color(0x0012111A))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier.testTag("btn_exit_emulation")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Emulation",
                            tint = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = runtime.game?.title ?: "Nintendo Switch",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${if (runtime.game?.isNx2 == true) "NX2 Ampere" else "Switch"} • ${activeDriver?.name?.take(18) ?: "Mesa Turnip"}",
                            color = EglantineCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                // Quick actions: Docked toggle, Pause/Resume, Menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onToggleDocked,
                        modifier = Modifier.testTag("btn_toggle_docked")
                    ) {
                        Icon(
                            imageVector = if (runtime.isDocked) Icons.Default.Tv else Icons.Default.TvOff,
                            contentDescription = "Toggle Docked Mode",
                            tint = if (runtime.isDocked) EglantineCyan else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { if (runtime.isPaused) onResume() else onPause() },
                        modifier = Modifier.testTag("btn_pause_resume")
                    ) {
                        Icon(
                            imageVector = if (runtime.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause or Resume",
                            tint = if (runtime.isPaused) Color(0xFFFFD54F) else Color.White
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("btn_emulation_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Save State", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onSaveState()
                                },
                                leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = EglantineMagenta) }
                            )
                            DropdownMenuItem(
                                text = { Text("Load State (${runtime.saveStates.size})", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    showStateDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Console Logs (${runtime.nx2Logs.size})", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    showLogsDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, tint = EglantineCyan) }
                            )
                            DropdownMenuItem(
                                text = { Text("Take Screenshot", color = TextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onScreenshot()
                                },
                                leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = EglantineCyan) }
                            )
                            DropdownMenuItem(
                                text = { Text("Exit Game", color = Color(0xFFFF5252)) },
                                onClick = {
                                    showMenu = false
                                    onExit()
                                }
                            )
                        }
                    }
                }
            }

            // 3. STATS OVERLAY HUD (Positioned cleanly below the shoulder trigger row)
            if (settings.showStatsOverlay) {
                StatsOverlay(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 104.dp),
                    fps = runtime.fps,
                    frametimeMs = runtime.frametimeMs,
                    driverName = activeDriver?.name ?: "Mesa Turnip Vulkan",
                    shadersBuilding = runtime.shadersBuilding,
                    appRamMb = runtime.appRamMb,
                    sysRamMb = runtime.sysRamMb,
                    batteryTempC = runtime.batteryTempC,
                    socPowerWatts = runtime.socPowerWatts,
                    isDocked = runtime.isDocked,
                    isNx2 = runtime.game?.isNx2 == true,
                    adrenoClocksMhz = runtime.adrenoClocksMhz,
                    cpuBackend = runtime.cpuBackendType,
                    fastmem = runtime.fastmemActive,
                    dramPoolGb = runtime.dramPoolGb
                )
            }

            // 4. ON-SCREEN GAMEPAD OVERLAY
            if (settings.showInputOverlay) {
                GamepadOverlay(
                    modifier = Modifier.fillMaxSize(),
                    opacity = settings.overlayOpacity,
                    hapticsEnabled = settings.hapticFeedback,
                    onButtonPressed = { btn ->
                        when (btn) {
                            "CAPTURE" -> onScreenshot()
                            "HOME" -> showMenu = true
                            "+" -> if (runtime.isPaused) onResume() else onPause()
                            else -> {
                                lastInputCommand = btn
                                inputEventCounter++
                            }
                        }
                    },
                    onLeftStickMoved = { nx, ny ->
                        leftStickX = nx
                        leftStickY = ny
                    },
                    onRightStickMoved = { nx, ny ->
                        rightStickX = nx
                        rightStickY = ny
                    }
                )
            }

            // 5. TOAST ACTION FEEDBACK
            AnimatedVisibility(
                visible = userActionFeedback != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
            ) {
                userActionFeedback?.let { msg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xDD1E1B29))
                            .border(1.dp, EglantineMagenta, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = msg, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // PAUSE OVERLAY BANNER
        if (runtime.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("EMULATION PAUSED", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Game clock frozen • Vulkan queues suspended", color = TextSecondary, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onResume,
                                colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta)
                            ) {
                                Text("Resume")
                            }
                            Button(
                                onClick = onExit,
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight)
                            ) {
                                Text("Exit")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStateDialog) {
        LoadStateDialog(
            states = runtime.saveStates,
            onDismiss = { showStateDialog = false },
            onSelect = { slot ->
                showStateDialog = false
                onLoadState(slot)
            }
        )
    }

    if (showLogsDialog) {
        EmulationLogsDialog(
            logs = runtime.nx2Logs,
            onDismiss = { showLogsDialog = false }
        )
    }
}

@Composable
fun LoadStateDialog(
    states: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Save State to Load", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                states.forEach { slot ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(slot) },
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceHighlight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = slot, color = TextPrimary, fontSize = 13.sp)
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EglantineMagenta)
                        }
                    }
                }
            }
        },
        confirmButton = {
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
fun EmulationLogsDialog(
    logs: List<String>,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Live Emulation Logs", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(Color(0xFF09080F), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs.size) { index ->
                        val log = logs[index]
                        val color = when {
                            log.contains("[Crypto]") || log.contains("Verified") -> Color(0xFF69F0AE)
                            log.contains("[Loader]") -> EglantineMagenta
                            log.contains("[Vulkan]") || log.contains("[Snapdragon]") -> EglantineCyan
                            log.contains("Error") || log.contains("Failed") -> Color(0xFFFF5252)
                            else -> TextSecondary
                        }
                        Text(
                            text = log,
                            color = color,
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
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

