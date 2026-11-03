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
    val currentVersion: String = "v1.2.0",
    val latestVersion: String = "v1.3.0 (Build 104 / Lichii NX2)",
    val apkFileSizeMb: Float = 18.9f,
    val releaseDate: String = "Nov 3, 2026",
    val channel: String = "zortos293/Eglantine Build 104 • Snapdragon Turnip",
    val releaseNotes: List<String> = listOf(
        "Synced with zortos293/Eglantine Build 104 (nx2_gsv sparse tile pre-free & EOS frame ordering)",
        "Fire Emblem: Fortune's Weave spatial upscale stand-in (14b4222bbe23abee + prepass de-jitter)",
        "ARM64 exclusive monitor reservation pre-check before CAS (cuts contention 10% -> 2%)",
        "Real-time 44.1kHz 16-bit PCM AudioTrack synthesizer for background music & gameplay SFX",
        "Persistent Game Library (SharedPreferences + SAF URIs) & Automatic Box Art Scraper"
    ),
    val status: ApkUpdateStatus = ApkUpdateStatus.UPDATE_AVAILABLE,
    val downloadProgress: Float = 0.0f,
    val downloadedMb: Float = 0.0f,
    val downloadSpeed: String = "0.0 MB/s",
    val downloadUrl: String = "https://github.com/s2android/sw2-android/releases/download/1.3.0/eglantine-v1.3.0-turnip.apk",
    val githubReleasePageUrl: String = "https://github.com/s2android/sw2-android/releases/tag/1.3.0",
    val githubRepoUrl: String = "https://github.com/s2android/sw2-android"
)
