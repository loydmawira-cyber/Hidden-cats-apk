package com.example.hiddencats.domain.model

enum class CellState {
    UNKNOWN,        // Unmarked cell
    POSSIBLE,       // Tentative player mark (Paw/Question mark)
    EMPTY,          // Player marked as definitely no cat (Cross / Dot)
    CONFIRMED_CAT;  // Player marked as confirmed cat

    fun nextState(): CellState = when (this) {
        UNKNOWN -> CONFIRMED_CAT
        CONFIRMED_CAT -> EMPTY
        EMPTY -> POSSIBLE
        POSSIBLE -> UNKNOWN
    }
}
