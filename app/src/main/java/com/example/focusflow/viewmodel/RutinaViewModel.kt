package com.example.focusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Rutina
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.RutinaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RutinaUiState(
    val rutinas: List<Rutina> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RutinaViewModel @Inject constructor(
    private val rutinaRepository: RutinaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RutinaUiState())
    val uiState: StateFlow<RutinaUiState> = _uiState.asStateFlow()

    init {
        loadRutinas()
    }

    private fun loadRutinas() {
        val userId = authRepository.getUserId()
        if (userId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            rutinaRepository.getRutinasByUser(userId).collect { rutinas ->
                _uiState.value = RutinaUiState(
                    rutinas = rutinas,
                    isLoading = false
                )
            }
        }
    }

    fun createRutina(name: String) {
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            rutinaRepository.createRutina(
                Rutina(name = name, userId = userId)
            )
        }
    }

    fun deleteRutina(rutina: Rutina) {
        viewModelScope.launch {
            rutinaRepository.deleteRutina(rutina)
        }
    }
}
