package com.example.hiddencats.ui.screens.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.ui.components.CatFaceIcon
import com.example.hiddencats.ui.components.GameBackground
import com.example.hiddencats.ui.components.OutlinedTitle
import com.example.hiddencats.ui.theme.CatAmberAccent
import com.example.hiddencats.ui.theme.CatOrangePrimary
import com.example.hiddencats.ui.theme.InkOutline
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    // Springy overshoot gives the logo a bouncy "game boot" feel
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "splash_scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1400L)
        onSplashFinished()
    }

    GameBackground(modifier = Modifier.testTag("splash_screen")) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(CatAmberAccent)
                    .border(6.dp, InkOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CatFaceIcon(size = 100.dp)
            }

            Spacer(modifier = Modifier.height(22.dp))

            OutlinedTitle(text = "HIDDEN CATS", fontSize = 40.sp, modifier = Modifier.scale(scale))

            Text(
                text = "COLOR LOGIC PUZZLES",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = CatOrangePrimary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}
