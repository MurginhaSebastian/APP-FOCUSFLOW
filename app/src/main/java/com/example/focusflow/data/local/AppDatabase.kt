package com.example.focusflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.focusflow.data.model.Rutina
import com.example.focusflow.data.model.Tarea

@Database(entities = [Tarea::class, Rutina::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tareaDao(): TareaDao
    abstract fun rutinaDao(): RutinaDao
}
