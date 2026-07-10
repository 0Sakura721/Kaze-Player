package com.kaze.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaze.player.data.model.Song
import com.kaze.player.player.PlayerManager
import com.kaze.player.player.PlayerUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playerManager: PlayerManager
) : ViewModel() {

    val state: StateFlow<PlayerUiState> = playerManager.state

    init {
        startPositionUpdates()
    }

    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (true) {
                playerManager.updatePosition()
                delay(200)
            }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        playerManager.playQueue(songs, startIndex)
    }

    fun playSongAt(index: Int) = playerManager.playSongAt(index)
    fun playPause() = playerManager.playPause()
    fun seekTo(position: Long) = playerManager.seekTo(position)
    fun next() = playerManager.next()
    fun previous() = playerManager.previous()
    fun toggleShuffle() = playerManager.toggleShuffle()
    fun cycleRepeatMode() = playerManager.cycleRepeatMode()
    fun addToQueue(song: Song) = playerManager.addToQueue(song)
    fun moveQueueItem(from: Int, to: Int) = playerManager.moveQueueItem(from, to)
    fun removeQueueItem(index: Int) = playerManager.removeQueueItem(index)

    fun setPlaybackSpeed(speed: Float) = playerManager.setPlaybackSpeed(speed)
    fun startSleepTimer(minutes: Int) = playerManager.startSleepTimer(minutes)
    fun cancelSleepTimer() = playerManager.cancelSleepTimer()

    companion object {
        fun factory(playerManager: PlayerManager) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PlayerViewModel(playerManager) as T
        }
    }
}
