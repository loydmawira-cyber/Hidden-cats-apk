package com.example.hiddencats.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.hiddencats.core.AppContainer
import com.example.hiddencats.domain.model.BoardSize
import com.example.hiddencats.domain.model.Difficulty
import com.example.hiddencats.domain.model.GameMode
import com.example.hiddencats.ui.screens.game.GameScreen
import com.example.hiddencats.ui.screens.game.GameViewModel
import com.example.hiddencats.ui.screens.home.HomeScreen
import com.example.hiddencats.ui.screens.home.HomeViewModel
import com.example.hiddencats.ui.screens.results.ResultsScreen
import com.example.hiddencats.ui.screens.rules.RulesScreen
import com.example.hiddencats.ui.screens.settings.SettingsScreen
import com.example.hiddencats.ui.screens.settings.SettingsViewModel
import com.example.hiddencats.ui.screens.shop.ShopScreen
import com.example.hiddencats.ui.screens.shop.ShopViewModel
import com.example.hiddencats.ui.screens.splash.SplashScreen
import com.example.hiddencats.ui.screens.statistics.StatisticsScreen
import com.example.hiddencats.ui.screens.statistics.StatisticsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    appContainer: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(appContainer)
            )
            HomeScreen(
                viewModel = viewModel,
                onStartGame = { mode, difficulty, size ->
                    navController.navigate(
                        Screen.Game.createRoute(
                            mode = mode.name,
                            difficulty = difficulty.name,
                            boardSize = size.dimension,
                            isDaily = false
                        )
                    )
                },
                onStartDaily = {
                    navController.navigate(
                        Screen.Game.createRoute(
                            mode = GameMode.DAILY.name,
                            difficulty = Difficulty.NORMAL.name,
                            boardSize = BoardSize.SIX.dimension,
                            isDaily = true
                        )
                    )
                },
                onNavigateRules = { navController.navigate(Screen.Rules.route) },
                onNavigateStats = { navController.navigate(Screen.Statistics.route) },
                onNavigateShop = { navController.navigate(Screen.Shop.route) },
                onNavigateSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Game.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("boardSize") { type = NavType.IntType },
                navArgument("isDaily") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val modeStr = backStackEntry.arguments?.getString("mode") ?: GameMode.CLASSIC.name
            val diffStr = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name
            val sizeInt = backStackEntry.arguments?.getInt("boardSize") ?: 6
            val isDaily = backStackEntry.arguments?.getBoolean("isDaily") ?: false

            val mode = runCatching { GameMode.valueOf(modeStr) }.getOrDefault(GameMode.CLASSIC)
            val difficulty = runCatching { Difficulty.valueOf(diffStr) }.getOrDefault(Difficulty.EASY)
            val boardSize = BoardSize.fromDimension(sizeInt)

            val viewModel: GameViewModel = viewModel(
                key = "game_${modeStr}_${diffStr}_${sizeInt}_${isDaily}",
                factory = GameViewModel.provideFactory(
                    appContainer = appContainer,
                    initialMode = mode,
                    initialDifficulty = difficulty,
                    initialBoardSize = boardSize,
                    isDaily = isDaily
                )
            )

            GameScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onGameFinished = { gameState ->
                    val won = gameState.isCompleted && !gameState.isGameOver
                    navController.navigate(
                        Screen.Results.createRoute(
                            won = won,
                            timeSeconds = gameState.elapsedSeconds.toLong(),
                            moves = gameState.moveHistory.size,
                            hints = gameState.hintsUsed,
                            score = gameState.score,
                            stars = gameState.starsEarned,
                            boardSize = boardSize.dimension,
                            difficulty = difficulty.name,
                            mode = mode.name
                        )
                    ) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(
                navArgument("won") { type = NavType.BoolType },
                navArgument("timeSeconds") { type = NavType.LongType },
                navArgument("moves") { type = NavType.IntType },
                navArgument("hints") { type = NavType.IntType },
                navArgument("score") { type = NavType.IntType },
                navArgument("stars") { type = NavType.IntType },
                navArgument("boardSize") { type = NavType.IntType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val won = backStackEntry.arguments?.getBoolean("won") ?: true
            val timeSeconds = backStackEntry.arguments?.getLong("timeSeconds") ?: 0L
            val moves = backStackEntry.arguments?.getInt("moves") ?: 0
            val hints = backStackEntry.arguments?.getInt("hints") ?: 0
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val stars = backStackEntry.arguments?.getInt("stars") ?: 3
            val boardDimension = backStackEntry.arguments?.getInt("boardSize") ?: 6
            val diffStr = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name
            val modeStr = backStackEntry.arguments?.getString("mode") ?: GameMode.CLASSIC.name

            ResultsScreen(
                won = won,
                timeSeconds = timeSeconds,
                movesCount = moves,
                hintsUsed = hints,
                score = score,
                stars = stars,
                boardSize = boardDimension,
                difficulty = diffStr,
                mode = modeStr,
                onPlayAgain = {
                    navController.navigate(
                        Screen.Game.createRoute(
                            mode = modeStr,
                            difficulty = diffStr,
                            boardSize = boardDimension,
                            isDaily = false
                        )
                    ) {
                        popUpTo(Screen.Results.route) { inclusive = true }
                    }
                },
                onNextPuzzle = {
                    navController.navigate(
                        Screen.Game.createRoute(
                            mode = modeStr,
                            difficulty = diffStr,
                            boardSize = boardDimension,
                            isDaily = false
                        )
                    ) {
                        popUpTo(Screen.Results.route) { inclusive = true }
                    }
                },
                onGoHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Statistics.route) {
            val viewModel: StatisticsViewModel = viewModel(
                factory = StatisticsViewModel.provideFactory(appContainer)
            )
            StatisticsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(appContainer)
            )
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Rules.route) {
            RulesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Shop.route) {
            val viewModel: ShopViewModel = viewModel(
                factory = ShopViewModel.provideFactory(appContainer)
            )
            ShopScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
