package com.example.hiddencats.domain.engine

import com.example.hiddencats.domain.model.CellColor

class PuzzleSolver {

    /**
     * Checks if placing a cat at (row, col) is valid with the existing placed cats.
     */
    fun isPlacementValid(
        row: Int,
        col: Int,
        colorGrid: List<List<CellColor>>,
        placedCats: Set<Pair<Int, Int>>,
        maxCatsPerRow: Int = 2,
        maxCatsPerCol: Int = 2
    ): Boolean {
        val color = colorGrid[row][col]

        // Rule 1: No two cats can touch horizontally, vertically, or diagonally (8-adjacency)
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = row + dr
                val nc = col + dc
                if (placedCats.contains(nr to nc)) {
                    return false
                }
            }
        }

        // Rule 2: Cats cannot share the same color in the same row
        var rowCatCount = 0
        for ((cr, cc) in placedCats) {
            if (cr == row) {
                rowCatCount++
                if (colorGrid[cr][cc] == color) {
                    return false
                }
            }
        }
        if (rowCatCount >= maxCatsPerRow) return false

        // Rule 3: Cats cannot share the same color in the same column
        var colCatCount = 0
        for ((cr, cc) in placedCats) {
            if (cc == col) {
                colCatCount++
                if (colorGrid[cr][cc] == color) {
                    return false
                }
            }
        }
        if (colCatCount >= maxCatsPerCol) return false

        return true
    }

    /**
     * Validates whether a full set of cats strictly conforms to all puzzle rules.
     */
    fun validateFullBoard(
        colorGrid: List<List<CellColor>>,
        cats: Set<Pair<Int, Int>>,
        rowCounts: List<Int>? = null,
        colCounts: List<Int>? = null
    ): Boolean {
        val dimension = colorGrid.size

        // Check row counts if provided
        if (rowCounts != null) {
            for (r in 0 until dimension) {
                val countInRow = cats.count { it.first == r }
                if (countInRow != rowCounts[r]) return false
            }
        }

        // Check col counts if provided
        if (colCounts != null) {
            for (c in 0 until dimension) {
                val countInCol = cats.count { it.second == c }
                if (countInCol != colCounts[c]) return false
            }
        }

        val checked = mutableSetOf<Pair<Int, Int>>()
        for ((r, c) in cats) {
            if (!isPlacementValid(r, c, colorGrid, checked)) {
                return false
            }
            checked.add(r to c)
        }
        return true
    }

    /**
     * Finds solutions for a given color grid, target cat count, and optional row/col clues.
     * Stops after finding maxSolutions to remain fast.
     */
    fun solve(
        colorGrid: List<List<CellColor>>,
        targetCatCount: Int,
        rowCounts: List<Int>? = null,
        colCounts: List<Int>? = null,
        maxSolutions: Int = 2
    ): List<Set<Pair<Int, Int>>> {
        val dimension = colorGrid.size
        val solutions = mutableListOf<Set<Pair<Int, Int>>>()
        val allCells = mutableListOf<Pair<Int, Int>>()

        for (r in 0 until dimension) {
            for (c in 0 until dimension) {
                // If rowCounts or colCounts are 0, skip
                if (rowCounts != null && rowCounts[r] == 0) continue
                if (colCounts != null && colCounts[c] == 0) continue
                allCells.add(r to c)
            }
        }

        fun backtrack(startIndex: Int, currentCats: MutableSet<Pair<Int, Int>>) {
            if (currentCats.size == targetCatCount) {
                if (validateFullBoard(colorGrid, currentCats, rowCounts, colCounts)) {
                    solutions.add(currentCats.toSet())
                }
                return
            }

            if (solutions.size >= maxSolutions) return

            for (i in startIndex until allCells.size) {
                val (r, c) = allCells[i]
                if (rowCounts != null && currentCats.count { it.first == r } >= rowCounts[r]) continue
                if (colCounts != null && currentCats.count { it.second == c } >= colCounts[c]) continue

                if (isPlacementValid(r, c, colorGrid, currentCats)) {
                    currentCats.add(r to c)
                    backtrack(i + 1, currentCats)
                    currentCats.remove(r to c)
                    if (solutions.size >= maxSolutions) return
                }
            }
        }

        backtrack(0, mutableSetOf())
        return solutions
    }
}
