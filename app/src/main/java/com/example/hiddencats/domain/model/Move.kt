package com.example.hiddencats.domain.model

data class Move(
    val row: Int,
    val col: Int,
    val previousState: CellState,
    val newState: CellState,
    val timestamp: Long = System.currentTimeMillis()
)
