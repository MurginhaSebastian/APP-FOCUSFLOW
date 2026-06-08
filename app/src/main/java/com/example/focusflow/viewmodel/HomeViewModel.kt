package com.example.focusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.data.remote.QuoteRepository
import com.example.focusflow.data.remote.WeatherInfo
import com.example.focusflow.data.remote.WeatherRepository
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.TareaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val photoUrl: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val nextTarea: Tarea? = null,
    val pendingTareas: List<Tarea> = emptyList(),
    val confirmCompletionTareas: List<Tarea> = emptyList(),
    val totalTareas: Int = 0,
    val completedTareas: Int = 0,
    val progress: Float = 0f,
    val quote: String = "",
    val weather: WeatherInfo? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tareaRepository: TareaRepository,
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
        val userEmail = authRepository.getUserEmail()

        _uiState.value = _uiState.value.copy(
            userName = userName,
            photoUrl = photoUrl,
            userId = userId,
            userEmail = userEmail
        )

        viewModelScope.launch {
            val quote = quoteRepository.getRandomQuote()
            _uiState.value = _uiState.value.copy(quote = quote)
        }

        if (userId.isNotBlank()) {
            viewModelScope.launch {
                tareaRepository.getTareasByUser(userId).collect { tareas ->
                    val totalTareas = tareas.size
                    val completedTareas = tareas.count { it.isCompleted }
                    val progress = if (totalTareas > 0) completedTareas.toFloat() / totalTareas else 0f
                    val currentTime = System.currentTimeMillis()
                    val oneHourInMillis = 60 * 60 * 1000L

                    val activeTareas = tareas.filter { it.status == Tarea.STATUS_ACTIVE }
                    val pendingTareas = tareas
                        .filter { it.status == Tarea.STATUS_PENDING }
                        .sortedBy { it.dueDate ?: Long.MAX_VALUE }
                    
                    // Tareas activas que llevan más de una hora (según su dueDate)
                    val confirmCompletionTareas = activeTareas.filter { 
                        val activationTime = it.dueDate ?: 0L
                        currentTime > (activationTime + oneHourInMillis)
                    }

                    _uiState.value = _uiState.value.copy(
                        totalTareas = totalTareas,
                        completedTareas = completedTareas,
                        progress = progress,
                        nextTarea = activeTareas.firstOrNull() ?: pendingTareas.firstOrNull(),
                        pendingTareas = pendingTareas.take(5),
                        confirmCompletionTareas = confirmCompletionTareas,
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

    fun completeTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareaRepository.completeTarea(tarea)
        }
    }
}
