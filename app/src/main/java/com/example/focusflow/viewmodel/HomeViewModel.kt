package com.example.focusflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.data.remote.QuoteRepository
import com.example.focusflow.data.remote.WeatherInfo
import com.example.focusflow.data.remote.WeatherRepository
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.TareaRepository
import com.example.focusflow.ui.components.Mood
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
    val activeTareas: List<Tarea> = emptyList(),
    val pendingTareas: List<Tarea> = emptyList(),
    val confirmCompletionTareas: List<Tarea> = emptyList(),
    val totalTareas: Int = 0,
    val completedTareas: Int = 0,
    val progress: Float = 0f,
    val quote: String = "",
    val weather: WeatherInfo? = null,
    val isLoading: Boolean = true,
    val streak: Int = 0,
    val currentMood: Mood? = null,
    val moodMessage: String? = null,
    val moodRoutine: String? = null,
    val showWeatherSuggestion: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val tareaRepository: TareaRepository,
    private val quoteRepository: QuoteRepository,
    private val weatherRepository: WeatherRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val moodPhrases = mapOf(
        Mood.SERIO to listOf(
            "Respira hondo. Cada día es una nueva oportunidad para empezar de nuevo.",
            "Tómate un momento para ti. Está bien avanzar poco a poco.",
            "No necesitas tenerlo todo resuelto. Un paso a la vez.",
        ),
        Mood.ENOJADO to listOf(
            "Cuenta hasta 10 y respira profundo. La calma volverá.",
            "Sal a caminar unos minutos, el aire fresco ayuda a despejar la mente.",
            "Escribe lo que sientes y luego déjalo ir. No todo merece tu energía.",
        ),
    )

    private val moodRoutines = mapOf(
        Mood.SERIO to "Ejercicio de respiración (5 min)",
        Mood.ENOJADO to "Meditación guiada (5 min)",
    )

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

        val context = getApplication<Application>()
        StreakTracker.updateStreak(context)
        val streak = StreakTracker.getStreak(context)
        _uiState.value = _uiState.value.copy(streak = streak)

        if (userId.isNotBlank()) {
            viewModelScope.launch {
                tareaRepository.getTareasByUser(userId).collect { tareas ->
                    val currentTime = System.currentTimeMillis()
                    val calendar = java.util.Calendar.getInstance()
                    
                    // Configurar inicio del día (00:00:00)
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    val startOfDay = calendar.timeInMillis
                    
                    // Configurar fin del día (23:59:59)
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    calendar.set(java.util.Calendar.MINUTE, 59)
                    calendar.set(java.util.Calendar.SECOND, 59)
                    calendar.set(java.util.Calendar.MILLISECOND, 999)
                    val endOfDay = calendar.timeInMillis

                    // Filtrar tareas que son para HOY (según dueDate)
                    val todayTareas = tareas.filter { it.dueDate != null && it.dueDate in startOfDay..endOfDay }
                    
                    val totalTareas = todayTareas.size
                    val completedTareas = todayTareas.count { it.isCompleted || it.status == Tarea.STATUS_COMPLETED }
                    val progress = if (totalTareas > 0) completedTareas.toFloat() / totalTareas else 0f

                    val oneHourInMillis = 60 * 60 * 1000L

                    val activeTareas = tareas.filter { it.status == Tarea.STATUS_ACTIVE }
                    val pendingTareas = tareas
                        .filter { it.status == Tarea.STATUS_PENDING }
                        .sortedBy { it.dueDate ?: Long.MAX_VALUE }

                    val confirmCompletionTareas = activeTareas.filter {
                        it.dueDate != null && it.dueDate!! <= currentTime && currentTime > (it.dueDate!! + oneHourInMillis)
                    }

                    _uiState.value = _uiState.value.copy(
                        totalTareas = totalTareas,
                        completedTareas = completedTareas,
                        progress = progress,
                        activeTareas = activeTareas.ifEmpty { pendingTareas.take(3) },
                        pendingTareas = pendingTareas.take(5),
                        confirmCompletionTareas = confirmCompletionTareas,
                        isLoading = false
                    )

                    if (_uiState.value.weather == null) {
                        updateWeather(19.4326, -99.1332)
                    }
                }
            }
        }
    }

    fun selectMood(mood: Mood) {
        _uiState.value = _uiState.value.copy(currentMood = mood)
        if (mood == Mood.FELIZ) return
        val phrases = moodPhrases[mood] ?: return
        val phrase = phrases.random()
        val routine = moodRoutines[mood]
        _uiState.value = _uiState.value.copy(moodMessage = phrase, moodRoutine = routine)
    }

    fun dismissMoodMessage() {
        _uiState.value = _uiState.value.copy(moodMessage = null, moodRoutine = null)
    }

    fun dismissWeatherSuggestion() {
        _uiState.value = _uiState.value.copy(showWeatherSuggestion = false)
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
            _uiState.value = _uiState.value.copy(
                weather = weather,
                showWeatherSuggestion = weather != null
            )
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
