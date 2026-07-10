package com.kaze.player.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    object Home : Screen()

    @Serializable
    object Songs : Screen()

    @Serializable
    object Albums : Screen()

    @Serializable
    data class AlbumDetail(val albumId: Long) : Screen()

    @Serializable
    object Artists : Screen()

    @Serializable
    data class ArtistDetail(val artistId: Long) : Screen()

    @Serializable
    object Search : Screen()

    @Serializable
    object Player : Screen()

    @Serializable
    object Queue : Screen()

    @Serializable
    object Settings : Screen()
}
