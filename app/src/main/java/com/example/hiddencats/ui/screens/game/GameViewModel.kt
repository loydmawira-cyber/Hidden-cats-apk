package com.example.hiddencats.ui.screens.game

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hiddencats.ads.AdsController
import com.example.hiddencats.core.AppContainer
import com.example.hiddencats.core.SoundHapticsManager
import com.example.hiddencats.data.GameRepository
import com.example.hiddencats.data.local.PlayerPreferences
import com.example.hiddencats.domain.engine.GameEngine
import com.example.hiddencats.domain.model.BoardSize
import com.example.hiddencats.domain.model.CellState
import com.example.hiddencats.domain.model.Difficulty
import com.example.hiddencats.domain.model.GameMode
import com.example.hiddencats.domain.model.GameState
import com.example.hiddencats.domain.model.Puzzle
import com.example.hiddencats.ui.components.ToolMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
    mode: GameMode,
    difficulty: Difficulty,
    size: BoardSize,
    isDaily: Boolean,
    private val repository: GameRepository,
    private val soundHaptics: SoundHapticsManager,
    private val ads: AdsController,
    private val gameEngine: GameEngine = GameEngine()
) : ViewModel() {

    private val puzzle: Puzzle = if (isDaily) {
        repository.getDailyPuzzle()
    } else {
        repository.createPuzzle(size, difficulty)
    }

    private val _gameState = MutableStateFlow(gameEngine.initGame(puzzle, mode))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _selectedTool = MutableStateFlow(ToolMode.CYCLE)
    val selectedTool: StateFlow<ToolMode> = _selectedTool.asStateFlow()

    val preferences: StateFlow<PlayerPreferences> = repository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlayerPreferences()
    )

    private val _showHintOffer = MutableStateFlow(false)
    /** True when the player is out of hints and we offer a rewarded ad / the shop. */
    val showHintOffer: StateFlow<Boolean> = _showHintOffer.asStateFlow()

    private val _hintOfferMessage = MutableStateFlow<String?>(null)
    val hintOfferMessage: StateFlow<String?> = _hintOfferMessage.asStateFlow()

    val rewardedReady: StateFlow<Boolean> = ads.rewardedReady

    private var timerJob: Job? = null
    private var hasRecordedCompletion = false

    init {
        startTimer()
        // Close the offer automatically once hints arrive (e.g. after buying in the shop).
        viewModelScope.launch {
            preferences.collect { p ->
                if (_showHintOffer.value && (p.hintBalance > 0 || p.premiumUnlocked)) dismissHintOffer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _gameState.value
                if (!current.isPaused && !current.isCompleted && !current.isGameOver) {
                    val updated = gameEngine.tickTimer(current)
                    _gameState.value = updated

                    if (updated.isGameOver && !hasRecordedCompletion) {
                        handleGameOver(updated)
                        break
                    }
                }
            }
        }
    }

    fun selectTool(tool: ToolMode) {
        _selectedTool.value = tool
        soundHaptics.playCellSelect(
            sound = preferences.value.soundEnabled,
            haptics = preferences.value.hapticsEnabled
        )
    }

    fun onCellClicked(row: Int, col: Int) {
        val current = _gameState.value
        if (current.isCompleted || current.isGameOver || current.isPaused) return

        val prevMistakes = current.mistakesCount
        val updated = when (_selectedTool.value) {
            ToolMode.CYCLE -> gameEngine.cycleCellState(current, row, col)
            ToolMode.CONFIRMED_CAT -> gameEngine.setCellState(current, row, col, CellState.CONFIRMED_CAT)
            ToolMode.EMPTY -> gameEngine.setCellState(current, row, col, CellState.EMPTY)
            ToolMode.POSSIBLE -> gameEngine.setCellState(current, row, col, CellState.POSSIBLE)
        }

        _gameState.value = updated

        // Audio and haptics feedback
        if (updated.mistakesCount > prevMistakes) {
            soundHaptics.playWrong(
                sound = preferences.value.soundEnabled,
                haptics = preferences.value.hapticsEnabled
            )
        } else if (updated.boardStates[row][col] == CellState.CONFIRMED_CAT && updated.puzzle.grid[row][col].hasCat) {
            soundHaptics.playCorrect(
                sound = preferences.value.soundEnabled,
                haptics = preferences.value.hapticsEnabled
            )
        } else {
            soundHaptics.playCellSelect(
                sound = preferences.value.soundEnabled,
                haptics = preferences.value.hapticsEnabled
            )
        }

        if (updated.isCompleted && !hasRecordedCompletion) {
            handleVictory(updated)
        } else if (updated.isGameOver && !hasRecordedCompletion) {
            handleGameOver(updated)
        }
    }

    fun undo() {
        val current = _gameState.value
        if (current.canUndo) {
            _gameState.value = gameEngine.undo(current)
            soundHaptics.playCellSelect(
                sound = preferences.value.soundEnabled,
                haptics = preferences.value.hapticsEnabled
            )
        }
    }

    fun redo() {
        val current = _gameState.value
        if (current.canRedo) {
            _gameState.value = gameEngine.redo(current)
            soundHaptics.playCellSelect(
                sound = preferences.value.soundEnabled,
                haptics = preferences.value.hapticsEnabled
            )
        }
    }

    fun requestHint() {
        val current = _gameState.value
        if (current.isCompleted || current.isGameOver || current.isPaused) return

        viewModelScope.launch {
            // Premium players get unlimited hints without spending the balance
            val allowed = preferences.value.premiumUnlocked || repository.consumeHint()
            val latest = _gameState.value
            if (latest.isCompleted || latest.isGameOver) return@launch
            if (allowed) {
                _gameState.value = gameEngine.requestHint(latest)
                soundHaptics.playHint(
                    sound = preferences.value.soundEnabled,
                    haptics = preferences.value.hapticsEnabled
                )
            } else {
                // Pause the clock and offer a rewarded ad or the shop.
                _gameState.value = latest.copy(isPaused = true, hintHighlightCells = emptySet())
                _hintOfferMessage.value = null
                _showHintOffer.value = true
            }
        }
    }

    fun dismissHintOffer() {
        _showHintOffer.value = false
        _hintOfferMessage.value = null
        val cur = _gameState.value
        if (cur.isPaused && !cur.isCompleted && !cur.isGameOver) {
            _gameState.value = cur.copy(isPaused = false)
        }
    }

    /** Rewarded video -> +hints -> immediately use one. */
    fun watchAdForHint(activity: Activity) {
        ads.showRewardedHint(activity) { earned ->
            if (earned) {
                dismissHintOffer()
                requestHint()
            } else {
                _hintOfferMessage.value = "Ad unavailable right now. Please try again later."
            }
        }
    }

    /** Called when the app goes to the background so the clock doesn't keep running. */
    fun pauseIfPlaying() {
        val current = _gameState.value
        if (!current.isPaused && !current.isCompleted && !current.isGameOver) {
            _gameState.value = current.copy(isPaused = true)
        }
    }

    fun togglePause() {
        val current = _gameState.value
        _gameState.value = gameEngine.togglePause(current)
        soundHaptics.playCellSelect(
            sound = preferences.value.soundEnabled,
            haptics = preferences.value.hapticsEnabled
        )
    }

    fun restart() {
        timerJob?.cancel()
        hasRecordedCompletion = false
        _gameState.value = gameEngine.restart(_gameState.value)
        startTimer()
        soundHaptics.playCellSelect(
            sound = preferences.value.soundEnabled,
            haptics = preferences.value.hapticsEnabled
        )
    }

    private fun handleVictory(state: GameState) {
        hasRecordedCompletion = true
        timerJob?.cancel()
        soundHaptics.playWin(
            sound = preferences.value.soundEnabled,
            haptics = preferences.value.hapticsEnabled
        )

        viewModelScope.launch {
            repository.recordGameFinished(
                id = state.puzzle.id,
                difficulty = state.puzzle.difficulty,
                boardDimension = state.puzzle.size.dimension,
                mode = state.mode.name,
                isWin = true,
                score = state.score,
                stars = state.starsEarned,
                elapsedSeconds = state.elapsedSeconds,
                hintsUsed = state.hintsUsed,
                mistakesCount = state.mistakesCount,
                isDaily = state.mode == GameMode.DAILY
            )
        }
    }

    private fun handleGameOver(state: GameState) {
        hasRecordedCompletion = true
        timerJob?.cancel()
        soundHaptics.playWrong(
            sound = preferences.value.soundEnabled,
            haptics = preferences.value.hapticsEnabled
        )

        viewModelScope.launch {
            repository.recordGameFinished(
                id = state.puzzle.id,
                difficulty = state.puzzle.difficulty,
                boardDimension = state.puzzle.size.dimension,
                mode = state.mode.name,
                isWin = false,
                score = 0,
                stars = 0,
                elapsedSeconds = state.elapsedSeconds,
                hintsUsed = state.hintsUsed,
                mistakesCount = state.mistakesCount,
                isDaily = state.mode == GameMode.DAILY
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun provideFactory(
            appContainer: AppContainer,
            initialMode: GameMode,
            initialDifficulty: Difficulty,
            initialBoardSize: BoardSize,
            isDaily: Boolean
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GameViewModel(
                        mode = initialMode,
                        difficulty = initialDifficulty,
                        size = initialBoardSize,
                        isDaily = isDaily,
                        repository = appContainer.gameRepository,
                        soundHaptics = appContainer.soundHapticsManager,
                        ads = appContainer.adsController,
                        gameEngine = appContainer.gameEngine
                    ) as T
                }
            }
    }
}
