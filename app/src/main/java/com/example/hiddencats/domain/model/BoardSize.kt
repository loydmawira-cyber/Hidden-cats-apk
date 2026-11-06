package com.example.hiddencats.domain.model

enum class BoardSize(val dimension: Int, val defaultCats: Int, val defaultColors: Int, val label: String) {
    FOUR(4, defaultCats = 3, defaultColors = 3, label = "4 x 4"),
    FIVE(5, defaultCats = 4, defaultColors = 4, label = "5 x 5"),
    SIX(6, defaultCats = 5, defaultColors = 4, label = "6 x 6"),
    SEVEN(7, defaultCats = 6, defaultColors = 5, label = "7 x 7"),
    EIGHT(8, defaultCats = 7, defaultColors = 5, label = "8 x 8"),
    NINE(9, defaultCats = 8, defaultColors = 6, label = "9 x 9");

    /** 8x8 and 9x9 are Premium-only. */
    val requiresPremium: Boolean get() = dimension >= 8

    companion object {
        fun fromDimension(dim: Int): BoardSize {
            return entries.find { it.dimension == dim } ?: SIX
        }
    }
}
