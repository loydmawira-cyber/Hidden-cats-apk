package com.example.hiddencats.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.domain.model.Cell
import com.example.hiddencats.domain.model.CellState
import com.example.hiddencats.ui.theme.CatAmberAccent
import com.example.hiddencats.ui.theme.CatRedMistake

@Composable
fun CellView(
    cell: Cell,
    cellSize: Dp,
    isColorBlindMode: Boolean,
    isHighlighted: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val baseColor = cell.color.composeColor(isColorBlindMode)
    val animatedBgColor by animateColorAsState(
        targetValue = baseColor,
        animationSpec = tween(200),
        label = "cell_bg"
    )

    val shape: Shape = RoundedCornerShape((cellSize.value * 0.22f).coerceIn(6f, 12f).dp)
    val edgeColor = baseColor.darken(0.35f)
    val borderColor = when {
        isWrong -> CatRedMistake
        isHighlighted -> CatAmberAccent
        else -> edgeColor
    }
    val borderWidth = if (isWrong || isHighlighted) 3.dp else 1.5.dp

    // Shake the tile when a wrong cat is placed
    val shake = remember { Animatable(0f) }
    LaunchedEffect(isWrong) {
        if (isWrong) {
            for (d in listOf(-9f, 9f, -7f, 7f, -3f, 0f)) shake.animateTo(d, tween(45))
        } else {
            shake.snapTo(0f)
        }
    }

    val stateDesc = when (cell.state) {
        CellState.CONFIRMED_CAT -> "Confirmed Cat"
        CellState.EMPTY -> "Empty"
        CellState.POSSIBLE -> "Possible Cat"
        CellState.UNKNOWN -> "Unknown"
    }

    Box(
        modifier = modifier
            .size(cellSize)
            .padding(1.5.dp)
            .graphicsLayer { translationX = shake.value * density }
            .shadow(elevation = if (cell.state == CellState.CONFIRMED_CAT) 4.dp else 1.5.dp, shape = shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(animatedBgColor.lighten(0.2f), animatedBgColor, animatedBgColor.darken(0.1f))
                )
            )
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Row ${cell.row + 1}, Column ${cell.col + 1}, ${cell.color.displayName}, $stateDesc"
            }
            .testTag("cell_${cell.row}_${cell.col}"),
        contentAlignment = Alignment.Center
    ) {
        if (isHighlighted) HintGlow()
        if (isWrong) Box(Modifier.fillMaxSize().background(CatRedMistake.copy(alpha = 0.28f)))

        // Color-blind symbol indicator at top corner
        if (isColorBlindMode) {
            Text(
                text = cell.color.symbolChar,
                fontSize = (cellSize.value * 0.22f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(3.dp)
            )
        }

        // Cat pops in with a bouncy spring
        AnimatedVisibility(
            visible = cell.state == CellState.CONFIRMED_CAT,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(tween(120)),
            exit = scaleOut(tween(120)) + fadeOut(tween(120))
        ) {
            CatFaceIcon(size = cellSize * 0.76f)
        }

        when (cell.state) {
            CellState.EMPTY -> Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.42f),
                modifier = Modifier.size(cellSize * 0.46f)
            )
            CellState.POSSIBLE -> CatPawIcon(
                size = cellSize * 0.5f,
                tint = Color.Black.copy(alpha = 0.45f)
            )
            else -> Unit
        }
    }
}

@Composable
private fun HintGlow() {
    val transition = rememberInfiniteTransition(label = "hint_glow")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "hint_glow_alpha"
    )
    Box(Modifier.fillMaxSize().background(CatAmberAccent.copy(alpha = alpha)))
}
