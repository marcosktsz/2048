@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.example.game2048

import android.media.AudioManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferences = getSharedPreferences("2048", MODE_PRIVATE)
        val initialGame = GameStateStorage.decode(preferences.getString("current_game", null))
            ?: GameLogic.newGame()
        val initialUndo = GameStateStorage.decode(preferences.getString("undo_game", null))
        if (!preferences.contains("current_match_undo_enabled")) {
            preferences.edit()
                .putBoolean("current_match_undo_enabled", preferences.getBoolean("undo_enabled", true))
                .apply()
        }
        persistGameState(preferences, initialGame, initialUndo)
        setContent {
            val context = LocalContext.current
            val hapticFeedback = LocalHapticFeedback.current
            val audioManager = remember(context) { checkNotNull(context.getSystemService(AudioManager::class.java)) }
            val systemDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
            var themeMode by remember {
                mutableStateOf(AppThemeMode.fromPreference(preferences.getString("theme_mode", null)))
            }
            var hapticsEnabled by remember { mutableStateOf(preferences.getBoolean("haptics_enabled", true)) }
            var undoEnabled by remember { mutableStateOf(preferences.getBoolean("undo_enabled", true)) }
            var undoEnabledForMatch by remember {
                mutableStateOf(preferences.getBoolean("current_match_undo_enabled", true))
            }
            var playSoundEnabled by remember { mutableStateOf(preferences.getBoolean("play_sound_enabled", true)) }
            val settingsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) {
                themeMode = AppThemeMode.fromPreference(preferences.getString("theme_mode", null))
                hapticsEnabled = preferences.getBoolean("haptics_enabled", true)
                undoEnabled = preferences.getBoolean("undo_enabled", true)
                playSoundEnabled = preferences.getBoolean("play_sound_enabled", true)
            }
            val darkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDarkTheme
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
                darkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme, motionScheme = MotionScheme.expressive()) {
                Surface(color = colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxSize()) {
                    var game by remember { mutableStateOf(initialGame) }
                    var best by remember { mutableStateOf(preferences.getInt("best", 0)) }
                    var undoState by remember { mutableStateOf(initialUndo) }
                    var motion by remember { mutableStateOf<BoardMotion?>(null) }
                    var motionId by remember { mutableIntStateOf(0) }

                    GameScreen(
                        game = game,
                        best = best,
                        canUndo = undoState != null,
                        undoEnabledForMatch = undoEnabledForMatch,
                        motion = motion,
                        onMotionFinished = { id -> if (motion?.id == id) motion = null },
                        onSettings = {
                            motion = null
                            settingsLauncher.launch(Intent(context, SettingsActivity::class.java))
                        },
                        hapticsEnabled = hapticsEnabled,
                        onMove = { direction ->
                            val previous = game
                            val next = GameLogic.move(previous, direction)
                            if (next != previous) {
                                if (playSoundEnabled && next.score > previous.score) {
                                    audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK)
                                }
                                if (hapticsEnabled && next.gameOver && !previous.gameOver) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                                } else if (hapticsEnabled && next.score > previous.score) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                }
                                motionId += 1
                                motion = GameLogic.motion(
                                    previous,
                                    next,
                                    direction,
                                    motionId,
                                )
                                val previousForUndo = previous.takeIf { undoEnabledForMatch }
                                undoState = previousForUndo
                                game = next
                                persistGameState(preferences, next, previousForUndo)
                                if (game.score > best) {
                                    best = game.score
                                    preferences.edit().putInt("best", best).apply()
                                }
                            }
                        },
                        onUndo = {
                            motion = null
                            undoState?.let {
                                game = it
                                persistGameState(preferences, it, null)
                            }
                            undoState = null
                        },
                        onNewGame = {
                            motion = null
                            game = GameLogic.newGame()
                            undoState = null
                            undoEnabledForMatch = undoEnabled
                            preferences.edit()
                                .putBoolean("current_match_undo_enabled", undoEnabledForMatch)
                                .apply()
                            persistGameState(preferences, game, null)
                        },
                    )
                }
            }
        }
    }
}

