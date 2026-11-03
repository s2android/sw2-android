package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.model.Game
import com.example.model.VulkanDriver
import com.example.ui.components.GlassActionPill
import com.example.ui.components.GlowingGlassIcon
import com.example.ui.components.IosGlassSurface
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.EglantineCyan
import com.example.ui.theme.EglantineMagenta
import com.example.ui.theme.GlowAmber
import com.example.ui.theme.GlowCyan
import com.example.ui.theme.GlowEmerald
import com.example.ui.theme.GlowPink
import com.example.ui.theme.GlowPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GameFilter
import com.example.ui.viewmodel.LaunchErrorInfo

@Composable
fun GamesScreen(
    games: List<Game>,
    activeDriver: VulkanDriver?,
    searchQuery: String,
    currentFilter: GameFilter,
    isScrapingBoxArt: Boolean = false,
    launchError: LaunchErrorInfo? = null,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (GameFilter) -> Unit,
    onLaunchGame: (Game) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQuickAddGame: () -> Unit,
    onOpenSetup: () -> Unit,
    onAddGameUri: ((Uri) -> Unit)? = null,
    onScanFolderUri: ((Uri) -> Unit)? = null,
    onAddCustomGame: ((String, String, Boolean) -> Unit)? = null,
    onScrapeAllBoxArt: () -> Unit = {},
    onScrapeGameBoxArt: (Game) -> Unit = {},
    onRemoveGame: (String) -> Unit = {},
    onRenameGame: (String, String) -> Unit = { _, _ -> },
    onDismissLaunchError: () -> Unit = {},
    onBypassKeysAndLaunch: (Game) -> Unit = {}
) {
    var selectedGameForDetails by remember { mutableStateOf<Game?>(null) }
    var gameToRename by remember { mutableStateOf<Game?>(null) }
    var showCustomAddDialog by remember { mutableStateOf(false) }

    val romFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            if (onAddGameUri != null) {
                onAddGameUri(uri)
            } else {
                onQuickAddGame()
            }
        }
    }

    val romFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            if (onScanFolderUri != null) {
                onScanFolderUri(uri)
            } else {
                onOpenSetup()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("games_screen_list")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. SIMPLIFIED IPHONE GLASS QUICK ACTION HEADER
        item {
            HeroBannerCard(
                activeDriver = activeDriver,
                isScrapingBoxArt = isScrapingBoxArt,
                onAddGame = {
                    try {
                        romFileLauncher.launch(arrayOf("*/*"))
                    } catch (_: Exception) {
                        showCustomAddDialog = true
                    }
                },
                onScanFolder = {
                    try {
                        romFolderLauncher.launch(null)
                    } catch (_: Exception) {
                        onOpenSetup()
                    }
                },
                onScrapeBoxArt = onScrapeAllBoxArt
            )
        }

        // 2. IOS SPOTLIGHT-STYLE FROSTED GLASS SEARCH PILL
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_games_input"),
                placeholder = {
                    Text(
                        "Search games…",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GlowCyan
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White.copy(alpha = 0.55f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                    focusedContainerColor = Color(0x4010182B),
                    unfocusedContainerColor = Color(0x3310182B),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(24.dp)
            )
        }

        // 3. MINIMAL GLASS FILTER PILLS
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    GameFilterChip(
                        label = "All (${games.size})",
                        selected = currentFilter == GameFilter.ALL,
                        badgeColor = GlowPink,
                        onClick = { onFilterChanged(GameFilter.ALL) }
                    )
                }
                item {
                    GameFilterChip(
                        label = "Switch 1",
                        selected = currentFilter == GameFilter.SWITCH1,
                        badgeColor = GlowCyan,
                        onClick = { onFilterChanged(GameFilter.SWITCH1) }
                    )
                }
                item {
                    GameFilterChip(
                        label = "Switch 2",
                        selected = currentFilter == GameFilter.NX2,
                        badgeColor = GlowPurple,
                        onClick = { onFilterChanged(GameFilter.NX2) }
                    )
                }
                item {
                    GameFilterChip(
                        label = "Homebrew",
                        selected = currentFilter == GameFilter.HOMEBREW,
                        badgeColor = GlowEmerald,
                        onClick = { onFilterChanged(GameFilter.HOMEBREW) }
                    )
                }
                item {
                    GameFilterChip(
                        label = "Favorites",
                        selected = currentFilter == GameFilter.FAVORITES,
                        badgeColor = GlowAmber,
                        onClick = { onFilterChanged(GameFilter.FAVORITES) }
                    )
                }
            }
        }

        // 4. GAMES LIST OR GLASS EMPTY STATE
        if (games.isEmpty()) {
            item {
                EmptyGamesCard(
                    onPickRomFile = {
                        try {
                            romFileLauncher.launch(arrayOf("*/*"))
                        } catch (_: Exception) {
                            showCustomAddDialog = true
                        }
                    },
                    onScanRomFolder = {
                        try {
                            romFolderLauncher.launch(null)
                        } catch (_: Exception) {
                            onOpenSetup()
                        }
                    },
                    onAddCustomEntry = { showCustomAddDialog = true },
                    onSetup = onOpenSetup
                )
            }
        } else {
            items(games, key = { it.programId }) { game ->
                GameCardItem(
                    game = game,
                    onPlay = { onLaunchGame(game) },
                    onToggleFavorite = { onToggleFavorite(game.programId) },
                    onScrapeBoxArt = { onScrapeGameBoxArt(game) },
                    onDetails = { selectedGameForDetails = game },
                    onRename = { gameToRename = game },
                    onRemove = { onRemoveGame(game.programId) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showCustomAddDialog) {
        CustomRomEntryDialog(
            onDismiss = { showCustomAddDialog = false },
            onAdd = { title, ext, isNx2 ->
                showCustomAddDialog = false
                if (onAddCustomGame != null) {
                    onAddCustomGame(title, ext, isNx2)
                } else {
                    onOpenSetup()
                }
            }
        )
    }

    launchError?.let { err ->
        LaunchErrorDialog(
            error = err,
            onDismiss = onDismissLaunchError,
            onOpenSetup = {
                onDismissLaunchError()
                onOpenSetup()
            },
            onBypassWithSampleKeys = {
                onDismissLaunchError()
                onBypassKeysAndLaunch(err.game)
            }
        )
    }

    selectedGameForDetails?.let { game ->
        GameDetailsDialog(
            game = game,
            onDismiss = { selectedGameForDetails = null },
            onLaunch = {
                selectedGameForDetails = null
                onLaunchGame(game)
            },
            onScrapeBoxArt = {
                onScrapeGameBoxArt(game)
            },
            onRename = {
                selectedGameForDetails = null
                gameToRename = game
            },
            onRemove = {
                selectedGameForDetails = null
                onRemoveGame(game.programId)
            }
        )
    }

    gameToRename?.let { game ->
        RenameGameDialog(
            game = game,
            onDismiss = { gameToRename = null },
            onConfirmRename = { newTitle ->
                onRenameGame(game.programId, newTitle)
                gameToRename = null
            }
        )
    }
}

@Composable
fun HeroBannerCard(
    activeDriver: VulkanDriver?,
    isScrapingBoxArt: Boolean = false,
    onAddGame: () -> Unit,
    onScanFolder: () -> Unit,
    onScrapeBoxArt: () -> Unit = {}
) {
    IosGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_banner_card"),
        cornerRadius = 26.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Game Library",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Switch 1 & Switch 2 • Auto Box Art",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, GlowCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = activeDriver?.name?.take(18) ?: "Mesa Turnip",
                        color = GlowCyan,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassActionPill(
                    label = "Add ROM",
                    icon = Icons.Default.Add,
                    glowColor = GlowPink,
                    onClick = onAddGame,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_hero_add_game")
                )
                GlassActionPill(
                    label = "Folder",
                    icon = Icons.Default.FolderOpen,
                    glowColor = GlowCyan,
                    onClick = onScanFolder,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_hero_scan_folder")
                )
                GlassActionPill(
                    label = if (isScrapingBoxArt) "Scraping…" else "Box Art",
                    icon = Icons.Default.Image,
                    glowColor = GlowAmber,
                    onClick = onScrapeBoxArt,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_scrape_all_box_art")
                )
            }
        }
    }
}

