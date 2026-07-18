package com.kaze.player.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaze.player.data.favorites.FavoritesRepository
import com.kaze.player.data.model.Album
import com.kaze.player.data.model.Artist
import com.kaze.player.data.model.Playlist
import com.kaze.player.data.model.Song
import com.kaze.player.data.playlist.PlaylistRepository
import com.kaze.player.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MusicRepository(app)
    private val favoritesRepository = FavoritesRepository(app)
    private val playlistRepository = PlaylistRepository(app)

    val songs: StateFlow<List<Song>> = repository.songs
    val albums: StateFlow<List<Album>> = repository.albums
    val artists: StateFlow<List<Artist>> = repository.artists
    val isScanning: StateFlow<Boolean> = repository.isScanning
    val scanProgress: StateFlow<Int> = repository.scanProgress

    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteIds: StateFlow<Set<Long>> = _favoriteIds.asStateFlow()

    private val _favorites = MutableStateFlow<List<Song>>(emptyList())
    val favorites: StateFlow<List<Song>> = _favorites.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        scanLibrary()
        viewModelScope.launch {
            favoritesRepository.favoriteIdsFlow.collect { ids ->
                _favoriteIds.value = ids
                _favorites.value = repository.songs.value.filter { ids.contains(it.id) }
            }
        }
        viewModelScope.launch {
            playlistRepository.playlistsFlow.collect { _playlists.value = it }
        }
    }

    fun scanLibrary() {
        viewModelScope.launch {
            repository.scanLibrary()
            _favorites.value = repository.songs.value
                .filter { _favoriteIds.value.contains(it.id) }
        }
    }

    fun getSongsByAlbum(albumId: Long): List<Song> = repository.getSongsByAlbum(albumId)
    fun getSongsByArtist(artistId: Long): List<Song> = repository.getSongsByArtist(artistId)
    fun getSongById(id: Long): Song? = repository.getSongById(id)
    fun search(query: String): List<Song> = repository.search(query)
    fun getRecentlyAdded(): List<Song> = repository.getRecentlyAdded()

    /**
     * Resolve a playlist's stored song ids into [Song] objects, dropping any ids that no
     * longer exist in the MediaStore library.
     */
    fun getSongsForPlaylist(playlist: Playlist): List<Song> =
        playlist.songIds.mapNotNull { getSongById(it) }

    // ---- Playlist operations ----

    suspend fun createPlaylist(name: String): Playlist = playlistRepository.createPlaylist(name)

    fun deletePlaylist(id: Long) {
        viewModelScope.launch { playlistRepository.deletePlaylist(id) }
    }

    fun renamePlaylist(id: Long, newName: String) {
        viewModelScope.launch { playlistRepository.renamePlaylist(id, newName) }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch { playlistRepository.addSong(playlistId, songId) }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch { playlistRepository.removeSong(playlistId, songId) }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(id) }
    }

    fun isFavorite(id: Long): Boolean = _favoriteIds.value.contains(id)

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                LibraryViewModel(app) as T
        }
    }
}
