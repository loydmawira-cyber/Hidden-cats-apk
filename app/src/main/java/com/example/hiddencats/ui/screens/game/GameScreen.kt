package com.example.hiddencats.ui.screens.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.domain.model.GameMode
import com.example.hiddencats.domain.model.GameState
import com.example.hiddencats.ui.components.CatFaceIcon
import com.example.hiddencats.ui.components.ControlBar
import com.example.hiddencats.ui.components.PuzzleBoardView
import com.example.hiddencats.ui.theme.CatAmberAccent
import com.example.hiddencats.ui.theme.CatOrangePrimary
import com.example.hiddencats.ui.theme.CatRedMistake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onGameFinished: (GameState) -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val selectedTool by viewModel.selectedTool.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val scrollState = rememberScrollState()
    var showRestartDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (!state.isCompleted && !state.isGameOver) {
            viewModel.togglePause()
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (state.mode == GameMode.DAILY) "Daily Challenge" else "${state.puzzle.size.label} Grid",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.puzzle.difficulty.label} • ${state.mode.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CatOrangePrimary
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
                            contentDescription = "Pause"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Status Bar (Lives, Timer, Cats to find)
                GameStatusBar(state = state)

                Spacer(modifier = Modifier.height(6.dp))

                // Active Hint Message Banner
                AnimatedVisibility(
                    visible = state.activeHintMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val msg = state.activeHintMessage
                    if (msg != null) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CatOrangePrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // The Interactive Puzzle Board
                PuzzleBoardView(
                    state = state,
                    isColorBlindMode = preferences.colorBlindMode,
                    onCellClick = { r, c -> viewModel.onCellClicked(r, c) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Control Bar
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
                    isLeftHanded = preferences.leftHandedMode
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Modals / Dialogs
        if (state.isPaused) {
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

@Composable
fun GameStatusBar(state: GameState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Lives or Timer
            when (state.mode) {
                GameMode.TIMED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = CatOrangePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.remainingSeconds}s",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state.remainingSeconds <= 30) CatRedMistake else CatOrangePrimary
                        )
                    }
                }
                GameMode.RELAXED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Zen Mode", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CatOrangePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("(${state.elapsedSeconds}s)", style = MaterialTheme.typography.bodySmall)
                    }
                }
                else -> {
                    // Lives hearts
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..state.maxLives) {
                            val active = i <= state.livesRemaining
                            Icon(
                                imageVector = if (active) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Life $i",
                                tint = if (active) CatRedMistake else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }
                }
            }

            // Right: Cats Remaining
            Row(verticalAlignment = Alignment.CenterVertically) {
                CatFaceIcon(size = 20.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${state.confirmedCatCount} / ${state.puzzle.totalCats} Cats",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onResume,
        title = { Text("Game Paused", fontWeight = FontWeight.Bold) },
        text = { Text("Take a pause! The colored logic puzzle will be waiting for you.") },
        confirmButton = {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = CatOrangePrimary),
                modifier = Modifier.testTag("pause_resume_button")
            ) {
                Text("Resume")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onRestart) { Text("Restart") }
                OutlinedButton(onClick = onExit) { Text("Exit") }
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
        title = { Text("Restart Puzzle?", fontWeight = FontWeight.Bold) },
        text = { Text("This will clear all your marks on the current board.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CatOrangePrimary)
            ) {
                Text("Restart")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
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
        title = { Text("Puzzle Failed", fontWeight = FontWeight.Bold, color = CatRedMistake) },
        text = { Text("You ran out of lives or time! Don't worry, every puzzle is a new opportunity to learn the clues.") },
        confirmButton = {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = CatOrangePrimary)
            ) {
                Text("Try Again")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onViewResults) { Text("View Stats") }
                OutlinedButton(onClick = onExit) { Text("Menu") }
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
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CatFaceIcon(size = 54.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("All Cats Found! 🐾", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Text(
                text = "Congratulations! You successfully solved the puzzle in ${state.elapsedSeconds}s with score of ${state.score} points!",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onViewResults,
                colors = ButtonDefaults.buttonColors(containerColor = CatOrangePrimary),
                modifier = Modifier.fillMaxWidth().testTag("view_results_button")
            ) {
                Text("View Full Results")
            }
        }
    )
}
