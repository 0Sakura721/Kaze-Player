package com.kaze.player.data.repository

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.content.Context
import com.kaze.player.data.model.Album
import com.kaze.player.data.model.Artist
import com.kaze.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0)
    val scanProgress: StateFlow<Int> = _scanProgress.asStateFlow()

    suspend fun scanLibrary() = withContext(Dispatchers.IO) {
        _isScanning.value = true
        try {
            val songList = loadSongs()
            _songs.value = songList
            _albums.value = buildAlbums(songList)
            _artists.value = buildArtists(songList)
            _scanProgress.value = songList.size
        } finally {
            _isScanning.value = false
        }
    }

    private fun loadSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

            while (cursor.moveToNext()) {
                val albumId = cursor.getLong(albumIdCol)
                songs.add(
                    Song(
                        id = cursor.getLong(idCol),
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown Artist",
                        album = cursor.getString(albumCol) ?: "Unknown Album",
                        albumId = albumId,
                        artistId = cursor.getLong(artistIdCol),
                        duration = cursor.getLong(durationCol),
                        data = cursor.getString(dataCol) ?: "",
                        track = cursor.getInt(trackCol),
                        dateAdded = cursor.getLong(dateAddedCol),
                        albumArtUri = getAlbumArtUri(albumId),
                        year = cursor.getInt(yearCol)
                    )
                )
            }
        }
        return songs
    }

    private fun buildAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.albumId }
            .map { (albumId, albumSongs) ->
                Album(
                    id = albumId,
                    title = albumSongs.first().album,
                    artist = albumSongs.first().artist,
                    albumArtUri = albumSongs.first().albumArtUri,
                    songCount = albumSongs.size,
                    year = albumSongs.first().year
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    private fun buildArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artistId }
            .map { (artistId, artistSongs) ->
                Artist(
                    id = artistId,
                    name = artistSongs.first().artist,
                    albumCount = artistSongs.map { it.albumId }.distinct().size,
                    songCount = artistSongs.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    fun getAlbumArtUri(albumId: Long): String {
        return ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        ).toString()
    }

    fun getSongsByAlbum(albumId: Long): List<Song> =
        _songs.value.filter { it.albumId == albumId }

    fun getSongsByArtist(artistId: Long): List<Song> =
        _songs.value.filter { it.artistId == artistId }

    fun getSongById(id: Long): Song? =
        _songs.value.find { it.id == id }

    fun search(query: String): List<Song> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return _songs.value.filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.album.lowercase().contains(q)
        }
    }

    fun getRecentlyAdded(limit: Int = 50): List<Song> =
        _songs.value.sortedByDescending { it.dateAdded }.take(limit)
}
