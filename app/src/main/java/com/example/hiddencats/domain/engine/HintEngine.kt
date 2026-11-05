package com.example.hiddencats.domain.engine

import com.example.hiddencats.domain.model.CellState
import com.example.hiddencats.domain.model.GameState

data class HintResult(
    val explanation: String,
    val targetCells: Set<Pair<Int, Int>> = emptySet(),
    val suggestedState: CellState? = null
)

class HintEngine {

    fun generateHint(state: GameState): HintResult {
        val puzzle = state.puzzle
        val dim = puzzle.size.dimension
        val board = state.boardStates

        // 1. Detect any obvious player mistake (touching cats or duplicate colors)
        for (r in 0 until dim) {
            for (c in 0 until dim) {
                if (board[r][c] == CellState.CONFIRMED_CAT) {
                    // Check if adjacent to another confirmed cat
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            if (dr == 0 && dc == 0) continue
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until dim && nc in 0 until dim && board[nr][nc] == CellState.CONFIRMED_CAT) {
                                return HintResult(
                                    explanation = "Cells (row ${r + 1}, col ${c + 1}) and (row ${nr + 1}, col ${nc + 1}) touch! Cats cannot touch horizontally, vertically, or diagonally.",
                                    targetCells = setOf(r to c, nr to nc)
                                )
                            }
                        }
                    }

                    // Check same color in column
                    val colColor = puzzle.grid[r][c].color
                    for (r2 in 0 until dim) {
                        if (r2 != r && board[r2][c] == CellState.CONFIRMED_CAT && puzzle.grid[r2][c].color == colColor) {
                            return HintResult(
                                explanation = "Column ${c + 1} has two ${colColor.displayName} cats! Cats cannot share the same color in the same column.",
                                targetCells = setOf(r to c, r2 to c)
                            )
                        }
                    }

                    // Check same color in row
                    val color = puzzle.grid[r][c].color
                    for (c2 in 0 until dim) {
                        if (c2 != c && board[r][c2] == CellState.CONFIRMED_CAT && puzzle.grid[r][c2].color == color) {
                            return HintResult(
                                explanation = "Row ${r + 1} has two ${color.displayName} cats! Cats cannot share the same color in the same row.",
                                targetCells = setOf(r to c, r to c2)
                            )
                        }
                    }
                }
            }
        }

        // 2. Row already has its max cats: remaining unknown cells must be EMPTY
        for (r in 0 until dim) {
            val confirmedInRow = (0 until dim).count { board[r][it] == CellState.CONFIRMED_CAT }
            val required = puzzle.rowCatCounts[r]
            if (confirmedInRow == required && required > 0) {
                val unknowns = (0 until dim).filter { board[r][it] == CellState.UNKNOWN || board[r][it] == CellState.POSSIBLE }
                if (unknowns.isNotEmpty()) {
                    return HintResult(
                        explanation = "Row ${r + 1} already has all its $required cat(s). The remaining unmarked cells in this row must be empty.",
                        targetCells = unknowns.map { r to it }.toSet(),
                        suggestedState = CellState.EMPTY
                    )
                }
            }
        }

        // 3. Col already has its max cats: remaining unknown cells must be EMPTY
        for (c in 0 until dim) {
            val confirmedInCol = (0 until dim).count { board[it][c] == CellState.CONFIRMED_CAT }
            val required = puzzle.colCatCounts[c]
            if (confirmedInCol == required && required > 0) {
                val unknowns = (0 until dim).filter { board[it][c] == CellState.UNKNOWN || board[it][c] == CellState.POSSIBLE }
                if (unknowns.isNotEmpty()) {
                    return HintResult(
                        explanation = "Column ${c + 1} already has all its $required cat(s). The remaining unmarked cells in this column must be empty.",
                        targetCells = unknowns.map { it to c }.toSet(),
                        suggestedState = CellState.EMPTY
                    )
                }
            }
        }

        // 4. Neighbors of confirmed cats: must be EMPTY
        for (r in 0 until dim) {
            for (c in 0 until dim) {
                if (board[r][c] == CellState.CONFIRMED_CAT) {
                    val unmarkedNeighbors = mutableListOf<Pair<Int, Int>>()
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            if (dr == 0 && dc == 0) continue
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until dim && nc in 0 until dim) {
                                if (board[nr][nc] == CellState.UNKNOWN || board[nr][nc] == CellState.POSSIBLE) {
                                    unmarkedNeighbors.add(nr to nc)
                                }
                            }
                        }
                    }
                    if (unmarkedNeighbors.isNotEmpty()) {
                        return HintResult(
                            explanation = "Because the cell at row ${r + 1}, col ${c + 1} contains a cat, all 8 surrounding cells must be marked Empty.",
                            targetCells = unmarkedNeighbors.toSet(),
                            suggestedState = CellState.EMPTY
                        )
                    }
                }
            }
        }

        // 5. Color uniqueness in row: if cat is placed on color C, other cells of color C in row must be EMPTY
        for (r in 0 until dim) {
            for (c in 0 until dim) {
                if (board[r][c] == CellState.CONFIRMED_CAT) {
                    val catColor = puzzle.grid[r][c].color
                    val sameColorUnknowns = (0 until dim)
                        .filter { it != c && puzzle.grid[r][it].color == catColor && (board[r][it] == CellState.UNKNOWN || board[r][it] == CellState.POSSIBLE) }
                    if (sameColorUnknowns.isNotEmpty()) {
                        return HintResult(
                            explanation = "Row ${r + 1} already has a ${catColor.displayName} cat! Other ${catColor.displayName} cells in this row cannot have cats.",
                            targetCells = sameColorUnknowns.map { r to it }.toSet(),
                            suggestedState = CellState.EMPTY
                        )
                    }
                }
            }
        }

        // 6. Direct deductive candidate reveal: find an undiscovered true cat location
        for (r in 0 until dim) {
            for (c in 0 until dim) {
                if (puzzle.grid[r][c].hasCat && board[r][c] != CellState.CONFIRMED_CAT) {
                    val cellColor = puzzle.grid[r][c].color
                    return HintResult(
                        explanation = "A sneaky cat is hiding at row ${r + 1}, col ${c + 1} on the ${cellColor.displayName} tile!",
                        targetCells = setOf(r to c),
                        suggestedState = CellState.CONFIRMED_CAT
                    )
                }
            }
        }

        return HintResult(
            explanation = "Look carefully at the row and column cat indicators. You are almost there!",
            targetCells = emptySet()
        )
    }
}
