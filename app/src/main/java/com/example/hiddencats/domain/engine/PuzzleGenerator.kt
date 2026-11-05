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
        val targetCats = when (difficulty) {
            Difficulty.EASY -> (size.defaultCats - 1).coerceAtLeast(2)
            Difficulty.NORMAL -> size.defaultCats
            Difficulty.HARD -> size.defaultCats
            Difficulty.EXPERT -> (size.defaultCats + 1).coerceAtMost(dimension)
        }

        var attempts = 0
        while (attempts < 50) {
            attempts++
            // 1. Generate balanced colorful grid
            val colorGrid = generateColorGrid(dimension, colors, random)

            // 2. Find a valid placement of targetCats
            val candidateCells = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until dimension) {
                for (c in 0 until dimension) {
                    candidateCells.add(r to c)
                }
            }
            candidateCells.shuffle(random)

            val placedCats = mutableSetOf<Pair<Int, Int>>()
            for ((r, c) in candidateCells) {
                if (solver.isPlacementValid(r, c, colorGrid, placedCats)) {
                    placedCats.add(r to c)
                    if (placedCats.size == targetCats) break
                }
            }

            if (placedCats.size == targetCats) {
                val rowCounts = (0 until dimension).map { r -> placedCats.count { it.first == r } }
                val colCounts = (0 until dimension).map { c -> placedCats.count { it.second == c } }

                // Check solver confirms validity
                if (solver.validateFullBoard(colorGrid, placedCats, rowCounts, colCounts)) {
                    // Build row and col color maps
                    val rowColorCatMap = mutableMapOf<Int, Set<CellColor>>()
                    val colColorCatMap = mutableMapOf<Int, Set<CellColor>>()

                    for (r in 0 until dimension) {
                        val rowCats = placedCats.filter { it.first == r }
                        rowColorCatMap[r] = rowCats.map { colorGrid[it.first][it.second] }.toSet()
                    }
                    for (c in 0 until dimension) {
                        val colCats = placedCats.filter { it.second == c }
                        colColorCatMap[c] = colCats.map { colorGrid[it.first][it.second] }.toSet()
                    }

                    // Build cell grid
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
            }
        }

        // Deterministic fallback guarantee
        return createFallbackPuzzle(size, difficulty, seed, colors, targetCats)
    }

    private fun generateColorGrid(
        dimension: Int,
        palette: List<CellColor>,
        random: Random
    ): List<List<CellColor>> {
        val grid = mutableListOf<MutableList<CellColor>>()
        for (r in 0 until dimension) {
            val row = mutableListOf<CellColor>()
            for (c in 0 until dimension) {
                val candidateColors = palette.filter { color ->
                    val sameAsLeft = if (c > 0) row[c - 1] == color else false
                    val sameAsTop = if (r > 0) grid[r - 1][c] == color else false
                    !(sameAsLeft && sameAsTop)
                }.ifEmpty { palette }
                row.add(candidateColors.random(random))
            }
            grid.add(row)
        }
        return grid
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
