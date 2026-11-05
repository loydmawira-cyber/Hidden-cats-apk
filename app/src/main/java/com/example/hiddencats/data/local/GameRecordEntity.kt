package com.example.hiddencats.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey val id: String,
    val difficulty: String,
    val boardDimension: Int,
    val mode: String,
    val isWin: Boolean,
    val score: Int,
    val stars: Int,
    val elapsedSeconds: Int,
    val hintsUsed: Int,
    val mistakesCount: Int,
    val dateKey: String,
    val completedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey val dateKey: String,
    val isCompleted: Boolean,
    val score: Int,
    val stars: Int,
    val timeSeconds: Int,
    val completedTimestamp: Long = System.currentTimeMillis()
)
