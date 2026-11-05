package com.example.hiddencats.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hiddencats.ui.theme.CatOrangePrimary
import com.example.hiddencats.ui.theme.CatRedMistake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Audio & Haptics Section
                SettingsSection(title = "Audio & Feedback") {
                    SettingToggleRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Sound Effects",
                        subtitle = "Chimes, clicks and validation sounds",
                        checked = uiState.preferences.soundEnabled,
                        onCheckedChange = { viewModel.toggleSound(it) },
                        testTag = "toggle_sound"
                    )
                    SettingToggleRow(
                        icon = Icons.Default.MusicNote,
                        title = "Music",
                        subtitle = "Gentle background melodies",
                        checked = uiState.preferences.musicEnabled,
                        onCheckedChange = { viewModel.toggleMusic(it) },
                        testTag = "toggle_music"
                    )
                    SettingToggleRow(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Vibration",
                        subtitle = "Tactile touch responses on moves",
                        checked = uiState.preferences.hapticsEnabled,
                        onCheckedChange = { viewModel.toggleHaptics(it) },
                        testTag = "toggle_haptics"
                    )
                }

                // Accessibility & Display Section
                SettingsSection(title = "Display & Accessibility") {
                    SettingToggleRow(
                        icon = Icons.Default.ColorLens,
                        title = "Color-Blind Mode",
                        subtitle = "Displays geometric symbols on tiles",
                        checked = uiState.preferences.colorBlindMode,
                        onCheckedChange = { viewModel.toggleColorBlind(it) },
                        testTag = "toggle_color_blind"
                    )
                    SettingToggleRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Cozy night palette",
                        checked = uiState.preferences.darkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        testTag = "toggle_dark_mode"
                    )
                    SettingToggleRow(
                        icon = Icons.Default.PanTool,
                        title = "Left-Handed Layout",
                        subtitle = "Optimizes board controls for left hand",
                        checked = uiState.preferences.leftHandedMode,
                        onCheckedChange = { viewModel.toggleLeftHanded(it) },
                        testTag = "toggle_left_handed"
                    )
                }

                // Cloud & Storage Section
                SettingsSection(title = "Cloud & Storage") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null, tint = CatOrangePrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cloud Synchronization", fontWeight = FontWeight.Bold)
                            Text(uiState.cloudSyncStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Legal & Policy Section
                SettingsSection(title = "Legal & Policies") {
                    OutlinedButton(
                        onClick = { viewModel.showPrivacyPolicy(true) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("privacy_policy_button")
                    ) {
                        Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Privacy Policy")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.showTerms(true) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("terms_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Terms of Service")
                    }
                }

                // Danger Zone: Delete / Reset Section
                SettingsSection(title = "Data Management") {
                    OutlinedButton(
                        onClick = { viewModel.promptReset() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CatRedMistake),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reset_data_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Game History & Reset Data")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Dialogs
        if (uiState.showResetDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissReset() },
                title = { Text("Delete All Data?", fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently clear your puzzle history, statistics, and daily streak. This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmReset() },
                        colors = ButtonDefaults.buttonColors(containerColor = CatRedMistake)
                    ) {
                        Text("Delete Everything")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.dismissReset() }) { Text("Cancel") }
                }
            )
        }

        if (uiState.showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showPrivacyPolicy(false) },
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Hidden Cats: Color Logic is committed to player privacy. We do not collect or share personal information, contacts, or location data. Game progress is stored locally on your device with optional anonymous cloud synchronization. In-app purchases are handled securely via Google Play Billing."
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.showPrivacyPolicy(false) }) { Text("Close") }
                }
            )
        }

        if (uiState.showTermsDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showTerms(false) },
                title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Hidden Cats: Color Logic is provided as-is for personal entertainment. All puzzle generation algorithms, artwork, and game logic are proprietary. Users agree to standard Google Play terms of distribution and safety."
                    )
                },
                confirmButton = {
                    Button(onClick = { viewModel.showTerms(false) }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CatOrangePrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = CatOrangePrimary),
            modifier = Modifier.testTag(testTag)
        )
    }
}
