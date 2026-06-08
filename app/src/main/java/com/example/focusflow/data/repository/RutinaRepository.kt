package com.example.focusflow.data.repository

import com.example.focusflow.data.local.RutinaDao
import com.example.focusflow.data.model.Rutina
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RutinaRepository @Inject constructor(
    private val rutinaDao: RutinaDao,
    private val firebaseDatabase: FirebaseDatabase,
    private val authRepository: AuthRepository
) {
    private val rutinasRef = firebaseDatabase.getReference("rutinas")

    private fun syncRutinaToFirebase(rutina: Rutina) {
        val userKey = authRepository.getSanitizedEmail()
        if (userKey.isNotEmpty()) {
            rutinasRef.child(userKey).child(rutina.id.toString()).setValue(rutina)
        }
    }

    suspend fun fetchRutinasFromFirebase() {
        val userKey = authRepository.getSanitizedEmail()
        val currentUserId = authRepository.getUserId()
        if (userKey.isNotEmpty()) {
            try {
                val snapshot = rutinasRef.child(userKey).get().await()
                snapshot.children.forEach { child ->
                    child.getValue(Rutina::class.java)?.let { rutina ->
                        val rutinaToSave = if (rutina.userId == "remote") {
                            rutina.copy(userId = currentUserId)
                        } else {
                            rutina
                        }
                        rutinaDao.insertRutina(rutinaToSave)
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

        rutinasRef.child(userKey).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    snapshot.children.forEach { child ->
                        child.getValue(Rutina::class.java)?.let { rutina ->
                            val rutinaToSave = if (rutina.userId == "remote") {
                                rutina.copy(userId = currentUserId)
                            } else {
                                rutina
                            }
                            rutinaDao.insertRutina(rutinaToSave)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getRutinasByUser(userId: String): Flow<List<Rutina>> {
        return rutinaDao.getRutinasByUser(userId)
    }

    suspend fun getRutinaById(rutinaId: Int): Rutina? {
        return rutinaDao.getRutinaById(rutinaId)
    }

    suspend fun createRutina(rutina: Rutina): Long {
        val id = rutinaDao.insertRutina(rutina)
        val newRutina = rutina.copy(id = id.toInt())
        syncRutinaToFirebase(newRutina)
        return id
    }

    suspend fun updateRutina(rutina: Rutina) {
        rutinaDao.updateRutina(rutina)
        syncRutinaToFirebase(rutina)
    }

    suspend fun deleteRutina(rutina: Rutina) {
        rutinaDao.deleteRutina(rutina)
        val userKey = authRepository.getSanitizedEmail()
        if (userKey.isNotEmpty()) {
            rutinasRef.child(userKey).child(rutina.id.toString()).removeValue()
        }
    }

    suspend fun getRutinasFromFirebase(email: String): List<Rutina> {
        val userKey = email.replace(".", "_")
        return try {
            val snapshot = rutinasRef.child(userKey).get().await()
            snapshot.children.mapNotNull { it.getValue(Rutina::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun assignRutinaToUser(targetUserEmail: String, rutina: Rutina) {
        val sanitizedEmail = targetUserEmail.replace(".", "_")
        if (sanitizedEmail.isNotEmpty()) {
            try {
                val newRef = rutinasRef.child(sanitizedEmail).push()
                val targetRutinaId = (System.currentTimeMillis() % 1000000).toInt()
                val rutinaToSync = rutina.copy(id = targetRutinaId)
                newRef.setValue(rutinaToSync).await()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
