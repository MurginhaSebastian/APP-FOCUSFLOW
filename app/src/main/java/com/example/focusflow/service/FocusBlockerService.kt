package com.example.focusflow.service

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.focusflow.MainActivity
import kotlinx.coroutines.*

class FocusBlockerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var blockerJob: Job? = null
    private val distractingApps = mutableSetOf<String>()
    
    // Lista de respaldo de apps comúnmente distractoras
    private val defaultDistractions = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.facebook.katana",
        "com.twitter.android",
        "com.whatsapp",
        "com.google.android.youtube",
        "com.netflix.mediaclient"
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        identifyDistractingApps()
        startBlockingLoop()
        return START_STICKY
    }

    private fun identifyDistractingApps() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000L * 60 * 60 * 24 * 7 // Última semana

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime)
        
        distractingApps.clear()
        distractingApps.addAll(defaultDistractions) // Añadimos las de defecto primero

        if (stats != null) {
            val sortedStats = stats
                .filter { it.totalTimeInForeground > 0 }
                .sortedByDescending { it.totalTimeInForeground }
            
            // Añadimos las top 15 más usadas del usuario
            sortedStats.take(15).forEach { stat ->
                if (stat.packageName != packageName && !isLauncher(stat.packageName)) {
                    distractingApps.add(stat.packageName)
                }
            }
        }
        Log.d("FocusBlocker", "Apps monitoreadas: ${distractingApps.size}")
    }

    private fun isLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    private fun startBlockingLoop() {
        blockerJob?.cancel()
        blockerJob = serviceScope.launch {
            while (isActive) {
                checkForegroundApp()
                delay(1000) // Revisar cada segundo
            }
        }
    }

    private fun checkForegroundApp() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        
        // Consultamos los eventos de los últimos 5 segundos
        val usageEvents = usageStatsManager.queryEvents(time - 5000, time)
        val event = UsageEvents.Event()
        var lastAppInForeground: String? = null
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            // Guardamos el último paquete que pasó al frente
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastAppInForeground = event.packageName
            }
        }

        // LOGICA DE BLOQUEO:
        // 1. Si no detectamos ninguna app nueva al frente en este ciclo, no hacemos nada.
        // 2. Si la app al frente es FocusFlow, NO HACEMOS NADA (permite navegar dentro).
        if (lastAppInForeground == null || lastAppInForeground == packageName) {
            return
        }

        // 3. Si la app al frente es una distracción conocida, bloqueamos.
        if (distractingApps.contains(lastAppInForeground)) {
            Log.d("FocusBlocker", "Distracción detectada: $lastAppInForeground. Bloqueando...")
            bringFocusFlowToFront()
        }
    }

    private fun bringFocusFlowToFront() {
        // Usamos FLAG_ACTIVITY_REORDER_TO_FRONT para que NO reinicie la app,
        // sino que simplemente la ponga encima de lo que sea que el usuario abrió.
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("FocusBlocker", "Error al recuperar el foco: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Blocker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Bloqueador activo de distracciones"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Modo Focus Activo")
            .setContentText("Tu productividad está protegida")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "focus_blocker_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
