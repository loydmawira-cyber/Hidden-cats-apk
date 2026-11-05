package com.example.hiddencats.domain.engine

import com.example.hiddencats.domain.model.Difficulty
import com.example.hiddencats.domain.model.GameMode

object ScoreCalculator {

    fun calculateScore(
        difficulty: Difficulty,
        mode: GameMode,
        elapsedSeconds: Int,
        mistakesCount: Int,
        hintsUsed: Int,
        isWin: Boolean
    ): Int {
        if (!isWin) return 0

        val baseScore = (1000 * difficulty.scoreMultiplier).toInt()
        val timeBonus = (maxOf(0, difficulty.baseTimeSeconds - elapsedSeconds) * 4)
        val accuracyBonus = maxOf(0, 3 - mistakesCount) * 250
        val hintPenalty = hintsUsed * 120

        val modeBonus = when (mode) {
            GameMode.DAILY -> 500
            GameMode.TIMED -> 300
            GameMode.CLASSIC -> 100
            GameMode.RELAXED -> 0
        }

        return maxOf(100, baseScore + timeBonus + accuracyBonus - hintPenalty + modeBonus)
    }

    fun calculateStars(
        elapsedSeconds: Int,
        baseTimeSeconds: Int,
        mistakesCount: Int,
        hintsUsed: Int,
        isWin: Boolean
    ): Int {
        if (!isWin) return 0
        return when {
            mistakesCount == 0 && hintsUsed == 0 && elapsedSeconds <= (baseTimeSeconds * 0.6).toInt() -> 3
            mistakesCount <= 1 && hintsUsed <= 1 && elapsedSeconds <= baseTimeSeconds -> 2
            else -> 1
        }
    }
}
