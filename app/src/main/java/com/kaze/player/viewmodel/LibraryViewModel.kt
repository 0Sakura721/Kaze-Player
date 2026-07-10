package com.kaze.player.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaze.player.data.repository.MusicRepository
import com.kaze.player.data.model.Album
import com.kaze.player.data.model.Artist
import com.kaze.player.data.model.Song
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    val repository = MusicRepository(app)

    val songs: StateFlow<List<Song>> = repository.songs
    val albums: StateFlow<List<Album>> = repository.albums
    val artists: StateFlow<List<Artist>> = repository.artists
    val isScanning: StateFlow<Boolean> = repository.isScanning
    val scanProgress: StateFlow<Int> = repository.scanProgress

    init {
        scanLibrary()
    }

    fun scanLibrary() {
        viewModelScope.launch {
            repository.scanLibrary()
        }
    }

    fun getSongsByAlbum(albumId: Long): List<Song> = repository.getSongsByAlbum(albumId)
    fun getSongsByArtist(artistId: Long): List<Song> = repository.getSongsByArtist(artistId)
    fun getSongById(id: Long): Song? = repository.getSongById(id)
    fun search(query: String): List<Song> = repository.search(query)
    fun getRecentlyAdded(): List<Song> = repository.getRecentlyAdded()

    companion object {
        fun factory(app: Application) = viewModelFactory {
            initializer { LibraryViewModel(app) }
        }
    }
}

private inline fun viewModelFactory(crossinline initializer: () -> LibraryViewModel) =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            initializer() as T
    }
