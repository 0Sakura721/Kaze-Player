package com.kaze.player.data.playlist

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kaze.player.data.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "kaze_playlists")

/**
 * Persists user-created playlists as a single JSON list via DataStore (Preferences).
 *
 * Lean and dependency-free beyond the existing kotlinx-serialization-json + DataStore stack —
 * no SQLite, no extra libraries. Consistent with Kaze Player's "no database" philosophy.
 */
class PlaylistRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    val playlistsFlow: Flow<List<Playlist>> = context.dataStore.data.map { prefs ->
        prefs[KEY_PLAYLISTS]?.let { raw ->
            runCatching { json.decodeFromString<List<Playlist>>(raw) }.getOrNull()
        } ?: emptyList()
    }

    private suspend fun saveAll(list: List<Playlist>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PLAYLISTS] = json.encodeToString(list)
        }
    }

    suspend fun createPlaylist(name: String): Playlist {
        val list = playlistsFlow.first().toMutableList()
        val playlist = Playlist(
            id = System.nanoTime(),
            name = name.trim().ifEmpty { "Playlist" },
            songIds = emptyList()
        )
        list.add(playlist)
        saveAll(list)
        return playlist
    }

    suspend fun deletePlaylist(id: Long) {
        saveAll(playlistsFlow.first().filter { it.id != id })
    }

    suspend fun renamePlaylist(id: Long, newName: String) {
        saveAll(
            playlistsFlow.first().map { pl ->
                if (pl.id == id) pl.copy(name = newName.trim().ifEmpty { pl.name }) else pl
            }
        )
    }

    suspend fun addSong(playlistId: Long, songId: Long) {
        saveAll(
            playlistsFlow.first().map { pl ->
                if (pl.id == playlistId && !pl.songIds.contains(songId)) {
                    pl.copy(songIds = pl.songIds + songId)
                } else {
                    pl
                }
            }
        )
    }

    suspend fun removeSong(playlistId: Long, songId: Long) {
        saveAll(
            playlistsFlow.first().map { pl ->
                if (pl.id == playlistId) pl.copy(songIds = pl.songIds - songId) else pl
            }
        )
    }

    suspend fun reorderSong(playlistId: Long, from: Int, to: Int) {
        saveAll(
            playlistsFlow.first().map { pl ->
                if (pl.id == playlistId) {
                    val ids = pl.songIds.toMutableList()
                    if (from in ids.indices && to in ids.indices && from != to) {
                        val moved = ids.removeAt(from)
                        ids.add(to, moved)
                        pl.copy(songIds = ids)
                    } else {
                        pl
                    }
                } else {
                    pl
                }
            }
        )
    }

    companion object {
        private val KEY_PLAYLISTS = stringPreferencesKey("playlists_json")
    }
}
