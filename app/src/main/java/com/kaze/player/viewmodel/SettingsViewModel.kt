package com.kaze.player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaze.player.data.settings.UserPreferences
import com.kaze.player.data.settings.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repo: UserPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repo.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        repo.setDynamicColor(enabled)
    }

    fun setThemeMode(mode: String) = viewModelScope.launch {
        repo.setThemeMode(mode)
    }

    fun setDefaultSpeed(speed: Float) = viewModelScope.launch {
        repo.setDefaultSpeed(speed)
    }

    companion object {
        fun factory(repo: UserPreferencesRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SettingsViewModel(repo) as T
        }
    }
}
