package com.example.hiddencats.domain.engine

import com.example.hiddencats.domain.model.CellState
import com.example.hiddencats.domain.model.GameMode
import com.example.hiddencats.domain.model.GameState
import com.example.hiddencats.domain.model.Move
import com.example.hiddencats.domain.model.Puzzle

class GameEngine(
    private val hintEngine: HintEngine = HintEngine()
) {

    fun initGame(puzzle: Puzzle, mode: GameMode): GameState {
        val dim = puzzle.size.dimension
        val initialBoard = (0 until dim).map {
            (0 until dim).map { CellState.UNKNOWN }
        }
        val maxLives = if (mode == GameMode.RELAXED) 999 else 3
        val initialTime = if (mode == GameMode.TIMED) puzzle.difficulty.baseTimeSeconds else 0

        return GameState(
            puzzle = puzzle,
            mode = mode,
            boardStates = initialBoard,
            livesRemaining = maxLives,
            maxLives = maxLives,
            remainingSeconds = initialTime
        )
    }

    fun setCellState(state: GameState, row: Int, col: Int, targetState: CellState): GameState {
        if (state.isCompleted || state.isGameOver || state.isPaused) return state
        val dim = state.puzzle.size.dimension
        if (row !in 0 until dim || col !in 0 until dim) return state

        val currentState = state.boardStates[row][col]
        if (currentState == targetState) return state

        // Check mistake if confirming a cell as a cat that has NO cat
        var newLives = state.livesRemaining
        var newMistakes = state.mistakesCount
        val newWrongCells = state.wrongCells.toMutableSet()
        var gameOver = false

        if (targetState == CellState.CONFIRMED_CAT && !state.puzzle.grid[row][col].hasCat) {
            newMistakes++
            newWrongCells.add(row to col)
            if (state.mode != GameMode.RELAXED) {
                newLives = (newLives - 1).coerceAtLeast(0)
                if (newLives == 0) {
                    gameOver = true
                }
            }
        } else if (targetState != CellState.CONFIRMED_CAT) {
            newWrongCells.remove(row to col)
        }

        // Update board
        val newBoard = state.boardStates.mapIndexed { r, rList ->
            rList.mapIndexed { c, current ->
                if (r == row && c == col) targetState else current
            }
        }

        // Record move
        val move = Move(row, col, currentState, targetState)
        val newHistory = state.moveHistory + move

        val intermediateState = state.copy(
            boardStates = newBoard,
            moveHistory = newHistory,
            redoHistory = emptyList(), // clear redo on new action
            livesRemaining = newLives,
            mistakesCount = newMistakes,
            wrongCells = newWrongCells,
            isGameOver = gameOver,
            activeHintMessage = null,
            hintHighlightCells = emptySet()
        )

        return checkWinCondition(intermediateState)
    }

    fun cycleCellState(state: GameState, row: Int, col: Int): GameState {
        if (state.isCompleted || state.isGameOver || state.isPaused) return state
        val current = state.boardStates[row][col]
        val next = current.nextState()
        return setCellState(state, row, col, next)
    }

    fun undo(state: GameState): GameState {
        if (!state.canUndo) return state
        val lastMove = state.moveHistory.last()
        val newHistory = state.moveHistory.dropLast(1)
        val newRedo = state.redoHistory + lastMove

        val newBoard = state.boardStates.mapIndexed { r, rList ->
            rList.mapIndexed { c, current ->
                if (r == lastMove.row && c == lastMove.col) lastMove.previousState else current
            }
        }

        val newWrong = state.wrongCells.toMutableSet().apply {
            remove(lastMove.row to lastMove.col)
        }

        return state.copy(
            boardStates = newBoard,
            moveHistory = newHistory,
            redoHistory = newRedo,
            wrongCells = newWrong,
            activeHintMessage = null,
            hintHighlightCells = emptySet()
        )
    }

    fun redo(state: GameState): GameState {
        if (!state.canRedo) return state
        val move = state.redoHistory.last()
        val newRedo = state.redoHistory.dropLast(1)
        val newBoard = state.boardStates.mapIndexed { r, rList ->
            rList.mapIndexed { c, current ->
                if (r == move.row && c == move.col) move.newState else current
            }
        }
        val newWrong = state.wrongCells.toMutableSet().apply {
            if (move.newState == CellState.CONFIRMED_CAT && !state.puzzle.grid[move.row][move.col].hasCat) {
                add(move.row to move.col)
            } else {
                remove(move.row to move.col)
            }
        }
        return checkWinCondition(
            state.copy(
                boardStates = newBoard,
                moveHistory = state.moveHistory + move,
                redoHistory = newRedo,
                wrongCells = newWrong,
                activeHintMessage = null,
                hintHighlightCells = emptySet()
            )
        )
    }

    fun requestHint(state: GameState): GameState {
        if (state.isCompleted || state.isGameOver || state.isPaused) return state
        val hintResult = hintEngine.generateHint(state)
        val newHintsUsed = state.hintsUsed + 1

        var updatedState = state.copy(
            hintsUsed = newHintsUsed,
            activeHintMessage = hintResult.explanation,
            hintHighlightCells = hintResult.targetCells
        )

        // If hint suggested a concrete state and has target cell, apply it if desired
        if (hintResult.suggestedState != null && hintResult.targetCells.size == 1) {
            val (r, c) = hintResult.targetCells.first()
            if (state.boardStates[r][c] != hintResult.suggestedState) {
                updatedState = setCellState(updatedState, r, c, hintResult.suggestedState).copy(
                    activeHintMessage = hintResult.explanation,
                    hintHighlightCells = hintResult.targetCells
                )
            }
        }

        return updatedState
    }

    fun tickTimer(state: GameState): GameState {
        if (state.isCompleted || state.isGameOver || state.isPaused) return state

        val newElapsed = state.elapsedSeconds + 1
        return if (state.mode == GameMode.TIMED) {
            val newRemaining = state.remainingSeconds - 1
            if (newRemaining <= 0) {
                state.copy(
                    elapsedSeconds = newElapsed,
                    remainingSeconds = 0,
                    isGameOver = true
                )
            } else {
                state.copy(
                    elapsedSeconds = newElapsed,
                    remainingSeconds = newRemaining
                )
            }
        } else {
            state.copy(elapsedSeconds = newElapsed)
        }
    }

    fun togglePause(state: GameState): GameState {
        if (state.isCompleted || state.isGameOver) return state
        return state.copy(isPaused = !state.isPaused)
    }

    fun restart(state: GameState): GameState {
        return initGame(state.puzzle, state.mode)
    }

    private fun checkWinCondition(state: GameState): GameState {
        if (state.isGameOver) return state
        val puzzle = state.puzzle
        val dim = puzzle.size.dimension

        // Win when all true cats are confirmed, and no wrong cats are confirmed
        var confirmedCount = 0
        var correctCount = 0

        for (r in 0 until dim) {
            for (c in 0 until dim) {
                if (state.boardStates[r][c] == CellState.CONFIRMED_CAT) {
                    confirmedCount++
                    if (puzzle.grid[r][c].hasCat) {
                        correctCount++
                    }
                }
            }
        }

        if (confirmedCount == puzzle.totalCats && correctCount == puzzle.totalCats) {
            val score = ScoreCalculator.calculateScore(
                difficulty = puzzle.difficulty,
                mode = state.mode,
                elapsedSeconds = state.elapsedSeconds,
                mistakesCount = state.mistakesCount,
                hintsUsed = state.hintsUsed,
                isWin = true
            )
            val stars = ScoreCalculator.calculateStars(
                elapsedSeconds = state.elapsedSeconds,
                baseTimeSeconds = puzzle.difficulty.baseTimeSeconds,
                mistakesCount = state.mistakesCount,
                hintsUsed = state.hintsUsed,
                isWin = true
            )

            // Auto-fill all remaining unknown cells as EMPTY
            val solvedBoard = state.boardStates.mapIndexed { r, rList ->
                rList.mapIndexed { c, current ->
                    if (current == CellState.UNKNOWN || current == CellState.POSSIBLE) CellState.EMPTY else current
                }
            }

            return state.copy(
                boardStates = solvedBoard,
                isCompleted = true,
                score = score,
                starsEarned = stars
            )
        }

        return state
    }
}
