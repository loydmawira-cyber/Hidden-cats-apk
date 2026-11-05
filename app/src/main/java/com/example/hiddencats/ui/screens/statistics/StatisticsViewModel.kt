package com.example.hiddencats.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hiddencats.core.AppContainer
import com.example.hiddencats.data.GameRepository
import com.example.hiddencats.data.local.GameRecordEntity
import com.example.hiddencats.domain.engine.PuzzleGenerator
import com.example.hiddencats.domain.model.BoardSize
import com.example.hiddencats.domain.model.Difficulty
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val winPercentage: Int = 0,
    val bestTimeSeconds: Int = 0,
    val totalHintsUsed: Int = 0,
    val totalCatsFound: Int = 0,
    val recentRecords: List<GameRecordEntity> = emptyList()
)

class StatisticsViewModel(
    private val repository: GameRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.totalGamesCount,
        repository.winsCount,
        repository.bestTimeSeconds,
        repository.totalHintsUsed,
        repository.gameRecords
    ) { games, wins, bestTime, hints, records ->
        val winPct = if (games > 0) ((wins.toFloat() / games.toFloat()) * 100).toInt() else 0
        val catsFound = records.filter { it.isWin }.sumOf { rec ->
            val diff = Difficulty.entries.firstOrNull { it.label == rec.difficulty || it.name == rec.difficulty } ?: Difficulty.NORMAL
            PuzzleGenerator.targetCatsFor(BoardSize.fromDimension(rec.boardDimension), diff)
        }

        StatsUiState(
            totalGames = games,
            totalWins = wins,
            winPercentage = winPct,
            bestTimeSeconds = bestTime ?: 0,
            totalHintsUsed = hints ?: 0,
            totalCatsFound = catsFound,
            recentRecords = records.take(20)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatisticsViewModel(
                        repository = appContainer.gameRepository
                    ) as T
                }
            }
    }
}
