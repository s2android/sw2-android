package com.example.model

import androidx.compose.runtime.Immutable

@Immutable
enum class AAAPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetGame: String,
    val recommendedDriverId: String,
    val description: String
) {
    TOTK(
        id = "totk",
        title = "Zelda: Tears of the Kingdom",
        subtitle = "NCE + 6GB DRAM + BC7 ASTC + Reactive Flush",
        targetGame = "The Legend of Zelda: Tears of the Kingdom",
        recommendedDriverId = "turnip_8gen3_opt",
        description = "Configured with 6GB Extended DRAM to prevent OOM crash, BC7 ASTC hardware recompression for Hyrule Depths, reactive flushing for glowing fog, and Fastmem."
    ),
    BOTW(
        id = "botw",
        title = "Zelda: Breath of the Wild",
        subtitle = "NCE + Fastmem + Turnip GMEM Bypass + 60 FPS",
        targetGame = "The Legend of Zelda: Breath of the Wild",
        recommendedDriverId = "turnip_8gen3_opt",
        description = "Full 60 FPS unlocked profile with asynchronous shader compilation, fast memory mapping, and Mailbox presentation."
    ),
    ODYSSEY(
        id = "odyssey",
        title = "Super Mario Odyssey",
        subtitle = "NCE + 1080p Docked + 60 FPS Mailbox + Adreno Turbo",
        targetGame = "Super Mario Odyssey",
        recommendedDriverId = "turnip_8gen3_opt",
        description = "Flawless 60 FPS 1080p docked profile with low latency frame pacing and Qualcomm UBWC compression."
    ),
    XENOBLADE_3(
        id = "xenoblade3",
        title = "Xenoblade Chronicles 3",
        subtitle = "NCE + 6GB DRAM + Float16 Math + Turnip v24+",
        targetGame = "Xenoblade Chronicles 3",
        recommendedDriverId = "turnip_8gen3_opt",
        description = "Prevents memory leaks in massive Aionios open environments using 6GB Extended DRAM and asynchronous pipeline generation."
    ),
    POKEMON_SV(
        id = "pokemonsv",
        title = "Pokemon Scarlet / Violet",
        subtitle = "NCE + 6GB DRAM + UBWC 4.0 + Conservative Flush",
        targetGame = "Pokemon Scarlet / Violet",
        recommendedDriverId = "turnip_8gen3_opt",
        description = "Smooths open-world Paldea terrain rendering with fast page tables and Adreno GMEM bypass."
    ),
    MONSTER_HUNTER(
        id = "mhrise",
        title = "Monster Hunter Rise",
        subtitle = "NCE + Fastmem + VK 1.3.282 + Float16 Fast-Math",
        targetGame = "Monster Hunter Rise",
        recommendedDriverId = "turnip_8gen3_opt",
        description = "Optimized for dense RE Engine geometry and dynamic lighting effects on Qualcomm Adreno."
    )
}

@Immutable
data class EmulatorSettings(
    // CPU Backend (Essential for AAA Games)
    val cpuBackend: String = "Native Code Execution (NCE)", // NCE vs Dynarmic JIT
    val fastmemEnabled: Boolean = true, // Host Page Table direct memory mapping (POSIX SIGSEGV)
    val extendedDramMode: Boolean = true, // 6GB/8GB RAM mode (prevents TOTK/Xenoblade OOM crash)
    val cpuUnsafeFastMath: Boolean = true, // Skip NaN checks for 15-20% CPU speedup in AAA games

    // Graphics Backend & Driver
    val graphicsApi: String = "Vulkan (Mesa Turnip)",
    val resolutionScale: Float = 1.0f, // 0.5x, 0.75x, 1.0x, 1.5x, 2.0x
    val isDockedMode: Boolean = true, // Docked (1080p) vs Handheld (720p)
    val vsyncMode: String = "Mailbox", // Mailbox, FIFO, Immediate
    val diskShaderCache: Boolean = true,
    val asyncShaderCompilation: Boolean = true, // Asynchronous SPIR-V compiler (removes shader stutter)
    val reactiveFlushing: Boolean = true, // Reactive GPU cache flushing (fixes TOTK/BOTW bloom & leaks)
    val gpuAccuracy: String = "Normal (AAA Recommended)", // Normal, High, Extreme
    val pipelineWorkerThreads: Int = 4,
    val frameGenMode: String = "LSFG 2x (Lossless)", // Off, LSFG 2x, LSFG 3x
    val astcRecompression: String = "BC7 (Fast)", // Uncompressed, BC1, BC7
    val windowFilter: String = "FSR (AMD FidelityFX)", // Nearest, Bilinear, Bicubic, FSR

    // Snapdragon / Qualcomm Adreno Optimizations
    val snapdragonTurbo: Boolean = true,
    val snapdragonGmemBypass: Boolean = true,
    val snapdragonUbwcCompression: Boolean = true,
    val snapdragonKgslFastPath: Boolean = true,

    // NX2 Experimental Architecture
    val nx2AmpereThreadSplit: Boolean = true,
    val nx2LdaprDecoding: Boolean = true,
    val nx2LseAtomics: Boolean = true,
    val nx2PacAuth: Boolean = true,
    val nx2AddressSpace39Bit: Boolean = true,
    val nx2CoresAllocated: Int = 6,

    // Controls & Overlay
    val showInputOverlay: Boolean = true,
    val overlayOpacity: Float = 0.75f,
    val overlayScale: Float = 1.0f,
    val hapticFeedback: Boolean = true,
    val showStatsOverlay: Boolean = true,

    // Audio
    val audioEngine: String = "AAudio",
    val audioVolume: Float = 1.0f,

    // Currently Applied AAA Profile
    val activeAaaPreset: AAAPreset? = AAAPreset.TOTK
)
