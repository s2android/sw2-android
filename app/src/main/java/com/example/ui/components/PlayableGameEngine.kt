package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ActiveGameGenre {
    TETRIS,
    RACING,
    OPEN_WORLD_ZELDA,
    PLATFORMER_MARIO,
    SHOOTER_SPLATOON
}

fun detectGameGenre(title: String): ActiveGameGenre {
    val lower = title.lowercase()
    return when {
        lower.contains("tetris") || lower.contains("puyo") || lower.contains("puzzle") || lower.contains("lumines") -> ActiveGameGenre.TETRIS
        lower.contains("kart") || lower.contains("racing") || lower.contains("f-zero") || lower.contains("asphalt") || lower.contains("cruis") -> ActiveGameGenre.RACING
        lower.contains("zelda") || lower.contains("tears") || lower.contains("breath") || lower.contains("xenoblade") || lower.contains("skyrim") || lower.contains("witcher") || lower.contains("pokemon") -> ActiveGameGenre.OPEN_WORLD_ZELDA
        lower.contains("splatoon") || lower.contains("raiders") || lower.contains("doom") || lower.contains("metroid") -> ActiveGameGenre.SHOOTER_SPLATOON
        else -> ActiveGameGenre.PLATFORMER_MARIO
    }
}

private val TETROMINO_SHAPES = arrayOf(
    // I
    arrayOf(intArrayOf(1, 1, 1, 1)),
    // O
    arrayOf(intArrayOf(2, 2), intArrayOf(2, 2)),
    // T
    arrayOf(intArrayOf(0, 3, 0), intArrayOf(3, 3, 3)),
    // S
    arrayOf(intArrayOf(0, 4, 4), intArrayOf(4, 4, 0)),
    // Z
    arrayOf(intArrayOf(5, 5, 0), intArrayOf(0, 5, 5)),
    // J
    arrayOf(intArrayOf(6, 0, 0), intArrayOf(6, 6, 6)),
    // L
    arrayOf(intArrayOf(0, 0, 7), intArrayOf(7, 7, 7))
)

private val TETROMINO_COLORS = arrayOf(
    Color.Transparent,
    Color(0xFF00E5FF), // Cyan I
    Color(0xFFFFEA00), // Yellow O
    Color(0xFFD500F9), // Purple T
    Color(0xFF00E676), // Green S
    Color(0xFFFF1744), // Red Z
    Color(0xFF2979FF), // Blue J
    Color(0xFFFF9100)  // Orange L
)

private fun rotateMatrixCW(matrix: Array<IntArray>): Array<IntArray> {
    val rows = matrix.size
    val cols = matrix[0].size
    val rotated = Array(cols) { IntArray(rows) }
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            rotated[c][rows - 1 - r] = matrix[r][c]
        }
    }
    return rotated
}

