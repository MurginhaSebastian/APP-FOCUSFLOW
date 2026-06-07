package com.example.focusflow.data.repository

import com.example.focusflow.data.local.TaskDao
import com.example.focusflow.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {

    fun getTasksByUser(userId: String): Flow<List<Task>> {
        return taskDao.getTasksByUser(userId)
    }

    fun getTasksByRoutine(routineId: Int): Flow<List<Task>> {
        return taskDao.getTasksByRoutine(routineId)
    }

    fun getTasksByStatus(userId: String, status: String): Flow<List<Task>> {
        return taskDao.getTasksByStatus(userId, status)
    }

    suspend fun getTaskById(taskId: Int): Task? {
        return taskDao.getTaskById(taskId)
    }

    suspend fun getNextActiveTask(userId: String): Task? {
        return taskDao.getNextActiveTask(userId)
    }

    suspend fun createTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskCompletion(task: Task) {
        val newStatus = if (task.status == Task.STATUS_COMPLETED) {
            Task.STATUS_ACTIVE
        } else {
            Task.STATUS_COMPLETED
        }
        taskDao.updateTask(
            task.copy(
                isCompleted = !task.isCompleted,
                status = newStatus
            )
        )
    }

    suspend fun completeTask(task: Task) {
        taskDao.updateTask(
            task.copy(
                isCompleted = true,
                status = Task.STATUS_COMPLETED
            )
        )
    }
}
