package com.example.crypto

import android.content.Context
import androidx.compose.runtime.Immutable
import java.io.File

@Immutable
data class TitleKeyEntry(
    val rightsId: String,       // 32-hex character rights ID (e.g. 01000000000100000000000000000000)
    val keyHex: String,         // 32-hex character 128-bit AES title key
    val programId: String,      // Extracted from the first 16 characters of rightsId
    val matchedTitle: String? = null
) {
    val maskedKey: String
        get() = if (keyHex.length >= 8) "${keyHex.take(4)}...${keyHex.takeLast(4)}" else "••••••••"
}

data class TitleKeyParseResult(
    val keys: List<TitleKeyEntry>,
    val validCount: Int,
    val skippedLinesCount: Int,
    val errors: List<String>
)

object TitleKeyManager {

    private val titleKeysMap = mutableMapOf<String, TitleKeyEntry>()

    // Known program IDs for title mapping linked to RomMetadataParser database
    private val knownTitleMap: Map<String, String> by lazy {
        com.example.util.RomMetadataParser.KNOWN_SWITCH_TITLES.mapValues { it.value.first }
    }

    fun findTitleName(programId: String): String? {
        val cleanId = programId.removePrefix("0x").removePrefix("0X").uppercase()
        return knownTitleMap[cleanId]
    }

    fun parseTitleKeysContent(content: String): TitleKeyParseResult {
        val keysList = mutableListOf<TitleKeyEntry>()
        val errors = mutableListOf<String>()
        var skipped = 0

        content.lineSequence().forEachIndexed { index, line ->
            val trimmed = line.trim()
            // Ignore empty lines and comments
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) {
                skipped++
                return@forEachIndexed
            }

            // Standard Switch title.keys format: <rights_id> = <title_key>
            val parts = trimmed.split('=').map { it.trim() }
            if (parts.size == 2) {
                val rightsId = parts[0].lowercase()
                val key = parts[1].lowercase()

                if (isValidHex(rightsId, 32) && isValidHex(key, 32)) {
                    val programId = rightsId.take(16).uppercase()
                    val matchedTitle = knownTitleMap[programId]
                    val entry = TitleKeyEntry(
                        rightsId = rightsId,
                        keyHex = key,
                        programId = programId,
                        matchedTitle = matchedTitle
                    )
                    keysList.add(entry)
                    titleKeysMap[rightsId] = entry
                } else {
                    errors.add("Line ${index + 1}: Invalid hex format (must be 32 hex characters each)")
                }
            } else {
                errors.add("Line ${index + 1}: Malformed line format (expected 'rights_id = key')")
            }
        }

        return TitleKeyParseResult(
            keys = keysList,
            validCount = keysList.size,
            skippedLinesCount = skipped,
            errors = errors
        )
    }

    private fun isValidHex(str: String, expectedLength: Int): Boolean {
        if (str.length != expectedLength) return false
        return str.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    fun getAllLoadedKeys(): List<TitleKeyEntry> = titleKeysMap.values.toList()

    fun findKeyForProgramId(programId: String): TitleKeyEntry? {
        val cleanId = programId.removePrefix("0x").removePrefix("0X").uppercase()
        return titleKeysMap.values.find { it.programId.equals(cleanId, ignoreCase = true) }
    }

    fun saveTitleKeysToFile(context: Context, content: String): File {
        val keysDir = File(context.filesDir, "yuzu/keys").apply { mkdirs() }
        val targetFile = File(keysDir, "title.keys")
        targetFile.writeText(content)
        return targetFile
    }

    fun getSampleTitleKeysText(): String {
        return """
            # Eglantine Switch 2 (NX2) & Switch 1 title.keys
            # Format: <rights_id> = <128-bit AES title key>
            
            # Mario Kart World (NX2 Build - 0400C3F00006E000)
            0400c3f00006e0000000000000000000 = a192e8c459b20485d820491847103957
            
            # Splatoon Raiders (NX2 Beta - 04005B902517C000)
            04005b902517c0000000000000000000 = b8291048592038475829104859203847
            
            # Super Mario Odyssey (0100000000010000)
            01000000000100000000000000000000 = 84e72049581029384758102938475610
            
            # The Legend of Zelda: Tears of the Kingdom (0100F2C0115B6000)
            0100f2c0115b60000000000000000000 = c4810293847561029384756102938475
            
            # NX2 Architecture Testbench (0000000000000001)
            00000000000000010000000000000000 = f0000000000000000000000000000001
        """.trimIndent()
    }
}
