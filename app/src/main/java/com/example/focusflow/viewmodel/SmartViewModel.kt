package com.example.focusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.RutinaSugerida
import com.example.focusflow.data.repository.SmartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartViewModel @Inject constructor(
    private val smartRepository: SmartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SmartUiState>(SmartUiState.Idle)
    val uiState: StateFlow<SmartUiState> = _uiState.asStateFlow()

    fun generarRutina(prompt: String) {
        viewModelScope.launch {
            _uiState.value = SmartUiState.Loading
            val resultado = smartRepository.generarRutinaSugerida(prompt)
            if (resultado != null) {
                _uiState.value = SmartUiState.Success(resultado)
            } else {
                _uiState.value = SmartUiState.Error("No se pudo generar la rutina. Intenta de nuevo.")
            }
        }
    }

    fun aplicarRutina(rutinaSugerida: RutinaSugerida) {
        viewModelScope.launch {
            smartRepository.aplicarRutinaSugerida(rutinaSugerida)
            _uiState.value = SmartUiState.RutinaAplicada
        }
    }

    fun resetState() {
        _uiState.value = SmartUiState.Idle
    }
}

sealed class SmartUiState {
    object Idle : SmartUiState()
    object Loading : SmartUiState()
    data class Success(val rutina: RutinaSugerida) : SmartUiState()
    data class Error(val message: String) : SmartUiState()
    object RutinaAplicada : SmartUiState()
}
