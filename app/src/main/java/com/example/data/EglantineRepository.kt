package com.example.data

import com.example.model.EmulatorSettings
import com.example.model.Game
import com.example.model.KeysFirmwareState
import com.example.model.SnapdragonChipset
import com.example.model.SnapdragonOptimizationConfig
import com.example.model.VulkanDriver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EglantineRepository {

    // Open source Vulkan drivers specifically tuned for Qualcomm Snapdragon & Adreno GPUs
    private val snapdragonOptimizedDrivers = listOf(
        VulkanDriver(
            id = "turnip_8elite_opt",
            name = "Mesa Turnip • Snapdragon 8 Elite",
            version = "v24.4.0-a830-experimental",
            vendor = "Mesa / Freedreno Lab",
            description = "Bleeding-edge open-source Vulkan driver for Oryon CPU + Adreno 830. Features 16KB kernel page alignment, VK_EXT_fragment_shader_interlock, and fast UBWC.",
            isOpenSource = true,
            isDefault = false,
            vulkanApiVersion = "1.3.288",
            hasFloat16Support = true,
            hasVulkanMemoryModel = true,
            targetAdrenoFamily = "Adreno 830 (A8xx)",
            snapdragonOptimized = true,
            gmemTilingBypass = true,
            ubwcSupported = true,
            releaseDate = "October 2026",
            changelog = "Initial Adreno 830 / 8 Elite bringup with ultra low latency submission queues."
        ),
        VulkanDriver(
            id = "turnip_8gen3_opt",
            name = "Mesa Turnip • Snapdragon 8 Gen 3/Elite",
            version = "v24.3.0-a750-a830-opt",
            vendor = "Mesa / Freedreno Qualcomm Lab",
            description = "Finely tuned open-source Vulkan driver for Snapdragon 8 Gen 3 (Adreno 750) and 8 Elite (Adreno 830). Features GMEM bypass, UBWC 4.0, zero-copy staging, and float16 ALU dispatch.",
            isOpenSource = true,
            isDefault = true,
            vulkanApiVersion = "1.3.282",
            hasFloat16Support = true,
            hasVulkanMemoryModel = true,
            targetAdrenoFamily = "Adreno 750 / 830",
            snapdragonOptimized = true,
            gmemTilingBypass = true,
            ubwcSupported = true,
            releaseDate = "October 2026",
            changelog = "Enabled fast-path KGSL ioctl, fixed UBWC memory compression on Adreno 750, boosted L2 cache utilization."
        ),
        VulkanDriver(
            id = "turnip_8gen2_opt",
            name = "Mesa Turnip • Snapdragon 8 Gen 2",
            version = "v24.3.0-a740-master",
            vendor = "Mesa Open-Source Project",
            description = "Community benchmark driver for Snapdragon 8 Gen 2 (Adreno 740). Rock-solid stability, zero shader stutter with timeline semaphores, and 16-bit float math.",
            isOpenSource = true,
            isDefault = false,
            vulkanApiVersion = "1.3.280",
            hasFloat16Support = true,
            hasVulkanMemoryModel = true,
            targetAdrenoFamily = "Adreno 740",
            snapdragonOptimized = true,
            gmemTilingBypass = true,
            ubwcSupported = true,
            releaseDate = "October 2026",
            changelog = "Optimized register allocation for Switch 2 ExeFS shaders, lowered power draw by 14% on TSMC N4 node."
        ),
        VulkanDriver(
            id = "turnip_8gen1_opt",
            name = "Mesa Turnip • Snapdragon 8/8+ Gen 1",
            version = "v24.2.1-a730-thermal-safe",
            vendor = "Mesa 3D Project",
            description = "Thermal-optimized Turnip build for Snapdragon 8 Gen 1 & 8+ Gen 1 (Adreno 730). Prevents aggressive throttling with conservative register pressure and batched sync.",
            isOpenSource = true,
            isDefault = false,
            vulkanApiVersion = "1.3.275",
            hasFloat16Support = true,
            hasVulkanMemoryModel = true,
            targetAdrenoFamily = "Adreno 730",
            snapdragonOptimized = true,
            gmemTilingBypass = true,
            ubwcSupported = true,
            releaseDate = "August 2026",
            changelog = "Thermal guard active: balances Adreno frequency scaling with sustained 60 FPS frame pacing."
        ),
        VulkanDriver(
            id = "freedreno_nx2_exp",
            name = "Freedreno NX2 Multi-Threaded",
            version = "v2026.10-NX2-Split",
            vendor = "Eglantine / Freedreno Lab",
            description = "Specialized open-source driver patched with Eglantine's Ampere GPU pushbuffer split architecture. Directly splits command parsing and execution threads for Adreno compute queues.",
            isOpenSource = true,
            isDefault = false,
            vulkanApiVersion = "1.3.284",
            hasFloat16Support = true,
            hasVulkanMemoryModel = true,
            targetAdrenoFamily = "Adreno 7xx / 8xx Universal",
            snapdragonOptimized = true,
            gmemTilingBypass = true,
            ubwcSupported = true,
            releaseDate = "October 2026",
            changelog = "Supports dual GPU worker threads for Switch 2 ExeFS pushbuffer streaming on Snapdragon."
        ),
        VulkanDriver(
            id = "turnip_legacy_a6xx",
            name = "Mesa Turnip • Snapdragon 865/870/888",
            version = "v24.1.2-a6xx-nir",
            vendor = "Mesa Project",
            description = "Specialized open-source build for legacy Adreno 650/660 series. Incorporates NIR scalarizing to avoid hardware register spilling on older Snapdragon chips.",
            isOpenSource = true,
            isDefault = false,
            vulkanApiVersion = "1.3.268",
            hasFloat16Support = true,
            hasVulkanMemoryModel = true,
            targetAdrenoFamily = "Adreno 650 / 660",
            snapdragonOptimized = true,
            gmemTilingBypass = false,
            ubwcSupported = true,
            releaseDate = "July 2026",
            changelog = "Mitigated A6xx hardware errata, enabled stable 60 FPS in handheld mode."
        ),
        VulkanDriver(
            id = "system_driver",
            name = "System Default Vulkan (Proprietary Stock)",
            version = "OEM Factory Driver",
            vendor = "Qualcomm Technologies Inc.",
            description = "Factory proprietary driver. Lacks community GMEM bypass and may suffer from memory leaks in modern Switch 2 shaders.",
            isOpenSource = false,
            isDefault = false,
            vulkanApiVersion = "1.1.128",
            hasFloat16Support = false,
            hasVulkanMemoryModel = false,
            targetAdrenoFamily = "Generic",
            snapdragonOptimized = false,
            gmemTilingBypass = false,
            ubwcSupported = false,
            releaseDate = "Pre-installed",
            changelog = "Stock OEM factory binary without community enhancements."
        )
    )

    private var appContext: android.content.Context? = null

    private val _drivers = MutableStateFlow(snapdragonOptimizedDrivers)
    val drivers: StateFlow<List<VulkanDriver>> = _drivers.asStateFlow()

    private val _selectedDriverId = MutableStateFlow("turnip_8gen3_opt")
    val selectedDriverId: StateFlow<String> = _selectedDriverId.asStateFlow()

    private val _snapdragonConfig = MutableStateFlow(SnapdragonOptimizationConfig())
    val snapdragonConfig: StateFlow<SnapdragonOptimizationConfig> = _snapdragonConfig.asStateFlow()

    // User's game library starts empty (no demo games) and loads persisted ROMs from disk
    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games: StateFlow<List<Game>> = _games.asStateFlow()

    private val _keysFirmwareState = MutableStateFlow(KeysFirmwareState())
    val keysFirmwareState: StateFlow<KeysFirmwareState> = _keysFirmwareState.asStateFlow()

    private val initialParsedKeys = com.example.crypto.TitleKeyManager.parseTitleKeysContent(
        com.example.crypto.TitleKeyManager.getSampleTitleKeysText()
    ).keys

    private val _titleKeys = MutableStateFlow(initialParsedKeys)
    val titleKeys: StateFlow<List<com.example.crypto.TitleKeyEntry>> = _titleKeys.asStateFlow()

    private val _settings = MutableStateFlow(EmulatorSettings())
    val settings: StateFlow<EmulatorSettings> = _settings.asStateFlow()

    private val _updateInfo = MutableStateFlow(com.example.model.ApkUpdateInfo())
    val updateInfo: StateFlow<com.example.model.ApkUpdateInfo> = _updateInfo.asStateFlow()

    /**
     * Attaches application context and restores persisted user games, ROM folder URI, and driver selection.
     */
    fun attachContext(context: android.content.Context) {
        appContext = context.applicationContext
        val savedGames = LibraryPersistenceManager.loadGames(appContext)
        if (savedGames.isNotEmpty()) {
            _games.value = savedGames
        }
        LibraryPersistenceManager.loadRomsDirectory(appContext)?.let { savedDir ->
            if (savedDir.isNotBlank()) {
                _keysFirmwareState.update {
                    it.copy(romsDirectoryUri = savedDir, totalRomsFound = savedGames.size)
                }
            }
        }
        LibraryPersistenceManager.loadSelectedDriver(appContext)?.let { savedDriverId ->
            if (_drivers.value.any { it.id == savedDriverId }) {
                _selectedDriverId.value = savedDriverId
            }
        }
    }

    private fun persistCurrentGames() {
        LibraryPersistenceManager.saveGames(appContext, _games.value)
    }

    fun updateApkDownloadState(
        status: com.example.model.ApkUpdateStatus,
        progress: Float = 0f,
        downloadedMb: Float = 0f,
        speed: String = "0.0 MB/s"
    ) {
        _updateInfo.update {
            it.copy(
                status = status,
                downloadProgress = progress,
                downloadedMb = downloadedMb,
                downloadSpeed = speed
            )
        }
    }

    fun completeApkInstallation() {
        _updateInfo.update {
            it.copy(
                currentVersion = it.latestVersion,
                status = com.example.model.ApkUpdateStatus.UP_TO_DATE,
                downloadProgress = 1.0f,
                downloadedMb = it.apkFileSizeMb,
                downloadSpeed = "Done"
            )
        }
    }

    fun checkForApkUpdates() {
        _updateInfo.update {
            it.copy(
                status = com.example.model.ApkUpdateStatus.UPDATE_AVAILABLE,
                downloadProgress = 0f,
                downloadedMb = 0f
            )
        }
    }

    fun selectDriver(driverId: String) {
        _selectedDriverId.value = driverId
        LibraryPersistenceManager.saveSelectedDriver(appContext, driverId)
    }

    fun applySnapdragonChipsetProfile(chipset: SnapdragonChipset) {
        _snapdragonConfig.update {
            it.copy(
                targetChipset = chipset,
                gmemBypassEnabled = chipset != SnapdragonChipset.SD_865_870_888,
                ubwcCompression = true,
                adrenoTurboBoost = true,
                kgslFastPath = true
            )
        }
        val targetDriver = drivers.value.find { it.id == chipset.defaultDriverId }
        if (targetDriver != null) {
            _selectedDriverId.value = targetDriver.id
            LibraryPersistenceManager.saveSelectedDriver(appContext, targetDriver.id)
        }
    }

    fun updateSnapdragonConfig(newConfig: SnapdragonOptimizationConfig) {
        _snapdragonConfig.value = newConfig
    }

    fun installCustomDriver(name: String, version: String, description: String) {
        val newDriver = VulkanDriver(
            id = "custom_" + System.currentTimeMillis(),
            name = name,
            version = version,
            vendor = "Custom Snapdragon Pack (Mesa)",
            description = description,
            isOpenSource = true,
            isCustom = true,
            vulkanApiVersion = "1.3.284",
            hasFloat16Support = true,
            hasVulkanMemoryModel = true,
            targetAdrenoFamily = "Custom Snapdragon",
            snapdragonOptimized = true,
            gmemTilingBypass = true,
            ubwcSupported = true,
            releaseDate = "Imported just now",
            changelog = "User-supplied custom Turnip/Mesa Vulkan driver package for Snapdragon."
        )
        _drivers.update { listOf(newDriver) + it }
        _selectedDriverId.value = newDriver.id
        LibraryPersistenceManager.saveSelectedDriver(appContext, newDriver.id)
    }

    fun updateKeysInstalled(fileName: String, count: Int, revision: String) {
        _keysFirmwareState.update {
            it.copy(
                prodKeysInstalled = true,
                prodKeysFileName = fileName,
                prodKeysCount = count,
                masterKeyRevision = revision
            )
        }
    }

    fun updateTitleKeysInstalled(fileName: String, count: Int) {
        _keysFirmwareState.update {
            it.copy(
                titleKeysInstalled = true,
                titleKeysFileName = fileName,
                titleKeysCount = count
            )
        }
    }

    fun importTitleKeysText(content: String): com.example.crypto.TitleKeyParseResult {
        val result = com.example.crypto.TitleKeyManager.parseTitleKeysContent(content)
        if (result.validCount > 0) {
            _titleKeys.value = result.keys
            updateTitleKeysInstalled("title.keys", result.validCount)
        }
        return result
    }

    fun updateFirmwareInstalled(version: String, packageName: String) {
        _keysFirmwareState.update {
            it.copy(
                firmwareInstalled = true,
                firmwareVersion = version,
                firmwarePackageName = packageName
            )
        }
    }

    fun updateRomsDirectory(dirUri: String, newlyFoundGames: List<Game> = emptyList()) {
        _keysFirmwareState.update {
            it.copy(
                romsDirectoryUri = dirUri,
                totalRomsFound = _games.value.size
            )
        }
        LibraryPersistenceManager.saveRomsDirectory(appContext, dirUri)
        if (newlyFoundGames.isNotEmpty()) {
            addScannedGames(newlyFoundGames)
        }
    }

    fun addCustomGame(game: Game) {
        _games.update { existing ->
            val filtered = existing.filter { it.programId != game.programId && it.path != game.path }
            listOf(game.copy(isSample = false)) + filtered
        }
        persistCurrentGames()
    }

    fun toggleFavorite(programId: String) {
        _games.update { list ->
            list.map {
                if (it.programId == programId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
        persistCurrentGames()
    }

    fun removeGame(programId: String) {
        _games.update { list -> list.filter { it.programId != programId } }
        persistCurrentGames()
    }

    fun updateGameTitle(programId: String, newTitle: String) {
        _games.update { list ->
            list.map {
                if (it.programId == programId) it.copy(title = newTitle.trim()) else it
            }
        }
        persistCurrentGames()
    }

    fun updateGameBoxArt(programId: String, boxArtUrl: String) {
        _games.update { list ->
            list.map {
                if (it.programId == programId) it.copy(boxArtUrl = boxArtUrl) else it
            }
        }
        persistCurrentGames()
    }

    fun markGamePlayed(programId: String) {
        _games.update { list ->
            list.map {
                if (it.programId == programId) {
                    it.copy(lastPlayedTime = "Just now", playtimeMinutes = it.playtimeMinutes + 1)
                } else it
            }
        }
        persistCurrentGames()
    }

    fun addScannedGames(newGames: List<Game>) {
        if (newGames.isEmpty()) return
        _games.update { existing ->
            val existingIds = existing.map { it.programId }.toSet()
            val existingPaths = existing.map { it.path }.toSet()
            val uniqueNew = newGames
                .map { it.copy(isSample = false) }
                .filter { it.programId !in existingIds && it.path !in existingPaths }
            uniqueNew + existing
        }
        persistCurrentGames()
    }

    fun updateSettings(newSettings: EmulatorSettings) {
        _settings.value = newSettings
    }

    fun applyAAAPreset(preset: com.example.model.AAAPreset) {
        _settings.update { current ->
            when (preset) {
                com.example.model.AAAPreset.TOTK -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    extendedDramMode = true,
                    cpuUnsafeFastMath = true,
                    asyncShaderCompilation = true,
                    reactiveFlushing = true,
                    gpuAccuracy = "Normal (AAA Recommended)",
                    astcRecompression = "BC7 (Fast)",
                    snapdragonGmemBypass = true,
                    snapdragonUbwcCompression = true,
                    snapdragonTurbo = true,
                    resolutionScale = 1.0f,
                    windowFilter = "FSR (AMD FidelityFX)"
                )
                com.example.model.AAAPreset.BOTW -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    extendedDramMode = true,
                    cpuUnsafeFastMath = true,
                    asyncShaderCompilation = true,
                    reactiveFlushing = true,
                    gpuAccuracy = "Normal (AAA Recommended)",
                    astcRecompression = "BC7 (Fast)",
                    vsyncMode = "Mailbox",
                    snapdragonTurbo = true,
                    snapdragonGmemBypass = true
                )
                com.example.model.AAAPreset.ODYSSEY -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    extendedDramMode = false,
                    isDockedMode = true,
                    resolutionScale = 1.0f,
                    vsyncMode = "Mailbox",
                    snapdragonTurbo = true,
                    snapdragonGmemBypass = true
                )
                com.example.model.AAAPreset.XENOBLADE_3 -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    extendedDramMode = true,
                    asyncShaderCompilation = true,
                    astcRecompression = "BC7 (Fast)",
                    pipelineWorkerThreads = 6,
                    snapdragonUbwcCompression = true
                )
                com.example.model.AAAPreset.POKEMON_SV -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    extendedDramMode = true,
                    reactiveFlushing = true,
                    snapdragonGmemBypass = true,
                    snapdragonUbwcCompression = true
                )
                com.example.model.AAAPreset.MONSTER_HUNTER -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    isDockedMode = true,
                    asyncShaderCompilation = true,
                    snapdragonTurbo = true,
                    snapdragonKgslFastPath = true
                )
                com.example.model.AAAPreset.MARIO_KART_WORLD -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    extendedDramMode = true,
                    vmaBlockAwareEviction = true,
                    arm64ReservationPreCheck = true,
                    nx2GsvSparsePreFree = true,
                    commandFetchSnapshotGuard = true,
                    forkPipelineCacheIsolation = true
                )
                com.example.model.AAAPreset.FIRE_EMBLEM_FW -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    extendedDramMode = true,
                    feSpatialUpscaleStandin = true,
                    vmaBlockAwareEviction = true,
                    forkPipelineCacheIsolation = true,
                    nx2GsvSparsePreFree = true
                )
                com.example.model.AAAPreset.DK_BANANZA -> current.copy(
                    activeAaaPreset = preset,
                    cpuBackend = "Native Code Execution (NCE)",
                    fastmemEnabled = true,
                    extendedDramMode = true,
                    commandFetchSnapshotGuard = true,
                    forkPipelineCacheIsolation = true,
                    arm64ReservationPreCheck = true,
                    nx2GsvSparsePreFree = true
                )
            }
        }
        val targetDriver = drivers.value.find { it.id == preset.recommendedDriverId }
        if (targetDriver != null) {
            _selectedDriverId.value = targetDriver.id
            LibraryPersistenceManager.saveSelectedDriver(appContext, targetDriver.id)
        }
    }
}

