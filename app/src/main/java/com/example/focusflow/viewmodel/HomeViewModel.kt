package com.example.focusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Task
import com.example.focusflow.data.remote.QuoteRepository
import com.example.focusflow.data.remote.WeatherInfo
import com.example.focusflow.data.remote.WeatherRepository
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val photoUrl: String = "",
    val nextTask: Task? = null,
    val pendingTasks: List<Task> = emptyList(),
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val progress: Float = 0f,
    val quote: String = "",
    val weather: WeatherInfo? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository,
    private val quoteRepository: QuoteRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    private fun loadHome() {
        val userId = authRepository.getUserId()
        val userName = authRepository.getUserName()
        val photoUrl = authRepository.getUserPhotoUrl()

        _uiState.value = _uiState.value.copy(
            userName = userName,
            photoUrl = photoUrl
        )

        viewModelScope.launch {
            val quote = quoteRepository.getRandomQuote()
            _uiState.value = _uiState.value.copy(quote = quote)
        }

        if (userId.isNotBlank()) {
            viewModelScope.launch {
                taskRepository.getTasksByUser(userId).collect { tasks ->
                    val totalTasks = tasks.size
                    val completedTasks = tasks.count { it.isCompleted }
                    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
                    val activeTasks = tasks.filter { it.status == Task.STATUS_ACTIVE }
                    val pendingTasks = tasks
                        .filter { it.status == Task.STATUS_PENDING }
                        .sortedBy { it.dueDate ?: Long.MAX_VALUE }

                    _uiState.value = _uiState.value.copy(
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        progress = progress,
                        nextTask = activeTasks.firstOrNull() ?: pendingTasks.firstOrNull(),
                        pendingTasks = pendingTasks.take(5),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refreshQuote() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(quote = "")
            val quote = quoteRepository.getRandomQuote()
            _uiState.value = _uiState.value.copy(quote = quote)
        }
    }

    fun updateWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            val weather = weatherRepository.getWeather(lat, lon)
            _uiState.value = _uiState.value.copy(weather = weather)
        }
    }

    fun refresh() {
        loadHome()
    }
}
