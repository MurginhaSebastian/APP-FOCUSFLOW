package com.example.focusflow.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusflow.data.model.Tarea
import com.example.focusflow.ui.components.FocusFlowCard
import com.example.focusflow.ui.theme.Spacing
import com.example.focusflow.viewmodel.StatsViewModel

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xmd),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = Spacing.md)
            )
        }

        // Fila superior: Progreso Circular y Stadisticas en Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Progreso Circular (Izquierda)
                FocusFlowCard(
                    modifier = Modifier.weight(1.2f).height(180.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            progress = { (state.totalCompleted.toFloat() / state.totalGoal.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.size(120.dp),
                            strokeWidth = 12.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.totalCompleted}",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "/ ${state.totalGoal}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${((state.totalCompleted.toFloat() / state.totalGoal) * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Grid de Stats (Derecha)
                Column(
                    modifier = Modifier.weight(1f).height(180.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    StatMiniCard(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${state.streakDays}",
                        label = "Días",
                        containerColor = Color(0xFFFFAB91).copy(alpha = 0.2f),
                        iconColor = Color(0xFFFF5722)
                    )
                }
            }
        }

        // Historial / Papelera (Tareas Eliminadas)
        item {
            WeeklyOverviewCard(weeklyData = state.weeklyData)
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = "Historial de Eliminadas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (state.deletedTareas.isEmpty()) {
            item {
                Text(
                    text = "No hay tareas eliminadas recientemente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.md)
                )
            }
        } else {
            items(state.deletedTareas) { tarea ->
                DeletedTareaItem(
                    tarea = tarea,
                    onRestore = { viewModel.restoreTarea(tarea) },
                    onDeletePermanent = { viewModel.permanentlyDeleteTarea(tarea) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.xl)) }
    }
}

@Composable
fun WeeklyOverviewCard(weeklyData: List<Int>) {
    FocusFlowCard {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = "Resumen Semanal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf("D", "L", "M", "X", "J", "V", "S")
                val maxVal = (weeklyData.maxOrNull() ?: 1).coerceAtLeast(1)
                
                weeklyData.forEachIndexed { index, value ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(maxOf(4.dp, (100 * value / maxVal).dp))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = days[index], style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun StatMiniCard(
    icon: ImageVector,
    value: String,
    label: String,
    containerColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column {
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DeletedTareaItem(
    tarea: Tarea,
    onRestore: () -> Unit,
    onDeletePermanent: () -> Unit
) {
    FocusFlowCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tarea.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Eliminada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDeletePermanent) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Eliminar permanentemente", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
