package com.kaze.player.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.kaze.player.data.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RepeatMode {
    OFF, ONE, ALL
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val currentSongIndex: Int = -1,
    val position: Long = 0,
    val duration: Long = 0,
    val queue: List<Song> = emptyList(),
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

class PlayerManager(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    // Keep a reference to the queue for metadata lookups
    private var currentQueue: List<Song> = emptyList()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updateCurrentSong()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentSong()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.value = _state.value.copy(shuffleEnabled = shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _state.value = _state.value.copy(
                repeatMode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            )
        }
    }

    fun connect() {
        if (controller != null) return
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(listener)
            updateCurrentSong()
            _state.value = _state.value.copy(
                isPlaying = controller?.isPlaying == true,
                shuffleEnabled = controller?.shuffleModeEnabled == true,
                repeatMode = when (controller?.repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            )
        }, MoreExecutors.directExecutor())
    }

    fun disconnect() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        controllerFuture = null
    }

    private fun updateCurrentSong() {
        val ctrl = controller ?: return
        val index = ctrl.currentMediaItemIndex
        val song = currentQueue.getOrNull(index)
        _state.value = _state.value.copy(
            currentSong = song,
            currentSongIndex = index,
            duration = ctrl.duration.coerceAtLeast(0)
        )
    }

    fun updatePosition() {
        val ctrl = controller ?: return
        _state.value = _state.value.copy(
            position = ctrl.currentPosition.coerceAtLeast(0),
            duration = ctrl.duration.coerceAtLeast(0)
        )
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        val ctrl = controller ?: return
        currentQueue = songs
        ctrl.setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0)
        ctrl.prepare()
        ctrl.play()
        _state.value = _state.value.copy(queue = songs)
    }

    fun playSongAt(index: Int) {
        controller?.seekToDefaultPosition(index)
        controller?.play()
    }

    fun playPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun next() {
        controller?.seekToNext()
    }

    fun previous() {
        controller?.seekToPrevious()
    }

    fun toggleShuffle() {
        val ctrl = controller ?: return
        ctrl.shuffleModeEnabled = !ctrl.shuffleModeEnabled
    }

    fun cycleRepeatMode() {
        val ctrl = controller ?: return
        ctrl.repeatMode = when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun addToQueue(song: Song) {
        val ctrl = controller ?: return
        ctrl.addMediaItem(song.toMediaItem())
        currentQueue = currentQueue + song
        _state.value = _state.value.copy(queue = currentQueue)
    }

    fun moveQueueItem(from: Int, to: Int) {
        val ctrl = controller ?: return
        ctrl.moveMediaItem(from, to)
        currentQueue = currentQueue.toMutableList().apply {
            add(to, removeAt(from))
        }
        _state.value = _state.value.copy(queue = currentQueue)
    }

    fun removeQueueItem(index: Int) {
        val ctrl = controller ?: return
        ctrl.removeMediaItem(index)
        currentQueue = currentQueue.toMutableList().apply { removeAt(index) }
        _state.value = _state.value.copy(queue = currentQueue)
    }
}
