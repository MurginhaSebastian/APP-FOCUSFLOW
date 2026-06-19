package com.example.focusflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.TareaRepository
import com.example.focusflow.data.repository.RutinaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StatsUiState(
    val totalCompleted: Int = 0,
    val totalGoal: Int = 300, // Ejemplo como en la imagen
    val streakDays: Int = 0,
    val weeklyData: List<Int> = List(7) { 0 },
    val focusDistribution: Map<String, Int> = emptyMap(),
    val deletedTareas: List<Tarea> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    application: Application,
    private val tareaRepository: TareaRepository,
    private val authRepository: AuthRepository,
    private val rutinaRepository: RutinaRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        val userId = authRepository.getUserId()
        if (userId.isBlank()) return

        val streak = StreakTracker.getStreak(getApplication())

        viewModelScope.launch {
            combine(
                tareaRepository.getTareasByUser(userId),
                tareaRepository.getDeletedTareas(userId),
                rutinaRepository.getRutinasByUser(userId)
            ) { allTareas, deleted, rutinas ->
                
                val completed = allTareas.count { it.status == Tarea.STATUS_COMPLETED }
                
                // Distribución semanal (ejemplo simplificado)
                val weekly = IntArray(7) { 0 }
                val cal = Calendar.getInstance()
                allTareas.filter { it.status == Tarea.STATUS_COMPLETED && it.dueDate != null }.forEach {
                    cal.timeInMillis = it.dueDate!!
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-6
                    if (dayOfWeek in 0..6) {
                        weekly[dayOfWeek]++
                    }
                }

                // Distribución por Rutina (Categoría)
                val distribution = rutinas.associate { rutina ->
                    rutina.name to allTareas.count { it.rutinaId == rutina.id }
                }.filter { it.value > 0 }

                StatsUiState(
                    totalCompleted = completed,
                    streakDays = streak,
                    weeklyData = weekly.toList(),
                    focusDistribution = distribution,
                    deletedTareas = deleted,
                    isLoading = false
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun restoreTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareaRepository.restoreTarea(tarea)
        }
    }

    fun permanentlyDeleteTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareaRepository.permanentlyDeleteTarea(tarea)
        }
    }
}
