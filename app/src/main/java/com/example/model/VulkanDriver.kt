package com.example.model

import androidx.compose.runtime.Immutable

@Immutable
data class VulkanDriver(
    val id: String,
    val name: String,
    val version: String,
    val vendor: String,
    val description: String,
    val isOpenSource: Boolean = true,
    val isDefault: Boolean = false,
    val isCustom: Boolean = false,
    val vulkanApiVersion: String = "1.3.280",
    val hasFloat16Support: Boolean = true,
    val hasVulkanMemoryModel: Boolean = true,
    val targetAdrenoFamily: String = "Adreno 7xx / 8xx",
    val snapdragonOptimized: Boolean = true,
    val gmemTilingBypass: Boolean = true,
    val ubwcSupported: Boolean = true,
    val releaseDate: String = "2026-10",
    val changelog: String = ""
)