private fun persistGameState(
    preferences: SharedPreferences,
    game: GameState,
    undoState: GameState?,
) {
    preferences.edit()
        .putString("current_game", GameStateStorage.encode(game))
        .apply {
            if (undoState == null) remove("undo_game")
            else putString("undo_game", GameStateStorage.encode(undoState))
        }
        .apply()
}

// 2.25x spring stiffness makes motion roughly 1.5x faster without changing damping.
private const val ANIMATION_STIFFNESS_MULTIPLIER = 2.25f
private fun <T> fasterSpring(dampingRatio: Float, stiffness: Float): FiniteAnimationSpec<T> =
    spring(dampingRatio = dampingRatio, stiffness = stiffness * ANIMATION_STIFFNESS_MULTIPLIER)

private fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = fasterSpring(0.6f, 800f)

private fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = fasterSpring(1f, 3800f)

private fun <T> standardFastSpatialSpec(): FiniteAnimationSpec<T> = fasterSpring(0.9f, 1400f)

@Composable
private fun GameScreen(
    game: GameState,
    best: Int,
    canUndo: Boolean,
    undoEnabledForMatch: Boolean,
    motion: BoardMotion?,
    onMotionFinished: (Int) -> Unit,
    onSettings: () -> Unit,
    hapticsEnabled: Boolean,
    onMove: (Direction) -> Unit,
    onUndo: () -> Unit,
    onNewGame: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val dragHapticFeedback = LocalHapticFeedback.current
    val swipeHapticIntervalPx = with(LocalDensity.current) { 2.dp.toPx() }
    val undoInteractionSource = remember { MutableInteractionSource() }
    val newGameInteractionSource = remember { MutableInteractionSource() }
    val settingsInteractionSource = remember { MutableInteractionSource() }
    var confirmNewGame by remember { mutableStateOf(false) }
    var showGameOverDialog by rememberSaveable { mutableStateOf(game.gameOver) }
    var gameOverDialogDismissed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(game.gameOver, motion?.id) {
        when {
            !game.gameOver -> {
                showGameOverDialog = false
                gameOverDialogDismissed = false
            }
            motion == null && !gameOverDialogDismissed -> showGameOverDialog = true
        }
    }
    Surface(color = colors.surfaceContainerHigh, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerHigh)
                    .statusBarsPadding()
                    .displayCutoutPadding(),
                contentAlignment = Alignment.TopCenter,
            ) {
                Header(
                    modifier = Modifier
                        .widthIn(max = 540.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 16.dp),
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .gameSwipeInput(
                        game.board,
                        hapticsEnabled,
                        dragHapticFeedback,
                        swipeHapticIntervalPx,
                        onMove,
                    ),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = colors.surface,
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        val compact = maxHeight < 760.dp
                        Column(
                            modifier = Modifier
                                .widthIn(max = 540.dp)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .heightIn(min = maxHeight)
                                .gameSwipeInput(
                                    game.board,
                                    hapticsEnabled,
                                    dragHapticFeedback,
                                    swipeHapticIntervalPx,
                                    onMove,
                                )
                                .padding(horizontal = 22.dp)
                                .padding(
                                    top = if (compact) 24.dp else 34.dp,
                                    bottom = 96.dp,
                                ),
                            verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ScoreCard(label = "SCORE", value = game.score, modifier = Modifier.weight(1f))
                                ScoreCard(label = "BEST", value = best, modifier = Modifier.weight(1f))
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(if (compact) 14.dp else 20.dp)) {
                                    GameBoard(game.board, motion, onMotionFinished)

                                    Text(
                                        text = when {
                                            game.gameOver -> "No moves left. Give it another go?"
                                            game.won -> "2048! Lovely work. Keep playing or start fresh."
                                            else -> "Slide the tiles to join matching numbers."
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = if (game.gameOver) colors.error else colors.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = "A LITTLE GAME OF BIG NUMBERS",
                                        modifier = Modifier.fillMaxWidth(),
                                        color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 10.sp,
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
                HorizontalFloatingToolbar(
                    expanded = true,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    ButtonGroup(
                        overflowIndicator = { menuState ->
                            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                        },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val tap: () -> Unit = {
                            if (hapticsEnabled) {
                                dragHapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            }
                        }
                        if (undoEnabledForMatch) {
                            toolbarActionItem(
                                onClick = { tap(); onUndo() },
                                label = "Undo",
                                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                                interactionSource = undoInteractionSource,
                                enabled = canUndo,
                            )
                        }
                        toolbarActionItem(
                            onClick = { tap(); confirmNewGame = true },
                            label = "New game",
                            icon = Icons.Rounded.Refresh,
                            interactionSource = newGameInteractionSource,
                        )
                        toolbarActionItem(
                            onClick = { tap(); onSettings() },
                            label = "Settings",
                            icon = Icons.Rounded.Settings,
                            interactionSource = settingsInteractionSource,
                        )
                    }
                }
            }
        }
    }

    if (confirmNewGame) {
        GameAlertDialog(
            title = "Start a new game?",
            message = "Your current board and score will be replaced.",
            dismissLabel = "CANCEL",
            confirmLabel = "NEW GAME",
            onDismiss = { confirmNewGame = false },
            onConfirm = {
                confirmNewGame = false
                onNewGame()
            },
        )
    }

    if (showGameOverDialog) {
        GameAlertDialog(
            title = "No moves left",
            message = "You scored ${game.score}. Ready for another round?",
            dismissLabel = "CLOSE",
            confirmLabel = "PLAY AGAIN",
            onDismiss = {
                showGameOverDialog = false
                gameOverDialogDismissed = true
            },
            onConfirm = {
                showGameOverDialog = false
                gameOverDialogDismissed = true
                onNewGame()
            },
        )
    }
}

@Composable
private fun GameAlertDialog(
    title: String,
    message: String,
    dismissLabel: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            val blurBehindPx = with(LocalDensity.current) { 12.dp.roundToPx() }
            val backgroundBlurPx = with(LocalDensity.current) { 20.dp.roundToPx() }
            SideEffect {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dialogWindow != null) {
                    dialogWindow.addFlags(
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND or
                            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                    )
                    dialogWindow.setDimAmount(0.18f)
                    val attributes = dialogWindow.attributes
                    attributes.blurBehindRadius = blurBehindPx
                    dialogWindow.attributes = attributes
                    dialogWindow.setBackgroundBlurRadius(backgroundBlurPx)
                }
            }
            Text(title)
        },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
    )
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = "2048",
                color = colors.onSurface,
                fontSize = 57.sp,
                lineHeight = 61.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-3).sp,
            )
            Text(
                text = "Make room for one more.",
                color = colors.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun ButtonGroupScope.toolbarActionItem(
    onClick: () -> Unit,
    label: String,
    icon: ImageVector,
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
) {
    val animatedWidth = Modifier.animateWidth(interactionSource)
    customItem(
        buttonGroupContent = {
            FilledTonalIconButton(
                onClick = onClick,
                modifier = animatedWidth,
                enabled = enabled,
                interactionSource = interactionSource,
            ) {
                androidx.compose.material3.Icon(icon, contentDescription = label)
            }
        },
        menuContent = { menuState ->
            DropdownMenuItem(
                text = { Text(label) },
                leadingIcon = {
                    androidx.compose.material3.Icon(icon, contentDescription = null)
                },
                onClick = {
                    onClick()
                    menuState.dismiss()
                },
                enabled = enabled,
            )
        },
    )
}

private fun Modifier.gameSwipeInput(
    board: List<List<Int>>,
    hapticsEnabled: Boolean,
    hapticFeedback: HapticFeedback,
    hapticIntervalPx: Float,
    onMove: (Direction) -> Unit,
): Modifier = pointerInput(board, hapticsEnabled, hapticIntervalPx) {
    val swipeHoldCommitDistancePx = 56f
    var swipe = SwipeGestureTracker(
        startedAtMillis = SystemClock.uptimeMillis(),
        heldCommitThresholdPx = swipeHoldCommitDistancePx,
    )
    var lastHapticDistance = 0f
    detectDragGestures(
        onDragStart = {
            swipe = SwipeGestureTracker(
                startedAtMillis = SystemClock.uptimeMillis(),
                heldCommitThresholdPx = swipeHoldCommitDistancePx,
            )
            lastHapticDistance = 0f
        },
        onDragEnd = {
            swipe.completedDirection(SystemClock.uptimeMillis())?.let(onMove)
        },
        onDragCancel = {
            swipe = SwipeGestureTracker(
                startedAtMillis = SystemClock.uptimeMillis(),
                heldCommitThresholdPx = swipeHoldCommitDistancePx,
            )
            lastHapticDistance = 0f
        },
        onDrag = { change, amount ->
            swipe.add(amount.x, amount.y, SystemClock.uptimeMillis())
            val direction = swipe.direction
            val distance = swipe.distance
            if (!swipe.isCancelled && swipe.progressDelta > 0f && direction != null &&
                hapticsEnabled && distance > 0f &&
                (lastHapticDistance == 0f || distance - lastHapticDistance >= hapticIntervalPx)
            ) {
                if (GameLogic.canMove(board, direction)) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    lastHapticDistance = distance
                }
            }
            change.consume()
        },
    )
}

