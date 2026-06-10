package com.example.focusflow.worker

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.data.repository.TareaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TareaWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TareaRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tareaId = inputData.getInt("tarea_id", -1)
        Log.d("TareaWorker", "Iniciando worker para tarea: $tareaId")
        
        if (tareaId == -1) return Result.failure()

        return try {
            val tarea = repository.getTareaById(tareaId)
            if (tarea != null && tarea.status == Tarea.STATUS_PENDING) {
                Log.d("TareaWorker", "Activando tarea: ${tarea.title}")
                
                // 1. Enviar notificación primero para asegurar visibilidad
                sendNotification(tarea)
                
                // 2. Actualizar estado
                val updatedTarea = tarea.copy(status = Tarea.STATUS_ACTIVE)
                repository.updateTarea(updatedTarea)
                
                // 3. Programar revisión
                repository.scheduleTaskReview(updatedTarea)
                
                Result.success()
            } else {
                Log.d("TareaWorker", "Tarea no encontrada o ya no está pendiente")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("TareaWorker", "Error en TareaWorker", e)
            Result.retry()
        }
    }

    private fun sendNotification(tarea: Tarea) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "TAREA_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Tarea Activada!")
            .setContentText("Es hora de comenzar: ${tarea.title}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(tarea.id, notification)
        Log.d("TareaWorker", "Notificación enviada para: ${tarea.title}")
    }
}
