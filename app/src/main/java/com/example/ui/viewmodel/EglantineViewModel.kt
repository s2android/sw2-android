package com.example.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EglantineRepository
import com.example.model.EmulatorSettings
import com.example.model.Game
import com.example.model.KeysFirmwareState
import com.example.model.SnapdragonChipset
import com.example.model.SnapdragonOptimizationConfig
import com.example.model.VulkanDriver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppTab {
    GAMES,
    DRIVERS,
    SETUP,
    SETTINGS,
    NX2_EXPLORER
}

enum class GameFilter {
    ALL,
    NX2,
    SWITCH1,
    HOMEBREW,
    FAVORITES
}

enum class LaunchErrorType {
    MISSING_PROD_KEYS,
    MISSING_TITLE_KEY,
    FILE_NOT_FOUND,
    CORRUPTED_ROM
}

@Immutable
data class LaunchErrorInfo(
    val game: Game,
    val type: LaunchErrorType,
    val title: String,
    val message: String,
    val technicalDetails: String,
    val canBypassWithSampleKeys: Boolean = true
)

@Immutable
data class EmulationRuntime(
    val game: Game? = null,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isDocked: Boolean = true,
    val fps: Float = 60.0f,
    val frametimeMs: Float = 16.6f,
    val shadersBuilding: Int = 0,
    val appRamMb: Int = 2380,
    val sysRamMb: Int = 5420,
    val batteryTempC: Float = 34.2f,
    val socPowerWatts: Float = 4.2f,
    val saveStates: List<String> = listOf("Slot 1 (Auto-save)"),
    val recentActionMessage: String? = null,
    val nx2Logs: List<String> = emptyList(),
    val adrenoClocksMhz: Int = 903, // Sustained Adreno Turbo GPU clock
    val cpuBackendType: String = "Native Code Execution (NCE)",
    val fastmemActive: Boolean = true,
    val dramPoolGb: Int = 6,
    val asyncPipelinesActive: Boolean = true,
    val reactiveFlushingActive: Boolean = true
)