@Composable
fun PlayableGameEngineViewport(
    gameTitle: String,
    programIdHex: String,
    isNx2: Boolean,
    isPaused: Boolean,
    isDocked: Boolean,
    audioEnabled: Boolean = true,
    lastInputCommand: String,
    inputEventCounter: Long,
    leftStickX: Float,
    leftStickY: Float,
    rightStickX: Float,
    rightStickY: Float
) {
    val genre = remember(gameTitle) { detectGameGenre(gameTitle) }
    val textMeasurer = rememberTextMeasurer()
    val audioEngine = remember { com.example.audio.SwitchAudioEngine() }

    // Keep audio engine mute/pause state synchronized
    LaunchedEffect(audioEnabled, isPaused) {
        audioEngine.isMuted = !audioEnabled
        audioEngine.isPaused = isPaused
    }

    // Ensure AudioTrack resources are stopped and released when leaving emulation screen
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            audioEngine.stopMusicLoop()
        }
    }

    // Boot sequence state: 0 = NCA/XCI Header Mount (0.65s), 1 = Nintendo Switch Boot Splash (0.75s), 2 = Live Interactive Game
    var bootStage by remember(gameTitle) { mutableIntStateOf(0) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var animTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(gameTitle) {
        bootStage = 0
        delay(650)
        bootStage = 1
        audioEngine.playBootChime()
        delay(750)
        bootStage = 2
        audioEngine.startMusicLoop(genre)
    }

    // --- TETRIS ENGINE STATE ---
    val boardCols = 10
    val boardRows = 20
    val tetrisBoard = remember(gameTitle) { Array(boardRows) { IntArray(boardCols) } }
    var currentPiece by remember(gameTitle) { mutableStateOf(TETROMINO_SHAPES[2]) }
    var nextPieceIdx by remember(gameTitle) { mutableIntStateOf(0) }
    var pieceX by remember(gameTitle) { mutableIntStateOf(3) }
    var pieceY by remember(gameTitle) { mutableIntStateOf(0) }
    var tetrisScore by remember(gameTitle) { mutableIntStateOf(1200) }
    var tetrisLines by remember(gameTitle) { mutableIntStateOf(4) }
    var tetrisLevel by remember(gameTitle) { mutableIntStateOf(1) }

    // Pre-populate a few realistic rows at the bottom of Tetris so it looks immediately alive
    LaunchedEffect(gameTitle) {
        if (genre == ActiveGameGenre.TETRIS) {
            val presetRows = arrayOf(
                intArrayOf(1, 1, 0, 3, 3, 3, 0, 6, 6, 6),
                intArrayOf(2, 2, 0, 4, 4, 5, 5, 6, 7, 7),
                intArrayOf(2, 2, 4, 4, 0, 0, 5, 5, 0, 7)
            )
            for (r in 0..2) {
                for (c in 0 until boardCols) {
                    tetrisBoard[boardRows - 1 - r][c] = presetRows[r][c]
                }
            }
        }
    }

    fun canPlacePiece(shape: Array<IntArray>, px: Int, py: Int): Boolean {
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val bx = px + c
                    val by = py + r
                    if (bx < 0 || bx >= boardCols || by >= boardRows) return false
                    if (by >= 0 && tetrisBoard[by][bx] != 0) return false
                }
            }
        }
        return true
    }

    fun lockAndSpawnPiece() {
        for (r in currentPiece.indices) {
            for (c in currentPiece[r].indices) {
                if (currentPiece[r][c] != 0) {
                    val by = pieceY + r
                    val bx = pieceX + c
                    if (by in 0 until boardRows && bx in 0 until boardCols) {
                        tetrisBoard[by][bx] = currentPiece[r][c]
                    }
                }
            }
        }
        // Clear full lines
        var cleared = 0
        var row = boardRows - 1
        while (row >= 0) {
            if (tetrisBoard[row].all { it != 0 }) {
                cleared++
                for (pull in row downTo 1) {
                    tetrisBoard[pull] = tetrisBoard[pull - 1].clone()
                }
                tetrisBoard[0] = IntArray(boardCols)
            } else {
                row--
            }
        }
        if (cleared > 0) {
            tetrisLines += cleared
            tetrisScore += when (cleared) {
                1 -> 100 * tetrisLevel
                2 -> 300 * tetrisLevel
                3 -> 500 * tetrisLevel
                else -> 800 * tetrisLevel
            }
            tetrisLevel = 1 + (tetrisLines / 10)
            audioEngine.playScoreOrLineClear()
        }
        currentPiece = TETROMINO_SHAPES[nextPieceIdx]
        nextPieceIdx = Random.nextInt(TETROMINO_SHAPES.size)
        pieceX = 3
        pieceY = 0
        if (!canPlacePiece(currentPiece, pieceX, pieceY)) {
            // Board topped out -> clear top half so game keeps running
            for (r in 0 until boardRows - 3) {
                tetrisBoard[r] = IntArray(boardCols)
            }
        }
    }

    // --- RACING / OPEN WORLD / PLATFORMER STATE ---
    var playerX by remember(gameTitle) { mutableFloatStateOf(0f) } // -1f..1f
    var playerY by remember(gameTitle) { mutableFloatStateOf(0f) }
    var playerJumpVel by remember(gameTitle) { mutableFloatStateOf(0f) }
    var playerJumpHeight by remember(gameTitle) { mutableFloatStateOf(0f) }
    var speedBoostTimer by remember(gameTitle) { mutableIntStateOf(0) }
    var attackSlashTimer by remember(gameTitle) { mutableIntStateOf(0) }
    var gameScoreOrCoins by remember(gameTitle) { mutableIntStateOf(24) }
    var cameraYaw by remember(gameTitle) { mutableFloatStateOf(0f) }

    // Handle discrete button presses from GamepadOverlay with instant PCM SFX
    LaunchedEffect(inputEventCounter) {
        if (inputEventCounter == 0L || isPaused || bootStage < 2) return@LaunchedEffect
        when (genre) {
            ActiveGameGenre.TETRIS -> {
                when (lastInputCommand) {
                    "DPAD_LEFT", "L" -> {
                        if (canPlacePiece(currentPiece, pieceX - 1, pieceY)) {
                            pieceX--
                            audioEngine.playMoveTick()
                        }
                    }
                    "DPAD_RIGHT", "R" -> {
                        if (canPlacePiece(currentPiece, pieceX + 1, pieceY)) {
                            pieceX++
                            audioEngine.playMoveTick()
                        }
                    }
                    "DPAD_DOWN", "B" -> {
                        if (canPlacePiece(currentPiece, pieceX, pieceY + 1)) {
                            pieceY++
                            tetrisScore += 2
                            audioEngine.playMoveTick()
                        } else {
                            audioEngine.playDropOrAction()
                            lockAndSpawnPiece()
                        }
                    }
                    "A", "X", "DPAD_UP" -> {
                        val rotated = rotateMatrixCW(currentPiece)
                        if (canPlacePiece(rotated, pieceX, pieceY)) {
                            currentPiece = rotated
                            audioEngine.playRotateOrJump()
                        } else if (canPlacePiece(rotated, pieceX - 1, pieceY)) {
                            pieceX -= 1
                            currentPiece = rotated
                            audioEngine.playRotateOrJump()
                        } else if (canPlacePiece(rotated, pieceX + 1, pieceY)) {
                            pieceX += 1
                            currentPiece = rotated
                            audioEngine.playRotateOrJump()
                        }
                    }
                    "Y", "ZL", "ZR" -> {
                        // Hard drop
                        var dropped = 0
                        while (canPlacePiece(currentPiece, pieceX, pieceY + 1)) {
                            pieceY++
                            dropped++
                        }
                        tetrisScore += dropped * 4
                        audioEngine.playDropOrAction()
                        lockAndSpawnPiece()
                    }
                }
            }
            else -> {
                when (lastInputCommand) {
                    "DPAD_LEFT" -> {
                        playerX = (playerX - 0.16f).coerceIn(-0.85f, 0.85f)
                        audioEngine.playMoveTick()
                    }
                    "DPAD_RIGHT" -> {
                        playerX = (playerX + 0.16f).coerceIn(-0.85f, 0.85f)
                        audioEngine.playMoveTick()
                    }
                    "DPAD_UP" -> {
                        speedBoostTimer = 35
                        audioEngine.playRotateOrJump()
                    }
                    "B", "A" -> {
                        if (playerJumpHeight <= 0.01f) {
                            playerJumpVel = 14f
                            audioEngine.playRotateOrJump()
                        }
                        if (genre == ActiveGameGenre.RACING) {
                            speedBoostTimer = 45
                            audioEngine.playDropOrAction()
                        }
                    }
                    "Y", "X", "ZR", "R", "ZL", "L" -> {
                        attackSlashTimer = 20
                        gameScoreOrCoins += 1
                        audioEngine.playDropOrAction()
                    }
                }
            }
        }
    }

    // 60 FPS Main Game Loop
    LaunchedEffect(isPaused, bootStage, genre) {
        var joystickMoveCooldown = 0
        while (!isPaused) {
            delay(16)
            frameTick++
            val speedFactor = if (speedBoostTimer > 0) 2.1f else 1.0f
            animTime += 0.045f * speedFactor

            if (speedBoostTimer > 0) speedBoostTimer--
            if (attackSlashTimer > 0) attackSlashTimer--

            // Continuous analog stick control
            if (bootStage >= 2) {
                if (genre == ActiveGameGenre.TETRIS) {
                    if (joystickMoveCooldown > 0) joystickMoveCooldown--
                    if (joystickMoveCooldown == 0) {
                        if (leftStickX < -0.35f && canPlacePiece(currentPiece, pieceX - 1, pieceY)) {
                            pieceX--
                            joystickMoveCooldown = 7
                        } else if (leftStickX > 0.35f && canPlacePiece(currentPiece, pieceX + 1, pieceY)) {
                            pieceX++
                            joystickMoveCooldown = 7
                        }
                        if (leftStickY > 0.45f && canPlacePiece(currentPiece, pieceX, pieceY + 1)) {
                            pieceY++
                            tetrisScore += 1
                            joystickMoveCooldown = 4
                        }
                    }
                    // Gravity drop every ~28 frames
                    val dropInterval = (32 - tetrisLevel * 2).coerceAtLeast(10)
                    if (frameTick % dropInterval == 0L) {
                        if (canPlacePiece(currentPiece, pieceX, pieceY + 1)) {
                            pieceY++
                        } else {
                            lockAndSpawnPiece()
                        }
                    }
                } else {
                    if (abs(leftStickX) > 0.05f) {
                        playerX = (playerX + leftStickX * 0.032f).coerceIn(-0.85f, 0.85f)
                    }
                    if (abs(leftStickY) > 0.05f) {
                        playerY = (playerY - leftStickY * 0.025f).coerceIn(-0.5f, 0.5f)
                    }
                    if (abs(rightStickX) > 0.05f) {
                        cameraYaw += rightStickX * 0.04f
                    }
                    // Jump physics
                    if (playerJumpHeight > 0f || playerJumpVel != 0f) {
                        playerJumpHeight += playerJumpVel
                        playerJumpVel -= 1.1f
                        if (playerJumpHeight <= 0f) {
                            playerJumpHeight = 0f
                            playerJumpVel = 0f
                        }
                    }
                    if (frameTick % 90L == 0L) {
                        gameScoreOrCoins++
                    }
                }
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        when (bootStage) {
            0 -> drawSwitchRomDecryptBootScreen(textMeasurer, gameTitle, programIdHex, isNx2, frameTick)
            1 -> drawNintendoSwitchSplash(textMeasurer, gameTitle, isNx2)
            else -> {
                when (genre) {
                    ActiveGameGenre.TETRIS -> drawPlayableTetrisFrame(
                        textMeasurer = textMeasurer,
                        gameTitle = gameTitle,
                        board = tetrisBoard,
                        currentPiece = currentPiece,
                        pieceX = pieceX,
                        pieceY = pieceY,
                        nextPiece = TETROMINO_SHAPES[nextPieceIdx],
                        score = tetrisScore,
                        lines = tetrisLines,
                        level = tetrisLevel,
                        canPlace = ::canPlacePiece
                    )
                    ActiveGameGenre.RACING -> drawPlayableRacingFrame(
                        textMeasurer = textMeasurer,
                        gameTitle = gameTitle,
                        animTime = animTime,
                        playerX = playerX,
                        jumpHeight = playerJumpHeight,
                        boostActive = speedBoostTimer > 0,
                        coins = gameScoreOrCoins,
                        isNx2 = isNx2
                    )
                    ActiveGameGenre.OPEN_WORLD_ZELDA -> drawPlayableZeldaOpenWorldFrame(
                        textMeasurer = textMeasurer,
                        gameTitle = gameTitle,
                        animTime = animTime,
                        playerX = playerX,
                        playerY = playerY,
                        jumpHeight = playerJumpHeight,
                        slashTimer = attackSlashTimer,
                        cameraYaw = cameraYaw
                    )
                    else -> drawPlayablePlatformerFrame(
                        textMeasurer = textMeasurer,
                        gameTitle = gameTitle,
                        animTime = animTime,
                        playerX = playerX,
                        jumpHeight = playerJumpHeight,
                        attackTimer = attackSlashTimer,
                        coins = gameScoreOrCoins
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSwitchRomDecryptBootScreen(
    textMeasurer: TextMeasurer,
    title: String,
    programId: String,
    isNx2: Boolean,
    tick: Long
) {
    drawRect(color = Color(0xFF09080E))
    val cx = size.width / 2f
    val cy = size.height / 2f

    val headerText = if (isNx2) "NINTENDO SWITCH 2 (NX2) • ExeFS LOADER" else "NINTENDO SWITCH • NCA/XCI DECRYPTOR"
    val t1 = textMeasurer.measure(
        text = headerText,
        style = TextStyle(color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    )
    drawText(t1, topLeft = Offset(cx - t1.size.width / 2f, cy - 60f))

    val t2 = textMeasurer.measure(
        text = "Mounting ROM: $title ($programId)",
        style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    )
    drawText(t2, topLeft = Offset(cx - t2.size.width / 2f, cy - 28f))

    // Progress bar
    val barW = size.width * 0.55f
    val progress = ((tick % 45) / 40f).coerceIn(0.2f, 1.0f)
    drawRoundRect(
        color = Color(0xFF232034),
        topLeft = Offset(cx - barW / 2f, cy + 12f),
        size = Size(barW, 12f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color(0xFFE040FB),
        topLeft = Offset(cx - barW / 2f, cy + 12f),
        size = Size(barW * progress, 12f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    val t3 = textMeasurer.measure(
        text = "Compiling SPIR-V Shaders • NCE ARM64 Direct Execution",
        style = TextStyle(color = Color(0xFFB0BEC5), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    )
    drawText(t3, topLeft = Offset(cx - t3.size.width / 2f, cy + 36f))
}

private fun DrawScope.drawNintendoSwitchSplash(
    textMeasurer: TextMeasurer,
    title: String,
    isNx2: Boolean
) {
    drawRect(color = if (isNx2) Color(0xFF1A0926) else Color(0xFFE60012))
    val cx = size.width / 2f
    val cy = size.height / 2f

    // Joy-Con stylized emblem
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - 58f, cy - 65f),
        size = Size(48f, 96f),
        cornerRadius = CornerRadius(22f, 22f),
        style = Stroke(width = 7f)
    )
    drawCircle(color = Color.White, radius = 10f, center = Offset(cx - 34f, cy - 35f))

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx + 10f, cy - 65f),
        size = Size(48f, 96f),
        cornerRadius = CornerRadius(22f, 22f)
    )
    drawCircle(color = if (isNx2) Color(0xFF1A0926) else Color(0xFFE60012), radius = 10f, center = Offset(cx + 34f, cy - 5f))

    val splashTitle = textMeasurer.measure(
        text = if (isNx2) "NINTENDO SWITCH 2" else "NINTENDO SWITCH",
        style = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
    )
    drawText(splashTitle, topLeft = Offset(cx - splashTitle.size.width / 2f, cy + 50f))

    val sub = textMeasurer.measure(
        text = "Booting $title...",
        style = TextStyle(color = Color(0xDDFFFFFF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    )
    drawText(sub, topLeft = Offset(cx - sub.size.width / 2f, cy + 82f))
}

private fun DrawScope.drawPlayableTetrisFrame(
    textMeasurer: TextMeasurer,
    gameTitle: String,
    board: Array<IntArray>,
    currentPiece: Array<IntArray>,
    pieceX: Int,
    pieceY: Int,
    nextPiece: Array<IntArray>,
    score: Int,
    lines: Int,
    level: Int,
    canPlace: (Array<IntArray>, Int, Int) -> Boolean
) {
    // Rich arcade background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0B0E23), Color(0xFF1A103C), Color(0xFF090714))
        )
    )

    val cols = 10
    val rows = 20
    val topReserved = size.height * 0.25f
    val bottomReserved = size.height * 0.23f
    val availHeight = (size.height - topReserved - bottomReserved).coerceAtLeast(220f)
    val cellSize = (availHeight / rows).coerceAtMost(size.width * 0.052f)
    val boardW = cellSize * cols
    val boardH = cellSize * rows
    val boardLeft = (size.width - (boardW + 106f)) / 2f
    val boardTop = topReserved

    // Title Banner above Matrix
    val titleLayout = textMeasurer.measure(
        text = "${gameTitle.uppercase()} • D-PAD/STICK: MOVE • A/X: ROTATE • Y: DROP",
        style = TextStyle(color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp)
    )
    drawText(titleLayout, topLeft = Offset((size.width - titleLayout.size.width) / 2f, boardTop - 24f))

    // Draw Board Outer Frame & Grid
    drawRoundRect(
        color = Color(0xFF060711),
        topLeft = Offset(boardLeft - 4f, boardTop - 4f),
        size = Size(boardW + 8f, boardH + 8f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(boardLeft - 4f, boardTop - 4f),
        size = Size(boardW + 8f, boardH + 8f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 3f)
    )

    // Subtle matrix grid lines
    for (r in 0..rows) {
        val y = boardTop + r * cellSize
        drawLine(Color(0x1FFFFFFF), Offset(boardLeft, y), Offset(boardLeft + boardW, y), 1f)
    }
    for (c in 0..cols) {
        val x = boardLeft + c * cellSize
        drawLine(Color(0x1FFFFFFF), Offset(x, boardTop), Offset(x, boardTop + boardH), 1f)
    }

    // Calculate Ghost Piece Y (where piece will land)
    var ghostY = pieceY
    while (canPlace(currentPiece, pieceX, ghostY + 1)) {
        ghostY++
    }

    // Draw Ghost Piece
    for (r in currentPiece.indices) {
        for (c in currentPiece[r].indices) {
            val colorIdx = currentPiece[r][c]
            if (colorIdx != 0) {
                val gx = boardLeft + (pieceX + c) * cellSize
                val gy = boardTop + (ghostY + r) * cellSize
                drawRect(
                    color = TETROMINO_COLORS[colorIdx].copy(alpha = 0.28f),
                    topLeft = Offset(gx + 1f, gy + 1f),
                    size = Size(cellSize - 2f, cellSize - 2f),
                    style = Stroke(width = 2f)
                )
            }
        }
    }

    // Draw Locked Blocks
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val colorIdx = board[r][c]
            if (colorIdx != 0) {
                val bx = boardLeft + c * cellSize
                val by = boardTop + r * cellSize
                drawRect(
                    color = TETROMINO_COLORS[colorIdx],
                    topLeft = Offset(bx + 1f, by + 1f),
                    size = Size(cellSize - 2f, cellSize - 2f)
                )
                // Inner bevel highlight
                drawRect(
                    color = Color(0x55FFFFFF),
                    topLeft = Offset(bx + 2f, by + 2f),
                    size = Size(cellSize - 4f, 3f)
                )
            }
        }
    }

    // Draw Active Falling Piece
    for (r in currentPiece.indices) {
        for (c in currentPiece[r].indices) {
            val colorIdx = currentPiece[r][c]
            if (colorIdx != 0) {
                val px = boardLeft + (pieceX + c) * cellSize
                val py = boardTop + (pieceY + r) * cellSize
                if (pieceY + r >= 0) {
                    drawRect(
                        color = TETROMINO_COLORS[colorIdx],
                        topLeft = Offset(px + 1f, py + 1f),
                        size = Size(cellSize - 2f, cellSize - 2f)
                    )
                    drawRect(
                        color = Color(0x88FFFFFF),
                        topLeft = Offset(px + 2f, py + 2f),
                        size = Size(cellSize - 4f, 3f)
                    )
                }
            }
        }
    }

    // Draw Side HUD Panel (SCORE, LINES, LEVEL, NEXT PIECE)
    val hudLeft = boardLeft + boardW + 14f
    val hudW = 92f

    // NEXT PIECE BOX
    drawRoundRect(
        color = Color(0xCC121428),
        topLeft = Offset(hudLeft, boardTop),
        size = Size(hudW, 78f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = Color(0xFFE040FB),
        topLeft = Offset(hudLeft, boardTop),
        size = Size(hudW, 78f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.5f)
    )
    val nextLabel = textMeasurer.measure(
        text = "NEXT",
        style = TextStyle(color = Color(0xFFE040FB), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    )
    drawText(nextLabel, topLeft = Offset(hudLeft + 10f, boardTop + 6f))

    val miniCell = cellSize * 0.72f
    for (r in nextPiece.indices) {
        for (c in nextPiece[r].indices) {
            val cIdx = nextPiece[r][c]
            if (cIdx != 0) {
                drawRect(
                    color = TETROMINO_COLORS[cIdx],
                    topLeft = Offset(hudLeft + 14f + c * miniCell, boardTop + 30f + r * miniCell),
                    size = Size(miniCell - 1.5f, miniCell - 1.5f)
                )
            }
        }
    }

    // SCORE / LINES / LEVEL BOX
    drawRoundRect(
        color = Color(0xCC121428),
        topLeft = Offset(hudLeft, boardTop + 90f),
        size = Size(hudW, 138f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(hudLeft, boardTop + 90f),
        size = Size(hudW, 138f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.5f)
    )

    val statsText = textMeasurer.measure(
        text = "SCORE\n$score\n\nLINES\n$lines\n\nLEVEL $level",
        style = TextStyle(color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
    )
    drawText(statsText, topLeft = Offset(hudLeft + 10f, boardTop + 98f))
}

private fun DrawScope.drawPlayableRacingFrame(
    textMeasurer: TextMeasurer,
    gameTitle: String,
    animTime: Float,
    playerX: Float,
    jumpHeight: Float,
    boostActive: Boolean,
    coins: Int,
    isNx2: Boolean
) {
    val w = size.width
    val h = size.height
    val horizonY = h * 0.44f

    // Sky & Sunset Horizon Gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = if (isNx2) listOf(Color(0xFF0D0628), Color(0xFF311042), Color(0xFFFF4081))
            else listOf(Color(0xFF0D254C), Color(0xFF1E5799), Color(0xFF7DB9E8)),
            startY = 0f,
            endY = horizonY
        ),
        size = Size(w, horizonY)
    )

    // Distant Mountains
    val mountainPath = Path().apply {
        moveTo(0f, horizonY)
        lineTo(w * 0.18f, horizonY - 45f)
        lineTo(w * 0.35f, horizonY - 15f)
        lineTo(w * 0.55f, horizonY - 65f)
        lineTo(w * 0.78f, horizonY - 22f)
        lineTo(w * 0.92f, horizonY - 50f)
        lineTo(w, horizonY)
        close()
    }
    drawPath(mountainPath, color = Color(0xFF1B1534))

    // Grass / Out-of-bounds Floor
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF143D29), Color(0xFF0A2116)),
            startY = horizonY,
            endY = h
        ),
        topLeft = Offset(0f, horizonY),
        size = Size(w, h - horizonY)
    )

    // Road Trapezoid with dynamic curve sway
    val curveSway = sin(animTime * 0.35f) * (w * 0.12f)
    val roadTopCenterX = w / 2f + curveSway
    val roadBottomCenterX = w / 2f - playerX * (w * 0.25f)
    val roadTopW = w * 0.14f
    val roadBottomW = w * 0.92f

    val roadPath = Path().apply {
        moveTo(roadTopCenterX - roadTopW / 2f, horizonY)
        lineTo(roadTopCenterX + roadTopW / 2f, horizonY)
        lineTo(roadBottomCenterX + roadBottomW / 2f, h)
        lineTo(roadBottomCenterX - roadBottomW / 2f, h)
        close()
    }
    drawPath(roadPath, color = Color(0xFF262936))

    // Red/White Alternating Curbs & Rainbow Boost Strips
    val segments = 12
    for (i in 0 until segments) {
        val t0 = i.toFloat() / segments
        val t1 = (i + 1).toFloat() / segments
        val y0 = horizonY + t0 * t0 * (h - horizonY)
        val y1 = horizonY + t1 * t1 * (h - horizonY)
        val cx0 = roadTopCenterX + (roadBottomCenterX - roadTopCenterX) * t0
        val cx1 = roadTopCenterX + (roadBottomCenterX - roadTopCenterX) * t1
        val rw0 = roadTopW + (roadBottomW - roadTopW) * t0
        val rw1 = roadTopW + (roadBottomW - roadTopW) * t1

        val isRed = ((i + (animTime * 4f).toInt()) % 2 == 0)
        val curbColor = if (isRed) Color(0xFFFF1744) else Color.White

        // Left & Right Curb Segments
        drawLine(curbColor, Offset(cx0 - rw0 / 2f, y0), Offset(cx1 - rw1 / 2f, y1), strokeWidth = 6f)
        drawLine(curbColor, Offset(cx0 + rw0 / 2f, y0), Offset(cx1 + rw1 / 2f, y1), strokeWidth = 6f)

        // Center dashed lane line
        if (isRed) {
            drawLine(Color(0x99FFEE58), Offset(cx0, y0), Offset(cx1, y1), strokeWidth = 4f)
        }
    }

    // Player Kart in center-bottom
    val kartX = w / 2f + playerX * (w * 0.26f)
    val kartY = h * 0.72f - jumpHeight

    // Kart Shadow
    drawOval(
        color = Color(0x88000000),
        topLeft = Offset(kartX - 44f, h * 0.72f + 16f),
        size = Size(88f, 20f)
    )

    // Kart Rear Tires
    drawRoundRect(Color(0xFF111115), topLeft = Offset(kartX - 46f, kartY - 6f), size = Size(20f, 28f), cornerRadius = CornerRadius(6f, 6f))
    drawRoundRect(Color(0xFF111115), topLeft = Offset(kartX + 26f, kartY - 6f), size = Size(20f, 28f), cornerRadius = CornerRadius(6f, 6f))

    // Kart Body Chassis
    drawRoundRect(
        color = Color(0xFFE53935),
        topLeft = Offset(kartX - 30f, kartY - 14f),
        size = Size(60f, 34f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    // Driver Helmet
    drawCircle(color = Color.White, radius = 15f, center = Offset(kartX, kartY - 22f))
    drawRoundRect(color = Color(0xFF00E5FF), topLeft = Offset(kartX - 10f, kartY - 26f), size = Size(20f, 6f), cornerRadius = CornerRadius(3f, 3f))

    // Dual Exhaust Flames (Bigger when boosting with A/B)
    val flameColor = if (boostActive) Color(0xFF00E5FF) else Color(0xFFFF9100)
    val flameRadius = if (boostActive) 18f else 9f
    drawCircle(color = flameColor, radius = flameRadius, center = Offset(kartX - 14f, kartY + 22f))
    drawCircle(color = flameColor, radius = flameRadius, center = Offset(kartX + 14f, kartY + 22f))

    // In-Game Racing HUD (POS 1st, LAP 2/3, Speedometer)
    val speedKmh = if (boostActive) 198 else 145
    val hudLayout = textMeasurer.measure(
        text = "$gameTitle\nPOS: 1st / 12   LAP: 2/3   COINS: $coins   SPEED: $speedKmh km/h",
        style = TextStyle(color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
    )
    drawRoundRect(
        color = Color(0xAA0D0C16),
        topLeft = Offset(w / 2f - hudLayout.size.width / 2f - 10f, h * 0.24f),
        size = Size(hudLayout.size.width + 20f, hudLayout.size.height + 12f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawText(hudLayout, topLeft = Offset(w / 2f - hudLayout.size.width / 2f, h * 0.24f + 6f))
}

private fun DrawScope.drawPlayableZeldaOpenWorldFrame(
    textMeasurer: TextMeasurer,
    gameTitle: String,
    animTime: Float,
    playerX: Float,
    playerY: Float,
    jumpHeight: Float,
    slashTimer: Int,
    cameraYaw: Float
) {
    val w = size.width
    val h = size.height
    val horizonY = h * 0.45f

    // Hyrule Sky & Floating Sky Islands
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1A3C6E), Color(0xFF4A89DC), Color(0xFFA5D6A7)),
            startY = 0f,
            endY = horizonY
        ),
        size = Size(w, horizonY)
    )

    // Floating Sky Islands
    val islandX = (w * 0.3f + cos(cameraYaw) * 80f)
    drawRoundRect(Color(0xFF8D6E63), topLeft = Offset(islandX, horizonY * 0.52f), size = Size(90f, 18f), cornerRadius = CornerRadius(6f, 6f))
    drawRoundRect(Color(0xFF66BB6A), topLeft = Offset(islandX - 4f, horizonY * 0.50f), size = Size(98f, 8f), cornerRadius = CornerRadius(4f, 4f))

    // Rolling Hyrule Grasslands
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF0D3311)),
            startY = horizonY,
            endY = h
        ),
        topLeft = Offset(0f, horizonY),
        size = Size(w, h - horizonY)
    )

    // Character Hero (Link / Adventurer)
    val heroX = w / 2f + playerX * (w * 0.28f)
    val heroY = h * 0.68f - playerY * 60f - jumpHeight

    // Stamina Wheel when moving or jumping
    if (jumpHeight > 0f || slashTimer > 0) {
        drawCircle(
            color = Color(0xFF00E676),
            radius = 22f,
            center = Offset(heroX - 42f, heroY - 36f),
            style = Stroke(width = 5f)
        )
    }

    // Hero Cloak & Tunic
    drawRoundRect(
        color = Color(0xFF00B0FF), // Champion Tunic Blue
        topLeft = Offset(heroX - 18f, heroY - 26f),
        size = Size(36f, 46f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Hero Head & Blonde Hair
    drawCircle(color = Color(0xFFFFD54F), radius = 15f, center = Offset(heroX, heroY - 38f))
    // Shield on Back
    drawRoundRect(Color(0xFF283593), topLeft = Offset(heroX - 12f, heroY - 20f), size = Size(24f, 30f), cornerRadius = CornerRadius(6f, 6f))

    // Master Sword Spin Slash when Y/X/ZR pressed
    if (slashTimer > 0) {
        drawCircle(
            color = Color(0x8800E5FF),
            radius = 68f,
            center = Offset(heroX, heroY - 8f),
            style = Stroke(width = 8f)
        )
    }

    // Hearts & Coordinates HUD
    val hudLayout = textMeasurer.measure(
        text = "♥ ♥ ♥ ♥ ♥ ♥ ♥ ♥ ♥ ♥   [$gameTitle]\n Pos: (${(playerX * 100).toInt()}, ${(playerY * 100).toInt()}) • Ultrahand Ready",
        style = TextStyle(color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    )
    drawRoundRect(
        color = Color(0xAA0D0C16),
        topLeft = Offset(24f, h * 0.24f),
        size = Size(hudLayout.size.width + 20f, hudLayout.size.height + 12f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawText(hudLayout, topLeft = Offset(34f, h * 0.24f + 6f))
}

private fun DrawScope.drawPlayablePlatformerFrame(
    textMeasurer: TextMeasurer,
    gameTitle: String,
    animTime: Float,
    playerX: Float,
    jumpHeight: Float,
    attackTimer: Int,
    coins: Int
) {
    val w = size.width
    val h = size.height
    val groundY = h * 0.72f

    // Vibrant Kingdom Sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF90CAF9)),
            startY = 0f,
            endY = groundY
        ),
        size = Size(w, groundY)
    )

    // Floating ? Blocks
    val blockY = groundY - 120f
    for (i in -1..1) {
        val bx = w / 2f + i * 75f - (animTime * 20f) % 45f
        drawRoundRect(
            color = Color(0xFFFFB300),
            topLeft = Offset(bx, blockY),
            size = Size(42f, 42f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = Color(0xFF5D4037),
            topLeft = Offset(bx, blockY),
            size = Size(42f, 42f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 3f)
        )
    }

    // Ground Bricks & Grass
    drawRect(color = Color(0xFF43A047), topLeft = Offset(0f, groundY), size = Size(w, 16f))
    drawRect(color = Color(0xFF8D6E63), topLeft = Offset(0f, groundY + 16f), size = Size(w, h - groundY))

    // Player Character
    val charX = w / 2f + playerX * (w * 0.32f)
    val charY = groundY - 24f - jumpHeight

    // Overalls & Cap
    drawRoundRect(Color(0xFF1E88E5), topLeft = Offset(charX - 16f, charY - 16f), size = Size(32f, 36f), cornerRadius = CornerRadius(8f, 8f))
    drawCircle(color = Color(0xFFE53935), radius = 16f, center = Offset(charX, charY - 28f))

    // Cappy Throw Spin Effect
    if (attackTimer > 0) {
        val capX = charX + (22 - attackTimer) * 8f
        drawOval(Color(0xFFE53935), topLeft = Offset(capX - 18f, charY - 26f), size = Size(36f, 16f))
    }

    val hudLayout = textMeasurer.measure(
        text = "$gameTitle   ★ × $coins   [L-Stick: Move • A/B: Jump • X/Y: Action]",
        style = TextStyle(color = Color(0xFFFFEA00), fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
    )
    drawRoundRect(
        color = Color(0xBB0D0C16),
        topLeft = Offset(w / 2f - hudLayout.size.width / 2f - 10f, h * 0.24f),
        size = Size(hudLayout.size.width + 20f, hudLayout.size.height + 12f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawText(hudLayout, topLeft = Offset(w / 2f - hudLayout.size.width / 2f, h * 0.24f + 6f))
}
