package com.example.hiddencats.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hidden_cats_preferences")

data class PlayerPreferences(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val colorBlindMode: Boolean = false,
    val leftHandedMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = true,
    val hintBalance: Int = PreferencesManager.DEFAULT_HINTS,
    val adsRemoved: Boolean = false,
    val premiumUnlocked: Boolean = false,
    val playerName: String = "Cat Detective"
)

class PreferencesManager(private val context: Context) {
    companion object {
        /** Free starter hints. More come from rewarded ads or the shop. */
        const val DEFAULT_HINTS = 3

        val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        val KEY_MUSIC = booleanPreferencesKey("music_enabled")
        val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_COLOR_BLIND = booleanPreferencesKey("color_blind_mode")
        val KEY_LEFT_HANDED = booleanPreferencesKey("left_handed_mode")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val KEY_CLOUD_SYNC = booleanPreferencesKey("cloud_sync_enabled")
        val KEY_HINT_BALANCE = intPreferencesKey("hint_balance")
        val KEY_ADS_REMOVED = booleanPreferencesKey("ads_removed")
        val KEY_PREMIUM = booleanPreferencesKey("premium_unlocked")
        val KEY_PLAYER_NAME = stringPreferencesKey("player_name")
    }

    val preferencesFlow: Flow<PlayerPreferences> = context.dataStore.data.map { prefs ->
        PlayerPreferences(
            soundEnabled = prefs[KEY_SOUND] ?: true,
            musicEnabled = prefs[KEY_MUSIC] ?: true,
            hapticsEnabled = prefs[KEY_HAPTICS] ?: true,
            darkMode = prefs[KEY_DARK_MODE] ?: false,
            colorBlindMode = prefs[KEY_COLOR_BLIND] ?: false,
            leftHandedMode = prefs[KEY_LEFT_HANDED] ?: false,
            notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
            cloudSyncEnabled = prefs[KEY_CLOUD_SYNC] ?: true,
            hintBalance = prefs[KEY_HINT_BALANCE] ?: DEFAULT_HINTS,
            adsRemoved = prefs[KEY_ADS_REMOVED] ?: false,
            premiumUnlocked = prefs[KEY_PREMIUM] ?: false,
            playerName = prefs[KEY_PLAYER_NAME] ?: "Cat Detective"
        )
    }

    suspend fun setSoundEnabled(enabled: Boolean) = context.dataStore.edit { it[KEY_SOUND] = enabled }
    suspend fun setMusicEnabled(enabled: Boolean) = context.dataStore.edit { it[KEY_MUSIC] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = context.dataStore.edit { it[KEY_HAPTICS] = enabled }
    suspend fun setDarkMode(enabled: Boolean) = context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    suspend fun setColorBlindMode(enabled: Boolean) = context.dataStore.edit { it[KEY_COLOR_BLIND] = enabled }
    suspend fun setLeftHandedMode(enabled: Boolean) = context.dataStore.edit { it[KEY_LEFT_HANDED] = enabled }
    suspend fun setNotificationsEnabled(enabled: Boolean) = context.dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    suspend fun setCloudSyncEnabled(enabled: Boolean) = context.dataStore.edit { it[KEY_CLOUD_SYNC] = enabled }

    suspend fun addHints(amount: Int) = context.dataStore.edit { prefs ->
        val current = prefs[KEY_HINT_BALANCE] ?: DEFAULT_HINTS
        prefs[KEY_HINT_BALANCE] = (current + amount).coerceAtLeast(0)
    }

    suspend fun consumeHint(): Boolean {
        var consumed = false
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_HINT_BALANCE] ?: DEFAULT_HINTS
            if (current > 0) {
                prefs[KEY_HINT_BALANCE] = current - 1
                consumed = true
            }
        }
        return consumed
    }

    suspend fun setAdsRemoved(removed: Boolean) = context.dataStore.edit { it[KEY_ADS_REMOVED] = removed }
    suspend fun setPremiumUnlocked(unlocked: Boolean) = context.dataStore.edit { it[KEY_PREMIUM] = unlocked }
    suspend fun setPlayerName(name: String) = context.dataStore.edit { it[KEY_PLAYER_NAME] = name.trim().take(20) }

    suspend fun resetAll() = context.dataStore.edit { it.clear() }
}
