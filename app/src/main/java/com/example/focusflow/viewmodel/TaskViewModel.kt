package com.example.focusflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.model.Task
import com.example.focusflow.data.repository.AuthRepository
import com.example.focusflow.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskUiState(
    val activeTasks: List<Task> = emptyList(),
    val pendingTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        val userId = authRepository.getUserId()
        if (userId.isBlank()) return

        viewModelScope.launch {
            taskRepository.getTasksByUser(userId).collect { tasks ->
                _uiState.value = TaskUiState(
                    activeTasks = tasks.filter { it.status == Task.STATUS_ACTIVE },
                    pendingTasks = tasks.filter { it.status == Task.STATUS_PENDING },
                    completedTasks = tasks.filter { it.status == Task.STATUS_COMPLETED },
                    isLoading = false
                )
            }
        }
    }

    fun createTask(title: String, description: String, dueDate: Long?, routineId: Int) {
        viewModelScope.launch {
            val userId = authRepository.getUserId()
            val existingActive = taskRepository.getNextActiveTask(userId)
            val status = if (existingActive == null) Task.STATUS_ACTIVE else Task.STATUS_PENDING

            val task = Task(
                title = title,
                description = description,
                dueDate = dueDate,
                routineId = routineId,
                userId = userId,
                status = status
            )
            taskRepository.createTask(task)
        }
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.completeTask(task)
            promoteNextPending(task.userId)
        }
    }

    private suspend fun promoteNextPending(userId: String) {
        val pendingTasks = taskRepository.getTasksByStatus(userId, Task.STATUS_PENDING)
        val firstPending = pendingTasks.first().firstOrNull() ?: return
        taskRepository.updateTask(firstPending.copy(status = Task.STATUS_ACTIVE))
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }
}
