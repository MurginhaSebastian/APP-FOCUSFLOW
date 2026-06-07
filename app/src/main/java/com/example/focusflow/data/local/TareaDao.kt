package com.example.focusflow.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.focusflow.data.model.Tarea
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {

    @Query("SELECT * FROM tareas WHERE userId = :userId ORDER BY dueDate ASC")
    fun getTareasByUser(userId: String): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE rutinaId = :rutinaId ORDER BY dueDate ASC")
    fun getTareasByRutina(rutinaId: Int): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE id = :tareaId")
    suspend fun getTareaById(tareaId: Int): Tarea?

    @Query("SELECT * FROM tareas WHERE userId = :userId AND status = :status ORDER BY dueDate ASC")
    fun getTareasByStatus(userId: String, status: String): Flow<List<Tarea>>

    @Query("SELECT * FROM tareas WHERE userId = :userId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveTarea(userId: String): Tarea?

    @Query("SELECT * FROM tareas WHERE userId = :userId AND status != 'COMPLETED' ORDER BY dueDate ASC LIMIT 1")
    suspend fun getNextActiveTarea(userId: String): Tarea?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarea(tarea: Tarea): Long

    @Update
    suspend fun updateTarea(tarea: Tarea)

    @Delete
    suspend fun deleteTarea(tarea: Tarea)

    @Query("DELETE FROM tareas WHERE id = :tareaId")
    suspend fun deleteTareaById(tareaId: Int)
}
