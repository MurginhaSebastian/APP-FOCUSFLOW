package com.example.focusflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userName: String = "",
    val photoUrl: String = "",
    val email: String = "",
    val streak: Int = 0,
    val isDarkMode: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val userName = authRepository.getUserName()
        val photoUrl = authRepository.getUserPhotoUrl()
        val email = authRepository.getUserEmail()
        val streak = StreakTracker.getStreak(application)

        viewModelScope.launch {
            settingsRepository.isDarkMode.collect { isDark ->
                _uiState.value = SettingsUiState(
                    userName = userName,
                    photoUrl = photoUrl,
                    email = email,
                    streak = streak,
                    isDarkMode = isDark
                )
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }
}
