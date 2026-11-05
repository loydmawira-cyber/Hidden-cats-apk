package com.example.hiddencats.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.domain.model.CellState
import com.example.hiddencats.domain.model.GameState
import com.example.hiddencats.ui.theme.CatGreenSuccess
import com.example.hiddencats.ui.theme.CatOrangePrimary
import com.example.hiddencats.ui.theme.CatRedMistake
import com.example.hiddencats.ui.theme.InkOutline
import com.example.hiddencats.ui.theme.WoodDark
import com.example.hiddencats.ui.theme.WoodLight

@Composable
fun PuzzleBoardView(
    state: GameState,
    isColorBlindMode: Boolean,
    onCellClick: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val puzzle = state.puzzle
    val dim = puzzle.size.dimension

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Everything that isn't a cell: wooden frame (6 + 3 border) x2, inner padding 6 x2, clue gutter.
        val clueGutter = 34.dp
        val chrome = 34.dp
        // Fit the board to the width on any phone (9x9 included) instead of overflowing.
        val cellSize = minOf((maxWidth - chrome - clueGutter) / dim, 60.dp)

        Box(
            modifier = Modifier
                .testTag("puzzle_board")
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(WoodLight, WoodDark)))
                .border(3.dp, InkOutline, RoundedCornerShape(24.dp))
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Column clues
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.size(clueGutter, clueGutter))
                    for (c in 0 until dim) {
                        val confirmed = (0 until dim).count { state.boardStates[it][c] == CellState.CONFIRMED_CAT }
                        Box(modifier = Modifier.size(cellSize, clueGutter), contentAlignment = Alignment.Center) {
                            ClueBadge(required = puzzle.colCatCounts[c], confirmed = confirmed, cellSize = cellSize)
                        }
                    }
                }

                // Rows: clue + cells
                for (r in 0 until dim) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        val confirmed = (0 until dim).count { state.boardStates[r][it] == CellState.CONFIRMED_CAT }
                        Box(modifier = Modifier.size(clueGutter, cellSize), contentAlignment = Alignment.Center) {
                            ClueBadge(required = puzzle.rowCatCounts[r], confirmed = confirmed, cellSize = cellSize)
                        }
                        for (c in 0 until dim) {
                            val cell = puzzle.grid[r][c].copy(state = state.boardStates[r][c])
                            CellView(
                                cell = cell,
                                cellSize = cellSize,
                                isColorBlindMode = isColorBlindMode,
                                isHighlighted = state.hintHighlightCells.contains(r to c),
                                isWrong = state.wrongCells.contains(r to c),
                                onClick = { onCellClick(r, c) },
                                modifier = Modifier.graphicsLayer { alpha = if (state.isPaused) 0f else 1f }
                            )
                        }
                    }
                }
            }

            if (state.isPaused) {
                Text(
                    text = "PAUSED",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/** Number badge for a row/column: orange while open, green when exactly met, red when too many cats. */
@Composable
private fun ClueBadge(required: Int, confirmed: Int, cellSize: Dp) {
    val state = when {
        confirmed > required -> BadgeState.OVER
        confirmed == required && required > 0 -> BadgeState.DONE
        required == 0 -> BadgeState.ZERO
        else -> BadgeState.OPEN
    }
    val bg by animateColorAsState(
        targetValue = when (state) {
            BadgeState.DONE -> CatGreenSuccess
            BadgeState.OVER -> CatRedMistake
            BadgeState.ZERO -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            BadgeState.OPEN -> CatOrangePrimary.copy(alpha = 0.18f)
        },
        label = "clue_bg"
    )
    val pop by animateFloatAsState(
        targetValue = if (state == BadgeState.DONE) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "clue_pop"
    )
    val fg = when (state) {
        BadgeState.DONE, BadgeState.OVER -> Color.White
        BadgeState.ZERO -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        BadgeState.OPEN -> CatOrangePrimary
    }
    val diameter = (cellSize * 0.72f).coerceIn(22.dp, 30.dp)
    Box(
        modifier = Modifier
            .size(diameter)
            .graphicsLayer { scaleX = pop; scaleY = pop }
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$required",
            fontWeight = FontWeight.Black,
            fontSize = (diameter.value * 0.5f).sp,
            color = fg
        )
    }
}

private enum class BadgeState { OPEN, DONE, OVER, ZERO }
