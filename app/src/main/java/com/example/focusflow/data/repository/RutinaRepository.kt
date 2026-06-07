package com.example.focusflow.data.repository

import com.example.focusflow.data.local.RutinaDao
import com.example.focusflow.data.model.Rutina
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
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
        if (userKey.isNotEmpty()) {
            try {
                val snapshot = rutinasRef.child(userKey).get().await()
                snapshot.children.forEach { child ->
                    child.getValue(Rutina::class.java)?.let { rutina ->
                        rutinaDao.insertRutina(rutina)
                    }
                }
            } catch (e: Exception) {
                // Manejar error de descarga
            }
        }
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
}
