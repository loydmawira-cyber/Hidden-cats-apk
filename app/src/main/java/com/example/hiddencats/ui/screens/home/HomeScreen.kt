package com.example.hiddencats.ui.screens.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.domain.model.BoardSize
import com.example.hiddencats.domain.model.Difficulty
import com.example.hiddencats.domain.model.GameMode
import com.example.hiddencats.ui.components.CatFaceIcon
import com.example.hiddencats.ui.components.GameBackground
import com.example.hiddencats.ui.components.GameButton
import com.example.hiddencats.ui.components.GameButtonText
import com.example.hiddencats.ui.components.GamePanel
import com.example.hiddencats.ui.components.OutlinedTitle
import com.example.hiddencats.ui.components.StatPill
import com.example.hiddencats.ui.theme.CatAmberAccent
import com.example.hiddencats.ui.theme.CatGreenSuccess
import com.example.hiddencats.ui.theme.CatOrangePrimary
import com.example.hiddencats.ui.theme.GoldStar
import com.example.hiddencats.ui.theme.InkOutline

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartGame: (mode: GameMode, difficulty: Difficulty, size: BoardSize) -> Unit,
    onStartDaily: () -> Unit,
    onNavigateRules: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateShop: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showPremiumDialog by remember { mutableStateOf(false) }

    val transition = rememberInfiniteTransition(label = "home_idle")
    val bob by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logo_bob"
    )
    val playPulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "play_pulse"
    )

    GameBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
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
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top HUD
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatPill {
                            Icon(Icons.Default.EmojiEvents, contentDescription = "Wins", tint = GoldStar, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${uiState.totalWins}", fontWeight = FontWeight.ExtraBold)
                        }
                        StatPill {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Hints", tint = CatAmberAccent, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (uiState.preferences.premiumUnlocked) "∞" else "${uiState.hintBalance}",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hero logo (gently bobbing)
                    Box(
                        modifier = Modifier
                            .offset(y = bob.dp)
                            .size(118.dp)
                            .clip(CircleShape)
                            .background(CatAmberAccent)
                            .border(5.dp, InkOutline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CatFaceIcon(size = 84.dp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTitle(text = "HIDDEN CATS", fontSize = 38.sp)
                    Text(
                        text = "COLOR LOGIC PUZZLES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Daily Challenge
                    GameButton(
                        onClick = {
                            viewModel.onButtonClicked()
                            onStartDaily()
                        },
                        color = if (uiState.isDailyCompleted) Color(0xFF2EAD6B) else Color(0xFF7C4DFF),
                        height = 74.dp,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth().testTag("home_daily_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                GameButtonText("Daily Challenge", fontSize = 18.sp)
                                Text(
                                    text = if (uiState.isDailyCompleted) "Completed today! Tap to replay" else "Today's mystery puzzle awaits",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            if (uiState.isDailyCompleted) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Setup
                    GamePanel(modifier = Modifier.fillMaxWidth()) {
                        SectionLabel("Game Mode")
                        OptionRow(
                            options = listOf(GameMode.CLASSIC, GameMode.TIMED, GameMode.RELAXED),
                            selected = uiState.selectedMode,
                            label = { it.label },
                            onSelect = { viewModel.selectMode(it) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        SectionLabel("Difficulty")
                        OptionRow(
                            options = Difficulty.entries.toList(),
                            selected = uiState.selectedDifficulty,
                            label = { it.label },
                            onSelect = { viewModel.selectDifficulty(it) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        SectionLabel("Board Size")
                        OptionRow(
                            options = BoardSize.entries.toList(),
                            selected = uiState.selectedBoardSize,
                            label = { "${it.dimension}×${it.dimension}" },
                            isLocked = { viewModel.isBoardLocked(it) },
                            onSelect = { size ->
                                if (!viewModel.selectBoardSize(size)) showPremiumDialog = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Play
                    GameButton(
                        onClick = {
                            viewModel.onButtonClicked()
                            if (viewModel.isBoardLocked(uiState.selectedBoardSize)) {
                                showPremiumDialog = true
                            } else {
                                onStartGame(uiState.selectedMode, uiState.selectedDifficulty, uiState.selectedBoardSize)
                            }
                        },
                        color = CatGreenSuccess,
                        height = 66.dp,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { scaleX = playPulse; scaleY = playPulse }
                            .testTag("play_game_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                GameButtonText("PLAY", fontSize = 24.sp)
                                Text(
                                    text = "${uiState.selectedBoardSize.label} • ${uiState.selectedDifficulty.label}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Menu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MenuButton(
                            title = "How to Play",
                            icon = Icons.Default.HelpOutline,
                            color = Color(0xFF5B8DEF),
                            modifier = Modifier.weight(1f).testTag("home_rules_button")
                        ) {
                            viewModel.onButtonClicked()
                            onNavigateRules()
                        }
                        MenuButton(
                            title = "Stats",
                            icon = Icons.Default.QueryStats,
                            color = Color(0xFF9D4EDD),
                            modifier = Modifier.weight(1f).testTag("home_stats_button")
                        ) {
                            viewModel.onButtonClicked()
                            onNavigateStats()
                        }
                        MenuButton(
                            title = "Shop",
                            icon = Icons.Default.ShoppingCart,
                            color = Color(0xFFF5A623),
                            modifier = Modifier.weight(1f).testTag("home_shop_button")
                        ) {
                            viewModel.onButtonClicked()
                            onNavigateShop()
                        }
                        MenuButton(
                            title = "Settings",
                            icon = Icons.Default.Settings,
                            color = Color(0xFF8B8FA8),
                            modifier = Modifier.weight(1f).testTag("home_settings_button")
                        ) {
                            viewModel.onButtonClicked()
                            onNavigateSettings()
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        if (showPremiumDialog) {
            AlertDialog(
                onDismissRequest = { showPremiumDialog = false },
                shape = RoundedCornerShape(28.dp),
                title = { Text("Premium board", fontWeight = FontWeight.Black) },
                text = { Text("8×8 and 9×9 boards are part of the Premium Detective Pass, along with unlimited hints and no ads.") },
                confirmButton = {
                    GameButton(
                        onClick = {
                            showPremiumDialog = false
                            onNavigateShop()
                        },
                        height = 46.dp,
                        modifier = Modifier.width(150.dp)
                    ) { GameButtonText("See Premium", fontSize = 16.sp) }
                },
                dismissButton = {
                    GameButton(
                        onClick = { showPremiumDialog = false },
                        height = 46.dp,
                        color = Color(0xFF8B8FA8),
                        modifier = Modifier.width(100.dp)
                    ) { GameButtonText("Not now", fontSize = 14.sp) }
                }
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        color = CatOrangePrimary
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun <T> OptionRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    isLocked: (T) -> Boolean = { false }
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            GameButton(
                onClick = { onSelect(option) },
                color = if (isSelected) CatOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
                height = 40.dp,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLocked(option)) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Premium only",
                            tint = CatAmberAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Text(
                        text = label(option),
                        fontSize = 13.sp,
                        maxLines = 1,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GameButton(
            onClick = onClick,
            color = color,
            height = 58.dp,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
