package com.example.hiddencats.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hiddencats.core.AppContainer
import com.example.hiddencats.core.SoundHapticsManager
import com.example.hiddencats.data.GameRepository
import com.example.hiddencats.data.local.PlayerPreferences
import com.example.hiddencats.data.local.PreferencesManager
import com.example.hiddencats.domain.model.BoardSize
import com.example.hiddencats.domain.model.Difficulty
import com.example.hiddencats.domain.model.GameMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private data class Tuple4<A, B, C, D>(val games: A, val wins: B, val isDailyCompleted: C, val prefs: D)

data class HomeUiState(
    val selectedMode: GameMode = GameMode.CLASSIC,
    val selectedDifficulty: Difficulty = Difficulty.NORMAL,
    val selectedBoardSize: BoardSize = BoardSize.SIX,
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val hintBalance: Int = PreferencesManager.DEFAULT_HINTS,
    val isDailyCompleted: Boolean = false,
    val preferences: PlayerPreferences = PlayerPreferences()
)

class HomeViewModel(
    private val repository: GameRepository,
    private val soundHaptics: SoundHapticsManager
) : ViewModel() {

    private val _selectedMode = MutableStateFlow(GameMode.CLASSIC)
    private val _selectedDifficulty = MutableStateFlow(Difficulty.NORMAL)
    private val _selectedBoardSize = MutableStateFlow(BoardSize.SIX)

    private val _gameSettings = combine(
        _selectedMode,
        _selectedDifficulty,
        _selectedBoardSize
    ) { mode, diff, size -> Triple(mode, diff, size) }

    private val _gameStats = combine(
        repository.totalGamesCount,
        repository.winsCount,
        repository.observeDailyProgress(),
        repository.preferences
    ) { games, wins, daily, prefs ->
        Tuple4(games, wins, daily?.isCompleted == true, prefs)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        _gameSettings,
        _gameStats
    ) { settings, stats ->
        HomeUiState(
            selectedMode = settings.first,
            selectedDifficulty = settings.second,
            selectedBoardSize = settings.third,
            totalGames = stats.games,
            totalWins = stats.wins,
            hintBalance = stats.prefs.hintBalance,
            isDailyCompleted = stats.isDailyCompleted,
            preferences = stats.prefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun selectMode(mode: GameMode) {
        _selectedMode.value = mode
        soundHaptics.playCellSelect()
    }

    private val isPremium: Boolean get() = uiState.value.preferences.premiumUnlocked

    fun isBoardLocked(size: BoardSize): Boolean = size.requiresPremium && !isPremium

    fun selectDifficulty(difficulty: Difficulty) {
        _selectedDifficulty.value = difficulty
        // Free players get the largest free board for Expert instead of a locked one.
        val recommended = difficulty.recommendedBoardSize()
        _selectedBoardSize.value = if (isBoardLocked(recommended)) BoardSize.SEVEN else recommended
        soundHaptics.playCellSelect()
    }

    /** Returns false (and changes nothing) when the size is locked behind Premium. */
    fun selectBoardSize(size: BoardSize): Boolean {
        if (isBoardLocked(size)) return false
        _selectedBoardSize.value = size
        soundHaptics.playCellSelect()
        return true
    }

    fun onButtonClicked() {
        soundHaptics.playCellSelect()
    }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        repository = appContainer.gameRepository,
                        soundHaptics = appContainer.soundHapticsManager
                    ) as T
                }
            }
    }
}
