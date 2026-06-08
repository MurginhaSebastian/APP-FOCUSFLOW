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
class TareaReviewWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TareaRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tareaId = inputData.getInt("tarea_id", -1)
        if (tareaId == -1) return Result.failure()

        return try {
            val tarea = repository.getTareaById(tareaId)
            // Solo notificamos si la tarea sigue activa y no ha sido completada
            if (tarea != null && tarea.status == Tarea.STATUS_ACTIVE && !tarea.isCompleted) {
                sendReviewNotification(tarea)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendReviewNotification(tarea: Tarea) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "TAREA_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¿Finalizaste tu tarea?")
            .setContentText("Ha pasado una hora desde el inicio de: ${tarea.title}. Confirma si ya terminaste.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(tarea.id + 1000, notification)
    }
}
