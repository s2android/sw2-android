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

    private val initialGames = listOf(
        Game(
            title = "Mario Kart World",
            programId = "0400C3F00006E000",
            developer = "Nintendo EPD",
            version = "1.0.0 (NX2 Build)",
            fileExtension = "xci",
            path = "Internal/Switch/MarioKartWorld_NX2.xci",
            isNx2 = true,
            isHomebrew = false,
            sizeMb = 14336,
            lastPlayedTime = "Yesterday",
            playtimeMinutes = 145,
            isFavorite = true,
            description = "Next-generation Switch 2 flagship racer running on ARMv8.2 architecture with 64-bit 39-bit memory space and dynamic weather tracks.",
            isSample = true
        ),
        Game(
            title = "Splatoon Raiders",
            programId = "04005B902517C000",
            developer = "Nintendo",
            version = "1.0.0 (NX2 Beta)",
            fileExtension = "nsp",
            path = "Internal/Switch/SplatoonRaiders_NX2.nsp",
            isNx2 = true,
            isHomebrew = false,
            sizeMb = 12288,
            lastPlayedTime = "3 days ago",
            playtimeMinutes = 68,
            isFavorite = false,
            description = "Tactical turf multiplayer running on NX2 Ampere graphics pushbuffers and LSE atomics.",
            isSample = true
        ),
        Game(
            title = "Super Mario Odyssey",
            programId = "0100000000010000",
            developer = "Nintendo",
            version = "1.3.0",
            fileExtension = "xci",
            path = "Internal/Switch/SuperMarioOdyssey.xci",
            isNx2 = false,
            isHomebrew = false,
            sizeMb = 5600,
            lastPlayedTime = "1 week ago",
            playtimeMinutes = 420,
            isFavorite = true,
            description = "Acclaimed 3D sandbox platformer featuring Mario and Cappy traversing global kingdoms.",
            isSample = true
        ),
        Game(
            title = "The Legend of Zelda: Tears of the Kingdom",
            programId = "0100F2C0115B6000",
            developer = "Nintendo",
            version = "1.2.1",
            fileExtension = "nsp",
            path = "Internal/Switch/ZeldaTOTK.nsp",
            isNx2 = false,
            isHomebrew = false,
            sizeMb = 16384,
            lastPlayedTime = "2 weeks ago",
            playtimeMinutes = 890,
            isFavorite = true,
            description = "Epic open-world action-adventure traversing the skies and depths of Hyrule.",
            isSample = true
        ),
        Game(
            title = "NX2 Architecture Testbench",
            programId = "0000000000000001",
            developer = "Camille LaVey / Eden Project",
            version = "0.9.4",
            fileExtension = "nro",
            path = "Internal/Switch/Homebrew/nx2_bench.nro",
            isNx2 = true,
            isHomebrew = true,
            sizeMb = 48,
            lastPlayedTime = "Just now",
            playtimeMinutes = 35,
            isFavorite = true,
            description = "Official Eglantine homebrew verifying LDAPR instructions, LSE atomics, memory limits, and Vulkan compute throughput.",
            isSample = true
        )
    )

    private val _drivers = MutableStateFlow(snapdragonOptimizedDrivers)
    val drivers: StateFlow<List<VulkanDriver>> = _drivers.asStateFlow()

    private val _selectedDriverId = MutableStateFlow("turnip_8gen3_opt")
    val selectedDriverId: StateFlow<String> = _selectedDriverId.asStateFlow()

    private val _snapdragonConfig = MutableStateFlow(SnapdragonOptimizationConfig())
    val snapdragonConfig: StateFlow<SnapdragonOptimizationConfig> = _snapdragonConfig.asStateFlow()

    private val _games = MutableStateFlow(initialGames)
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
                totalRomsFound = it.totalRomsFound + newlyFoundGames.size
            )
        }
        if (newlyFoundGames.isNotEmpty()) {
            _games.update { it + newlyFoundGames }
        }
    }

    fun addCustomGame(game: Game) {
        _games.update { listOf(game) + it }
    }

    fun toggleFavorite(programId: String) {
        _games.update { list ->
            list.map {
                if (it.programId == programId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }

    fun clearSampleGames(): Int {
        val countBefore = _games.value.count { it.isSample }
        _games.update { list -> list.filter { !it.isSample } }
        return countBefore
    }

    fun restoreSampleGames() {
        val nonSamples = _games.value.filter { !it.isSample }
        _games.value = nonSamples + initialGames
    }

    fun removeGame(programId: String) {
        _games.update { list -> list.filter { it.programId != programId } }
    }

    fun updateGameTitle(programId: String, newTitle: String) {
        _games.update { list ->
            list.map {
                if (it.programId == programId) it.copy(title = newTitle.trim()) else it
            }
        }
    }

    fun addScannedGames(newGames: List<Game>) {
        if (newGames.isEmpty()) return
        // Merge without duplicating program IDs or paths
        _games.update { existing ->
            val existingIds = existing.map { it.programId }.toSet()
            val existingPaths = existing.map { it.path }.toSet()
            val uniqueNew = newGames.filter { it.programId !in existingIds && it.path !in existingPaths }
            uniqueNew + existing
        }
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
            }
        }
        val targetDriver = drivers.value.find { it.id == preset.recommendedDriverId }
        if (targetDriver != null) {
            _selectedDriverId.value = targetDriver.id
        }
    }
}

