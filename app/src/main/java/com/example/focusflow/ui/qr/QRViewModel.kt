package com.example.focusflow.ui.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Rutina
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.RutinaRepository
import com.example.focusflow.data.repository.TareaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QRUiState(
    val scannedEmail: String? = null,
    val rutinas: List<Rutina> = emptyList(),
    val isAssigning: Boolean = false,
    val success: Boolean = false,
    val infoMessage: String? = null
)

@HiltViewModel
class QRViewModel @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val rutinaRepository: RutinaRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: com.example.focusflow.data.repository.SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QRUiState())
    val uiState: StateFlow<QRUiState> = _uiState.asStateFlow()

    val linkedEmail = settingsRepository.linkedUserEmail

    init {
        viewModelScope.launch {
            linkedEmail.collect { email ->
                if (email != null) {
                    fetchRemoteRutinas(email)
                } else {
                    loadLocalRutinas()
                }
            }
        }
    }

    private fun loadLocalRutinas() {
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            if (userId.isNotEmpty()) {
                rutinaRepository.getRutinasByUser(userId).collect { rutinas ->
                    _uiState.value = _uiState.value.copy(rutinas = rutinas)
                }
            }
        }
    }

    private suspend fun fetchRemoteRutinas(email: String) {
        val remoteRutinas = rutinaRepository.getRutinasFromFirebase(email)
        _uiState.value = _uiState.value.copy(rutinas = remoteRutinas)
    }

    fun setScannedEmail(email: String) {
        viewModelScope.launch {
            settingsRepository.setLinkedUserEmail(email)
            _uiState.value = _uiState.value.copy(
                success = true,
                infoMessage = "Cuenta vinculada con éxito"
            )
        }
    }

    fun assignTarea(title: String, dueDate: Long?, rutinaId: Int, location: String, targetEmail: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAssigning = true)
            
            // La tarea se crea para el usuario destino
            val tarea = Tarea(
                title = title,
                description = "Asignada por ${authRepository.getUserName()}",
                dueDate = dueDate,
                rutinaId = rutinaId,
                location = location,
                status = Tarea.STATUS_PENDING,
                userId = "remote" // El receptor actualizará esto al descargarla
            )
            
            tareaRepository.assignTareaToUser(targetEmail, tarea)
            
            _uiState.value = _uiState.value.copy(
                isAssigning = false,
                success = true,
                infoMessage = "¡Tarea enviada con éxito!"
            )
        }
    }
    
    fun resetSuccess() {
        _uiState.value = _uiState.value.copy(success = false)
    }

    fun createRutina(name: String) {
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            if (userId.isNotEmpty()) {
                rutinaRepository.createRutina(Rutina(name = name, userId = userId))
            }
        }
    }

    fun assignRutina(name: String, targetEmail: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAssigning = true)
            val rutina = Rutina(
                name = name,
                userId = "remote" // El receptor la actualizará al descargarla
            )
            rutinaRepository.assignRutinaToUser(targetEmail, rutina)
            
            // Refrescar la lista de rutinas remotas para que aparezca la nueva
            fetchRemoteRutinas(targetEmail)

            _uiState.value = _uiState.value.copy(
                isAssigning = false,
                success = true,
                infoMessage = "¡Rutina enviada con éxito!"
            )
        }
    }

    fun showInfoMessage(message: String) {
        _uiState.value = _uiState.value.copy(infoMessage = message)
    }

    fun clearInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }
}
