package com.example.focusflow.ui.tasks

import android.view.HapticFeedbackConstants
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusflow.data.model.Rutina
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.ui.components.BadgeStatus
import com.example.focusflow.ui.components.EmptyState
import com.example.focusflow.ui.components.FocusFlowCard
import com.example.focusflow.ui.components.StatusBadge
import com.example.focusflow.ui.theme.Spacing
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
    rutinaViewModel: RutinaViewModel = hiltViewModel(),
) {
    val tareaState by tareaViewModel.uiState.collectAsState()
    val rutinaState by rutinaViewModel.uiState.collectAsState()
    val view = LocalView.current
    var showAddOptions by remember { mutableStateOf(false) }
    var showAddRutinaDialog by remember { mutableStateOf(false) }
    var showAddTareaDialog by remember { mutableStateOf(false) }
    var selectedRutinaForTask by remember { mutableStateOf<Rutina?>(null) }
    val allRutinas = rutinaState.rutinas

    val allActiveTareas = tareaState.activeTareas
    val allPendingTareas = tareaState.pendingTareas
    val allCompletedTareas = tareaState.completedTareas

    val tareasByRutina: Map<Int, List<Tarea>> = remember(allActiveTareas, allPendingTareas, allCompletedTareas) {
        (allActiveTareas + allPendingTareas + allCompletedTareas).groupBy { it.rutinaId }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xmd),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Mis Tareas",
                    style = MaterialTheme.typography.headlineMedium,
                )
                FloatingActionButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        showAddOptions = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva")
                }
            }
        }

        if (allRutinas.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.CreateNewFolder,
                    title = "No hay rutinas aún",
                    subtitle = "Presiona + para crear tu primera rutina",
                )
            }
        } else {
            allRutinas.forEach { rutina ->
                val tareasDeRutina = tareasByRutina[rutina.id] ?: emptyList()
                val active = tareasDeRutina.filter { it.status == Tarea.STATUS_ACTIVE }
                val pending = tareasDeRutina.filter { it.status == Tarea.STATUS_PENDING }
                val completed = tareasDeRutina.filter { it.status == Tarea.STATUS_COMPLETED }

                item(key = "rutina_header_${rutina.id}") {
                    FocusFlowCard(
                        onClick = {
                            selectedRutinaForTask = rutina
                            showAddTareaDialog = true
                        },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rutina.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (tareasDeRutina.isEmpty()) "Vacía. Toca para agregar tareas"
                                    else "${active.size} activas · ${pending.size} pendientes · ${completed.size} completadas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = "Agregar tarea",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                items(active, key = { "tarea_${it.id}" }) { tarea ->
                    TareaCardIndented(
                        tarea = tarea,
                        badge = "Activa",
                        badgeStatus = BadgeStatus.ACTIVE,
                        showCompleteButton = true,
                        onComplete = { tareaViewModel.completeTarea(tarea) },
                    )
                }

                items(pending, key = { "tarea_${it.id}" }) { tarea ->
                    TareaCardIndented(
                        tarea = tarea,
                        badge = "Pendiente",
                        badgeStatus = BadgeStatus.PENDING,
                        showCompleteButton = false,
                        onComplete = {},
                    )
                }

                items(completed, key = { "tarea_${it.id}" }) { tarea ->
                    TareaCardIndented(
                        tarea = tarea,
                        badge = "Completada",
                        badgeStatus = BadgeStatus.COMPLETED,
                        showCompleteButton = false,
                        onComplete = {},
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
                // Abrimos el diálogo de tarea sin una rutina pre-seleccionada
                selectedRutinaForTask = allRutinas.firstOrNull()
                showAddTareaDialog = true
            }
        )
    }

    if (showAddRutinaDialog) {
        AddRutinaDialog(
            onDismiss = { showAddRutinaDialog = false },
            onConfirm = { name ->
                rutinaViewModel.createRutina(name)
                showAddRutinaDialog = false
            },
        )
    }

    val rutinaParaTarea = selectedRutinaForTask
    if (showAddTareaDialog && rutinaParaTarea != null) {
        AddTareaDialog(
            rutinas = allRutinas,
            initialRutina = rutinaParaTarea,
            onDismiss = {
                showAddTareaDialog = false
                selectedRutinaForTask = null
            },
            onConfirm = { title, dueDate, rutinaId, location ->
                tareaViewModel.createTarea(title, "", dueDate, rutinaId, location)
                showAddTareaDialog = false
                selectedRutinaForTask = null
            },
            onPickLocation = onPickLocation,
            initialLocation = tareaState.pickedLocation,
        )
    }
}

@Composable
private fun TareaCardIndented(
    tarea: Tarea,
    badge: String,
    badgeStatus: BadgeStatus,
    showCompleteButton: Boolean,
    onComplete: () -> Unit,
) {
    val view = LocalView.current
    Row(modifier = Modifier.padding(start = 12.dp)) {
        Surface(
            modifier = Modifier
                .width(3.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when (badgeStatus) {
                BadgeStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                BadgeStatus.PENDING -> MaterialTheme.colorScheme.tertiary
                BadgeStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                BadgeStatus.INFO -> MaterialTheme.colorScheme.secondary
            },
        ) {}
        Spacer(modifier = Modifier.width(Spacing.sm))
        FocusFlowCard(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tarea.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (tarea.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                StatusBadge(text = badge, status = badgeStatus)
                if (tarea.isCompleted) {
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            if (tarea.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = tarea.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (tarea.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tarea.dueDate?.let {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
                    } ?: "Sin horario",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (tarea.location.isNotBlank()) {
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tarea.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }

            if (showCompleteButton) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                FilledTonalButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        onComplete()
                    },
                    shape = MaterialTheme.shapes.small,
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Finalizar tarea", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
