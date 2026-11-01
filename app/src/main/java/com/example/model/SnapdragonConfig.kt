package com.example.model

import androidx.compose.runtime.Immutable

@Immutable
enum class SnapdragonChipset(
    val title: String,
    val adrenoGpu: String,
    val series: String,
    val defaultDriverId: String
) {
    AUTO_DETECT("Auto-Detect Snapdragon SoC", "Adreno (Auto)", "Adreno 7xx/8xx", "turnip_8gen3_opt"),
    SD_8_ELITE("Snapdragon 8 Elite", "Adreno 830", "A8xx Series", "turnip_8elite_opt"),
    SD_8_GEN_3("Snapdragon 8 Gen 3", "Adreno 750", "A7xx Series", "turnip_8gen3_opt"),
    SD_8_GEN_2("Snapdragon 8 Gen 2", "Adreno 740", "A7xx Series", "turnip_8gen2_opt"),
    SD_8_GEN_1("Snapdragon 8 / 8+ Gen 1", "Adreno 730", "A7xx Series", "turnip_8gen1_opt"),
    SD_7_PLUS_GEN_2("Snapdragon 7+ Gen 2", "Adreno 725", "A7xx Series", "turnip_8gen2_opt"),
    SD_865_870_888("Snapdragon 865 / 870 / 888", "Adreno 650/660", "A6xx Series", "turnip_legacy_a6xx")
}

@Immutable
data class SnapdragonOptimizationConfig(
    val targetChipset: SnapdragonChipset = SnapdragonChipset.AUTO_DETECT,
    val detectedSoC: String = "Qualcomm Snapdragon 8 Gen 3 (Adreno 750)",
    val detectedKernel: String = "Linux 6.1 (KGSL ioctl v5.2)",
    val gmemBypassEnabled: Boolean = true, // Bypass Adreno on-chip GMEM for large geometry
    val ubwcCompression: Boolean = true, // Qualcomm Universal Bandwidth Compression
    val adrenoTurboBoost: Boolean = true, // Sustained high GPU clock governor
    val kgslFastPath: Boolean = true, // Zero-copy command submission via KGSL
    val float16FastMath: Boolean = true, // VK_KHR_shader_float16_int8 on Adreno ALUs
    val timelineSemaphores: Boolean = true, // Multi-queue synchronization
    val pipelinePrewarmThreads: Int = 4 // Pipeline worker thread count
)
