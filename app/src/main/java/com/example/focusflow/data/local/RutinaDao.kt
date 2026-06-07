package com.example.focusflow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.focusflow.data.model.Rutina
import kotlinx.coroutines.flow.Flow

@Dao
interface RutinaDao {

    @Query("SELECT * FROM rutinas WHERE userId = :userId ORDER BY id DESC")
    fun getRutinasByUser(userId: String): Flow<List<Rutina>>

    @Query("SELECT * FROM rutinas WHERE id = :rutinaId")
    suspend fun getRutinaById(rutinaId: Int): Rutina?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRutina(rutina: Rutina): Long

    @Update
    suspend fun updateRutina(rutina: Rutina)

    @Delete
    suspend fun deleteRutina(rutina: Rutina)

    @Query("DELETE FROM rutinas WHERE id = :rutinaId")
    suspend fun deleteRutinaById(rutinaId: Int)
}
