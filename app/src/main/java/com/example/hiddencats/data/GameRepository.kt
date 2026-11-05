package com.example.hiddencats.data

import com.example.hiddencats.data.local.DailyProgressEntity
import com.example.hiddencats.data.local.GameRecordDao
import com.example.hiddencats.data.local.GameRecordEntity
import com.example.hiddencats.data.local.PlayerPreferences
import com.example.hiddencats.data.local.PreferencesManager
import com.example.hiddencats.domain.engine.PuzzleGenerator
import com.example.hiddencats.domain.model.BoardSize
import com.example.hiddencats.domain.model.Difficulty
import com.example.hiddencats.domain.model.Puzzle
import com.example.hiddencats.firebase.FirebaseManager
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneOffset

class GameRepository(
    private val dao: GameRecordDao,
    private val preferencesManager: PreferencesManager,
    private val firebaseManager: FirebaseManager,
    private val generator: PuzzleGenerator = PuzzleGenerator()
) {
    val preferences: Flow<PlayerPreferences> = preferencesManager.preferencesFlow
    val gameRecords: Flow<List<GameRecordEntity>> = dao.getAllRecords()
    val totalGamesCount: Flow<Int> = dao.getTotalGamesCount()
    val winsCount: Flow<Int> = dao.getWinsCount()
    val bestTimeSeconds: Flow<Int?> = dao.getBestTimeSeconds()
    val totalHintsUsed: Flow<Int?> = dao.getTotalHintsUsed()
    val cloudSyncStatus: Flow<String> = firebaseManager.syncStatus

    fun createPuzzle(size: BoardSize, difficulty: Difficulty): Puzzle {
        return generator.generate(size, difficulty)
    }

    fun getDailyPuzzle(dateKey: String = getTodayUtcDateKey()): Puzzle {
        val seed = kotlin.math.abs(dateKey.hashCode().toLong())
        return generator.generate(
            size = BoardSize.SIX,
            difficulty = Difficulty.NORMAL,
            seed = seed
        )
    }

    fun observeDailyProgress(dateKey: String = getTodayUtcDateKey()): Flow<DailyProgressEntity?> {
        return dao.observeDailyProgress(dateKey)
    }

    suspend fun recordGameFinished(
        id: String,
        difficulty: Difficulty,
        boardDimension: Int,
        mode: String,
        isWin: Boolean,
        score: Int,
        stars: Int,
        elapsedSeconds: Int,
        hintsUsed: Int,
        mistakesCount: Int,
        isDaily: Boolean = false,
        dateKey: String = getTodayUtcDateKey()
    ) {
        val record = GameRecordEntity(
            id = "${id}_${System.currentTimeMillis()}",
            difficulty = difficulty.label,
            boardDimension = boardDimension,
            mode = mode,
            isWin = isWin,
            score = score,
            stars = stars,
            elapsedSeconds = elapsedSeconds,
            hintsUsed = hintsUsed,
            mistakesCount = mistakesCount,
            dateKey = dateKey
        )
        dao.insertRecord(record)
        firebaseManager.syncGameRecord(record)

        if (isDaily && isWin) {
            val daily = DailyProgressEntity(
                dateKey = dateKey,
                isCompleted = true,
                score = score,
                stars = stars,
                timeSeconds = elapsedSeconds
            )
            dao.saveDailyProgress(daily)
            firebaseManager.syncDailyChallenge(dateKey, score, elapsedSeconds, stars)
        }
    }

    suspend fun consumeHint(): Boolean = preferencesManager.consumeHint()
    suspend fun addHints(amount: Int) = preferencesManager.addHints(amount)
    suspend fun setSoundEnabled(enabled: Boolean) = preferencesManager.setSoundEnabled(enabled)
    suspend fun setMusicEnabled(enabled: Boolean) = preferencesManager.setMusicEnabled(enabled)
    suspend fun setHapticsEnabled(enabled: Boolean) = preferencesManager.setHapticsEnabled(enabled)
    suspend fun setDarkMode(enabled: Boolean) = preferencesManager.setDarkMode(enabled)
    suspend fun setColorBlindMode(enabled: Boolean) = preferencesManager.setColorBlindMode(enabled)
    suspend fun setLeftHandedMode(enabled: Boolean) = preferencesManager.setLeftHandedMode(enabled)
    suspend fun setAdsRemoved(removed: Boolean) = preferencesManager.setAdsRemoved(removed)
    suspend fun setPremiumUnlocked(unlocked: Boolean) = preferencesManager.setPremiumUnlocked(unlocked)

    suspend fun resetAllData() {
        dao.clearHistory()
        dao.clearDailyHistory()
        preferencesManager.resetAll()
        firebaseManager.deleteUserData()
    }

    companion object {
        fun getTodayUtcDateKey(): String {
            return try {
                LocalDate.now(ZoneOffset.UTC).toString()
            } catch (_: Exception) {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                sdf.format(java.util.Date())
            }
        }
    }
}
