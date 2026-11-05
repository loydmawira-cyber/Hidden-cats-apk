package com.example.hiddencats.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecordEntity)

    @Query("SELECT * FROM game_records ORDER BY completedTimestamp DESC")
    fun getAllRecords(): Flow<List<GameRecordEntity>>

    @Query("SELECT COUNT(*) FROM game_records")
    fun getTotalGamesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_records WHERE isWin = 1")
    fun getWinsCount(): Flow<Int>

    @Query("SELECT MIN(elapsedSeconds) FROM game_records WHERE isWin = 1")
    fun getBestTimeSeconds(): Flow<Int?>

    @Query("SELECT SUM(hintsUsed) FROM game_records")
    fun getTotalHintsUsed(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyProgress(daily: DailyProgressEntity)

    @Query("SELECT * FROM daily_progress WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyProgress(dateKey: String): DailyProgressEntity?

    @Query("SELECT * FROM daily_progress WHERE dateKey = :dateKey LIMIT 1")
    fun observeDailyProgress(dateKey: String): Flow<DailyProgressEntity?>

    @Query("SELECT * FROM daily_progress WHERE isCompleted = 1 ORDER BY dateKey DESC")
    fun getAllCompletedDailies(): Flow<List<DailyProgressEntity>>

    @Query("DELETE FROM game_records")
    suspend fun clearHistory()

    @Query("DELETE FROM daily_progress")
    suspend fun clearDailyHistory()
}
