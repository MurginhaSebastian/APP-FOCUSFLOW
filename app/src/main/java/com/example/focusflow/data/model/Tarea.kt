package com.example.focusflow.data.model

import com.google.firebase.database.PropertyName
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tareas")
data class Tarea(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    
    @get:PropertyName("completed")
    @set:PropertyName("completed")
    var isCompleted: Boolean = false,

    val status: String = STATUS_PENDING,
    val dueDate: Long? = null,
    val location: String = "",
    val rutinaId: Int = 0,
    val userId: String = ""
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_COMPLETED = "COMPLETED"
    }
}
