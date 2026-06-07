package com.example.focusflow.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.data.repository.TareaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TareaWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TareaRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tareaId = inputData.getInt("tarea_id", -1)
        if (tareaId == -1) return Result.failure()

        return try {
            val tarea = repository.getTareaById(tareaId)
            if (tarea != null && tarea.status == Tarea.STATUS_PENDING) {
                val updatedTarea = tarea.copy(status = Tarea.STATUS_ACTIVE)
                repository.updateTarea(updatedTarea)
                // Forzar sincronización inmediata para evitar que Firebase se quede atrás
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
