package com.example.focusflow.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.viewmodel.RutinaViewModel
import com.example.focusflow.viewmodel.TareaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TareaListScreen(
    modifier: Modifier = Modifier,
    onPickLocation: () -> Unit,
    tareaViewModel: TareaViewModel = hiltViewModel(),
    rutinaViewModel: RutinaViewModel = hiltViewModel()
) {
    val tareaState by tareaViewModel.uiState.collectAsState()
    val rutinaState by rutinaViewModel.uiState.collectAsState()
    var showAddOptions by remember { mutableStateOf(false) }
    var showAddRutinaDialog by remember { mutableStateOf(false) }
    var showAddTareaDialog by remember { mutableStateOf(false) }
    val allRutinas = rutinaState.rutinas

    // Sincronizar ubicación seleccionada desde el ViewModel
    // La ubicación llega al ViewModel desde el NavGraph/MainScreen

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

        val activeTareas = tareaState.activeTareas
        val pendingTareas = tareaState.pendingTareas
        val completedTareas = tareaState.completedTareas

        if (activeTareas.isNotEmpty()) {
            item {
                Text(
                    text = "🟢 PRÓXIMA ACTIVIDAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.1.sp
                )
            }
            items(activeTareas) { tarea ->
                TareaCard(
                    tarea = tarea,
                    color = Color(0xFF97E3F0),
                    showCompleteButton = true,
                    onComplete = { tareaViewModel.completeTarea(tarea) }
                )
            }
        }

        if (pendingTareas.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🟡 PRÓXIMAS (${pendingTareas.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.1.sp
                )
            }
            items(pendingTareas) { tarea ->
                TareaCard(
                    tarea = tarea,
                    color = Color(0xFFFFDF85),
                    showCompleteButton = false,
                    onComplete = {}
                )
            }
        }

        if (completedTareas.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🔴 FINALIZADAS (${completedTareas.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.1.sp
                )
            }
            items(completedTareas) { tarea ->
                TareaCard(
                    tarea = tarea,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    showCompleteButton = false,
                    onComplete = {}
                )
            }
        }

        if (!tareaState.isLoading && activeTareas.isEmpty() && pendingTareas.isEmpty()) {
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
            onAddRutina = {
                showAddOptions = false
                showAddRutinaDialog = true
            },
            onAddTarea = {
                showAddOptions = false
                showAddTareaDialog = true
            }
        )
    }

    if (showAddRutinaDialog) {
        AddRutinaDialog(
            onDismiss = { showAddRutinaDialog = false },
            onConfirm = { name ->
                rutinaViewModel.createRutina(name)
            }
        )
    }

    if (showAddTareaDialog && allRutinas.isNotEmpty()) {
        AddTareaDialog(
            rutinas = allRutinas,
            onDismiss = { showAddTareaDialog = false },
            onConfirm = { title, dueDate, rutinaId, location ->
                tareaViewModel.createTarea(title, "", dueDate, rutinaId, location)
            },
            onPickLocation = onPickLocation,
            initialLocation = tareaState.pickedLocation // Pasar ubicación del estado
        )
    }

    if (showAddTareaDialog && allRutinas.isEmpty()) {
        NoRutinasDialog(
            onDismiss = {
                showAddTareaDialog = false
                showAddRutinaDialog = true
            }
        )
    }
}

@Composable
private fun TareaCard(
    tarea: Tarea,
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
                    text = tarea.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (tarea.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
                if (tarea.isCompleted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (tarea.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tarea.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (tarea.isCompleted) TextDecoration.LineThrough else TextDecoration.None
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
                    text = tarea.dueDate?.let {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
                    } ?: "Sin horario",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (tarea.location.isNotBlank()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tarea.location,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            if (showCompleteButton) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onComplete,
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
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
