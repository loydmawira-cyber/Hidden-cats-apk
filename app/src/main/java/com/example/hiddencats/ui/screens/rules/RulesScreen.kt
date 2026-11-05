package com.example.hiddencats.ui.screens.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hiddencats.domain.model.CellColor
import com.example.hiddencats.domain.model.CellState
import com.example.hiddencats.ui.components.CatFaceIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How to Play", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("rules_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RuleCard(
                number = "1",
                title = "The Objective",
                description = "Each row and column has a number indicator showing exactly how many cats are hiding in that line. Find all cats to solve the puzzle!",
                badgeColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFB5A7)),
                        contentAlignment = Alignment.Center
                    ) {
                        CatFaceIcon(size = 36.dp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Tap cells to place Cat, mark Empty, or flag Possible.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            RuleCard(
                number = "2",
                title = "Cats Never Touch (8 Neighbors)",
                description = "Cats are territorial! Two cats can NEVER touch each other horizontally, vertically, or diagonally.",
                badgeColor = Color(0xFFE57373)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (r in 0..2) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (c in 0..2) {
                                    val isCenter = r == 1 && c == 1
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isCenter) Color(0xFFFFB5A7)
                                                else Color(0xFFFFCDD2)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCenter) {
                                            CatFaceIcon(size = 24.dp)
                                        } else {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Forbidden",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Once a cat is confirmed, all 8 surrounding cells must be EMPTY.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            RuleCard(
                number = "3",
                title = "Color Uniqueness Rule",
                description = "Within the SAME ROW or the SAME COLUMN, no two cats may sit on the SAME COLOR cell.",
                badgeColor = Color(0xFF64B5F6)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFB5A7)),
                            contentAlignment = Alignment.Center
                        ) {
                            CatFaceIcon(size = 24.dp)
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFB5EAD7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("·", fontSize = 24.sp, color = Color(0xFF2E7D32))
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFB5A7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "No cat", tint = Color(0xFFD32F2F))
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFB8E0D2)),
                            contentAlignment = Alignment.Center
                        ) {
                            CatFaceIcon(size = 24.dp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "If a row already has a cat on Coral, other Coral cells in that row are empty!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            RuleCard(
                number = "4",
                title = "Try Interactive Practice (4x4)",
                description = "Tap below to test placing a cat and see the rules in action!",
                badgeColor = Color(0xFF81C784)
            ) {
                InteractivePracticeGrid()
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RuleCard(
    number: String,
    title: String,
    description: String,
    badgeColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        number,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun InteractivePracticeGrid() {
    var states by remember {
        mutableStateOf(
            listOf(
                CellState.UNKNOWN, CellState.UNKNOWN,
                CellState.UNKNOWN, CellState.UNKNOWN
            )
        )
    }

    val colors = listOf(
        CellColor.CORAL, CellColor.SKY,
        CellColor.MINT, CellColor.AMBER
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Tap to cycle: Unknown → Cat → Empty → Possible",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (r in 0..1) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (c in 0..1) {
                        val index = r * 2 + c
                        val state = states[index]
                        val cellColor = colors[index]
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cellColor.composeColor())
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val nextState = when (state) {
                                        CellState.UNKNOWN -> CellState.CONFIRMED_CAT
                                        CellState.CONFIRMED_CAT -> CellState.EMPTY
                                        CellState.EMPTY -> CellState.POSSIBLE
                                        CellState.POSSIBLE -> CellState.UNKNOWN
                                    }
                                    val newList = states.toMutableList()
                                    newList[index] = nextState
                                    states = newList
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when (state) {
                                CellState.CONFIRMED_CAT -> CatFaceIcon(
                                    size = 28.dp
                                )
                                CellState.EMPTY -> Text("·", fontSize = 28.sp, color = Color(0xFF555555))
                                CellState.POSSIBLE -> Icon(
                                    Icons.Default.Pets,
                                    contentDescription = "Possible",
                                    tint = Color(0x883E2723),
                                    modifier = Modifier.size(20.dp)
                                )
                                CellState.UNKNOWN -> {}
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                states = listOf(
                    CellState.UNKNOWN, CellState.UNKNOWN,
                    CellState.UNKNOWN, CellState.UNKNOWN
                )
            },
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text("Reset Practice")
        }
    }
}
