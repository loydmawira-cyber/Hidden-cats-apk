package com.example.hiddencats.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hiddencats.core.AppContainer
import com.example.hiddencats.core.SoundHapticsManager
import com.example.hiddencats.data.GameRepository
import com.example.hiddencats.data.local.PlayerPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: PlayerPreferences = PlayerPreferences(),
    val cloudSyncStatus: String = "Local Offline Mode",
    val showResetDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false,
    val showTermsDialog: Boolean = false
)

class SettingsViewModel(
    private val repository: GameRepository,
    private val soundHaptics: SoundHapticsManager
) : ViewModel() {

    private val _showResetDialog = MutableStateFlow(false)
    private val _showPrivacyDialog = MutableStateFlow(false)
    private val _showTermsDialog = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.preferences,
        repository.cloudSyncStatus,
        _showResetDialog,
        _showPrivacyDialog,
        _showTermsDialog
    ) { prefs, syncStatus, showReset, showPrivacy, showTerms ->
        SettingsUiState(
            preferences = prefs,
            cloudSyncStatus = syncStatus,
            showResetDialog = showReset,
            showPrivacyDialog = showPrivacy,
            showTermsDialog = showTerms
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun toggleSound(enabled: Boolean) = viewModelScope.launch { repository.setSoundEnabled(enabled) }
    fun toggleMusic(enabled: Boolean) = viewModelScope.launch { repository.setMusicEnabled(enabled) }
    fun toggleHaptics(enabled: Boolean) = viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    fun toggleDarkMode(enabled: Boolean) = viewModelScope.launch { repository.setDarkMode(enabled) }
    fun toggleColorBlind(enabled: Boolean) = viewModelScope.launch { repository.setColorBlindMode(enabled) }
    fun toggleLeftHanded(enabled: Boolean) = viewModelScope.launch { repository.setLeftHandedMode(enabled) }

    fun promptReset() { _showResetDialog.value = true }
    fun dismissReset() { _showResetDialog.value = false }
    fun confirmReset() {
        viewModelScope.launch {
            repository.resetAllData()
            _showResetDialog.value = false
        }
    }

    fun showPrivacyPolicy(show: Boolean) { _showPrivacyDialog.value = show }
    fun showTerms(show: Boolean) { _showTermsDialog.value = show }

    companion object {
        fun provideFactory(appContainer: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        repository = appContainer.gameRepository,
                        soundHaptics = appContainer.soundHapticsManager
                    ) as T
                }
            }
    }
}
