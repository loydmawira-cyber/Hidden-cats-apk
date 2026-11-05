package com.example.hiddencats.domain.model

import androidx.compose.ui.graphics.Color

enum class CellColor(
    val id: Int,
    val displayName: String,
    val standardHex: Long,
    val colorBlindHex: Long,
    val symbolChar: String
) {
    CORAL(0, "Coral", 0xFFFF6B6B, 0xFFE66101, "●"), // Circle
    SKY(1, "Sky", 0xFF4D96FF, 0xFF5D3A9B, "▲"),     // Triangle
    MINT(2, "Mint", 0xFF6BCB77, 0xFF1B9E77, "■"),   // Square
    AMBER(3, "Amber", 0xFFFFD93D, 0xFFE7298A, "★"), // Star
    LAVENDER(4, "Lavender", 0xFF9D4EDD, 0xFF7570B3, "◆"), // Diamond
    TANGERINE(5, "Tangerine", 0xFFFF9F45, 0xFFD95F02, "⬡"); // Hexagon

    fun composeColor(isColorBlindMode: Boolean = false): Color {
        return Color(if (isColorBlindMode) colorBlindHex else standardHex)
    }

    companion object {
        fun fromId(id: Int): CellColor {
            return entries.find { it.id == id } ?: CORAL
        }

        fun palette(count: Int): List<CellColor> {
            return entries.take(count.coerceIn(2, entries.size))
        }
    }
}
