package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.EmulationScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.Nx2ExplorerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.VulkanDriversScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EglantineCyan
import com.example.ui.theme.EglantineMagenta
import com.example.ui.theme.EglantineTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.EglantineViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: EglantineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EglantineTheme {
                EglantineApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EglantineApp(viewModel: EglantineViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val gameFilter by viewModel.gameFilter.collectAsStateWithLifecycle()
    val games by viewModel.filteredGames.collectAsStateWithLifecycle()
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val selectedDriverId by viewModel.selectedDriverId.collectAsStateWithLifecycle()
    val activeDriver by viewModel.activeDriver.collectAsStateWithLifecycle()
    val snapdragonConfig by viewModel.snapdragonConfig.collectAsStateWithLifecycle()
    val keysFirmwareState by viewModel.keysFirmwareState.collectAsStateWithLifecycle()
    val titleKeys by viewModel.titleKeys.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val updateInfo by viewModel.updateInfo.collectAsStateWithLifecycle()
    val emulation by viewModel.emulation.collectAsStateWithLifecycle()
    val isFetchingDrivers by viewModel.isFetchingDrivers.collectAsStateWithLifecycle()
    val isScrapingBoxArt by viewModel.isScrapingBoxArt.collectAsStateWithLifecycle()
    val userFeedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()
    val launchError by viewModel.launchError.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Load persisted user games, scraped box art URLs, and ROM folder URI on launch
    LaunchedEffect(Unit) {
        viewModel.initializePersistence(context)
    }

    LaunchedEffect(userFeedbackMessage) {
        userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserFeedback()
        }
    }

    // IF A GAME EMULATION SESSION IS ACTIVE, PRESENT FULL EMULATION SCREEN
    if (emulation.isRunning) {
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
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.statusBars,
            containerColor = DarkBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentTab == AppTab.GAMES,
                        onClick = { viewModel.selectTab(AppTab.GAMES) },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Games"
                            )
                        },
                        label = { Text("Games", fontSize = 11.sp, fontWeight = if (currentTab == AppTab.GAMES) FontWeight.Bold else FontWeight.Normal) },
                        colors = navigationBarItemColors(),
                        modifier = Modifier.testTag("nav_tab_games")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.DRIVERS,
                        onClick = { viewModel.selectTab(AppTab.DRIVERS) },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Vulkan Drivers"
                            )
                        },
                        label = { Text("Vulkan", fontSize = 11.sp, fontWeight = if (currentTab == AppTab.DRIVERS) FontWeight.Bold else FontWeight.Normal) },
                        colors = navigationBarItemColors(selectedColor = EglantineCyan),
                        modifier = Modifier.testTag("nav_tab_drivers")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.SETUP,
                        onClick = { viewModel.selectTab(AppTab.SETUP) },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Keys & ROMs"
                            )
                        },
                        label = { Text("Setup", fontSize = 11.sp, fontWeight = if (currentTab == AppTab.SETUP) FontWeight.Bold else FontWeight.Normal) },
                        colors = navigationBarItemColors(),
                        modifier = Modifier.testTag("nav_tab_setup")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.NX2_EXPLORER,
                        onClick = { viewModel.selectTab(AppTab.NX2_EXPLORER) },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.DeveloperBoard,
                                contentDescription = "NX2 Architecture"
                            )
                        },
                        label = { Text("NX2 Core", fontSize = 11.sp, fontWeight = if (currentTab == AppTab.NX2_EXPLORER) FontWeight.Bold else FontWeight.Normal) },
                        colors = navigationBarItemColors(),
                        modifier = Modifier.testTag("nav_tab_nx2")
                    )

                    NavigationBarItem(
                        selected = currentTab == AppTab.SETTINGS,
                        onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                        icon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = if (currentTab == AppTab.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                        colors = navigationBarItemColors(),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBackground)
            ) {
                Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        AppTab.GAMES -> {
                            GamesScreen(
                                games = games,
                                activeDriver = activeDriver,
                                searchQuery = searchQuery,
                                currentFilter = gameFilter,
                                isScrapingBoxArt = isScrapingBoxArt,
                                launchError = launchError,
                                onSearchChanged = { viewModel.setSearchQuery(it) },
                                onFilterChanged = { viewModel.setGameFilter(it) },
                                onLaunchGame = { viewModel.launchGame(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onQuickAddGame = { viewModel.selectTab(AppTab.SETUP) },
                                onOpenSetup = { viewModel.selectTab(AppTab.SETUP) },
                                onAddGameUri = { uri -> viewModel.addGameFromUri(context, uri) },
                                onScanFolderUri = { uri -> viewModel.scanFolderForRoms(context, uri) },
                                onAddCustomGame = { title, ext, isNx2 -> viewModel.addGameFile(title, ext, isNx2) },
                                onScrapeAllBoxArt = { viewModel.scrapeAllBoxArt() },
                                onScrapeGameBoxArt = { game -> viewModel.scrapeBoxArtForGame(game) },
                                onRemoveGame = { viewModel.removeGame(it) },
                                onRenameGame = { id, title -> viewModel.renameGame(id, title) },
                                onDismissLaunchError = { viewModel.dismissLaunchError() },
                                onBypassKeysAndLaunch = { viewModel.bypassKeysAndLaunch(it) }
                            )
                        }

                        AppTab.DRIVERS -> {
                            VulkanDriversScreen(
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
                        }

                        AppTab.SETUP -> {
                            SetupScreen(
                                state = keysFirmwareState,
                                titleKeys = titleKeys,
                                onInstallProdKeys = { name, count, rev ->
                                    viewModel.installProdKeys(name, count, rev)
                                },
                                onInstallTitleKeys = { name, count ->
                                    viewModel.installTitleKeys(name, count)
                                },
                                onImportTitleKeysText = { viewModel.importTitleKeysFromText(it) },
                                onLoadSampleTitleKeys = { viewModel.loadDefaultTitleKeys() },
                                onInstallFirmware = { ver, pkg ->
                                    viewModel.installFirmware(ver, pkg)
                                },
                                onSelectRomsDirectory = { path ->
                                    viewModel.selectRomsDirectory(path)
                                },
                                onAddCustomGame = { title, ext, isNx2 ->
                                    viewModel.addGameFile(title, ext, isNx2)
                                },
                                onAddGameUri = { uri ->
                                    viewModel.addGameFromUri(context, uri)
                                },
                                onScanFolderUri = { uri ->
                                    viewModel.scanFolderForRoms(context, uri)
                                }
                            )
                        }

                        AppTab.NX2_EXPLORER -> {
                            Nx2ExplorerScreen(logs = emulation.nx2Logs)
                        }

                        AppTab.SETTINGS -> {
                            SettingsScreen(
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
    }
}

@Composable
private fun navigationBarItemColors(
    selectedColor: Color = EglantineMagenta
): androidx.compose.material3.NavigationBarItemColors = NavigationBarItemDefaults.colors(
    selectedIconColor = selectedColor,
    selectedTextColor = selectedColor,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
    indicatorColor = selectedColor.copy(alpha = 0.15f)
)
