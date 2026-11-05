package com.example.hiddencats.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Game : Screen("game/{mode}/{difficulty}/{boardSize}/{isDaily}") {
        fun createRoute(mode: String, difficulty: String, boardSize: Int, isDaily: Boolean): String {
            return "game/$mode/$difficulty/$boardSize/$isDaily"
        }
    }
    data object Results : Screen("results/{won}/{timeSeconds}/{moves}/{hints}/{score}/{stars}/{boardSize}/{difficulty}/{mode}") {
        fun createRoute(
            won: Boolean,
            timeSeconds: Long,
            moves: Int,
            hints: Int,
            score: Int,
            stars: Int,
            boardSize: Int,
            difficulty: String,
            mode: String
        ): String {
            return "results/$won/$timeSeconds/$moves/$hints/$score/$stars/$boardSize/$difficulty/$mode"
        }
    }
    data object Statistics : Screen("statistics")
    data object Settings : Screen("settings")
    data object Rules : Screen("rules")
    data object Shop : Screen("shop")
}
