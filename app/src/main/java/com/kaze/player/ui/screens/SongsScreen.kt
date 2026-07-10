package com.kaze.player.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaze.player.data.model.Song
import com.kaze.player.ui.components.SongItem
import com.kaze.player.ui.components.SongOptionsSheet
import com.kaze.player.viewmodel.LibraryViewModel
import com.kaze.player.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    viewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val playerState by playerViewModel.state.collectAsStateWithLifecycle()
    val favIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    var sheetSong by remember { mutableStateOf<Song?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Songs (${songs.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 80.dp
            )
        ) {
            itemsIndexed(songs) { index, song ->
                SongItem(
                    song = song,
                    index = index,
                    isPlaying = playerState.currentSong?.id == song.id && playerState.isPlaying,
                    isFavorite = favIds.contains(song.id),
                    onClick = {
                        playerViewModel.playQueue(songs, index)
                    },
                    onMoreClick = {
                        sheetSong = song
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(song.id)
                    }
                )
            }
        }
    }

    SongOptionsSheet(
        song = sheetSong,
        playerViewModel = playerViewModel,
        libraryViewModel = viewModel,
        onDismiss = { sheetSong = null }
    )
}
