package com.example.focusflow.data.repository

import com.example.focusflow.data.model.Rutina
import com.example.focusflow.data.model.RutinaSugerida
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.data.remote.GroqService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartRepository @Inject constructor(
    private val groqService: GroqService,
    private val rutinaRepository: RutinaRepository,
    private val tareaRepository: TareaRepository,
    private val authRepository: AuthRepository
) {
    suspend fun generarRutinaSugerida(prompt: String): RutinaSugerida? {
        return groqService.generarRutina(prompt)
    }

    suspend fun aplicarRutinaSugerida(rutinaSugerida: RutinaSugerida) {
        val userId = authRepository.getUserId()
        
        // 1. Crear la rutina
        val rutinaId = rutinaRepository.createRutina(
            Rutina(name = rutinaSugerida.nombre, userId = userId)
        )

        // 2. Crear las tareas asociadas
        rutinaSugerida.tareas.forEach { tareaSugerida ->
            tareaRepository.createTarea(
                Tarea(
                    title = tareaSugerida.titulo,
                    description = "${tareaSugerida.descripcion} (Duración: ${tareaSugerida.duracionEstimada})",
                    rutinaId = rutinaId.toInt(),
                    userId = userId,
                    status = Tarea.STATUS_ACTIVE, // Cambiado a ACTIVE para que se puedan finalizar
                    dueDate = System.currentTimeMillis() // Asignamos el momento actual como referencia
                )
            )
        }
    }
}
