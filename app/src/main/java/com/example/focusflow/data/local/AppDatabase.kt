package com.example.focusflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.focusflow.data.model.Routine
import com.example.focusflow.data.model.Task

@Database(entities = [Task::class, Routine::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun routineDao(): RoutineDao
}
