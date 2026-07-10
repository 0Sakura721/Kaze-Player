package com.kaze.player.player

import android.content.ComponentName
import android.content.Context
import android.os.CountDownTimer
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
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val playbackSpeed: Float = 1f,
    val sleepTimerRemainingMs: Long = 0L
)

/**
 * Bridges the Media3 [MediaController] with the UI. The active song is resolved by the
 * media item's id (the song's MediaStore id) rather than its position in the queue, so it
 * stays correct even when shuffle reorders playback internally.
 */
class PlayerManager(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    // media id (MediaStore song id) -> Song, used to resolve the active item regardless of order.
    private var songMap: Map<Long, Song> = emptyMap()

    // Default speed applied when the controller connects (comes from user settings).
    var defaultSpeed: Float = 1f

    private var sleepTimer: CountDownTimer? = null

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
            refreshQueue()
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
            controller?.setPlaybackSpeed(defaultSpeed)
            updateCurrentSong()
            _state.value = _state.value.copy(
                isPlaying = controller?.isPlaying == true,
                shuffleEnabled = controller?.shuffleModeEnabled == true,
                repeatMode = when (controller?.repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                },
                playbackSpeed = controller?.playbackSpeed ?: defaultSpeed
            )
        }, MoreExecutors.directExecutor())
    }

    fun disconnect() {
        cancelSleepTimer()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        controllerFuture = null
        songMap = emptyMap()
    }

    private fun updateCurrentSong() {
        val ctrl = controller ?: return
        val id = ctrl.currentMediaItem?.mediaId?.toLongOrNull()
        val song = id?.let { songMap[it] }
        _state.value = _state.value.copy(
            currentSong = song,
            currentSongIndex = ctrl.currentMediaItemIndex,
            duration = ctrl.duration.coerceAtLeast(0)
        )
        refreshQueue()
    }

    /**
     * Rebuild the visible queue from the controller's actual item order. This keeps the
     * queue list and the highlighted index in sync even when shuffle is enabled.
     */
    private fun refreshQueue() {
        val ctrl = controller ?: return
        val list = (0 until ctrl.mediaItemCount).mapNotNull { i ->
            ctrl.getMediaItemAt(i).mediaId.toLongOrNull()?.let { songMap[it] }
        }
        _state.value = _state.value.copy(queue = list)
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
        songMap = songs.associateBy { it.id }
        ctrl.setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0)
        ctrl.prepare()
        ctrl.play()
        refreshQueue()
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

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.25f, 2f)
        controller?.setPlaybackSpeed(clamped)
        _state.value = _state.value.copy(playbackSpeed = clamped)
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return
        val total = minutes * 60_000L
        sleepTimer = object : CountDownTimer(total, 1000) {
            override fun onTick(remaining: Long) {
                _state.value = _state.value.copy(sleepTimerRemainingMs = remaining)
            }

            override fun onFinish() {
                controller?.pause()
                _state.value = _state.value.copy(sleepTimerRemainingMs = 0L)
                sleepTimer = null
            }
        }.start()
        _state.value = _state.value.copy(sleepTimerRemainingMs = total)
    }

    fun cancelSleepTimer() {
        sleepTimer?.cancel()
        sleepTimer = null
        if (_state.value.sleepTimerRemainingMs != 0L) {
            _state.value = _state.value.copy(sleepTimerRemainingMs = 0L)
        }
    }

    fun addToQueue(song: Song) {
        val ctrl = controller ?: return
        ctrl.addMediaItem(song.toMediaItem())
        songMap = songMap + (song.id to song)
        refreshQueue()
    }

    fun moveQueueItem(from: Int, to: Int) {
        val ctrl = controller ?: return
        ctrl.moveMediaItem(from, to)
        refreshQueue()
    }

    fun removeQueueItem(index: Int) {
        val ctrl = controller ?: return
        ctrl.removeMediaItem(index)
        refreshQueue()
    }
}
