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

        // Global rule: every color can contain at most one cat on the entire board.
        val catColors = cats.map { (r, c) -> colorGrid[r][c] }
        if (catColors.size != catColors.toSet().size) return false

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
     * Fast row-by-row solver used to prove a puzzle has exactly one solution.
     * Returns up to [limit] solutions, each as a set of (row, col) cat cells.
     */
    fun countSolutions(
        colorGrid: List<List<CellColor>>,
        rowCounts: List<Int>,
        colCounts: List<Int>,
        limit: Int = 2
    ): List<Set<Pair<Int, Int>>> {
        val dim = colorGrid.size
        val rowOptions: List<List<List<Int>>> = (0 until dim).map { r ->
            columnCombos(dim, rowCounts[r]).filter { combo ->
                combo.zipWithNext().all { (a, b) -> b - a > 1 } &&
                    combo.map { colorGrid[r][it] }.toSet().size == combo.size
            }
        }

        val solutions = mutableListOf<Set<Pair<Int, Int>>>()
        val colUsed = IntArray(dim)
        val colColors = Array(dim) { mutableSetOf<CellColor>() }
        val usedColors = mutableSetOf<CellColor>()
        val chosen = Array<List<Int>>(dim) { emptyList() }

        fun backtrack(r: Int) {
            if (solutions.size >= limit) return
            if (r == dim) {
                if ((0 until dim).all { colUsed[it] == colCounts[it] }) {
                    val set = mutableSetOf<Pair<Int, Int>>()
                    for (row in 0 until dim) for (c in chosen[row]) set.add(row to c)
                    solutions.add(set)
                }
                return
            }
            val prev = if (r > 0) chosen[r - 1] else emptyList()
            for (combo in rowOptions[r]) {
                if (combo.any { c -> prev.any { p -> kotlin.math.abs(c - p) <= 1 } }) continue
                if (combo.any { c -> colUsed[c] + 1 > colCounts[c] || colorGrid[r][c] in colColors[c] }) continue
                if (combo.map { colorGrid[r][it] }.any { it in usedColors }) continue
                for (c in combo) {
                    colUsed[c]++
                    colColors[c].add(colorGrid[r][c])
                    usedColors.add(colorGrid[r][c])
                }
                chosen[r] = combo
                backtrack(r + 1)
                for (c in combo) {
                    colUsed[c]--
                    colColors[c].remove(colorGrid[r][c])
                    usedColors.remove(colorGrid[r][c])
                }
                chosen[r] = emptyList()
                if (solutions.size >= limit) return
            }
        }

        backtrack(0)
        return solutions
    }

    private fun columnCombos(dim: Int, size: Int): List<List<Int>> {
        if (size == 0) return listOf(emptyList())
        val result = mutableListOf<List<Int>>()
        fun build(start: Int, current: MutableList<Int>) {
            if (current.size == size) { result.add(current.toList()); return }
            for (c in start until dim) {
                current.add(c)
                build(c + 1, current)
                current.removeAt(current.size - 1)
            }
        }
        build(0, mutableListOf())
        return result
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
