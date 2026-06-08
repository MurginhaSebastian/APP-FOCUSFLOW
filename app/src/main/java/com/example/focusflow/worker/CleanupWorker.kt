package com.example.focusflow.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.focusflow.data.local.TareaDao
import com.example.focusflow.data.repository.TareaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TareaRepository,
    private val tareaDao: TareaDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Obtener todas las tareas marcadas como COMPLETED
            val completedTareas = tareaDao.getCompletedTareasSync()
            
            completedTareas.forEach { tarea ->
                // Eliminar de local y de Firebase a través del repositorio
                repository.deleteTarea(tarea)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
