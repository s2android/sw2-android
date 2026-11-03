package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.model.Game
import com.example.model.VulkanDriver
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EglantineCyan
import com.example.ui.theme.EglantineMagenta
import com.example.ui.theme.EglantineViolet
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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HERO BANNER
        item {
            HeroBannerCard(
                activeDriver = activeDriver,
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
                onConfigure = onOpenSetup
            )
        }

        // SEARCH BAR & BOX ART SCRAPER ACTION BAR
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChanged,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_games_input"),
                    placeholder = { Text("Search your saved Switch & NX2 games…", color = TextSecondary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = EglantineMagenta
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EglantineMagenta,
                        unfocusedBorderColor = Color(0x55E040FB),
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Button(
                    onClick = onScrapeAllBoxArt,
                    enabled = games.isNotEmpty() && !isScrapingBoxArt,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceHighlight,
                        disabledContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                    modifier = Modifier.testTag("btn_scrape_all_box_art")
                ) {
                    if (isScrapingBoxArt) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = EglantineCyan
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Scrape Box Art",
                            tint = EglantineCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isScrapingBoxArt) "Scraping…" else "Box Art",
                        color = EglantineCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // FILTER CHIPS ROW
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    GameFilterChip(
                        label = "All Games (${games.size})",
                        selected = currentFilter == GameFilter.ALL,
                        onClick = { onFilterChanged(GameFilter.ALL) }
                    )
                }
                item {
                    GameFilterChip(
                        label = "Switch 1",
                        selected = currentFilter == GameFilter.SWITCH1,
                        badgeColor = Color(0xFFFF5252),
                        onClick = { onFilterChanged(GameFilter.SWITCH1) }
                    )
                }
                item {
                    GameFilterChip(
                        label = "Switch 2 (NX2)",
                        selected = currentFilter == GameFilter.NX2,
                        badgeColor = EglantineMagenta,
                        onClick = { onFilterChanged(GameFilter.NX2) }
                    )
                }
                item {
                    GameFilterChip(
                        label = "Homebrew",
                        selected = currentFilter == GameFilter.HOMEBREW,
                        badgeColor = EglantineCyan,
                        onClick = { onFilterChanged(GameFilter.HOMEBREW) }
                    )
                }
                item {
                    GameFilterChip(
                        label = "Favorites",
                        selected = currentFilter == GameFilter.FAVORITES,
                        badgeColor = Color(0xFFFFD54F),
                        onClick = { onFilterChanged(GameFilter.FAVORITES) }
                    )
                }
            }
        }

        // GAMES LIST / EMPTY STATE (NO DEMO GAMES)
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

    // CUSTOM ROM ADD DIALOG (DIRECTLY FROM GAMES SCREEN)
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

    // LAUNCH ERROR DIALOG
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

    // GAME DETAILS DIALOG
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

    // RENAME GAME DIALOG
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
    onAddGame: () -> Unit,
    onScanFolder: () -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(178.dp)) {
            Image(
                painter = painterResource(id = R.drawable.banner_eglantine),
                contentDescription = "Eglantine Console Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0x22000000), Color(0xF0100F17))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(AccentSuccess)
                        )
                        Text(
                            text = "EGLANTINE • LICHII NX2",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC1A1828))
                            .border(1.dp, EglantineCyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeDriver?.name?.take(22) ?: "Mesa Turnip Vulkan",
                            color = EglantineCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Switch 2 & Switch 1",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Persistent Library • Auto Box Art Scraper",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onAddGame,
                            colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_hero_add_game")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add ROM", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onScanFolder,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_hero_scan_folder")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp), tint = EglantineCyan)
                            Spacer(Modifier.width(4.dp))
                            Text("Folder", fontSize = 11.5.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameFilterChip(
    label: String,
    selected: Boolean,
    badgeColor: Color = EglantineMagenta,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = badgeColor.copy(alpha = 0.25f),
            selectedLabelColor = Color.White,
            containerColor = DarkSurfaceVariant,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color(0x33FFFFFF),
            selectedBorderColor = badgeColor,
            borderWidth = 1.dp
        ),
        shape = RoundedCornerShape(10.dp)
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_card_${game.programId}")
            .clickable { onPlay() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (game.isNx2) Color(0x66E040FB) else Color(0x33FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scraped Box Art Cover (Vertical Switch Case Aspect Ratio)
            GameBoxArtThumbnail(
                title = game.title,
                boxArtUrl = game.boxArtUrl,
                isNx2 = game.isNx2,
                modifier = Modifier
                    .width(62.dp)
                    .height(86.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (game.isNx2) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EglantineMagenta)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("SWITCH 2", color = Color.Black, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    } else if (game.isHomebrew) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EglantineCyan)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("HOMEBREW", color = Color.Black, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE53935))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("SWITCH 1", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x3300E5FF))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("SAVED ROM", color = EglantineCyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceHighlight)
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ".${game.fileExtension.uppercase()}",
                            color = Color(0xFFFFD54F),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = game.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${game.developer} • ID: ${game.programId.take(12)}…",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${game.sizeMb} MB • Played: ${game.lastPlayedTime}",
                    color = if (game.boxArtUrl != null) AccentSuccess else TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            // Actions: Favorite, 3-dots Menu, Play
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("fav_btn_${game.programId}")
            ) {
                Icon(
                    imageVector = if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (game.isFavorite) Color(0xFFFF4081) else TextSecondary,
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
                    modifier = Modifier.background(DarkSurfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Launch Game", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onPlay()
                        },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EglantineMagenta) }
                    )
                    DropdownMenuItem(
                        text = { Text("Scrape / Refresh Box Art", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onScrapeBoxArt()
                        },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = EglantineCyan) }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename Title", color = TextPrimary) },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = EglantineCyan) }
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
                        text = { Text("Remove from Library", color = Color(0xFFFF5252)) },
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
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EglantineMagenta)
                    .clickable { onPlay() }
                    .testTag("play_btn_${game.programId}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Game",
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
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
    val spineColor = if (isNx2) EglantineMagenta else Color(0xFFE53935)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    if (isNx2) listOf(Color(0xFF311B92), Color(0xFF880E4F))
                    else listOf(Color(0xFFB71C1C), Color(0xFF1A1829))
                )
            )
            .border(1.dp, spineColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!boxArtUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = boxArtUrl,
                contentDescription = "$title Box Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    FallbackBoxArtContent(title = title, isNx2 = isNx2)
                },
                error = {
                    FallbackBoxArtContent(title = title, isNx2 = isNx2)
                }
            )
        } else {
            FallbackBoxArtContent(title = title, isNx2 = isNx2)
        }

        // Top Switch header strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(spineColor.copy(alpha = 0.88f))
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
    title: String,
    isNx2: Boolean
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
            modifier = Modifier.size(22.dp)
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
                    shape = RoundedCornerShape(8.dp),
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
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Open Setup (Install Keys)")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (error.canBypassWithSampleKeys) {
                    OutlinedButton(
                        onClick = onBypassWithSampleKeys,
                        border = androidx.compose.foundation.BorderStroke(1.dp, EglantineCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineCyan),
                        shape = RoundedCornerShape(8.dp)
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
fun EmptyGamesCard(
    onPickRomFile: () -> Unit,
    onScanRomFolder: () -> Unit,
    onAddCustomEntry: () -> Unit,
    onSetup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp)
            .testTag("empty_library_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44E040FB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = EglantineMagenta,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Your Game Library is Ready",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add your Nintendo Switch (.nsp, .xci, .nro) or Switch 2 (NX2) games. Eglantine automatically remembers all added games across app restarts and scrapes official box art.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPickRomFile,
                    colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_empty_pick_rom")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add ROM File", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onScanRomFolder,
                    colors = ButtonDefaults.buttonColors(containerColor = EglantineCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_empty_scan_folder")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Scan ROM Folder", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onAddCustomEntry,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EglantineMagenta),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = EglantineMagenta),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_empty_custom_entry")
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Quick Add by Title", fontSize = 12.sp)
                }
                TextButton(onClick = onSetup) {
                    Text("Keys & Firmware Setup", color = EglantineCyan, fontSize = 12.sp)
                }
            }
        }
    }
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, EglantineCyan),
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

                // Recommended AAA Backend Configuration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
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