@Composable
fun GameFilterChip(
    label: String,
    selected: Boolean,
    badgeColor: Color = GlowPink,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = badgeColor.copy(alpha = 0.28f),
            selectedLabelColor = Color.White,
            containerColor = Color(0x3310182B),
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.White.copy(alpha = 0.22f),
            selectedBorderColor = badgeColor,
            borderWidth = 1.dp
        ),
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun GameCardItem(
    game: Game,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onScrapeBoxArt: () -> Unit,
    onDetails: () -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    IosGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_card_${game.programId}"),
        cornerRadius = 24.dp,
        accentGlowColor = if (game.isNx2) GlowPurple else GlowCyan,
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameBoxArtThumbnail(
                title = game.title,
                boxArtUrl = game.boxArtUrl,
                isNx2 = game.isNx2,
                modifier = Modifier
                    .width(58.dp)
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val badgeGlow = if (game.isNx2) GlowPurple else if (game.isHomebrew) GlowEmerald else GlowPink
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeGlow.copy(alpha = 0.28f))
                            .border(1.dp, badgeGlow.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (game.isNx2) "SWITCH 2" else if (game.isHomebrew) "HOMEBREW" else "SWITCH",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = ".${game.fileExtension.uppercase()}",
                        color = GlowAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = game.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${game.developer} • ${game.sizeMb} MB",
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("fav_btn_${game.programId}")
            ) {
                Icon(
                    imageVector = if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (game.isFavorite) GlowPink else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("menu_btn_${game.programId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Launch Game", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onPlay()
                        },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GlowPink) }
                    )
                    DropdownMenuItem(
                        text = { Text("Refresh Box Art", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onScrapeBoxArt()
                        },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = GlowCyan) }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename Title", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = GlowAmber) }
                    )
                    DropdownMenuItem(
                        text = { Text("Game Details", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onDetails()
                        },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = TextPrimary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove", color = Color(0xFFFF5252)) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .clickable { onPlay() }
                    .testTag("play_btn_${game.programId}")
            ) {
                GlowingGlassIcon(
                    icon = Icons.Default.PlayArrow,
                    contentDescription = "Play Game",
                    glowColor = GlowEmerald,
                    isSelected = true,
                    size = 40.dp,
                    iconSize = 22.dp
                )
            }
        }
    }
}

