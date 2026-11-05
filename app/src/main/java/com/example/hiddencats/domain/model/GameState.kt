package com.example.hiddencats.domain.model

data class GameState(
    val puzzle: Puzzle,
    val mode: GameMode,
    val boardStates: List<List<CellState>>,
    val moveHistory: List<Move> = emptyList(),
    val redoHistory: List<Move> = emptyList(),
    val elapsedSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val livesRemaining: Int = 3,
    val maxLives: Int = 3,
    val hintsUsed: Int = 0,
    val isCompleted: Boolean = false,
    val isGameOver: Boolean = false,
    val mistakesCount: Int = 0,
    val score: Int = 0,
    val starsEarned: Int = 0,
    val activeHintMessage: String? = null,
    val hintHighlightCells: Set<Pair<Int, Int>> = emptySet(),
    val isPaused: Boolean = false,
    val wrongCells: Set<Pair<Int, Int>> = emptySet()
) {
    val confirmedCatCount: Int
        get() = boardStates.sumOf { row -> row.count { it == CellState.CONFIRMED_CAT } }

    val correctlyFoundCatCount: Int
        get() {
            var count = 0
            for (r in 0 until puzzle.size.dimension) {
                for (c in 0 until puzzle.size.dimension) {
                    if (boardStates[r][c] == CellState.CONFIRMED_CAT && puzzle.grid[r][c].hasCat) {
                        count++
                    }
                }
            }
            return count
        }

    val remainingCatsToFind: Int
        get() = (puzzle.totalCats - confirmedCatCount).coerceAtLeast(0)

    val canUndo: Boolean
        get() = moveHistory.isNotEmpty() && !isCompleted && !isGameOver

    val canRedo: Boolean
        get() = redoHistory.isNotEmpty() && !isCompleted && !isGameOver
}
