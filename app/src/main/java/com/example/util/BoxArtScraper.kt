package com.example.util

import com.example.model.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Real-time Box Art Scraper for Nintendo Switch & Switch 2 titles.
 * Queries Wikipedia's MediaWiki PageImages API (for Nintendo 1st-party & retail box art),
 * Steam's public StoreSearch API (for vertical 600x900 box art on multi-platform titles like Tetris Forever),
 * and Nintendo Switch Title ID CDN fallback.
 */
object BoxArtScraper {

    private val memoryCache = mutableMapOf<String, String>()

    // Map of canonical Wikipedia article titles for flagship Nintendo titles to guarantee instant exact box art hits
    private val CANONICAL_WIKI_TITLES = mapOf(
        "tetris forever" to "Tetris Forever",
        "tetris 99" to "Tetris 99",
        "puyo puyo tetris" to "Puyo Puyo Tetris",
        "super mario odyssey" to "Super Mario Odyssey",
        "the legend of zelda: tears of the kingdom" to "The Legend of Zelda: Tears of the Kingdom",
        "tears of the kingdom" to "The Legend of Zelda: Tears of the Kingdom",
        "the legend of zelda: breath of the wild" to "The Legend of Zelda: Breath of the Wild",
        "breath of the wild" to "The Legend of Zelda: Breath of the Wild",
        "mario kart 8 deluxe" to "Mario Kart 8 Deluxe",
        "mario kart world" to "Mario Kart 8 Deluxe",
        "super smash bros. ultimate" to "Super Smash Bros. Ultimate",
        "super mario bros. wonder" to "Super Mario Bros. Wonder",
        "animal crossing: new horizons" to "Animal Crossing: New Horizons",
        "metroid dread" to "Metroid Dread",
        "metroid prime remastered" to "Metroid Prime Remastered",
        "metroid prime 4" to "Metroid Prime 4: Beyond",
        "splatoon 3" to "Splatoon 3",
        "splatoon 2" to "Splatoon 2",
        "splatoon raiders" to "Splatoon 3",
        "xenoblade chronicles 3" to "Xenoblade Chronicles 3",
        "luigi's mansion 3" to "Luigi's Mansion 3",
        "kirby and the forgotten land" to "Kirby and the Forgotten Land",
        "fire emblem: three houses" to "Fire Emblem: Three Houses",
        "fire emblem engage" to "Fire Emblem Engage",
        "pokemon legends: arceus" to "Pokémon Legends: Arceus",
        "pokémon legends: arceus" to "Pokémon Legends: Arceus",
        "pokemon scarlet" to "Pokémon Scarlet and Violet",
        "pokémon scarlet" to "Pokémon Scarlet and Violet",
        "pokemon violet" to "Pokémon Scarlet and Violet",
        "pokémon violet" to "Pokémon Scarlet and Violet",
        "pikmin 4" to "Pikmin 4",
        "super mario rpg" to "Super Mario RPG",
        "hollow knight" to "Hollow Knight",
        "hades" to "Hades (video game)",
        "celeste" to "Celeste (video game)",
        "persona 5 royal" to "Persona 5 Royal",
        "sonic frontiers" to "Sonic Frontiers",
        "monster hunter rise" to "Monster Hunter Rise",
        "octopath traveler" to "Octopath Traveler",
        "Astral Chain" to "Astral Chain"
    )

    /**
     * Scrapes the best high-resolution cover art URL for the given [game].
     */
    suspend fun scrapeBoxArtUrl(game: Game): String? = withContext(Dispatchers.IO) {
        val cleanTitle = cleanSearchTitle(game.title)
        val cacheKey = "${game.programId}_${cleanTitle.lowercase()}"
        memoryCache[cacheKey]?.let { return@withContext it }

        // 1. Try canonical or direct Wikipedia infobox cover art first (best for Nintendo Switch exclusives & retail box art)
        val wikiTitle = CANONICAL_WIKI_TITLES[cleanTitle.lowercase()] ?: cleanTitle
        fetchWikipediaExactBoxArt(wikiTitle)?.let { url ->
            memoryCache[cacheKey] = url
            return@withContext url
        }

        // 2. Try Steam StoreSearch vertical 600x900 library box art (great for Tetris Forever, indie & 3rd-party Switch titles)
        fetchSteamBoxArt(cleanTitle)?.let { url ->
            memoryCache[cacheKey] = url
            return@withContext url
        }

        // 3. Try Wikipedia search generator with "Nintendo Switch video game" query
        fetchWikipediaSearchBoxArt("$cleanTitle video game")?.let { url ->
            memoryCache[cacheKey] = url
            return@withContext url
        }

        // 4. Fallback to Switch Title ID CDN if it's a valid 16-char hex ID
        val cleanId = game.programId.removePrefix("0x").removePrefix("0X").uppercase()
        if (cleanId.length == 16 && (cleanId.startsWith("0100") || cleanId.startsWith("0400"))) {
            val cdnUrl = "https://tinfoil.media/ti/$cleanId/256/256/"
            memoryCache[cacheKey] = cdnUrl
            return@withContext cdnUrl
        }

        null
    }

