package com.example.hiddencats.domain.model

enum class Difficulty(val label: String, val scoreMultiplier: Float, val baseTimeSeconds: Int) {
    EASY("Easy", scoreMultiplier = 1.0f, baseTimeSeconds = 180),
    NORMAL("Normal", scoreMultiplier = 1.5f, baseTimeSeconds = 240),
    HARD("Hard", scoreMultiplier = 2.0f, baseTimeSeconds = 300),
    EXPERT("Expert", scoreMultiplier = 2.5f, baseTimeSeconds = 360);

    fun recommendedBoardSize(): BoardSize = when (this) {
        EASY -> BoardSize.FOUR
        NORMAL -> BoardSize.SIX
        HARD -> BoardSize.SEVEN
        EXPERT -> BoardSize.EIGHT
    }
}
