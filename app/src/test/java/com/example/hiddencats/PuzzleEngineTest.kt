package com.example.hiddencats

import com.example.hiddencats.billing.FakeBillingRepository
import com.example.hiddencats.billing.PurchaseResult
import com.example.hiddencats.domain.engine.*
import com.example.hiddencats.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleEngineTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun `test 8-neighbor adjacency validation`() {
        val solver = PuzzleSolver()
        val colorGrid = listOf(
            listOf(CellColor.CORAL, CellColor.SKY),
            listOf(CellColor.SKY, CellColor.MINT)
        )

        // Place a cat at (0,0)
        val placedCats = setOf(0 to 0)

        // Placing another cat at (1,1) -> Touching diagonally -> MUST FAIL
        assertFalse(solver.isPlacementValid(1, 1, colorGrid, placedCats))

        // Placing another cat at (0,1) -> Touching horizontally -> MUST FAIL
        assertFalse(solver.isPlacementValid(0, 1, colorGrid, placedCats))

        // Placing a cat at distant cell (2,2) in a 3x3 -> VALID
        val colorGrid3 = listOf(
            listOf(CellColor.CORAL, CellColor.SKY, CellColor.MINT),
            listOf(CellColor.SKY, CellColor.AMBER, CellColor.LAVENDER),
            listOf(CellColor.MINT, CellColor.LAVENDER, CellColor.CORAL)
        )
        assertTrue(solver.isPlacementValid(2, 2, colorGrid3, setOf(0 to 0)))
    }

    @Test
    fun `test row and column color uniqueness`() {
        val solver = PuzzleSolver()
        val colorGrid = listOf(
            listOf(CellColor.CORAL, CellColor.SKY, CellColor.CORAL),
            listOf(CellColor.MINT, CellColor.AMBER, CellColor.LAVENDER),
            listOf(CellColor.SKY, CellColor.CORAL, CellColor.MINT)
        )

        // Cat already placed at (0,0) which is CORAL
        val placed = setOf(0 to 0)

        // Placing another cat in Row 0 at (0,2) which is ALSO CORAL -> MUST FAIL
        assertFalse(solver.isPlacementValid(0, 2, colorGrid, placed))
    }

    @Test
    fun `test puzzle generator generates valid puzzle`() {
        val generator = PuzzleGenerator()
        val puzzle = generator.generate(
            size = BoardSize.FOUR,
            difficulty = Difficulty.EASY,
            seed = 42L
        )

        assertNotNull(puzzle)
        assertEquals(4, puzzle.size.dimension)
        assertTrue(puzzle.totalCats > 0)
        assertEquals(4, puzzle.grid.size)

        // Validate full board via solver
        val solver = PuzzleSolver()
        val colorGrid = puzzle.grid.map { row -> row.map { it.color } }
        val trueCats = puzzle.grid.flatMap { row -> row.filter { it.hasCat }.map { it.row to it.col } }.toSet()

        assertTrue(
            solver.validateFullBoard(
                colorGrid = colorGrid,
                cats = trueCats,
                rowCounts = puzzle.rowCatCounts,
                colCounts = puzzle.colCatCounts
            )
        )
    }

    @Test
    fun `test game engine move undo and redo`() {
        val generator = PuzzleGenerator()
        val puzzle = generator.generate(BoardSize.FOUR, Difficulty.EASY, 1234L)
        val engine = GameEngine()

        var state = engine.initGame(puzzle, GameMode.CLASSIC)

        assertEquals(CellState.UNKNOWN, state.boardStates[0][0])
        assertFalse(state.canUndo)
        assertFalse(state.canRedo)

        // Place cat
        state = engine.setCellState(state, 0, 0, CellState.CONFIRMED_CAT)
        assertEquals(CellState.CONFIRMED_CAT, state.boardStates[0][0])
        assertTrue(state.canUndo)
        assertFalse(state.canRedo)

        // Undo
        state = engine.undo(state)
        assertEquals(CellState.UNKNOWN, state.boardStates[0][0])
        assertFalse(state.canUndo)
        assertTrue(state.canRedo)

        // Redo
        state = engine.redo(state)
        assertEquals(CellState.CONFIRMED_CAT, state.boardStates[0][0])
        assertTrue(state.canUndo)
        assertFalse(state.canRedo)
    }

    @Test
    fun `test game engine mistake penalty in classic mode`() {
        val generator = PuzzleGenerator()
        val puzzle = generator.generate(BoardSize.FOUR, Difficulty.EASY, 5678L)
        val engine = GameEngine()

        var state = engine.initGame(puzzle, GameMode.CLASSIC)
        val nonCatCell = puzzle.grid.flatten().first { !it.hasCat }
        val initialLives = state.livesRemaining

        // Confirm cat where there is no cat
        state = engine.setCellState(state, nonCatCell.row, nonCatCell.col, CellState.CONFIRMED_CAT)
        assertEquals(1, state.mistakesCount)
        assertEquals(initialLives - 1, state.livesRemaining)
    }

    @Test
    fun `test score calculator logic`() {
        val winScore = ScoreCalculator.calculateScore(
            difficulty = Difficulty.HARD,
            mode = GameMode.CLASSIC,
            elapsedSeconds = 120,
            mistakesCount = 0,
            hintsUsed = 0,
            isWin = true
        )
        assertTrue("Win score should be high", winScore > 1000)

        val stars = ScoreCalculator.calculateStars(
            elapsedSeconds = 90,
            baseTimeSeconds = 300,
            mistakesCount = 0,
            hintsUsed = 0,
            isWin = true
        )
        assertEquals(3, stars)

        val zeroStars = ScoreCalculator.calculateStars(
            elapsedSeconds = 500,
            baseTimeSeconds = 300,
            mistakesCount = 3,
            hintsUsed = 4,
            isWin = false
        )
        assertEquals(0, zeroStars)
    }

    @Test
    fun `test billing repository simulated purchase`() = testScope.runTest {
        val fakeBilling = FakeBillingRepository(scope = this)

        val isReady = fakeBilling.isReady.first()
        assertTrue(isReady)

        fakeBilling.launchBillingFlow(null, "premium_upgrade")
        testScheduler.advanceUntilIdle()

        val event = fakeBilling.purchaseEvents.first()
        assertTrue(event is PurchaseResult.Success)
        assertEquals("premium_upgrade", (event as PurchaseResult.Success).productId)
    }

    @Test
    fun `generated puzzles have exactly one solution`() {
        val generator = PuzzleGenerator()
        val solver = PuzzleSolver()
        for (size in BoardSize.entries) {
            for (diff in Difficulty.entries) {
                val puzzle = generator.generate(size, diff, seed = 99L + size.dimension)
                val colors = puzzle.grid.map { row -> row.map { it.color } }
                val solutions = solver.countSolutions(colors, puzzle.rowCatCounts, puzzle.colCatCounts, 2)
                assertEquals("${size.label} ${diff.label}", 1, solutions.size)
                assertEquals(puzzle.catCoordinates, solutions[0])
            }
        }
    }
}
