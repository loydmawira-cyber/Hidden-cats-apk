package com.example.hiddencats.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.ui.theme.CatAmberAccent
import com.example.hiddencats.ui.theme.CatGreenSuccess
import com.example.hiddencats.ui.theme.CatOrangePrimary

enum class ToolMode {
    CYCLE,          // Tap cycles states
    CONFIRMED_CAT,  // Tap directly places Cat
    EMPTY,          // Tap directly marks Empty
    POSSIBLE        // Tap marks Possible
}

@Composable
fun ControlBar(
    selectedTool: ToolMode,
    onToolSelected: (ToolMode) -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    canRedo: Boolean,
    onRedo: () -> Unit,
    hintsAvailable: Int,
    onHint: () -> Unit,
    onRestart: () -> Unit,
    isLeftHanded: Boolean = false,
    unlimitedHints: Boolean = false,
    modifier: Modifier = Modifier
) {
    GamePanel(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        contentPadding = 12.dp
    ) {
        // Tool selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton(
                label = "Cycle",
                isSelected = selectedTool == ToolMode.CYCLE,
                onClick = { onToolSelected(ToolMode.CYCLE) },
                icon = { Text("⇄", fontSize = 20.sp, fontWeight = FontWeight.Black) },
                tag = "tool_cycle",
                modifier = Modifier.weight(1f)
            )
            ToolButton(
                label = "Cat",
                isSelected = selectedTool == ToolMode.CONFIRMED_CAT,
                onClick = { onToolSelected(ToolMode.CONFIRMED_CAT) },
                icon = { CatFaceIcon(size = 24.dp) },
                tag = "tool_cat",
                modifier = Modifier.weight(1f)
            )
            ToolButton(
                label = "Empty",
                isSelected = selectedTool == ToolMode.EMPTY,
                onClick = { onToolSelected(ToolMode.EMPTY) },
                icon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(22.dp)) },
                tag = "tool_empty",
                modifier = Modifier.weight(1f)
            )
            ToolButton(
                label = "Maybe",
                isSelected = selectedTool == ToolMode.POSSIBLE,
                onClick = { onToolSelected(ToolMode.POSSIBLE) },
                icon = { CatPawIcon(size = 22.dp, tint = androidx.compose.material3.LocalContentColor.current) },
                tag = "tool_possible",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Undo / Redo / Restart + Hint. Mirrored for left-handed players.
        val undoRedo: @Composable () -> Unit = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                SquareIconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    tag = "undo_button",
                    description = "Undo",
                    icon = { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo", tint = Color.White) }
                )
                SquareIconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    tag = "redo_button",
                    description = "Redo",
                    icon = { Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo", tint = Color.White) }
                )
                SquareIconButton(
                    onClick = onRestart,
                    enabled = true,
                    tag = "restart_button",
                    description = "Restart",
                    color = Color(0xFF8B8FA8),
                    icon = { Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White) }
                )
            }
        }
        val hintButton: @Composable () -> Unit = {
            GameButton(
                onClick = onHint,
                color = CatGreenSuccess,
                height = 46.dp,
                modifier = Modifier.width(128.dp).testTag("hint_button")
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = CatAmberAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                GameButtonText(
                    text = if (unlimitedHints) "Hint ∞" else "Hint $hintsAvailable",
                    fontSize = 16.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLeftHanded) {
                hintButton()
                undoRedo()
            } else {
                undoRedo()
                hintButton()
            }
        }
    }
}

@Composable
fun ToolButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    GameButton(
        onClick = onClick,
        color = if (isSelected) CatOrangePrimary else MaterialTheme.colorScheme.surfaceVariant,
        height = 58.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.testTag(tag)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides
                    (if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
            ) {
                icon()
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SquareIconButton(
    onClick: () -> Unit,
    enabled: Boolean,
    tag: String,
    description: String,
    icon: @Composable () -> Unit,
    color: Color = Color(0xFF5B8DEF)
) {
    GameButton(
        onClick = onClick,
        enabled = enabled,
        color = color,
        height = 46.dp,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.width(52.dp).testTag(tag)
    ) {
        icon()
    }
}
