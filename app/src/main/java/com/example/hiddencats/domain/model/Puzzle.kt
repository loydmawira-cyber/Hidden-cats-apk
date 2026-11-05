package com.example.hiddencats.domain.model

data class Puzzle(
    val id: String,
    val size: BoardSize,
    val difficulty: Difficulty,
    val grid: List<List<Cell>>,
    val totalCats: Int,
    val seed: Long,
    val rowCatCounts: List<Int>,
    val colCatCounts: List<Int>,
    val rowColorCatMap: Map<Int, Set<CellColor>>,
    val colColorCatMap: Map<Int, Set<CellColor>>
) {
    fun getCell(row: Int, col: Int): Cell? {
        if (row in 0 until size.dimension && col in 0 until size.dimension) {
            return grid[row][col]
        }
        return null
    }

    val catCoordinates: Set<Pair<Int, Int>> by lazy {
        val set = mutableSetOf<Pair<Int, Int>>()
        for (r in 0 until size.dimension) {
            for (c in 0 until size.dimension) {
                if (grid[r][c].hasCat) {
                    set.add(r to c)
                }
            }
        }
        set
    }
}
