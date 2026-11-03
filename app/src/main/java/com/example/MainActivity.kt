package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GlowingGlassIcon
import com.example.ui.components.ScenicMountainLakeWallpaper
import com.example.ui.screens.EmulationScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.Nx2ExplorerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.VulkanDriversScreen
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.EglantineTheme
import com.example.ui.theme.GlowAmber
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.GlowEmerald
import com.example.ui.theme.GlowPink
import com.example.ui.theme.GlowPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.EglantineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EglantineTheme(darkTheme = true, dynamicColor = false) {
                EglantineConsoleApp()
            }
        }
    }
}

private data class GlassDockItem(
    val tab: AppTab,
    val label: String,
    val icon: ImageVector,
    val glowColor: Color,
    val testTag: String
)

@Composable
fun EglantineConsoleApp(
    viewModel: EglantineViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val emulation by viewModel.emulation.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentFilter by viewModel.gameFilter.collectAsStateWithLifecycle()
    val filteredGames by viewModel.filteredGames.collectAsStateWithLifecycle()
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val selectedDriverId by viewModel.selectedDriverId.collectAsStateWithLifecycle()
    val activeDriver by viewModel.activeDriver.collectAsStateWithLifecycle()
    val snapdragonConfig by viewModel.snapdragonConfig.collectAsStateWithLifecycle()
    val isFetchingDrivers by viewModel.isFetchingDrivers.collectAsStateWithLifecycle()
    val keysState by viewModel.keysFirmwareState.collectAsStateWithLifecycle()
    val titleKeys by viewModel.titleKeys.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val userFeedback by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()
    val launchError by viewModel.launchError.collectAsStateWithLifecycle()
    val isScrapingBoxArt by viewModel.isScrapingBoxArt.collectAsStateWithLifecycle()

    LaunchedEffect(context) {
        viewModel.initializePersistence(context)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val quickRomPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.addGameFromUri(context, uri)
        }
    }

    LaunchedEffect(userFeedback) {
        userFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserFeedback()
        }
    }

    // Back navigation handler when not on Games tab
    if (!emulation.isRunning && currentTab != AppTab.GAMES) {
        BackHandler {
            viewModel.selectTab(AppTab.GAMES)
        }
    }

    // Fullscreen Active Emulation Mode
    if (emulation.isRunning && emulation.game != null) {
        EmulationScreen(
            runtime = emulation,
            activeDriver = activeDriver,
            settings = settings,
            onPause = { viewModel.pauseEmulation() },
            onResume = { viewModel.resumeEmulation() },
            onToggleDocked = { viewModel.toggleDockedMode() },
            onToggleAudio = { viewModel.toggleAudioMute() },
            onSaveState = { viewModel.saveState() },
            onLoadState = { slot -> viewModel.loadState(slot) },
            onScreenshot = { viewModel.takeScreenshot() },
            onExit = { viewModel.exitEmulation() }
        )
        return
    }

    val dockItems = remember {
        listOf(
            GlassDockItem(
                tab = AppTab.GAMES,
                label = "Games",
                icon = Icons.Default.SportsEsports,
                glowColor = GlowPink,
                testTag = "nav_tab_games"
            ),
            GlassDockItem(
                tab = AppTab.DRIVERS,
                label = "Vulkan",
                icon = Icons.Default.Bolt,
                glowColor = GlowCyan,
                testTag = "nav_tab_drivers"
            ),
            GlassDockItem(
                tab = AppTab.SETUP,
                label = "Setup",
                icon = Icons.Default.Security,
                glowColor = GlowAmber,
                testTag = "nav_tab_setup"
            ),
            GlassDockItem(
                tab = AppTab.NX2_EXPLORER,
                label = "NX2 Core",
                icon = Icons.Default.DeveloperBoard,
                glowColor = GlowEmerald,
                testTag = "nav_tab_nx2"
            ),
            GlassDockItem(
                tab = AppTab.SETTINGS,
                label = "Settings",
                icon = Icons.Default.Settings,
                glowColor = GlowPurple,
                testTag = "nav_tab_settings"
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Full-Bleed Scenic Mountain & Lake Twilight Wallpaper
        ScenicMountainLakeWallpaper()

        // 2. Translucent iPhone Glass Interface Layer
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                IosGlassHeaderBar(
                    activeDriverName = activeDriver?.name?.take(18) ?: "Mesa Turnip",
                    keysReady = keysState.prodKeysInstalled,
                    onQuickAddRom = {
                        try {
                            quickRomPickerLauncher.launch(arrayOf("*/*"))
                        } catch (_: Exception) {
                            viewModel.selectTab(AppTab.SETUP)
                        }
                    }
                )
            },
            bottomBar = {
                IosGlassBottomDock(
                    items = dockItems,
                    currentTab = currentTab,
                    onSelectTab = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.GAMES -> GamesScreen(
                        games = filteredGames,
                        activeDriver = activeDriver,
                        searchQuery = searchQuery,
                        currentFilter = currentFilter,
                        isScrapingBoxArt = isScrapingBoxArt,
                        launchError = launchError,
                        onSearchChanged = { viewModel.setSearchQuery(it) },
                        onFilterChanged = { viewModel.setGameFilter(it) },
                        onLaunchGame = { viewModel.launchGame(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onQuickAddGame = {
                            try {
                                quickRomPickerLauncher.launch(arrayOf("*/*"))
                            } catch (_: Exception) {
                                viewModel.selectTab(AppTab.SETUP)
                            }
                        },
                        onOpenSetup = { viewModel.selectTab(AppTab.SETUP) },
                        onAddGameUri = { uri -> viewModel.addGameFromUri(context, uri) },
                        onScanFolderUri = { treeUri -> viewModel.scanFolderForRoms(context, treeUri) },
                        onAddCustomGame = { title, ext, isNx2 ->
                            viewModel.addGameFile(title, ext, isNx2)
                        },
                        onScrapeAllBoxArt = { viewModel.scrapeAllBoxArt() },
                        onScrapeGameBoxArt = { game -> viewModel.scrapeBoxArtForGame(game, notifyUser = true) },
                        onRemoveGame = { programId -> viewModel.removeGame(programId) },
                        onRenameGame = { programId, newTitle -> viewModel.renameGame(programId, newTitle) },
                        onDismissLaunchError = { viewModel.dismissLaunchError() },
                        onBypassKeysAndLaunch = { game -> viewModel.bypassKeysAndLaunch(game) }
                    )

                    AppTab.DRIVERS -> VulkanDriversScreen(
                        drivers = drivers,
                        selectedDriverId = selectedDriverId,
                        snapdragonConfig = snapdragonConfig,
                        isFetchingOnline = isFetchingDrivers,
                        onSelectDriver = { viewModel.selectDriver(it) },
                        onApplySnapdragonChipset = { viewModel.applySnapdragonProfile(it) },
                        onToggleGmemBypass = { viewModel.toggleGmemBypass(it) },
                        onToggleUbwc = { viewModel.toggleUbwcCompression(it) },
                        onToggleTurbo = { viewModel.toggleAdrenoTurbo(it) },
                        onToggleKgsl = { viewModel.toggleKgslFastPath(it) },
                        onFetchOnlineDrivers = { viewModel.fetchOnlineTurnipDrivers() },
                        onInstallCustomDriver = { name, ver, desc ->
                            viewModel.installCustomDriver(name, ver, desc)
                        }
                    )

                    AppTab.SETUP -> SetupScreen(
                        state = keysState,
                        titleKeys = titleKeys,
                        onInstallProdKeys = { name, count, rev ->
                            viewModel.installProdKeys(name, count, rev)
                        },
                        onInstallTitleKeys = { name, count ->
                            viewModel.installTitleKeys(name, count)
                        },
                        onImportTitleKeysText = { rawText ->
                            viewModel.importTitleKeysFromText(rawText)
                        },
                        onLoadSampleTitleKeys = {
                            viewModel.loadDefaultTitleKeys()
                        },
                        onInstallFirmware = { ver, pkg ->
                            viewModel.installFirmware(ver, pkg)
                        },
                        onSelectRomsDirectory = { dir ->
                            viewModel.selectRomsDirectory(dir)
                        },
                        onAddCustomGame = { title, ext, isNx2 ->
                            viewModel.addGameFile(title, ext, isNx2)
                        },
                        onAddGameUri = { uri -> viewModel.addGameFromUri(context, uri) },
                        onScanFolderUri = { treeUri -> viewModel.scanFolderForRoms(context, treeUri) }
                    )

                    AppTab.NX2_EXPLORER -> Nx2ExplorerScreen(
                        logs = emulation.nx2Logs
                    )

                    AppTab.SETTINGS -> SettingsScreen(
                        settings = settings,
                        updateInfo = updateInfo,
                        onSettingsChanged = { viewModel.updateSettings(it) },
                        onApplyAaaPreset = { viewModel.applyAAAPreset(it) },
                        onStartUpdate = { viewModel.startApkUpdate() },
                        onInstallUpdate = { viewModel.installApkUpdate() },
                        onCheckForUpdates = { viewModel.checkForUpdates() }
                    )
                }
            }
        }
    }
}

/**
 * Minimal iPhone-style floating frosted glass top status pill.
 */
@Composable
private fun IosGlassHeaderBar(
    activeDriverName: String,
    keysReady: Boolean,
    onQuickAddRom: () -> Unit
) {
    val pillShape = RoundedCornerShape(26.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("main_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(pillShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                )
                .background(Color(0x400D1527))
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.45f),
                                Color.White.copy(alpha = 0.14f)
                            )
                        )
                    ),
                    pillShape
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlowingGlassIcon(
                    icon = Icons.Default.SportsEsports,
                    contentDescription = null,
                    glowColor = GlowPink,
                    isSelected = true,
                    size = 36.dp,
                    iconSize = 18.dp
                )
                Column {
                    Text(
                        text = "Eglantine",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (keysReady) AccentSuccess else GlowAmber)
                        )
                        Text(
                            text = activeDriverName,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick Add ROM Glowing Glass Button
            Box(
                modifier = Modifier
                    .clickable { onQuickAddRom() }
                    .testTag("top_quick_add_rom")
            ) {
                GlowingGlassIcon(
                    icon = Icons.Default.Add,
                    contentDescription = "Add ROM File",
                    glowColor = GlowCyan,
                    isSelected = true,
                    size = 38.dp,
                    iconSize = 20.dp
                )
            }
        }
    }
}

/**
 * Floating iPhone-style translucent glass dock at the bottom where each icon has
 * its own distinct colour glow around it.
 */
@Composable
private fun IosGlassBottomDock(
    items: List<GlassDockItem>,
    currentTab: AppTab,
    onSelectTab: (AppTab) -> Unit
) {
    val dockShape = RoundedCornerShape(32.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("main_bottom_nav")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dockShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.10f)
                        )
                    )
                )
                .background(Color(0x590B1120))
                .border(
                    BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.16f)
                            )
                        )
                    ),
                    dockShape
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentTab == item.tab
                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onSelectTab(item.tab) }
                        .padding(vertical = 2.dp)
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GlowingGlassIcon(
                        icon = item.icon,
                        contentDescription = item.label,
                        glowColor = item.glowColor,
                        isSelected = isSelected,
                        size = 44.dp,
                        iconSize = 22.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
