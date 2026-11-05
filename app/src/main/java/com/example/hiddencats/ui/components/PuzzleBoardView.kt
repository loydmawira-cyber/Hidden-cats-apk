package com.example.hiddencats.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.domain.model.Cell
import com.example.hiddencats.domain.model.CellColor
import com.example.hiddencats.domain.model.GameState
import com.example.hiddencats.ui.theme.CatAmberAccent
import com.example.hiddencats.ui.theme.CatGreenSuccess
import com.example.hiddencats.ui.theme.CatOrangePrimary

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
        val availableWidth = maxWidth - 16.dp
        // Reserve space for row indicator (e.g. 52dp)
        val indicatorSize = 44.dp
        val boardGridWidth = availableWidth - indicatorSize
        val cellSize = (boardGridWidth / dim).coerceIn(34.dp, 64.dp)

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(4.dp).testTag("puzzle_board")
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Column Clues
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Empty top-left corner
                    Box(modifier = Modifier.size(indicatorSize, indicatorSize))

                    // Column Clue Headers
                    for (c in 0 until dim) {
                        val requiredCats = puzzle.colCatCounts[c]
                        val confirmedInCol = (0 until dim).count { state.boardStates[it][c] == com.example.hiddencats.domain.model.CellState.CONFIRMED_CAT }
                        val isColFulfilled = confirmedInCol == requiredCats && requiredCats > 0

                        Column(
                            modifier = Modifier.size(cellSize, indicatorSize),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isColFulfilled) CatGreenSuccess else CatOrangePrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$requiredCats",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isColFulfilled) Color.White else CatOrangePrimary
                                    )
                                }
                            }
                            // Mini dots of colors with cats in this column
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                val colors = puzzle.colColorCatMap[c] ?: emptySet()
                                colors.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(color.composeColor(isColorBlindMode))
                                    )
                                }
                            }
                        }
                    }
                }

                // Grid with Left Row Clues
                for (r in 0 until dim) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Row Clue Header (Left)
                        val requiredCats = puzzle.rowCatCounts[r]
                        val confirmedInRow = (0 until dim).count { state.boardStates[r][it] == com.example.hiddencats.domain.model.CellState.CONFIRMED_CAT }
                        val isRowFulfilled = confirmedInRow == requiredCats && requiredCats > 0

                        Row(
                            modifier = Modifier.size(indicatorSize, cellSize),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isRowFulfilled) CatGreenSuccess else CatOrangePrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$requiredCats",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isRowFulfilled) Color.White else CatOrangePrimary
                                    )
                                }
                            }
                        }

                        // Cells for row r
                        for (c in 0 until dim) {
                            val cell = puzzle.grid[r][c].copy(state = state.boardStates[r][c])
                            val isHighlighted = state.hintHighlightCells.contains(r to c)
                            val isWrong = state.wrongCells.contains(r to c)

                            CellView(
                                cell = cell,
                                cellSize = cellSize,
                                isColorBlindMode = isColorBlindMode,
                                isHighlighted = isHighlighted,
                                isWrong = isWrong,
                                onClick = { onCellClick(r, c) }
                            )
                        }
                    }
                }
            }
        }
    }
}
