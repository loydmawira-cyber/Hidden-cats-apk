package com.example.hiddencats

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hiddencats.data.local.AppDatabase
import com.example.hiddencats.data.local.GameRecordDao
import com.example.hiddencats.data.local.GameRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class HiddenCatsRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: GameRecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.gameRecordDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testRoomDatabaseInsertAndQuery() = runBlocking {
        val record = GameRecordEntity(
            id = "test_puzzle_01",
            difficulty = "NORMAL",
            boardDimension = 5,
            mode = "CLASSIC",
            isWin = true,
            score = 1500,
            stars = 3,
            elapsedSeconds = 95,
            hintsUsed = 1,
            mistakesCount = 0,
            dateKey = "2026-11-04"
        )

        dao.insertRecord(record)

        val totalPlayed = dao.getTotalGamesCount().first()
        assertEquals(1, totalPlayed)

        val totalWon = dao.getWinsCount().first()
        assertEquals(1, totalWon)

        val bestTime = dao.getBestTimeSeconds().first()
        assertEquals(95, bestTime)

        val totalHints = dao.getTotalHintsUsed().first()
        assertEquals(1, totalHints)
    }

    @Test
    fun testStringResourcesExist() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val gameTitle = context.getString(R.string.game_title)
        assertEquals("Hidden Cats", appName)
        assertEquals("Hidden Cats: Color Logic", gameTitle)
    }
}
