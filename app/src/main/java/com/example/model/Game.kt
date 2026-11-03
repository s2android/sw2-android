package com.example.model

import androidx.compose.runtime.Immutable

@Immutable
data class Game(
    val title: String,
    val programId: String,
    val developer: String,
    val version: String,
    val fileExtension: String, // xci, nsp, nro, nca, nsz
    val path: String,
    val isNx2: Boolean = false, // Switch 2 experimental target
    val isHomebrew: Boolean = false,
    val sizeMb: Long = 1024,
    val lastPlayedTime: String = "Never",
    val playtimeMinutes: Int = 0,
    val isFavorite: Boolean = false,
    val description: String = "",
    val isSample: Boolean = false,
    val uriString: String? = null,
    val boxArtUrl: String? = null
) {
    val programIdHex: String
        get() = if (programId.startsWith("0x", ignoreCase = true)) programId else "0x$programId"
}
