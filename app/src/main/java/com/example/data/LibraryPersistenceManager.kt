package com.example.data

import android.content.Context
import com.example.model.Game
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists user-added games, scraped box art URLs, ROM directory URIs, and driver selections
 * using SharedPreferences + JSON so the user's library is always remembered across app launches.
 */
object LibraryPersistenceManager {

    private const val PREFS_NAME = "eglantine_persistent_store"
    private const val KEY_SAVED_GAMES = "saved_user_games_json"
    private const val KEY_ROMS_DIRECTORY_URI = "saved_roms_directory_uri"
    private const val KEY_SELECTED_DRIVER_ID = "saved_selected_driver_id"
    private const val KEY_AUDIO_ENABLED = "saved_audio_enabled"

    fun saveGames(context: Context?, games: List<Game>) {
        if (context == null) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (g in games) {
                if (g.isSample) continue // Never persist demo games
                val obj = JSONObject().apply {
                    put("title", g.title)
                    put("programId", g.programId)
                    put("developer", g.developer)
                    put("version", g.version)
                    put("fileExtension", g.fileExtension)
                    put("path", g.path)
                    put("isNx2", g.isNx2)
                    put("isHomebrew", g.isHomebrew)
                    put("sizeMb", g.sizeMb)
                    put("lastPlayedTime", g.lastPlayedTime)
                    put("playtimeMinutes", g.playtimeMinutes)
                    put("isFavorite", g.isFavorite)
                    put("description", g.description)
                    put("uriString", g.uriString ?: "")
                    put("boxArtUrl", g.boxArtUrl ?: "")
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_SAVED_GAMES, jsonArray.toString()).apply()
        } catch (_: Exception) {}
    }

    fun loadGames(context: Context?): List<Game> {
        if (context == null) return emptyList()
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_SAVED_GAMES, null) ?: return emptyList()
            val jsonArray = JSONArray(raw)
            val result = mutableListOf<Game>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val uriStr = obj.optString("uriString", "").takeIf { it.isNotBlank() }
                val boxArt = obj.optString("boxArtUrl", "").takeIf { it.isNotBlank() }
                result.add(
                    Game(
                        title = obj.optString("title", "Unknown Game"),
                        programId = obj.optString("programId", "0100000000000000"),
                        developer = obj.optString("developer", "Nintendo"),
                        version = obj.optString("version", "1.0.0"),
                        fileExtension = obj.optString("fileExtension", "xci"),
                        path = obj.optString("path", ""),
                        isNx2 = obj.optBoolean("isNx2", false),
                        isHomebrew = obj.optBoolean("isHomebrew", false),
                        sizeMb = obj.optLong("sizeMb", 4096L),
                        lastPlayedTime = obj.optString("lastPlayedTime", "Recently added"),
                        playtimeMinutes = obj.optInt("playtimeMinutes", 0),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        description = obj.optString("description", ""),
                        isSample = false,
                        uriString = uriStr,
                        boxArtUrl = boxArt
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveRomsDirectory(context: Context?, dirUri: String) {
        if (context == null) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_ROMS_DIRECTORY_URI, dirUri).apply()
        } catch (_: Exception) {}
    }

    fun loadRomsDirectory(context: Context?): String? {
        if (context == null) return null
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_ROMS_DIRECTORY_URI, null)
        } catch (_: Exception) {
            null
        }
    }

    fun saveSelectedDriver(context: Context?, driverId: String) {
        if (context == null) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_SELECTED_DRIVER_ID, driverId).apply()
        } catch (_: Exception) {}
    }

    fun loadSelectedDriver(context: Context?): String? {
        if (context == null) return null
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_SELECTED_DRIVER_ID, null)
        } catch (_: Exception) {
            null
        }
    }
}
