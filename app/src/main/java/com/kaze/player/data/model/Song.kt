package com.kaze.player.data.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val artistId: Long,
    val duration: Long,
    val data: String,
    val track: Int = 0,
    val dateAdded: Long = 0,
    val albumArtUri: String? = null,
    val year: Int = 0
) {
    fun toMediaItem(): androidx.media3.common.MediaItem {
        val metadata = androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setAlbumArtist(artist)
            .build()
        return androidx.media3.common.MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(data)
            .setMediaMetadata(metadata)
            .build()
    }
}
