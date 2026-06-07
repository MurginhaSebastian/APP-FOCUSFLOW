package com.example.focusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Routine
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineUiState(
    val routines: List<Routine> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

    init {
        loadRoutines()
    }

    private fun loadRoutines() {
        val userId = authRepository.getUserId()
        if (userId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            routineRepository.getRoutinesByUser(userId).collect { routines ->
                _uiState.value = RoutineUiState(
                    routines = routines,
                    isLoading = false
                )
            }
        }
    }

    fun createRoutine(name: String) {
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            routineRepository.createRoutine(
                Routine(name = name, userId = userId)
            )
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routine)
        }
    }
}
