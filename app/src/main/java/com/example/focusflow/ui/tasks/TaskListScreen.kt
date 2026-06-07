package com.example.focusflow.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusflow.data.model.Routine
import com.example.focusflow.data.model.Task
import com.example.focusflow.ui.theme.FocusFlowTheme
import com.example.focusflow.viewmodel.RoutineViewModel
import com.example.focusflow.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val taskState by taskViewModel.uiState.collectAsState()
    val routineState by routineViewModel.uiState.collectAsState()

    TaskListContent(
        activeTasks = taskState.activeTasks,
        pendingTasks = taskState.pendingTasks,
        completedTasks = taskState.completedTasks,
        allRoutines = routineState.routines,
        isLoading = taskState.isLoading,
        onAddRoutine = { name -> routineViewModel.createRoutine(name) },
        onAddTask = { title, dueDate, routineId -> taskViewModel.createTask(title, "", dueDate, routineId) },
        onComplete = { task -> taskViewModel.completeTask(task) },
        modifier = modifier
    )
}

@Composable
fun TaskListContent(
    activeTasks: List<Task>,
    pendingTasks: List<Task>,
    completedTasks: List<Task>,
    allRoutines: List<Routine>,
    isLoading: Boolean,
    onAddRoutine: (String) -> Unit,
    onAddTask: (title: String, dueDate: Long?, routineId: Int) -> Unit,
    onComplete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddOptions by remember { mutableStateOf(false) }
    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis Tareas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                FloatingActionButton(
                    onClick = { showAddOptions = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva")
                }
            }
        }

        if (activeTasks.isNotEmpty()) {
            item {
                Text(
                    text = "🟢 PRÓXIMA ACTIVIDAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.1.sp
                )
            }
            items(activeTasks) { task ->
                TaskCard(
                    task = task,
                    color = Color(0xFF97E3F0),
                    showCompleteButton = true,
                    onComplete = { onComplete(task) }
                )
            }
        }

        if (pendingTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🟡 PRÓXIMAS (${pendingTasks.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.1.sp
                )
            }
            items(pendingTasks) { task ->
                TaskCard(
                    task = task,
                    color = Color(0xFFFFDF85),
                    showCompleteButton = false,
                    onComplete = {}
                )
            }
        }

        if (completedTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🔴 FINALIZADAS (${completedTasks.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.1.sp
                )
            }
            items(completedTasks) { task ->
                TaskCard(
                    task = task,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    showCompleteButton = false,
                    onComplete = {}
                )
            }
        }

        if (!isLoading && activeTasks.isEmpty() && pendingTasks.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay tareas aún",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Presiona + para agregar una",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showAddOptions) {
        AddChoiceDialog(
            onDismiss = { showAddOptions = false },
            onAddRoutine = {
                showAddOptions = false
                showAddRoutineDialog = true
            },
            onAddTask = {
                showAddOptions = false
                showAddTaskDialog = true
            }
        )
    }

    if (showAddRoutineDialog) {
        AddRoutineDialog(
            onDismiss = { showAddRoutineDialog = false },
            onConfirm = { name ->
                onAddRoutine(name)
            }
        )
    }

    if (showAddTaskDialog && allRoutines.isNotEmpty()) {
        AddTaskDialog(
            routines = allRoutines,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, dueDate, routineId ->
                onAddTask(title, dueDate, routineId)
            }
        )
    }

    if (showAddTaskDialog && allRoutines.isEmpty()) {
        NoRoutinesDialog(
            onDismiss = {
                showAddTaskDialog = false
                showAddRoutineDialog = true
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: Task,
    color: Color,
    showCompleteButton: Boolean,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
                if (task.isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = task.dueDate?.let {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
                    } ?: "Sin horario",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (task.location.isNotBlank()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.location,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showCompleteButton) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onComplete,
                    shape = RoundedCornerShape(30.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Finalizar tarea", fontSize = 13.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TaskListScreenPreview() {
    FocusFlowTheme {
        TaskListContent(
            activeTasks = listOf(
                Task(title = "Estudiar matemáticas", status = Task.STATUS_ACTIVE, dueDate = System.currentTimeMillis() + 3600000)
            ),
            pendingTasks = listOf(
                Task(title = "Leer capítulo 5", status = Task.STATUS_PENDING, dueDate = System.currentTimeMillis() + 7200000),
                Task(title = "Hacer ejercicio", status = Task.STATUS_PENDING),
                Task(title = "Revisar correo", status = Task.STATUS_PENDING)
            ),
            completedTasks = listOf(
                Task(title = "Desayuno saludable", status = Task.STATUS_COMPLETED, isCompleted = true),
                Task(title = "Meditar 5 min", status = Task.STATUS_COMPLETED, isCompleted = true)
            ),
            allRoutines = listOf(
                Routine(name = "Mañana", userId = "1"),
                Routine(name = "Tarde", userId = "1")
            ),
            isLoading = false,
            onAddRoutine = {},
            onAddTask = { _, _, _ -> },
            onComplete = {}
        )
    }
}
