package com.kaze.player.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kaze_settings")

data class UserPreferences(
    val dynamicColor: Boolean = true,
    val themeMode: String = "system",
    val defaultSpeed: Float = 1f
)

/**
 * Persists user settings with DataStore (Preferences). No SQL, no extra dependencies —
 * keeping Kaze Player lean as designed.
 */
class UserPreferencesRepository(private val context: Context) {

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            dynamicColor = prefs[KEY_DYNAMIC] ?: true,
            themeMode = prefs[KEY_THEME] ?: "system",
            defaultSpeed = prefs[KEY_SPEED] ?: 1f
        )
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME] = mode }
    }

    suspend fun setDefaultSpeed(speed: Float) {
        context.dataStore.edit { it[KEY_SPEED] = speed }
    }

    companion object {
        private val KEY_DYNAMIC = booleanPreferencesKey("dynamic_color")
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_SPEED = floatPreferencesKey("default_speed")
    }
}
