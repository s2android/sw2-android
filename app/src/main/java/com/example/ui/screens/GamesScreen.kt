package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    launchError: LaunchErrorInfo? = null,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (GameFilter) -> Unit,
    onLaunchGame: (Game) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQuickAddGame: () -> Unit,
    onOpenSetup: () -> Unit,
    onRemoveGame: (String) -> Unit = {},
    onRenameGame: (String, String) -> Unit = { _, _ -> },
    onClearSampleGames: () -> Unit = {},
    onRestoreSampleGames: () -> Unit = {},
    onDismissLaunchError: () -> Unit = {},
    onBypassKeysAndLaunch: (Game) -> Unit = {}
) {
    var selectedGameForDetails by remember { mutableStateOf<Game?>(null) }
    var gameToRename by remember { mutableStateOf<Game?>(null) }

    val sampleGamesCount = remember(games) { games.count { it.isSample } }

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
                onAddGame = onQuickAddGame,
                onConfigure = onOpenSetup
            )
        }

        // SAMPLE GAMES MANAGEMENT BANNER
        if (sampleGamesCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demo_games_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceHighlight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFB300))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Showing $sampleGamesCount Demo Entries",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Clear mock entries to see only your real ROMs",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = onClearSampleGames,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF5252)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_clear_sample_games")
                        ) {
                            Text("Clear Demo Games", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SEARCH BAR
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_games_input"),
                placeholder = { Text("Search Switch & NX2 titles, IDs…", color = TextSecondary) },
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

        // GAMES LIST / EMPTY STATE
        if (games.isEmpty()) {
            item {
                EmptyGamesCard(
                    onAddGame = onQuickAddGame,
                    onSetup = onOpenSetup,
                    onRestoreSamples = onRestoreSampleGames
                )
            }
        } else {
            items(games, key = { it.programId }) { game ->
                GameCardItem(
                    game = game,
                    onPlay = { onLaunchGame(game) },
                    onToggleFavorite = { onToggleFavorite(game.programId) },
                    onDetails = { selectedGameForDetails = game },
                    onRename = { gameToRename = game },
                    onRemove = { onRemoveGame(game.programId) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
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
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(175.dp)) {
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
                            text = "EGLANTINE",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
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
                    Column {
                        Text(
                            text = "Switch 2 & Switch 1",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Snapdragon Turnip • UBWC 4.0 Memory Tiling",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onAddGame,
                            colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_hero_add_game")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add ROM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onConfigure,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_hero_setup")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp), tint = EglantineCyan)
                            Spacer(Modifier.width(4.dp))
                            Text("Setup", fontSize = 12.sp, color = TextPrimary)
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            if (game.isNx2) listOf(EglantineViolet, EglantineMagenta)
                            else listOf(Color(0xFFD32F2F), Color(0xFFFF5252))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

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

                    // Demo vs Local badge
                    if (game.isSample) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x33FFB300))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("DEMO", color = Color(0xFFFFB300), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3300E5FF))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("LOCAL ROM", color = EglantineCyan, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = game.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${game.developer} • ID: ${game.programId.take(12)}… • ${game.sizeMb} MB",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                        Text("Load Test Keys & Play", fontSize = 11.sp)
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
        title = { Text("Rename Game Title", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Update how this game appears in your Eglantine library:",
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
                Text("Save Title")
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
    onAddGame: () -> Unit,
    onSetup: () -> Unit,
    onRestoreSamples: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
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
                text = "Library is Empty",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add your Nintendo Switch (.nsp, .xci, .nro) games in Setup or restore sample titles.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAddGame,
                    colors = ButtonDefaults.buttonColors(containerColor = EglantineMagenta),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Add ROM File")
                }
                Button(
                    onClick = onSetup,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Open Setup")
                }
            }
            TextButton(onClick = onRestoreSamples) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = EglantineCyan)
                Spacer(Modifier.width(4.dp))
                Text("Restore Sample Games", color = EglantineCyan, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun GameDetailsDialog(
    game: Game,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = game.title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = game.description.ifEmpty { "Nintendo Switch retail title playable on Eglantine emulator." },
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                Text("Program ID: ${game.programIdHex}", color = EglantineCyan, fontSize = 12.sp)
                Text("Developer: ${game.developer}", color = TextPrimary, fontSize = 12.sp)
                Text(
                    "Architecture: ${if (game.isNx2) "Switch 2 (NX2 / ARMv8.2-A / NVN2)" else "Switch 1 (Maxwell Tegra X1)"}",
                    color = EglantineMagenta,
                    fontSize = 12.sp
                )
                Text("Format: .${game.fileExtension.uppercase()} • Size: ${game.sizeMb} MB", color = TextSecondary, fontSize = 11.sp)
                Text("Source: ${if (game.isSample) "Demo / Sample Entry" else "Local Storage"}", color = if (game.isSample) Color(0xFFFFB300) else AccentSuccess, fontSize = 11.sp)
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
                            text = "RECOMMENDED AAA BACKEND",
                            color = EglantineMagenta,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (game.title.contains("Zelda", ignoreCase = true)) "NCE • 6GB DRAM • Turnip v24.3 • BC7 ASTC • Reactive Flush"
                                   else if (game.title.contains("Mario", ignoreCase = true)) "NCE • Fastmem • Turnip • 1080p Docked • 60 FPS Mailbox"
                                   else if (game.isNx2) "NX2 Ampere Split • 6 Cores • LDAPR • Turnip 8xx"
                                   else "NCE • Fastmem • Mesa Turnip Vulkan 1.3 • Async Shaders",
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
