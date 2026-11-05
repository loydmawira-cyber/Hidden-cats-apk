package com.example.hiddencats.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.domain.model.CellState
import com.example.hiddencats.ui.theme.CatAmberAccent
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
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Tool selector buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton(
                    label = "Auto-Cycle",
                    isSelected = selectedTool == ToolMode.CYCLE,
                    onClick = { onToolSelected(ToolMode.CYCLE) },
                    icon = {
                        Text("⇄", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    },
                    tag = "tool_cycle"
                )

                ToolButton(
                    label = "Cat",
                    isSelected = selectedTool == ToolMode.CONFIRMED_CAT,
                    onClick = { onToolSelected(ToolMode.CONFIRMED_CAT) },
                    icon = {
                        CatFaceIcon(size = 22.dp)
                    },
                    tag = "tool_cat"
                )

                ToolButton(
                    label = "Empty",
                    isSelected = selectedTool == ToolMode.EMPTY,
                    onClick = { onToolSelected(ToolMode.EMPTY) },
                    icon = {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    tag = "tool_empty"
                )

                ToolButton(
                    label = "Possible",
                    isSelected = selectedTool == ToolMode.POSSIBLE,
                    onClick = { onToolSelected(ToolMode.POSSIBLE) },
                    icon = {
                        CatPawIcon(size = 20.dp)
                    },
                    tag = "tool_possible"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row (Undo, Redo, Hint, Restart)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo & Redo
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }

                    FilledTonalIconButton(
                        onClick = onRedo,
                        enabled = canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }

                    FilledTonalIconButton(
                        onClick = onRestart,
                        modifier = Modifier.testTag("restart_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart")
                    }
                }

                // Hint button with badge
                Box {
                    Surface(
                        onClick = onHint,
                        shape = RoundedCornerShape(14.dp),
                        color = CatOrangePrimary,
                        modifier = Modifier.testTag("hint_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hint ($hintsAvailable)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
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
    tag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) CatOrangePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
