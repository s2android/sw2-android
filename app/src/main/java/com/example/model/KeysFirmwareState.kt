package com.example.model

import androidx.compose.runtime.Immutable

@Immutable
data class KeysFirmwareState(
    val prodKeysInstalled: Boolean = true,
    val prodKeysFileName: String = "prod.keys",
    val prodKeysCount: Int = 348,
    val masterKeyRevision: String = "MasterKey 19.0.0 / NX2-v1",
    val prodKeysSha256: String = "8f4b...39e1",

    val titleKeysInstalled: Boolean = true,
    val titleKeysFileName: String = "title.keys",
    val titleKeysCount: Int = 186,

    val firmwareInstalled: Boolean = true,
    val firmwareVersion: String = "19.0.1 (NX2-Preview)",
    val firmwarePackageName: String = "Firmware_19.0.1_NX2.zip",
    val installedApplets: List<String> = listOf("qlaunch", "swkbd", "browser", "cabinet", "mii_edit"),

    val romsDirectoryUri: String = "Internal / RetroGames / Switch",
    val totalRomsFound: Int = 5
)
