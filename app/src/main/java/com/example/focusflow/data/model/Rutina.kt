package com.example.focusflow.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rutinas")
data class Rutina(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val userId: String = ""
)
