package com.example.hiddencats.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.hiddencats.core.findActivity
import com.example.hiddencats.domain.model.GameMode
import com.example.hiddencats.domain.model.GameState
import com.example.hiddencats.ui.components.CatFaceIcon
import com.example.hiddencats.ui.components.ControlBar
import com.example.hiddencats.ui.components.GameBackground
import com.example.hiddencats.ui.components.GameButton
import com.example.hiddencats.ui.components.GameButtonText
import com.example.hiddencats.ui.components.GamePanel
import com.example.hiddencats.ui.components.PuzzleBoardView
import com.example.hiddencats.ui.components.StarRow
import com.example.hiddencats.ui.components.StatPill
import com.example.hiddencats.ui.theme.CatAmberAccent
import com.example.hiddencats.ui.theme.CatGreenSuccess
import com.example.hiddencats.ui.theme.CatOrangePrimary
import com.example.hiddencats.ui.theme.CatRedMistake

private fun formatClock(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onGameFinished: (GameState) -> Unit,
    onNavigateShop: () -> Unit = {}
) {
    val state by viewModel.gameState.collectAsState()
    val showHintOffer by viewModel.showHintOffer.collectAsState()
    val hintOfferMessage by viewModel.hintOfferMessage.collectAsState()
    val rewardedReady by viewModel.rewardedReady.collectAsState()
    val activity = LocalContext.current.findActivity()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val scrollState = rememberScrollState()
    var showRestartDialog by remember { mutableStateOf(false) }

    // Auto-pause when the app leaves the foreground so the timer can't run in the background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.pauseIfPlaying()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler {
        if (!state.isCompleted && !state.isGameOver) {
            viewModel.togglePause()
        } else {
            onNavigateBack()
        }
    }

    GameBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (state.mode == GameMode.DAILY) "Daily Challenge" else "${state.puzzle.size.label} Grid",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${state.puzzle.difficulty.label} • ${state.mode.label}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (!state.isCompleted && !state.isGameOver) {
                                    viewModel.togglePause()
                                } else {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.testTag("game_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.togglePause() },
                            modifier = Modifier.testTag("game_pause_button")
                        ) {
                            Icon(
                                imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (state.isPaused) "Resume" else "Pause"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GameStatusBar(state = state)

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = state.activeHintMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val msg = state.activeHintMessage
                        if (msg != null) {
                            GamePanel(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = 12.dp
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = CatOrangePrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    PuzzleBoardView(
                        state = state,
                        isColorBlindMode = preferences.colorBlindMode,
                        onCellClick = { r, c -> viewModel.onCellClicked(r, c) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ControlBar(
                        selectedTool = selectedTool,
                        onToolSelected = { viewModel.selectTool(it) },
                        canUndo = state.canUndo,
                        onUndo = { viewModel.undo() },
                        canRedo = state.canRedo,
                        onRedo = { viewModel.redo() },
                        hintsAvailable = preferences.hintBalance,
                        onHint = { viewModel.requestHint() },
                        onRestart = { showRestartDialog = true },
                        isLeftHanded = preferences.leftHandedMode,
                        unlimitedHints = preferences.premiumUnlocked
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (showHintOffer) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissHintOffer() },
                    shape = RoundedCornerShape(28.dp),
                    title = { Text("Out of hints!", fontWeight = FontWeight.Black) },
                    text = {
                        Column {
                            Text("Watch a short ad for a free hint, or grab more in the Shop.")
                            hintOfferMessage?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(it, color = CatRedMistake, fontSize = 13.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (rewardedReady && activity != null) {
                                GameButton(
                                    onClick = { viewModel.watchAdForHint(activity) },
                                    height = 46.dp,
                                    color = CatGreenSuccess,
                                    modifier = Modifier.width(180.dp).testTag("hint_watch_ad_button")
                                ) { GameButtonText("Watch ad: +1 hint", fontSize = 14.sp) }
                            }
                            GameButton(
                                onClick = onNavigateShop,
                                height = 46.dp,
                                modifier = Modifier.width(180.dp).testTag("hint_shop_button")
                            ) { GameButtonText("Open Shop", fontSize = 14.sp) }
                        }
                    },
                    dismissButton = {
                        GameButton(
                            onClick = { viewModel.dismissHintOffer() },
                            height = 46.dp,
                            color = Color(0xFF8B8FA8),
                            modifier = Modifier.width(90.dp)
                        ) { GameButtonText("Not now", fontSize = 14.sp) }
                    }
                )
            }

            if (state.isPaused && !showHintOffer) {
                PauseDialog(
                    onResume = { viewModel.togglePause() },
                    onRestart = { viewModel.restart() },
                    onExit = onNavigateBack
                )
            }

            if (showRestartDialog) {
                RestartConfirmDialog(
                    onConfirm = {
                        showRestartDialog = false
                        viewModel.restart()
                    },
                    onDismiss = { showRestartDialog = false }
                )
            }

            if (state.isGameOver) {
                GameOverDialog(
                    onRetry = { viewModel.restart() },
                    onViewResults = { onGameFinished(state) },
                    onExit = onNavigateBack
                )
            }

            if (state.isCompleted) {
                VictoryCardDialog(
                    state = state,
                    onViewResults = { onGameFinished(state) }
                )
            }
        }
    }
}

@Composable
fun GameStatusBar(state: GameState) {
    val showLives = state.mode != GameMode.RELAXED
    GamePanel(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        contentPadding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lives (hidden in zen mode)
            if (showLives) {
                StatPill {
                    for (i in 1..state.maxLives) {
                        Heart(active = i <= state.livesRemaining, index = i)
                    }
                }
            } else {
                StatPill { Text("Zen", fontWeight = FontWeight.ExtraBold, color = CatGreenSuccess) }
            }

            // Countdown clock only exists in Timed mode (other modes track time silently for scoring)
            if (state.mode == GameMode.TIMED) {
                ClockPill(
                    text = formatClock(state.remainingSeconds),
                    danger = state.remainingSeconds <= 30
                )
            }

            // Cats found
            StatPill {
                CatFaceIcon(size = 20.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${state.confirmedCatCount}/${state.puzzle.totalCats}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress bar of cats placed
        val progress by animateFloatAsState(
            targetValue = (state.confirmedCatCount.toFloat() / state.puzzle.totalCats.coerceAtLeast(1)).coerceIn(0f, 1f),
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
            label = "cat_progress"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(CatOrangePrimary)
            )
        }
    }
}

@Composable
private fun ClockPill(text: String, danger: Boolean) {
    val scale = if (danger) {
        val pulse = rememberInfiniteTransition(label = "clock_pulse")
        pulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
            label = "clock_pulse_scale"
        ).value
    } else {
        1f
    }
    StatPill(modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            tint = if (danger) CatRedMistake else CatOrangePrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = if (danger) CatRedMistake else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Heart(active: Boolean, index: Int) {
    val scale by animateFloatAsState(
        targetValue = if (active) 1f else 0.78f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heart_$index"
    )
    Icon(
        imageVector = if (active) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        contentDescription = "Life $index",
        tint = if (active) CatRedMistake else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .size(22.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    )
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onResume,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Paused", fontWeight = FontWeight.Black) },
        text = { Text("Take a breather! The puzzle is hidden until you're back.") },
        confirmButton = {
            GameButton(
                onClick = onResume,
                height = 46.dp,
                modifier = Modifier.width(120.dp).testTag("pause_resume_button")
            ) { GameButtonText("Resume", fontSize = 16.sp) }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameButton(onClick = onRestart, height = 46.dp, color = Color(0xFF8B8FA8), modifier = Modifier.width(92.dp)) {
                    GameButtonText("Restart", fontSize = 14.sp)
                }
                GameButton(onClick = onExit, height = 46.dp, color = CatRedMistake, modifier = Modifier.width(72.dp)) {
                    GameButtonText("Exit", fontSize = 14.sp)
                }
            }
        }
    )
}

@Composable
fun RestartConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Restart puzzle?", fontWeight = FontWeight.Black) },
        text = { Text("This clears all your marks on the current board.") },
        confirmButton = {
            GameButton(onClick = onConfirm, height = 46.dp, modifier = Modifier.width(110.dp)) {
                GameButtonText("Restart", fontSize = 16.sp)
            }
        },
        dismissButton = {
            GameButton(onClick = onDismiss, height = 46.dp, color = Color(0xFF8B8FA8), modifier = Modifier.width(100.dp)) {
                GameButtonText("Cancel", fontSize = 16.sp)
            }
        }
    )
}

@Composable
fun GameOverDialog(
    onRetry: () -> Unit,
    onViewResults: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CatFaceIcon(size = 54.dp, catColor = Color(0xFF9CA3AF))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Puzzle failed", fontWeight = FontWeight.Black, color = CatRedMistake)
            }
        },
        text = {
            Text(
                "You ran out of lives or time. Every puzzle is a new chance to learn the clues!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            GameButton(onClick = onRetry, height = 46.dp, modifier = Modifier.width(120.dp)) {
                GameButtonText("Try Again", fontSize = 16.sp)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameButton(onClick = onViewResults, height = 46.dp, color = Color(0xFF5B8DEF), modifier = Modifier.width(80.dp)) {
                    GameButtonText("Stats", fontSize = 14.sp)
                }
                GameButton(onClick = onExit, height = 46.dp, color = Color(0xFF8B8FA8), modifier = Modifier.width(80.dp)) {
                    GameButtonText("Menu", fontSize = 14.sp)
                }
            }
        }
    )
}

@Composable
fun VictoryCardDialog(
    state: GameState,
    onViewResults: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onViewResults,
        shape = RoundedCornerShape(28.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CatFaceIcon(size = 60.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("All cats found! 🐾", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))
                StarRow(stars = state.starsEarned, starSize = 38.dp)
            }
        },
        text = {
            Text(
                text = "Solved in ${formatClock(state.elapsedSeconds)} for ${state.score} points!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            GameButton(
                onClick = onViewResults,
                color = CatGreenSuccess,
                modifier = Modifier.fillMaxWidth().testTag("view_results_button")
            ) { GameButtonText("View Full Results") }
        }
    )
}
