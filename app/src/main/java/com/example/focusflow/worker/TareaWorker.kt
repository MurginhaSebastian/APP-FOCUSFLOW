package com.example.focusflow.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.focusflow.R
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
        if (tareaId == -1) return Result.failure()

        return try {
            val tarea = repository.getTareaById(tareaId)
            if (tarea != null && tarea.status == Tarea.STATUS_PENDING) {
                val updatedTarea = tarea.copy(status = Tarea.STATUS_ACTIVE)
                repository.updateTarea(updatedTarea)
                
                sendNotification(updatedTarea)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(tarea: Tarea) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "TAREA_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de que este icono exista o cámbialo
            .setContentTitle("¡Tarea Activada!")
            .setContentText("Es hora de: ${tarea.title}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(tarea.id, notification)
    }
}