@Composable
private fun ScoreCard(label: String, value: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val displayedValue by animateIntAsState(
        targetValue = value,
        animationSpec = fastEffectsSpec(),
        label = "score-count",
    )
    val emphasis = remember { Animatable(1f) }
    var previousValue by remember { mutableIntStateOf(value) }
    LaunchedEffect(value) {
        if (value != previousValue) {
            previousValue = value
            emphasis.snapTo(0.86f)
            emphasis.animateTo(1f, fastSpatialSpec())
        }
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(19.dp))
            .background(colors.surfaceContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(label, color = colors.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
        Text(
            displayedValue.toString(),
            modifier = Modifier.graphicsLayer {
                scaleX = emphasis.value
                scaleY = emphasis.value
            },
            color = colors.onSurface,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 26.sp,
        )
    }
}

@Composable
private fun GameBoard(
    board: List<List<Int>>,
    motion: BoardMotion?,
    onMotionFinished: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val boardColor = colors.surface
    val gap = 8.dp
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(28.dp))
            .background(boardColor)
            .padding(11.dp),
    ) {
        val cellSize = (maxWidth - gap * 3) / 4
        val stepPx = with(LocalDensity.current) { cellSize.roundToPx() + gap.roundToPx() }

        Box(Modifier.fillMaxSize()) {
            if (motion == null) {
                TileGrid(board, cellSize, gap)
            } else {
                val current = motion
                var phase by remember(current.id) { mutableStateOf(BoardAnimationPhase.MOVING) }
                val completed = remember(current.id) { mutableIntStateOf(0) }
                val movingTiles = current.tiles.filter { it.kind != TileMotionKind.SPAWN }
                val spawnedTiles = current.tiles.filter { it.kind == TileMotionKind.SPAWN }
                TileGrid(
                    GameLogic.animationBoard(board, current, phase),
                    cellSize,
                    gap,
                )
                val onPartFinished = {
                    completed.intValue += 1
                    val partCount = when (phase) {
                        BoardAnimationPhase.MOVING -> movingTiles.size + current.merges.size
                        BoardAnimationPhase.SPAWNING -> spawnedTiles.size
                    }
                    if (completed.intValue == partCount) {
                        when (phase) {
                            BoardAnimationPhase.MOVING -> if (spawnedTiles.isNotEmpty()) {
                                completed.intValue = 0
                                phase = BoardAnimationPhase.SPAWNING
                            } else {
                                onMotionFinished(current.id)
                            }
                            BoardAnimationPhase.SPAWNING -> onMotionFinished(current.id)
                        }
                    }
                }
                when (phase) {
                    BoardAnimationPhase.MOVING -> {
                        movingTiles.forEachIndexed { index, tileMotion ->
                            AnimatedPathTile(
                                tileMotion = tileMotion,
                                index = index,
                                motionId = current.id,
                                cellSize = cellSize,
                                stepPx = stepPx,
                                dragDirection = current.direction,
                                onFinished = onPartFinished,
                            )
                        }
                        current.merges.forEachIndexed { index, merge ->
                            AnimatedMergeTile(
                                merge = merge,
                                index = index,
                                motionId = current.id,
                                cellSize = cellSize,
                                stepPx = stepPx,
                                onFinished = onPartFinished,
                            )
                        }
                    }
                    BoardAnimationPhase.SPAWNING -> {
                        spawnedTiles.forEachIndexed { index, tileMotion ->
                            AnimatedPathTile(
                                tileMotion = tileMotion,
                                index = index + movingTiles.size,
                                motionId = current.id,
                                cellSize = cellSize,
                                stepPx = stepPx,
                                dragDirection = current.direction,
                                onFinished = onPartFinished,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TileGrid(
    board: List<List<Int>>,
    cellSize: Dp,
    gap: Dp,
) {
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        board.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { value ->
                    Tile(value = value, size = cellSize)
                }
            }
        }
    }
}

@Composable
private fun AnimatedPathTile(
    tileMotion: TileMotion,
    index: Int,
    motionId: Int,
    cellSize: Dp,
    stepPx: Int,
    dragDirection: Direction,
    onFinished: () -> Unit,
) {
    val horizontal = dragDirection == Direction.LEFT || dragDirection == Direction.RIGHT
    val inFlightStretch = if (horizontal) Offset(1.02f, 0.9925f) else Offset(0.9925f, 1.02f)
    val start = Offset(tileMotion.from.column * stepPx.toFloat(), tileMotion.from.row * stepPx.toFloat())
    val target = Offset(tileMotion.to.column * stepPx.toFloat(), tileMotion.to.row * stepPx.toFloat())
    val position = remember(motionId, index) { Animatable(start, Offset.VectorConverter) }
    val scale = remember(motionId, index) {
        Animatable(if (tileMotion.kind == TileMotionKind.SPAWN) 0.5f else 1f)
    }
    val stretchScale = remember(motionId, index) { Animatable(Offset(1f, 1f), Offset.VectorConverter) }
    val alpha = remember(motionId, index) { Animatable(1f) }

    LaunchedEffect(motionId, index) {
        when (tileMotion.kind) {
            TileMotionKind.SLIDE -> {
                coroutineScope {
                    launch { position.animateTo(target, standardFastSpatialSpec()) }
                    launch { stretchScale.animateTo(inFlightStretch, fastSpatialSpec()) }
                }
                stretchScale.animateTo(Offset(1f, 1f), fastSpatialSpec())
            }
            TileMotionKind.MERGE_SOURCE -> {
                coroutineScope {
                    launch { position.animateTo(target, standardFastSpatialSpec()) }
                    launch {
                        delay(40)
                        scale.animateTo(0.35f, fastSpatialSpec())
                    }
                    launch {
                        delay(40)
                        alpha.animateTo(0f, fastEffectsSpec())
                    }
                }
            }
            TileMotionKind.SPAWN -> scale.animateTo(1f, fastSpatialSpec())
        }
        onFinished()
    }

    Tile(
        value = tileMotion.value,
        size = cellSize,
        modifier = Modifier
            .offset {
                IntOffset(position.value.x.roundToInt(), position.value.y.roundToInt())
            }
            .graphicsLayer {
                scaleX = scale.value * stretchScale.value.x
                scaleY = scale.value * stretchScale.value.y
                this.alpha = alpha.value
                transformOrigin = TransformOrigin.Center
            },
    )
}

@Composable
private fun AnimatedMergeTile(
    merge: MergeMotion,
    index: Int,
    motionId: Int,
    cellSize: Dp,
    stepPx: Int,
    onFinished: () -> Unit,
) {
    val scale = remember(motionId, index) { Animatable(0.65f) }
    val alpha = remember(motionId, index) { Animatable(0f) }

    LaunchedEffect(motionId, index) {
        delay(27)
        coroutineScope {
            launch { scale.animateTo(1f, fastSpatialSpec()) }
            launch { alpha.animateTo(1f, fastEffectsSpec()) }
        }
        onFinished()
    }

    Tile(
        value = merge.value,
        size = cellSize,
        modifier = Modifier
            .offset {
                IntOffset(merge.position.column * stepPx, merge.position.row * stepPx)
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
                transformOrigin = TransformOrigin.Center
            },
    )
}

@Composable
private fun Tile(value: Int, size: Dp, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val tilePalette = TilePalette.colorsFor(value, colors)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(17.dp))
            .background(tilePalette.container)
            .semantics { contentDescription = if (value == 0) "Empty" else value.toString() },
        contentAlignment = Alignment.Center,
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                color = tilePalette.content,
                fontSize = when {
                    value < 100 -> 32.sp
                    value < 1000 -> 27.sp
                    else -> 21.sp
                },
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                maxLines = 1,
            )
        }
    }
}
