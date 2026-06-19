package com.example.focusflow.data.repository

import android.content.Context
import android.location.Geocoder
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.focusflow.data.local.TareaDao
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.worker.TareaReviewWorker
import com.example.focusflow.worker.TareaWorker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TareaRepository @Inject constructor(
    private val tareaDao: TareaDao,
    private val firebaseDatabase: FirebaseDatabase,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) {
    private val tareasRef = firebaseDatabase.getReference("tareas")
    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    fun scheduleTaskReview(tarea: Tarea) {
        val data = Data.Builder()
            .putInt("tarea_id", tarea.id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TareaReviewWorker>()
            .setInitialDelay(1, TimeUnit.HOURS)
            .setInputData(data)
            .addTag("review_${tarea.id}")
            .build()

        workManager.enqueueUniqueWork(
            "review_task_${tarea.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun scheduleTaskActivation(tarea: Tarea) {
        val dueDate = tarea.dueDate ?: return
        val currentTime = System.currentTimeMillis()
        val delay = dueDate - currentTime

        // Si es PENDING, programamos el Worker. 
        // Si el delay es negativo (ya pasó), usamos 0 para activarla de inmediato.
        if (tarea.status == Tarea.STATUS_PENDING) {
            val data = Data.Builder()
                .putInt("tarea_id", tarea.id)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<TareaWorker>()
                .setInitialDelay(maxOf(0, delay), TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("activation_${tarea.id}")
                .build()

            workManager.enqueueUniqueWork(
                "activate_task_${tarea.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    private fun syncTareaToFirebase(tarea: Tarea) {
        val userKey = authRepository.getSanitizedEmail()
        if (userKey.isNotEmpty()) {
            tareasRef.child(userKey).child(tarea.id.toString()).setValue(tarea)
        }
    }

    suspend fun fetchTareasFromFirebase() {
        val userKey = authRepository.getSanitizedEmail()
        val currentUserId = authRepository.getUserId()
        if (userKey.isNotEmpty()) {
            try {
                val snapshot = tareasRef.child(userKey).get().await()
                val firebaseTaskIds = snapshot.children.mapNotNull { it.child("id").getValue(Int::class.java) }.toSet()

                // 1. Limpiar localmente lo que ya no está en Firebase
                val localTasks = tareaDao.getAllTareasSync(currentUserId)
                localTasks.forEach { local ->
                    if (local.id !in firebaseTaskIds) {
                        tareaDao.deleteTarea(local)
                    }
                }

                // 2. Insertar o actualizar lo que viene de Firebase
                snapshot.children.forEach { child ->
                    child.getValue(Tarea::class.java)?.let { tarea ->
                        val tareaToSave = if (tarea.userId == "remote") {
                            tarea.copy(userId = currentUserId)
                        } else {
                            tarea
                        }
                        tareaDao.insertTarea(tareaToSave)
                        if (tareaToSave.status == Tarea.STATUS_PENDING) {
                            scheduleTaskActivation(tareaToSave)
                        }
                    }
                }
            } catch (e: Exception) {
                // Manejar error de descarga
            }
        }
    }

    fun startRealtimeSync() {
        val userKey = authRepository.getSanitizedEmail()
        val currentUserId = authRepository.getUserId()
        if (userKey.isEmpty()) return

        // Listener para borrado total del nodo
        tareasRef.child(userKey).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        tareaDao.getAllTareasSync(currentUserId).forEach {
                            tareaDao.deleteTarea(it)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        tareasRef.child(userKey).addChildEventListener(object : com.google.firebase.database.ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                saveSnapshot(snapshot, currentUserId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                saveSnapshot(snapshot, currentUserId)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val tareaId = snapshot.child("id").getValue(Int::class.java)
                tareaId?.let { id ->
                    CoroutineScope(Dispatchers.IO).launch {
                        tareaDao.deleteTareaById(id)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}

            private fun saveSnapshot(snapshot: DataSnapshot, userId: String) {
                CoroutineScope(Dispatchers.IO).launch {
                    snapshot.getValue(Tarea::class.java)?.let { tarea ->
                        val tareaToSave = if (tarea.userId == "remote") {
                            tarea.copy(userId = userId)
                        } else {
                            tarea
                        }
                        tareaDao.insertTarea(tareaToSave)
                        if (tareaToSave.status == Tarea.STATUS_PENDING) {
                            scheduleTaskActivation(tareaToSave)
                        }
                    }
                }
            }
        })
    }

    fun getTareasByUser(userId: String): Flow<List<Tarea>> {
        return tareaDao.getTareasByUser(userId)
    }

    fun getTareasByStatus(userId: String, status: String): Flow<List<Tarea>> {
        return tareaDao.getTareasByStatus(userId, status)
    }

    fun getDeletedTareas(userId: String): Flow<List<Tarea>> {
        return tareaDao.getDeletedTareas(userId)
    }

    fun getCompletedTareas(userId: String): Flow<List<Tarea>> {
        return tareaDao.getCompletedTareas(userId)
    }

    suspend fun getTareaById(tareaId: Int): Tarea? {
        return tareaDao.getTareaById(tareaId)
    }

    suspend fun getActiveTarea(userId: String): Tarea? {
        return tareaDao.getActiveTarea(userId)
    }

    suspend fun getNextActiveTarea(userId: String): Tarea? {
        return tareaDao.getNextActiveTarea(userId)
    }

    suspend fun createTarea(tarea: Tarea): Long {
        val id = tareaDao.insertTarea(tarea)
        val nuevaTarea = tarea.copy(id = id.toInt())
        
        // Sincronizar inmediatamente
        syncTareaToFirebase(nuevaTarea)
        
        // Programar activación si es necesario
        if (nuevaTarea.status == Tarea.STATUS_PENDING) {
            scheduleTaskActivation(nuevaTarea)
        }
        return id
    }

    suspend fun updateTarea(tarea: Tarea) {
        tareaDao.updateTarea(tarea)
        syncTareaToFirebase(tarea)
        
        when (tarea.status) {
            Tarea.STATUS_PENDING -> {
                scheduleTaskActivation(tarea)
                workManager.cancelUniqueWork("review_task_${tarea.id}")
            }
            Tarea.STATUS_ACTIVE -> {
                workManager.cancelUniqueWork("activate_task_${tarea.id}")
                // Si pasa a ACTIVE manualmente, también programamos la revisión
                scheduleTaskReview(tarea)
            }
            Tarea.STATUS_COMPLETED -> {
                workManager.cancelUniqueWork("activate_task_${tarea.id}")
                workManager.cancelUniqueWork("review_task_${tarea.id}")
            }
        }
    }

    suspend fun deleteTarea(tarea: Tarea) {
        val deletedTarea = tarea.copy(status = Tarea.STATUS_DELETED)
        updateTarea(deletedTarea)
        workManager.cancelUniqueWork("activate_task_${tarea.id}")
        workManager.cancelUniqueWork("review_task_${tarea.id}")
    }

    suspend fun restoreTarea(tarea: Tarea) {
        val restoredTarea = tarea.copy(status = Tarea.STATUS_PENDING)
        updateTarea(restoredTarea)
    }

    suspend fun permanentlyDeleteTarea(tarea: Tarea) {
        tareaDao.deleteTarea(tarea)
        val userKey = authRepository.getSanitizedEmail()
        if (userKey.isNotEmpty()) {
            tareasRef.child(userKey).child(tarea.id.toString()).removeValue()
        }
    }

    suspend fun completeTarea(tarea: Tarea) {
        val updatedTarea = tarea.copy(
            isCompleted = true,
            status = Tarea.STATUS_COMPLETED
        )
        updateTarea(updatedTarea)
    }

    suspend fun assignTareaToUser(targetUserEmail: String, tarea: Tarea) {
        val sanitizedEmail = targetUserEmail.replace(".", "_")
        if (sanitizedEmail.isNotEmpty()) {
            try {
                // Generar un ID numérico único basado en el tiempo
                val targetTaskId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
                
                // Marcamos la tarea como remota para que el receptor la tome como propia
                val tareaToSync = tarea.copy(
                    id = targetTaskId,
                    userId = "remote"
                )
                
                // Usamos el ID numérico como llave para mantener consistencia con el ChildEventListener
                tareasRef.child(sanitizedEmail).child(targetTaskId.toString()).setValue(tareaToSync).await()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun getTareasByRutina(rutinaId: Int): Flow<List<Tarea>> {
        return tareaDao.getTareasByRutina(rutinaId)
    }

    suspend fun getAddressFromCoords(latLngString: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val parts = latLngString.split(",")
                if (parts.size != 2) return@withContext latLngString

                val lat = parts[0].trim().toDouble()
                val lng = parts[1].trim().toDouble()

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)

                if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Ubicación sin nombre"
                } else {
                    "Coordenadas: $lat, $lng"
                }
            } catch (e: Exception) {
                latLngString
            }
        }
    }
}
