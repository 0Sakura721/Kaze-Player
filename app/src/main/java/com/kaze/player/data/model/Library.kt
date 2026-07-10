package com.kaze.player.data.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val albumArtUri: String?,
    val songCount: Int,
    val year: Int = 0
)

data class Artist(
    val id: Long,
    val name: String,
    val albumCount: Int,
    val songCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val songIds: List<Long>,
    val createdAt: Long = System.currentTimeMillis()
)
