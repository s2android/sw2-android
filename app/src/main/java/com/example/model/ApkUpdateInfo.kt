package com.example.model

import androidx.compose.runtime.Immutable

@Immutable
enum class ApkUpdateStatus {
    IDLE,
    CHECKING,
    UPDATE_AVAILABLE,
    DOWNLOADING,
    READY_TO_INSTALL,
    UP_TO_DATE
}

@Immutable
data class ApkUpdateInfo(
    val currentVersion: String = "v1.0.4",
    val latestVersion: String = "v1.2.0 (AAA Turnip Build)",
    val apkFileSizeMb: Float = 18.9f,
    val releaseDate: String = "Nov 1, 2026",
    val channel: String = "Snapdragon Optimized Release",
    val releaseNotes: List<String> = listOf(
        "ARM64 Native Code Execution (NCE) with direct host dispatch",
        "6GB Extended DRAM pool (fixes Zelda TOTK OOM crash)",
        "Mesa Turnip v24.4 Vulkan driver with GMEM tile bypass",
        "Switch 1 retail ROM automatic metadata and Title Key parser",
        "Zero-stutter asynchronous SPIR-V shader compiler"
    ),
    val status: ApkUpdateStatus = ApkUpdateStatus.UPDATE_AVAILABLE,
    val downloadProgress: Float = 0.0f,
    val downloadedMb: Float = 0.0f,
    val downloadSpeed: String = "0.0 MB/s",
    val downloadUrl: String = "https://github.com/s2android/sw2-android/releases/download/1.2.0/eglantine-v1.2.0-turnip.apk",
    val githubReleasePageUrl: String = "https://github.com/s2android/sw2-android/releases/tag/1.2.0",
    val githubRepoUrl: String = "https://github.com/s2android/sw2-android"
)
