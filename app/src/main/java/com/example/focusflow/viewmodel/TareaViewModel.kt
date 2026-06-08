package com.example.focusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.TareaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TareaUiState(
    val activeTareas: List<Tarea> = emptyList(),
    val pendingTareas: List<Tarea> = emptyList(),
    val completedTareas: List<Tarea> = emptyList(),
    val isLoading: Boolean = true,
    val pickedLocation: String = ""
)

@HiltViewModel
class TareaViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TareaUiState())
    val uiState: StateFlow<TareaUiState> = _uiState.asStateFlow()

    init {
        loadTareas()
        startWatchdog()
    }

    private fun startWatchdog() {
        viewModelScope.launch {
            while (true) {
                val userId = authRepository.getUserId()
                if (userId.isNotBlank()) {
                    checkAndActivateOverdue()
                }
                delay(10000) // Revisar cada 10 segundos
            }
        }
    }

    private suspend fun checkAndActivateOverdue() {
        val now = System.currentTimeMillis()
        // Usamos el estado actual para evitar lecturas constantes a DB
        val toActivate = _uiState.value.pendingTareas.filter { 
            it.dueDate != null && it.dueDate <= now 
        }
        
        toActivate.forEach { tarea ->
            tareaRepository.updateTarea(tarea.copy(status = Tarea.STATUS_ACTIVE))
        }
    }

    private fun loadTareas() {
        val userId = authRepository.getUserId()
        if (userId.isBlank()) return

        // Intentar descargar nuevas tareas desde Firebase
        viewModelScope.launch {
            tareaRepository.fetchTareasFromFirebase()
        }

        viewModelScope.launch {
            tareaRepository.getTareasByUser(userId).collect { tareas ->
                _uiState.value = _uiState.value.copy(
                    activeTareas = tareas.filter { it.status == Tarea.STATUS_ACTIVE },
                    pendingTareas = tareas.filter { it.status == Tarea.STATUS_PENDING },
                    completedTareas = tareas.filter { it.status == Tarea.STATUS_COMPLETED },
                    isLoading = false
                )
                // Después de cargar el estado, verificar si hay que activar algo
                checkAndActivateOverdue()
            }
        }
    }

    fun setPickedLocation(location: String) {
        _uiState.value = _uiState.value.copy(pickedLocation = location)
    }

    fun createTarea(title: String, description: String, dueDate: Long?, rutinaId: Int, location: String = "") {
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            
            val currentTime = System.currentTimeMillis()
            val status = if (dueDate != null && dueDate > currentTime) {
                Tarea.STATUS_PENDING
            } else {
                val activeTarea = tareaRepository.getActiveTarea(userId)
                if (activeTarea == null) Tarea.STATUS_ACTIVE else Tarea.STATUS_PENDING
            }

            val tarea = Tarea(
                title = title,
                description = description,
                dueDate = dueDate,
                rutinaId = rutinaId,
                userId = userId,
                status = status,
                location = location
            )
            tareaRepository.createTarea(tarea)
            _uiState.value = _uiState.value.copy(pickedLocation = "")
        }
    }

    fun completeTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareaRepository.completeTarea(tarea)
            promoteNextPending(tarea.userId)
        }
    }

    private suspend fun promoteNextPending(userId: String) {
        val pendingTareas = tareaRepository.getTareasByStatus(userId, Tarea.STATUS_PENDING)
        val firstPending = pendingTareas.first().firstOrNull() ?: return
        tareaRepository.updateTarea(firstPending.copy(status = Tarea.STATUS_ACTIVE))
    }

    fun deleteTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareaRepository.deleteTarea(tarea)
        }
    }

    fun updateTarea(tarea: Tarea) {
        viewModelScope.launch {
            tareaRepository.updateTarea(tarea)
        }
    }
}
