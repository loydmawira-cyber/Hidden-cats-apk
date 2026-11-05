package com.example.hiddencats.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

    val shape = RoundedCornerShape(10.dp)
    val borderColor = when {
        isWrong -> CatRedMistake
        isHighlighted -> CatAmberAccent
        else -> Color.Black.copy(alpha = 0.15f)
    }
    val borderWidth = when {
        isWrong || isHighlighted -> 3.dp
        else -> 1.dp
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
            .padding(2.dp)
            .shadow(elevation = if (cell.state == CellState.CONFIRMED_CAT) 3.dp else 1.dp, shape = shape)
            .clip(shape)
            .background(animatedBgColor)
            .border(borderWidth, borderColor, shape)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "Row ${cell.row + 1}, Column ${cell.col + 1}, ${cell.color.displayName}, $stateDesc"
            }
            .testTag("cell_${cell.row}_${cell.col}"),
        contentAlignment = Alignment.Center
    ) {
        // Color-blind symbol indicator at top corner
        if (isColorBlindMode) {
            Text(
                text = cell.color.symbolChar,
                fontSize = (cellSize.value * 0.22f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.35f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(3.dp)
            )
        }

        // Cell State Visual Content
        when (cell.state) {
            CellState.CONFIRMED_CAT -> {
                CatFaceIcon(size = cellSize * 0.72f)
            }
            CellState.EMPTY -> {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.45f),
                    modifier = Modifier.size(cellSize * 0.5f)
                )
            }
            CellState.POSSIBLE -> {
                CatPawIcon(
                    size = cellSize * 0.5f,
                    tint = Color.Black.copy(alpha = 0.4f)
                )
            }
            CellState.UNKNOWN -> {
                // Blank canvas
            }
        }
    }
}
