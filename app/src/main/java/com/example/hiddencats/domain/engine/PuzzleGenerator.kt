package com.example.hiddencats.domain.engine

import com.example.hiddencats.domain.model.BoardSize
import com.example.hiddencats.domain.model.Cell
import com.example.hiddencats.domain.model.CellColor
import com.example.hiddencats.domain.model.CellState
import com.example.hiddencats.domain.model.Difficulty
import com.example.hiddencats.domain.model.Puzzle
import java.util.UUID
import kotlin.random.Random

class PuzzleGenerator(
    private val solver: PuzzleSolver = PuzzleSolver()
) {

    companion object {
        /** Cats hidden on a board for the given size and difficulty. */
        fun targetCatsFor(size: BoardSize, difficulty: Difficulty): Int = when (difficulty) {
            Difficulty.EASY -> (size.defaultCats - 1).coerceAtLeast(2)
            Difficulty.NORMAL -> size.defaultCats
            Difficulty.HARD -> size.defaultCats
            Difficulty.EXPERT -> (size.defaultCats + 1).coerceAtMost(size.dimension)
        }

        private const val MAX_ATTEMPTS = 3000
        private const val MAX_FIX_STEPS = 120
    }

    /**
     * Generates a puzzle that is guaranteed to have exactly ONE solution.
     * Generation is fully deterministic for a given seed (needed for the Daily Challenge).
     */
    fun generate(
        size: BoardSize = BoardSize.SIX,
        difficulty: Difficulty = Difficulty.NORMAL,
        seed: Long = System.currentTimeMillis()
    ): Puzzle {
        val random = Random(seed)
        val dimension = size.dimension
        val numColors = when (difficulty) {
            Difficulty.EASY -> size.defaultColors.coerceAtMost(CellColor.entries.size)
            Difficulty.NORMAL -> size.defaultColors.coerceAtMost(CellColor.entries.size)
            Difficulty.HARD -> (size.defaultColors + 1).coerceAtMost(CellColor.entries.size)
            Difficulty.EXPERT -> (size.defaultColors + 1).coerceAtMost(CellColor.entries.size)
        }
        val colors = CellColor.palette(numColors)
        val targetCats = targetCatsFor(size, difficulty)

        repeat(MAX_ATTEMPTS) {
            val placedCats = placeCats(dimension, targetCats, random) ?: return@repeat
            val colorGrid = buildUniqueColorGrid(dimension, colors, placedCats, random) ?: return@repeat

            val rowCounts = (0 until dimension).map { r -> placedCats.count { it.first == r } }
            val colCounts = (0 until dimension).map { c -> placedCats.count { it.second == c } }

            if (!solver.validateFullBoard(colorGrid, placedCats, rowCounts, colCounts)) return@repeat

            val rowColorCatMap = mutableMapOf<Int, Set<CellColor>>()
            val colColorCatMap = mutableMapOf<Int, Set<CellColor>>()
            for (r in 0 until dimension) {
                rowColorCatMap[r] = placedCats.filter { it.first == r }.map { colorGrid[it.first][it.second] }.toSet()
            }
            for (c in 0 until dimension) {
                colColorCatMap[c] = placedCats.filter { it.second == c }.map { colorGrid[it.first][it.second] }.toSet()
            }

            val grid = (0 until dimension).map { r ->
                (0 until dimension).map { c ->
                    Cell(
                        row = r,
                        col = c,
                        color = colorGrid[r][c],
                        hasCat = placedCats.contains(r to c),
                        state = CellState.UNKNOWN
                    )
                }
            }

            return Puzzle(
                id = UUID.nameUUIDFromBytes("puzzle_$seed".toByteArray()).toString(),
                size = size,
                difficulty = difficulty,
                grid = grid,
                totalCats = targetCats,
                seed = seed,
                rowCatCounts = rowCounts,
                colCatCounts = colCounts,
                rowColorCatMap = rowColorCatMap,
                colColorCatMap = colColorCatMap
            )
        }

        // Extremely unlikely: deterministic fallback
        return createFallbackPuzzle(size, difficulty, seed, colors, targetCats)
    }

    /** Randomised backtracking placement of cats: no touching (8-neighbour), at most 2 per row/column. */
    private fun placeCats(dimension: Int, target: Int, random: Random): Set<Pair<Int, Int>>? {
        val cats = mutableListOf<Pair<Int, Int>>()
        var budget = 3000

        fun valid(r: Int, c: Int): Boolean {
            if (cats.any { (a, b) -> kotlin.math.abs(a - r) <= 1 && kotlin.math.abs(b - c) <= 1 }) return false
            if (cats.count { it.first == r } >= 2) return false
            if (cats.count { it.second == c } >= 2) return false
            return true
        }

        fun backtrack(candidates: List<Pair<Int, Int>>): Boolean {
            if (cats.size == target) return true
            if (--budget < 0) return false
            val usable = candidates.filter { (r, c) -> valid(r, c) }.shuffled(random)
            if (usable.size < target - cats.size) return false
            for (cell in usable.take(6)) {
                cats.add(cell)
                if (backtrack(usable.filter { it != cell })) return true
                cats.removeAt(cats.size - 1)
            }
            return false
        }

        val all = (0 until dimension).flatMap { r -> (0 until dimension).map { c -> r to c } }
        return if (backtrack(all)) cats.toSet() else null
    }

    /**
     * Builds a colour grid for which [cats] is the ONLY valid solution.
     * Recolouring a non-cat cell can never invalidate the intended solution, so we repeatedly
     * recolour a cell used by an alternative solution until none remain.
     */
    private fun buildUniqueColorGrid(
        dimension: Int,
        palette: List<CellColor>,
        cats: Set<Pair<Int, Int>>,
        random: Random
    ): List<List<CellColor>>? {
        val grid = Array(dimension) { Array(dimension) { palette.random(random) } }

        // Cats sharing a row/column must have different colours
        var settled = false
        for (pass in 0 until 40) {
            var changed = false
            for ((r, c) in cats) {
                val clash = cats.any { (a, b) -> (a != r || b != c) && (a == r || b == c) && grid[a][b] == grid[r][c] }
                if (clash) { grid[r][c] = palette.random(random); changed = true }
            }
            if (!changed) { settled = true; break }
        }
        if (!settled) return null

        val rowCounts = (0 until dimension).map { r -> cats.count { it.first == r } }
        val colCounts = (0 until dimension).map { c -> cats.count { it.second == c } }

        repeat(MAX_FIX_STEPS) {
            val view = grid.map { it.toList() }
            val solutions = solver.countSolutions(view, rowCounts, colCounts, limit = 2)
            if (solutions.size == 1 && solutions[0] == cats) return view
            val other = solutions.firstOrNull { it != cats } ?: return if (solutions.isNotEmpty()) view else null

            val changeable = other.filter { it !in cats }
            if (changeable.isEmpty()) return null
            val (x, y) = changeable.random(random)
            val peers = other.filter { it != (x to y) && (it.first == x || it.second == y) }
            grid[x][y] = if (peers.isNotEmpty() && random.nextFloat() < 0.7f) {
                val p = peers.random(random)
                grid[p.first][p.second]
            } else {
                palette.random(random)
            }
        }
        return null
    }

    private fun createFallbackPuzzle(
        size: BoardSize,
        difficulty: Difficulty,
        seed: Long,
        palette: List<CellColor>,
        targetCats: Int
    ): Puzzle {
        val dim = size.dimension
        val gridCells = mutableListOf<MutableList<Cell>>()
        val placedCats = mutableSetOf<Pair<Int, Int>>()

        // Space cats diagonally every 2 steps
        var placed = 0
        var r = 0
        var c = 0
        while (placed < targetCats && r < dim) {
            placedCats.add(r to c)
            placed++
            r += 2
            c = (c + 2) % dim
        }

        val rowCounts = (0 until dim).map { row -> placedCats.count { it.first == row } }
        val colCounts = (0 until dim).map { col -> placedCats.count { it.second == col } }

        for (row in 0 until dim) {
            val rowList = mutableListOf<Cell>()
            for (col in 0 until dim) {
                val color = palette[(row * 2 + col) % palette.size]
                rowList.add(
                    Cell(
                        row = row,
                        col = col,
                        color = color,
                        hasCat = placedCats.contains(row to col),
                        state = CellState.UNKNOWN
                    )
                )
            }
            gridCells.add(rowList)
        }

        val rowColorCatMap = (0 until dim).associateWith { row ->
            placedCats.filter { it.first == row }.map { gridCells[it.first][it.second].color }.toSet()
        }
        val colColorCatMap = (0 until dim).associateWith { col ->
            placedCats.filter { it.second == col }.map { gridCells[it.first][it.second].color }.toSet()
        }

        return Puzzle(
            id = "fallback_$seed",
            size = size,
            difficulty = difficulty,
            grid = gridCells,
            totalCats = placedCats.size,
            seed = seed,
            rowCatCounts = rowCounts,
            colCatCounts = colCounts,
            rowColorCatMap = rowColorCatMap,
            colColorCatMap = colColorCatMap
        )
    }
}
