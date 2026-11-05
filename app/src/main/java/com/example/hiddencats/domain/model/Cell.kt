package com.example.hiddencats.domain.model

data class Cell(
    val row: Int,
    val col: Int,
    val color: CellColor,
    val hasCat: Boolean = false,
    val state: CellState = CellState.UNKNOWN
) {
    fun isAdjacent(other: Cell): Boolean {
        val rowDiff = kotlin.math.abs(row - other.row)
        val colDiff = kotlin.math.abs(col - other.col)
        return rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0)
    }

    fun isAdjacent(r: Int, c: Int): Boolean {
        val rowDiff = kotlin.math.abs(row - r)
        val colDiff = kotlin.math.abs(col - c)
        return rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0)
    }
}