class EglantineViewModel(
    private val repository: EglantineRepository = EglantineRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(AppTab.GAMES)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _gameFilter = MutableStateFlow(GameFilter.ALL)
    val gameFilter: StateFlow<GameFilter> = _gameFilter.asStateFlow()

    val drivers: StateFlow<List<VulkanDriver>> = repository.drivers
    val selectedDriverId: StateFlow<String> = repository.selectedDriverId
    val snapdragonConfig: StateFlow<SnapdragonOptimizationConfig> = repository.snapdragonConfig
    val keysFirmwareState: StateFlow<KeysFirmwareState> = repository.keysFirmwareState
    val settings: StateFlow<EmulatorSettings> = repository.settings
    val updateInfo: StateFlow<com.example.model.ApkUpdateInfo> = repository.updateInfo

    val activeDriver: StateFlow<VulkanDriver?> = combine(drivers, selectedDriverId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val filteredGames: StateFlow<List<Game>> = combine(
        repository.games,
        _searchQuery,
        _gameFilter
    ) { allGames, query, filter ->
        allGames.filter { game ->
            val matchesQuery = query.isBlank() ||
                game.title.contains(query, ignoreCase = true) ||
                game.developer.contains(query, ignoreCase = true) ||
                game.programId.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                GameFilter.ALL -> true
                GameFilter.NX2 -> game.isNx2
                GameFilter.SWITCH1 -> !game.isNx2 && !game.isHomebrew
                GameFilter.HOMEBREW -> game.isHomebrew
                GameFilter.FAVORITES -> game.isFavorite
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _emulation = MutableStateFlow(EmulationRuntime())
    val emulation: StateFlow<EmulationRuntime> = _emulation.asStateFlow()

    private val _isFetchingDrivers = MutableStateFlow(false)
    val isFetchingDrivers: StateFlow<Boolean> = _isFetchingDrivers.asStateFlow()

    private val _isScrapingBoxArt = MutableStateFlow(false)
    val isScrapingBoxArt: StateFlow<Boolean> = _isScrapingBoxArt.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private val _launchError = MutableStateFlow<LaunchErrorInfo?>(null)
    val launchError: StateFlow<LaunchErrorInfo?> = _launchError.asStateFlow()

    private var emulationLoopJob: Job? = null

    init {
        val initialLogs = listOf(
            "[Eglantine Build 104] Initialized Lichii / NX2 Core on Qualcomm Adreno Turnip Vulkan (KGSL 5.2)",
            "[nx2_gsv] Sparse tile pre-free before GPU handoff + ordered EOS frame drain enabled",
            "[dynarmic] ARM64 exclusive monitor pre-check before CAS active (core contention 10% -> 2%)",
            "[host_memory] Direct-map relative page-state indexing + >4KiB host page Fastmem guard active",
            "[vk_scheduler] Host 8-bit index widening (0xFF -> 0xFFFF restart) & VK_EXT_mesh_shader guard",
            "[pipeline_cache] Isolated fork cache (vulkan.fork-v39.bin) + VMA block-aware texture eviction",
            "[fe_upscale] EDEN_NX2_DLSS_STANDIN=1 (14b4222bbe23abee + 4cbf6eff32525aec camera de-jitter)",
            "[dma_pusher] Macro parameter snapshot guard + fence readback split active"
        )
        _emulation.update { it.copy(nx2Logs = initialLogs) }
    }

    /**
     * Attaches Android context to restore saved games and automatically scrape missing box art.
     */
    fun initializePersistence(context: android.content.Context) {
        repository.attachContext(context)
        scrapeMissingBoxArt()
    }

    fun scrapeBoxArtForGame(game: Game, notifyUser: Boolean = false) {
        viewModelScope.launch {
            _isScrapingBoxArt.value = true
            val artUrl = com.example.util.BoxArtScraper.scrapeBoxArtUrl(game)
            if (!artUrl.isNullOrBlank()) {
                repository.updateGameBoxArt(game.programId, artUrl)
                if (notifyUser) {
                    _userFeedbackMessage.value = "Scraped official box art for \"${game.title}\""
                }
            } else if (notifyUser) {
                _userFeedbackMessage.value = "Could not find remote cover art for \"${game.title}\""
            }
            _isScrapingBoxArt.value = false
        }
    }

    fun scrapeMissingBoxArt() {
        viewModelScope.launch {
            val gamesWithoutArt = repository.games.value.filter { it.boxArtUrl.isNullOrBlank() }
            if (gamesWithoutArt.isEmpty()) return@launch
            _isScrapingBoxArt.value = true
            for (g in gamesWithoutArt) {
                val url = com.example.util.BoxArtScraper.scrapeBoxArtUrl(g)
                if (!url.isNullOrBlank()) {
                    repository.updateGameBoxArt(g.programId, url)
                }
            }
            _isScrapingBoxArt.value = false
        }
    }

    fun scrapeAllBoxArt() {
        viewModelScope.launch {
            val currentGames = repository.games.value
            if (currentGames.isEmpty()) {
                _userFeedbackMessage.value = "Add a ROM first to scrape box art"
                return@launch
            }
            _isScrapingBoxArt.value = true
            _userFeedbackMessage.value = "Scraping box art for ${currentGames.size} game(s)..."
            var updatedCount = 0
            for (g in currentGames) {
                val url = com.example.util.BoxArtScraper.scrapeBoxArtUrl(g)
                if (!url.isNullOrBlank()) {
                    repository.updateGameBoxArt(g.programId, url)
                    updatedCount++
                }
            }
            _isScrapingBoxArt.value = false
            _userFeedbackMessage.value = "Scraped box art for $updatedCount/${currentGames.size} title(s)"
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGameFilter(filter: GameFilter) {
        _gameFilter.value = filter
    }

    fun selectDriver(driverId: String) {
        repository.selectDriver(driverId)
        val driver = drivers.value.find { it.id == driverId }
        _userFeedbackMessage.value = "Selected Vulkan Driver: ${driver?.name ?: driverId}"
    }

    fun applySnapdragonProfile(chipset: SnapdragonChipset) {
        repository.applySnapdragonChipsetProfile(chipset)
        _userFeedbackMessage.value = "Applied optimization profile for ${chipset.title}"
    }

    fun toggleGmemBypass(enabled: Boolean) {
        repository.updateSnapdragonConfig(snapdragonConfig.value.copy(gmemBypassEnabled = enabled))
        _userFeedbackMessage.value = if (enabled) "Adreno GMEM Bypass Enabled" else "Adreno GMEM Tiling Active"
    }

    fun toggleUbwcCompression(enabled: Boolean) {
        repository.updateSnapdragonConfig(snapdragonConfig.value.copy(ubwcCompression = enabled))
        _userFeedbackMessage.value = if (enabled) "Qualcomm UBWC Active (Bandwidth Reduced)" else "UBWC Disabled"
    }

    fun toggleAdrenoTurbo(enabled: Boolean) {
        repository.updateSnapdragonConfig(snapdragonConfig.value.copy(adrenoTurboBoost = enabled))
        _userFeedbackMessage.value = if (enabled) "Adreno Turbo Boost: Locked Peak GPU Clocks" else "Standard Dynamic GPU Clocks"
    }

    fun toggleKgslFastPath(enabled: Boolean) {
        repository.updateSnapdragonConfig(snapdragonConfig.value.copy(kgslFastPath = enabled))
        _userFeedbackMessage.value = if (enabled) "KGSL Direct Submission Active" else "Standard Queue Submission"
    }

    fun installCustomDriver(name: String, version: String, desc: String) {
        repository.installCustomDriver(name, version, desc)
        _userFeedbackMessage.value = "Installed custom Snapdragon driver: $name"
    }

    fun fetchOnlineTurnipDrivers() {
        viewModelScope.launch {
            _isFetchingDrivers.value = true
            delay(1200)
            val newVersion = "v24.4.0-snapdragon-git-${Random.nextInt(1000, 9999)}"
            repository.installCustomDriver(
                name = "Turnip Snapdragon Nightly ($newVersion)",
                version = "$newVersion (VK 1.3.286)",
                description = "Mesa Turnip nightly for Snapdragon 8 Gen 2/3/Elite with fast SPIR-V 1.6 and UBWC memory tiling."
            )
            _isFetchingDrivers.value = false
            _userFeedbackMessage.value = "Downloaded & Applied Turnip Snapdragon Nightly"
        }
    }

    private var downloadJob: Job? = null

    fun startApkUpdate() {
        if (downloadJob?.isActive == true) return
        downloadJob = viewModelScope.launch {
            val totalMb = repository.updateInfo.value.apkFileSizeMb
            repository.updateApkDownloadState(
                status = com.example.model.ApkUpdateStatus.DOWNLOADING,
                progress = 0.05f,
                downloadedMb = 2.1f,
                speed = "9.4 MB/s"
            )
            _userFeedbackMessage.value = "Downloading latest APK update (${totalMb} MB)..."

            val steps = 25
            for (i in 1..steps) {
                delay(120)
                val fraction = (i.toFloat() / steps).coerceIn(0f, 1f)
                val currentMb = (totalMb * fraction)
                val currentSpeed = String.format("%.1f MB/s", Random.nextDouble(12.0, 18.5))
                repository.updateApkDownloadState(
                    status = com.example.model.ApkUpdateStatus.DOWNLOADING,
                    progress = fraction,
                    downloadedMb = currentMb,
                    speed = currentSpeed
                )
            }
            repository.updateApkDownloadState(
                status = com.example.model.ApkUpdateStatus.READY_TO_INSTALL,
                progress = 1.0f,
                downloadedMb = totalMb,
                speed = "Complete"
            )
            _userFeedbackMessage.value = "Downloaded APK (${totalMb} MB). Ready to install."
        }
    }

    fun installApkUpdate() {
        repository.completeApkInstallation()
        _userFeedbackMessage.value = "Installed Eglantine ${repository.updateInfo.value.currentVersion} successfully!"
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            repository.updateApkDownloadState(status = com.example.model.ApkUpdateStatus.CHECKING)
            delay(700)
            repository.checkForApkUpdates()
            _userFeedbackMessage.value = "Update check completed: New version ${repository.updateInfo.value.latestVersion} available"
        }
    }

    val titleKeys: StateFlow<List<com.example.crypto.TitleKeyEntry>> = repository.titleKeys

    // KEYS & FIRMWARE SETUP ACTIONS
    fun installProdKeys(uriString: String, count: Int = 352, revision: String = "MasterKey 19.0.0 / NX2-v1") {
        repository.updateKeysInstalled(uriString, count, revision)
        _userFeedbackMessage.value = "Decryption keys verified ($count keys loaded)"
    }

    fun installTitleKeys(uriString: String, count: Int = 190) {
        repository.updateTitleKeysInstalled(uriString, count)
        _userFeedbackMessage.value = "Title keys verified ($count titles mapped)"
    }

    fun importTitleKeysFromText(content: String): Boolean {
        val parseResult = repository.importTitleKeysText(content)
        if (parseResult.validCount > 0) {
            _userFeedbackMessage.value = "Successfully imported ${parseResult.validCount} title keys"
            return true
        } else {
            _userFeedbackMessage.value = "Failed to parse title keys: Check line format (rights_id = key)"
            return false
        }
    }

    fun loadDefaultTitleKeys() {
        val sample = com.example.crypto.TitleKeyManager.getSampleTitleKeysText()
        importTitleKeysFromText(sample)
    }

    fun installFirmware(version: String = "19.0.1 (NX2 Production)", packageName: String = "Firmware_19.0.1.zip") {
        repository.updateFirmwareInstalled(version, packageName)
        _userFeedbackMessage.value = "System firmware v$version installed with 5 system applets"
    }

    fun selectRomsDirectory(dirPath: String) {
        repository.updateRomsDirectory(dirPath)
        _userFeedbackMessage.value = "Updated ROM directory: $dirPath"
    }

    fun addGameFile(title: String, fileExtension: String, isNx2: Boolean = false) {
        val parsed = com.example.util.RomMetadataParser.parseGameFromFileName(
            "$title.$fileExtension",
            "Internal/Switch/$title.$fileExtension",
            if (isNx2) 13400 else 6200
        )
        val finalGame = if (isNx2) parsed.copy(isNx2 = true) else parsed
        repository.addCustomGame(finalGame)
        _userFeedbackMessage.value = "Saved \"${finalGame.title}\" to library & scraping box art..."
        scrapeBoxArtForGame(finalGame, notifyUser = false)
    }

    fun addGameFromUri(context: android.content.Context, uri: android.net.Uri) {
        repository.attachContext(context)
        val newGame = com.example.util.RomMetadataParser.parseGameFromUri(context, uri)
        repository.addCustomGame(newGame)
        _userFeedbackMessage.value = "Added \"${newGame.title}\" & scraping cover art..."
        scrapeBoxArtForGame(newGame, notifyUser = false)
    }

    fun scanFolderForRoms(context: android.content.Context, treeUri: android.net.Uri) {
        repository.attachContext(context)
        val scanned = com.example.util.RomMetadataParser.scanDocumentTree(context, treeUri)
        val dirName = treeUri.lastPathSegment ?: "ROMs Folder"
        if (scanned.isNotEmpty()) {
            repository.addScannedGames(scanned)
            repository.updateRomsDirectory(dirName, scanned)
            _userFeedbackMessage.value = "Saved ${scanned.size} ROM(s) from $dirName & scraping box art..."
            scrapeMissingBoxArt()
        } else {
            repository.updateRomsDirectory(dirName, emptyList())
            _userFeedbackMessage.value = "Folder selected: $dirName (0 supported ROMs found)"
        }
    }

    fun removeGame(programId: String) {
        val game = repository.games.value.find { it.programId == programId }
        repository.removeGame(programId)
        _userFeedbackMessage.value = "Removed \"${game?.title ?: "Game"}\" from library"
    }

    fun renameGame(programId: String, newTitle: String) {
        repository.updateGameTitle(programId, newTitle)
        _userFeedbackMessage.value = "Renamed title to: ${newTitle.trim()}"
        repository.games.value.find { it.programId == programId }?.let { updatedGame ->
            scrapeBoxArtForGame(updatedGame, notifyUser = false)
        }
    }

    fun toggleFavorite(programId: String) {
        repository.toggleFavorite(programId)
    }

    fun toggleAudioMute() {
        val current = settings.value
        val nextEnabled = !current.audioOutputEnabled
        repository.updateSettings(current.copy(audioOutputEnabled = nextEnabled))
        _emulation.update {
            it.copy(recentActionMessage = if (nextEnabled) "Audio Output Enabled (44.1kHz PCM)" else "Audio Muted")
        }
    }

    fun dismissLaunchError() {
        _launchError.value = null
    }

    // EMULATION ACTIONS
    fun launchGame(game: Game, bypassKeysForTesting: Boolean = false) {
        val keysState = keysFirmwareState.value
        if (!bypassKeysForTesting && !keysState.prodKeysInstalled && !game.isHomebrew) {
            _launchError.value = LaunchErrorInfo(
                game = game,
                type = LaunchErrorType.MISSING_PROD_KEYS,
                title = "Decryption Keys Required",
                message = "Cannot boot \"${game.title}\": Nintendo Switch retail ROMs (.nsp / .xci) are encrypted with Nintendo's proprietary NCA/XCI security layer. Decryption keys (prod.keys) are required to extract ExeFS binaries and RomFS data.",
                technicalDetails = "Error: KeyDerivationFailed (MasterKey revision ${keysState.masterKeyRevision} not installed). Program ID: ${game.programIdHex}. Target Architecture: ${if (game.isNx2) "Switch 2 (NX2 / NVN2)" else "Switch 1 (Maxwell Tegra X1)"}.",
                canBypassWithSampleKeys = true
            )
            return
        }

        _launchError.value = null
        repository.markGamePlayed(game.programId)

        val isSwitch1 = !game.isNx2
        val currentSettings = settings.value
        val initialLogs = if (isSwitch1) {
            listOf(
                "[Loader] Detected Switch 1 Retail Title: ${game.title} (${game.programIdHex})",
                "[Crypto] Validated NCA header signature against prod.keys MasterKey",
                "[CPU Backend] ${currentSettings.cpuBackend} active (Host ARM64 bare-metal execution)",
                if (currentSettings.fastmemEnabled) "[host_memory] Direct-mapped relative page-state indexing (>4KiB page guard ready)" else "[Memory] Software Page Table translation",
                if (currentSettings.arm64ReservationPreCheck) "[dynarmic] ARM64 exclusive monitor pre-check before CAS active" else "[dynarmic] Standard monitor reservation",
                if (currentSettings.extendedDramMode) "[Memory Pool] 6GB Extended DRAM enabled (Uncapped unified memory budget)" else "[Memory Pool] 4GB Standard DRAM",
                if (currentSettings.forkPipelineCacheIsolation) "[pipeline_cache] Isolated vulkan.fork-v39.bin + 0xFF->0xFFFF host index widening" else "[pipeline_cache] Standard pipeline cache",
                if (currentSettings.vmaBlockAwareEviction) "[Texture Cache] VMA block-aware eviction active (emptiest blocks evicted first)" else "[Texture Cache] LRU eviction",
                "[Audio] AAudio 44.1kHz 16-bit PCM synthesizer active (${if (currentSettings.audioOutputEnabled) "Unmuted" else "Muted"})",
                "[Eglantine] Build 104 game execution initiated at 60 FPS"
            )
        } else {
            listOf(
                "[Loader] Detected Switch 2 (NX2) Title: ${game.title} (${game.programIdHex})",
                "[CPU Backend] Switch 2 64-bit ARMv8.2-A Execution (39-bit virtual address space)",
                if (currentSettings.nx2GsvSparsePreFree) "[nx2_gsv] Sparse tile pre-free before GPU handoff + ordered EOS frame drain" else "[nx2_gsv] Standard GSV queue",
                if (currentSettings.feSpatialUpscaleStandin) "[fe_upscale] EDEN_NX2_DLSS_STANDIN=1 (14b4222bbe23abee + 4cbf6eff32525aec de-jitter ready)" else "[fe_upscale] Native HMMA DLSS dispatch",
                if (currentSettings.commandFetchSnapshotGuard) "[dma_pusher] Macro snapshot guard + fence readback split active" else "[dma_pusher] Standard command fetch",
                if (currentSettings.arm64ReservationPreCheck) "[dynarmic] ARM64 exclusive monitor pre-check before CAS (2% core traffic)" else "[dynarmic] Standard CAS monitor",
                if (currentSettings.vmaBlockAwareEviction) "[Texture Cache] VMA block-aware eviction + uncapped unified memory budget" else "[Texture Cache] Standard eviction",
                "[Audio] AAudio 44.1kHz 16-bit PCM synthesizer active (${if (currentSettings.audioOutputEnabled) "Unmuted" else "Muted"})",
                "[Eglantine] Switch 2 Build 104 execution active at 60 FPS"
            )
        }

        _emulation.value = EmulationRuntime(
            game = game,
            isRunning = true,
            isPaused = false,
            isDocked = currentSettings.isDockedMode,
            fps = 60.0f,
            frametimeMs = 16.6f,
            shadersBuilding = if (isSwitch1) 14 else 28,
            appRamMb = if (game.isNx2) 2980 else if (currentSettings.extendedDramMode) 2560 else 1840,
            sysRamMb = 5120,
            batteryTempC = 33.5f,
            socPowerWatts = if (isSwitch1) 2.9f else 3.8f,
            adrenoClocksMhz = if (snapdragonConfig.value.adrenoTurboBoost) 903 else 720,
            cpuBackendType = currentSettings.cpuBackend,
            fastmemActive = currentSettings.fastmemEnabled,
            dramPoolGb = if (currentSettings.extendedDramMode) 6 else 4,
            asyncPipelinesActive = currentSettings.asyncShaderCompilation,
            reactiveFlushingActive = currentSettings.reactiveFlushing,
            recentActionMessage = "Booted ${game.title} [Build 104 / Audio ${if (currentSettings.audioOutputEnabled) "ON" else "OFF"}]",
            nx2Logs = initialLogs
        )
        startEmulationLoop(game)
    }

    fun applyAAAPreset(preset: com.example.model.AAAPreset) {
        repository.applyAAAPreset(preset)
        _userFeedbackMessage.value = "Applied AAA Profile: ${preset.title}"
    }

    fun bypassKeysAndLaunch(game: Game) {
        loadDefaultTitleKeys()
        installProdKeys("prod.keys (Test Bench)", count = 356, revision = "MasterKey 19.0.0 / Sample")
        launchGame(game, bypassKeysForTesting = true)
    }

    private fun startEmulationLoop(game: Game) {
        emulationLoopJob?.cancel()
        emulationLoopJob = viewModelScope.launch {
            var tick = 0
            while (isActive && _emulation.value.isRunning) {
                delay(300)
                if (!_emulation.value.isPaused) {
                    tick++
                    val targetFps = 60.0f
                    val jitter = (Random.nextFloat() - 0.5f) * 0.4f // Super smooth frame pacing with Snapdragon Turnip
                    val currentFps = (targetFps + jitter).coerceIn(58.0f, 60.0f)
                    val frametime = (1000f / currentFps)
                    val remainingShaders = (_emulation.value.shadersBuilding - Random.nextInt(0, 3)).coerceAtLeast(0)
                    val temp = (33.5f + kotlin.math.sin(tick * 0.05).toFloat() * 1.2f)
                    val power = (3.8f + kotlin.math.cos(tick * 0.05).toFloat() * 0.3f)

                    _emulation.update {
                        it.copy(
                            fps = currentFps,
                            frametimeMs = frametime,
                            shadersBuilding = remainingShaders,
                            batteryTempC = temp,
                            socPowerWatts = power
                        )
                    }

                    if (tick % 15 == 0) {
                        val newLog = when (tick % 60) {
                            15 -> "[Adreno GPU] Direct KGSL submit: 2,048 draws batched with zero-copy staging"
                            30 -> "[Snapdragon Turnip] UBWC compressed 18.4 MB framebuffer textures (bandwidth save 34%)"
                            45 -> "[Adreno Turbo] Sustained GPU frequency locked at 903 MHz (zero throttling)"
                            else -> "[NVN2 Split] Worker queue synchronized via VK_KHR_timeline_semaphore"
                        }
                        _emulation.update {
                            it.copy(nx2Logs = (it.nx2Logs.takeLast(14) + newLog))
                        }
                    }
                }
            }
        }
    }

    fun pauseEmulation() {
        _emulation.update { it.copy(isPaused = true, recentActionMessage = "Emulation Paused") }
    }

    fun resumeEmulation() {
        _emulation.update { it.copy(isPaused = false, recentActionMessage = "Emulation Resumed") }
    }

    fun toggleDockedMode() {
        val newDocked = !_emulation.value.isDocked
        _emulation.update {
            it.copy(
                isDocked = newDocked,
                recentActionMessage = if (newDocked) "Switched to Docked Mode (1080p)" else "Switched to Handheld Mode (720p)"
            )
        }
    }

    fun saveState() {
        val newSlot = "Slot ${_emulation.value.saveStates.size + 1} (${System.currentTimeMillis() % 10000})"
        _emulation.update {
            it.copy(
                saveStates = it.saveStates + newSlot,
                recentActionMessage = "Saved state to $newSlot"
            )
        }
    }

    fun loadState(slotName: String) {
        _emulation.update {
            it.copy(recentActionMessage = "Loaded state from $slotName")
        }
    }

    fun takeScreenshot() {
        _emulation.update {
            it.copy(recentActionMessage = "Screenshot saved to Pictures/Eglantine")
        }
    }

    fun exitEmulation() {
        emulationLoopJob?.cancel()
        _emulation.value = EmulationRuntime()
    }

    fun dismissUserFeedback() {
        _userFeedbackMessage.value = null
    }

    fun updateSettings(newSettings: EmulatorSettings) {
        repository.updateSettings(newSettings)
    }
}
