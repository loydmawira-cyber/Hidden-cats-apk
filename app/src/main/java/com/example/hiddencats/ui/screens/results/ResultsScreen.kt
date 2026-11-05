package com.example.hiddencats.ui.screens.results

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.ui.components.CatFaceIcon
import com.example.hiddencats.ui.components.ConfettiBurst
import com.example.hiddencats.ui.components.GameBackground
import com.example.hiddencats.ui.components.GameButton
import com.example.hiddencats.ui.components.GameButtonText
import com.example.hiddencats.ui.components.GamePanel
import com.example.hiddencats.ui.components.OutlinedTitle
import com.example.hiddencats.ui.components.StarRow
import com.example.hiddencats.ui.theme.CatAmberAccent
import com.example.hiddencats.ui.theme.CatGreenSuccess
import com.example.hiddencats.ui.theme.CatOrangePrimary
import com.example.hiddencats.ui.theme.CatRedMistake
import com.example.hiddencats.ui.theme.InkOutline

private fun prettify(raw: String): String =
    raw.lowercase().replaceFirstChar { it.uppercase() }

private fun clock(seconds: Long): String {
    val s = seconds.coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

@Composable
fun ResultsScreen(
    won: Boolean,
    timeSeconds: Long,
    movesCount: Int,
    hintsUsed: Int,
    score: Int,
    stars: Int,
    boardSize: Int,
    difficulty: String,
    mode: String,
    onPlayAgain: () -> Unit,
    onNextPuzzle: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler {
        onGoHome()
    }

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
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(if (won) CatAmberAccent else Color(0xFFCBD5E1))
                            .border(5.dp, InkOutline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CatFaceIcon(size = 80.dp, catColor = if (won) Color(0xFFF97316) else Color(0xFF9CA3AF))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTitle(
                        text = if (won) "PUZZLE SOLVED!" else "GAME OVER",
                        fontSize = 32.sp,
                        fill = if (won) Color.White else Color(0xFFFFB4B4)
                    )

                    Text(
                        text = if (won) "Purr-fect logic! All hidden cats uncovered." else "The cats were too sneaky this time.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (won) {
                        StarRow(stars = stars, starSize = 52.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    GamePanel(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ResultRow(label = "Final Score", value = "$score pts", highlight = true)
                            ResultRow(label = "Time", value = clock(timeSeconds))
                            ResultRow(label = "Moves", value = "$movesCount")
                            ResultRow(label = "Hints Used", value = "$hintsUsed")
                            ResultRow(label = "Board", value = "${boardSize}×$boardSize • ${prettify(difficulty)} • ${prettify(mode)}")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    GameButton(
                        onClick = onNextPuzzle,
                        color = CatGreenSuccess,
                        height = 58.dp,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().testTag("results_next_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            GameButtonText(if (won) "Next Puzzle" else "New Puzzle", fontSize = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GameButton(
                            onClick = onPlayAgain,
                            color = Color(0xFF5B8DEF),
                            height = 48.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            GameButtonText("Retry", fontSize = 16.sp)
                        }

                        // Only offer sharing a score you actually earned
                        if (won) {
                            GameButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "I solved a ${boardSize}x$boardSize puzzle in Hidden Cats: Color Logic with $score points and $stars stars! 🐾"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share your score"))
                                },
                                color = CatOrangePrimary,
                                height = 48.dp,
                                modifier = Modifier.weight(1f).testTag("results_share_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                GameButtonText("Share", fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GameButton(
                        onClick = onGoHome,
                        color = Color(0xFF8B8FA8),
                        height = 48.dp,
                        modifier = Modifier.fillMaxWidth().testTag("results_home_button")
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        GameButtonText("Back to Home", fontSize = 16.sp)
                    }
                }
            }
        }

        if (won) ConfettiBurst()
    }
}

@Composable
private fun ResultRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (highlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlight) FontWeight.Black else FontWeight.Bold,
            color = if (highlight) CatOrangePrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
