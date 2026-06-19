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
    
    // Estado para el formulario de Nueva Tarea
    val pendingTaskTitle: String = "",
    val pendingTaskRutinaId: Int? = null,
    val pendingTaskTime: Pair<Int, Int>? = null,
    val pickedLocation: String = "",
    val isShowingAddDialog: Boolean = false,
    val showWelcomeBrazuca: Boolean = false
)

@HiltViewModel
class TareaViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TareaUiState())
    val uiState: StateFlow<TareaUiState> = _uiState.asStateFlow()

    private var welcomeShown = false

    init {
        loadTareas()
        startWatchdog()
    }

    fun triggerWelcomeBrazuca() {
        if (!welcomeShown) {
            welcomeShown = true
            _uiState.value = _uiState.value.copy(showWelcomeBrazuca = true)
            viewModelScope.launch {
                delay(6000)
                _uiState.value = _uiState.value.copy(showWelcomeBrazuca = false)
            }
        }
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
                checkAndActivateOverdue()
            }
        }
    }

    // Funciones para actualizar el formulario temporalmente
    fun updateDraftTask(
        title: String? = null,
        rutinaId: Int? = null,
        time: Pair<Int, Int>? = null,
        showDialog: Boolean? = null
    ) {
        _uiState.value = _uiState.value.copy(
            pendingTaskTitle = title ?: _uiState.value.pendingTaskTitle,
            pendingTaskRutinaId = rutinaId ?: _uiState.value.pendingTaskRutinaId,
            pendingTaskTime = time ?: _uiState.value.pendingTaskTime,
            isShowingAddDialog = showDialog ?: _uiState.value.isShowingAddDialog
        )
    }

    fun setPickedLocation(location: String) {
        _uiState.value = _uiState.value.copy(
            pickedLocation = location,
            isShowingAddDialog = true
        )
    }

    fun clearDraft() {
        _uiState.value = _uiState.value.copy(
            pendingTaskTitle = "",
            pendingTaskRutinaId = null,
            pendingTaskTime = null,
            pickedLocation = "",
            isShowingAddDialog = false
        )
    }

    fun createTarea(title: String, description: String, dueDate: Long?, rutinaId: Int, location: String = "") {
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            
            // Convertir coordenadas a dirección si es necesario
            val finalLocation = if (location.contains(",")) {
                tareaRepository.getAddressFromCoords(location)
            } else {
                location
            }

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
                location = finalLocation
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
