package com.example.hiddencats.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = CatEspresso,
    primaryContainer = CatOrangeDark,
    onPrimaryContainer = CatCreamBackground,
    secondary = CatAmberAccent,
    onSecondary = CatEspresso,
    tertiary = CatGreenSuccess,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFDCD3F7)
)

private val LightColorScheme = lightColorScheme(
    primary = CatOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = CatWarmSurface,
    onPrimaryContainer = CatDarkBrown,
    secondary = CatAmberAccent,
    onSecondary = CatDarkBrown,
    tertiary = CatGreenSuccess,
    background = CatCreamBackground,
    onBackground = CatEspresso,
    surface = Color.White,
    onSurface = CatEspresso,
    surfaceVariant = CatWarmSurface,
    onSurfaceVariant = CatDarkBrown
)

@Composable
fun HiddenCatsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
