package com.kaze.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaze.player.data.model.Song
import com.kaze.player.viewmodel.LibraryViewModel
import com.kaze.player.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch

/**
 * Reusable bottom-sheet with per-song actions, inspired by the song context menus found in
 * players like Salt Player. Handles play-next, queue, playlist assignment (incl. create new),
 * and favorite toggling. Pass [onRemoveFromPlaylist] on screens that show a playlist's contents.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsSheet(
    song: Song?,
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel,
    onDismiss: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val playlists by libraryViewModel.playlists.collectAsStateWithLifecycle()
    val favoriteIds by libraryViewModel.favoriteIds.collectAsStateWithLifecycle()

    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    if (song != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showPlaylistPicker = false
                onDismiss()
            },
            sheetState = sheetState
        ) {
            if (showPlaylistPicker) {
                PlaylistPicker(
                    playlists = playlists,
                    onPick = { playlist ->
                        libraryViewModel.addSongToPlaylist(playlist.id, song.id)
                        showPlaylistPicker = false
                        onDismiss()
                    },
                    onNewPlaylist = {
                        showPlaylistPicker = false
                        showNewPlaylistDialog = true
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    val isFav = favoriteIds.contains(song.id)
                    SheetItem(
                        icon = Icons.Filled.PlayArrow,
                        text = "Play next",
                        onClick = {
                            playerViewModel.playNext(song)
                            onDismiss()
                        }
                    )
                    SheetItem(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        text = "Add to queue",
                        onClick = {
                            playerViewModel.addToQueue(song)
                            onDismiss()
                        }
                    )
                    SheetItem(
                        icon = Icons.Filled.PlaylistAdd,
                        text = "Add to playlist",
                        onClick = { showPlaylistPicker = true }
                    )
                    SheetItem(
                        icon = if (isFav) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                        text = if (isFav) "Remove from favorites" else "Add to favorites",
                        onClick = {
                            libraryViewModel.toggleFavorite(song.id)
                            onDismiss()
                        }
                    )
                    if (onRemoveFromPlaylist != null) {
                        SheetItem(
                            icon = Icons.Filled.Delete,
                            text = "Remove from playlist",
                            onClick = {
                                onRemoveFromPlaylist()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false },
            title = { Text("New playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val created = libraryViewModel.createPlaylist(
                                newPlaylistName.ifBlank { "My Playlist" }
                            )
                            libraryViewModel.addSongToPlaylist(created.id, song?.id ?: 0L)
                        }
                        newPlaylistName = ""
                        showNewPlaylistDialog = false
                        onDismiss()
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PlaylistPicker(
    playlists: List<com.kaze.player.data.model.Playlist>,
    onPick: (com.kaze.player.data.model.Playlist) -> Unit,
    onNewPlaylist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Add to playlist",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(playlists) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(playlist) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(text = playlist.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${playlist.songIds.size} songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNewPlaylist() }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "New playlist",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
