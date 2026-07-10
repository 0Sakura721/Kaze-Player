package com.kaze.player.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "kaze_favorites")

/**
 * Stores favorite song ids (MediaStore ids) as a persisted string set via DataStore.
 * Lean and dependency-free, consistent with the rest of the app.
 */
class FavoritesRepository(private val context: Context) {

    val favoriteIdsFlow: Flow<Set<Long>> = context.dataStore.data.map { prefs ->
        prefs[KEY_FAVS]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
    }

    suspend fun toggleFavorite(id: Long) {
        context.dataStore.edit { prefs ->
            val set = prefs[KEY_FAVS]?.toMutableSet() ?: mutableSetOf()
            if (!set.add(id.toString())) set.remove(id.toString())
            prefs[KEY_FAVS] = set
        }
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) {
        context.dataStore.edit { prefs ->
            val set = prefs[KEY_FAVS]?.toMutableSet() ?: mutableSetOf()
            if (favorite) set.add(id.toString()) else set.remove(id.toString())
            prefs[KEY_FAVS] = set
        }
    }

    companion object {
        private val KEY_FAVS = stringSetPreferencesKey("favorite_ids")
    }
}
