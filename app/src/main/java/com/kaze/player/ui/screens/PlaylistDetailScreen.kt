package com.kaze.player.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaze.player.data.model.Song
import com.kaze.player.ui.components.AlbumArt
import com.kaze.player.ui.components.SongOptionsSheet
import com.kaze.player.viewmodel.LibraryViewModel
import com.kaze.player.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues
) {
    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()
    val favIds by libraryViewModel.favoriteIds.collectAsStateWithLifecycle()
    val playerState by playerViewModel.state.collectAsStateWithLifecycle()

    val playlist = playlists.find { it.id == playlistId }
    val songs: List<Song> = remember(playlist, libraryViewModel.songs.value) {
        playlist?.let { libraryViewModel.getSongsForPlaylist(it) } ?: emptyList()
    }

    var sheetSong by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        playlist?.name ?: "Playlist",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("This playlist is empty", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Open any song's menu and choose \"Add to playlist\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 80.dp
                )
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${songs.size} songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { playerViewModel.playQueue(songs, 0) },
                            modifier = Modifier.clip(RoundedCornerShape(50))
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Play all",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                itemsIndexed(songs) { index, song ->
                    SongItem(
                        song = song,
                        index = index,
                        isPlaying = playerState.currentSong?.id == song.id && playerState.isPlaying,
                        isFavorite = favIds.contains(song.id),
                        onClick = { playerViewModel.playQueue(songs, index) },
                        onMoreClick = { sheetSong = song },
                        onToggleFavorite = { libraryViewModel.toggleFavorite(song.id) }
                    )
                }
            }
        }
    }

    SongOptionsSheet(
        song = sheetSong,
        playerViewModel = playerViewModel,
        libraryViewModel = libraryViewModel,
        onDismiss = { sheetSong = null },
        onRemoveFromPlaylist = {
            sheetSong?.let { libraryViewModel.removeSongFromPlaylist(playlistId, it.id) }
        }
    )
}