@Composable
fun GameBoxArtThumbnail(
    title: String,
    boxArtUrl: String?,
    isNx2: Boolean,
    modifier: Modifier = Modifier
) {
    val spineColor = if (isNx2) GlowPurple else GlowPink
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    if (isNx2) listOf(Color(0xFF311B92), Color(0xFF880E4F))
                    else listOf(Color(0xFFB71C1C), Color(0xFF1A1829))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!boxArtUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = boxArtUrl,
                contentDescription = "$title Box Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    FallbackBoxArtContent(title = title)
                },
                error = {
                    FallbackBoxArtContent(title = title)
                }
            )
        } else {
            FallbackBoxArtContent(title = title)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(spineColor.copy(alpha = 0.85f))
                .padding(vertical = 1.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isNx2) "SWITCH 2" else "SWITCH",
                color = Color.White,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun FallbackBoxArtContent(
    title: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.SportsEsports,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 3,
            lineHeight = 9.5.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyGamesCard(
    onPickRomFile: () -> Unit,
    onScanRomFolder: () -> Unit,
    onAddCustomEntry: () -> Unit,
    onSetup: () -> Unit
) {
    IosGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("empty_library_card"),
        cornerRadius = 28.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlowingGlassIcon(
                icon = Icons.Default.SportsEsports,
                contentDescription = null,
                glowColor = GlowPink,
                isSelected = true,
                size = 56.dp,
                iconSize = 28.dp
            )

            Text(
                text = "Ready to Play",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Add your Switch (.nsp, .xci, .nro) or Switch 2 games. Your library and box art are saved automatically.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassActionPill(
                    label = "Add ROM File",
                    icon = Icons.Default.Add,
                    glowColor = GlowPink,
                    onClick = onPickRomFile,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_empty_pick_rom")
                )
                GlassActionPill(
                    label = "Scan Folder",
                    icon = Icons.Default.FolderOpen,
                    glowColor = GlowCyan,
                    onClick = onScanRomFolder,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_empty_scan_folder")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassActionPill(
                    label = "Quick Add",
                    icon = Icons.Default.CreateNewFolder,
                    glowColor = GlowEmerald,
                    onClick = onAddCustomEntry,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_empty_custom_entry")
                )
                GlassActionPill(
                    label = "Keys & Setup",
                    icon = Icons.Default.Security,
                    glowColor = GlowAmber,
                    onClick = onSetup,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun LaunchErrorDialog(
    error: LaunchErrorInfo,
    onDismiss: () -> Unit,
    onOpenSetup: () -> Unit,
    onBypassWithSampleKeys: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = error.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = error.message,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceHighlight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Diagnostic Details:",
                            color = EglantineMagenta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = error.technicalDetails,
                            color = EglantineCyan,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenSetup,
                colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Open Setup (Install Keys)")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (error.canBypassWithSampleKeys) {
                    OutlinedButton(
                        onClick = onBypassWithSampleKeys,
                        border = BorderStroke(1.dp, EglantineCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Load Keys & Play", fontSize = 11.sp)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", color = TextSecondary)
                }
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun RenameGameDialog(
    game: Game,
    onDismiss: () -> Unit,
    onConfirmRename: (String) -> Unit
) {
    var titleText by remember { mutableStateOf(game.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Game & Re-Scrape Box Art", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Update how this game appears in your Eglantine library (box art will automatically update):",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EglantineMagenta,
                        unfocusedBorderColor = Color(0x55E040FB),
                        focusedContainerColor = DarkSurfaceHighlight,
                        unfocusedContainerColor = DarkSurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Text("Program ID: ${game.programIdHex}", color = EglantineCyan, fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = { if (titleText.isNotBlank()) onConfirmRename(titleText) },
                colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta)
            ) {
                Text("Save & Scrape Cover")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun GameDetailsDialog(
    game: Game,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onScrapeBoxArt: () -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = game.title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameBoxArtThumbnail(
                        title = game.title,
                        boxArtUrl = game.boxArtUrl,
                        isNx2 = game.isNx2,
                        modifier = Modifier
                            .width(74.dp)
                            .height(104.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                        Text("Program ID: ${game.programIdHex}", color = EglantineCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Developer: ${game.developer}", color = TextPrimary, fontSize = 12.sp)
                        Text(
                            "Architecture: ${if (game.isNx2) "Switch 2 (NX2 / ARMv8.2-A)" else "Switch 1 (Maxwell Tegra X1)"}",
                            color = EglantineMagenta,
                            fontSize = 11.sp
                        )
                        Text("Format: .${game.fileExtension.uppercase()} • ${game.sizeMb} MB", color = TextSecondary, fontSize = 11.sp)
                        OutlinedButton(
                            onClick = onScrapeBoxArt,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            border = BorderStroke(1.dp, EglantineCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineCyan),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Scrape Box Art", fontSize = 10.sp)
                        }
                    }
                }

                Text(
                    text = game.description.ifEmpty { "Nintendo Switch retail title saved in your persistent Eglantine library." },
                    color = TextSecondary,
                    fontSize = 12.5.sp
                )
                Text("Location: ${game.path}", color = TextSecondary, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceHighlight)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "ACTIVE BUILD 104 / LICHII NX2 PROFILE",
                            color = EglantineMagenta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (game.title.contains("Fire Emblem", ignoreCase = true))
                                "EDEN_NX2_DLSS_STANDIN=1 • 14b4222bbe23abee Upscale • Prepass De-Jitter"
                            else if (game.title.contains("Zelda", ignoreCase = true))
                                "NCE • 6GB DRAM • Turnip v24.4 • BC7 ASTC • VMA Block Eviction"
                            else if (game.isNx2)
                                "NX2 GSV Sparse Tile Pre-Free • ARM64 CAS Pre-Check • EOS Frame Drain"
                            else
                                "NCE • Fastmem • Mesa Turnip Vulkan 1.3 • AAudio PCM Synthesizer",
                            color = EglantineCyan,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onLaunch,
                colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta)
            ) {
                Text("Launch Now")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = onRename,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineCyan)
                ) {
                    Text("Rename", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Text("Remove", fontSize = 11.sp)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = TextSecondary)
                }
            }
        },
        containerColor = DarkSurface
    )
}