    /**
     * Returns an instant cover art URL (from cache, Steam library CDN for known multiplatform games, or Switch Title ID CDN)
     * before or alongside the async Wikipedia/Steam scraper.
     */
    fun getInstantFallbackCoverUrl(title: String, programId: String): String {
        val cleanTitle = cleanSearchTitle(title).lowercase()
        val cacheKey = "${programId}_${cleanTitle}"
        memoryCache[cacheKey]?.let { return it }

        return when {
            cleanTitle.contains("tetris forever") ->
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3180070/library_600x900.jpg"
            cleanTitle.contains("hollow knight") ->
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/367520/library_600x900.jpg"
            cleanTitle.contains("celeste") ->
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/504230/library_600x900.jpg"
            cleanTitle.contains("hades") ->
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1145360/library_600x900.jpg"
            cleanTitle.contains("persona 5") ->
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1687950/library_600x900.jpg"
            cleanTitle.contains("sonic frontiers") ->
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1237320/library_600x900.jpg"
            cleanTitle.contains("monster hunter rise") ->
                "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1446780/library_600x900.jpg"
            else -> {
                val cleanId = programId.removePrefix("0x").removePrefix("0X").uppercase()
                "https://tinfoil.media/ti/$cleanId/256/256/"
            }
        }
    }

    private fun cleanSearchTitle(raw: String): String {
        return raw
            .replace(Regex("""\[.*?\]"""), "")
            .replace(Regex("""\(.*?\)"""), "")
            .replace(Regex("""(?i)\b(NX2|Switch 2|Switch|NSP|XCI|NSZ|v\d+(\.\d+)*)\b"""), "")
            .replace('_', ' ')
            .replace(Regex("""\s+"""), " ")
            .trim()
            .ifEmpty { raw }
    }

    private fun fetchWikipediaExactBoxArt(title: String): String? {
        return try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val apiUrl = "https://en.wikipedia.org/w/api.php?action=query&titles=$encoded&prop=pageimages&piprop=thumbnail|original&pithumbsize=600&redirects=1&format=json"
            val jsonStr = httpGet(apiUrl) ?: return null
            val root = JSONObject(jsonStr)
            val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: return null
            val keys = pages.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key == "-1") continue
                val page = pages.optJSONObject(key) ?: continue
                val thumb = page.optJSONObject("thumbnail")?.optString("source")
                if (!thumb.isNullOrBlank()) return thumb
                val orig = page.optJSONObject("original")?.optString("source")
                if (!orig.isNullOrBlank()) return orig
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchWikipediaSearchBoxArt(searchQuery: String): String? {
        return try {
            val encoded = URLEncoder.encode(searchQuery, "UTF-8")
            val apiUrl = "https://en.wikipedia.org/w/api.php?action=query&generator=search&gsrsearch=$encoded&gsrlimit=4&prop=pageimages&piprop=thumbnail&pithumbsize=600&format=json"
            val jsonStr = httpGet(apiUrl) ?: return null
            val root = JSONObject(jsonStr)
            val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: return null
            val keys = pages.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val page = pages.optJSONObject(key) ?: continue
                val thumb = page.optJSONObject("thumbnail")?.optString("source")
                if (!thumb.isNullOrBlank() && !thumb.endsWith(".svg.png", ignoreCase = true)) {
                    return thumb
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchSteamBoxArt(title: String): String? {
        return try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val apiUrl = "https://store.steampowered.com/api/storesearch/?term=$encoded&l=english&cc=US"
            val jsonStr = httpGet(apiUrl) ?: return null
            val root = JSONObject(jsonStr)
            val items = root.optJSONArray("items") ?: return null
            if (items.length() > 0) {
                val first = items.getJSONObject(0)
                val appId = first.optInt("id", 0)
                if (appId > 0) {
                    // Steam's 600x900 vertical box art asset URL
                    return "https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/$appId/library_600x900.jpg"
                }
                val tiny = first.optString("tiny_image")
                if (tiny.isNotBlank()) return tiny
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun httpGet(urlStr: String): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4500
                readTimeout = 4500
                setRequestProperty("User-Agent", "EglantineSwitchEmulator/1.2.0 (Android; Mobile)")
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }
}
