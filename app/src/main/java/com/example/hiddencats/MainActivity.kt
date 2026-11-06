package com.example.hiddencats

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hiddencats.core.AppContainer
import com.example.hiddencats.data.local.PlayerPreferences
import com.example.hiddencats.ui.navigation.NavGraph
import com.example.hiddencats.ui.theme.HiddenCatsTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = AppContainer(applicationContext)
        appContainer.adsController.initialize(this)

        setContent {
            val prefs by appContainer.preferencesManager.preferencesFlow.collectAsState(initial = PlayerPreferences())
            val darkTheme = prefs.darkMode

            // Keep status/navigation bar icon colours in sync with the in-app theme toggle
            // (previously they followed the system theme, giving unreadable icons).
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT) { darkTheme }
                )
                onDispose { }
            }

            HiddenCatsTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        appContainer = appContainer
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appContainer.soundHapticsManager.release()
    }
}
