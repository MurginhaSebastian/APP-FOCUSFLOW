package com.example.focusflow.ui.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.focusflow.data.model.Task
import com.example.focusflow.data.remote.WeatherInfo
import com.example.focusflow.ui.theme.FocusFlowTheme
import com.example.focusflow.viewmodel.HomeUiState
import com.example.focusflow.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshQuote()
    }

    if (state.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    HomeContent(state = state, onRefreshQuote = viewModel::refreshQuote, modifier = modifier)
}

@Composable
fun HomeContent(
    state: HomeUiState,
    onRefreshQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "¡Bienvenido, ${state.userName}!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (state.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = state.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF97E3F0)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⏰ AHORA MISMO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        letterSpacing = 0.1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (state.nextTask != null) {
                        Text(
                            text = state.nextTask!!.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        state.nextTask!!.dueDate?.let { dueDate ->
                            val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            Text(
                                text = dateFormat.format(Date(dueDate)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Text(
                            text = "Sin tarea activa",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (state.pendingTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Pendientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(state.pendingTasks) { task ->
                PendingTaskItem(task = task)
            }
        }

        item {
            Column {
                Text(
                    text = "Progreso diario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.completedTasks}/${state.totalTasks} tareas completadas",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFDF85)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💬 ${state.quote}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { onRefreshQuote() }) {
                        Text(text = "Nueva frase", fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            val weather = state.weather
            if (weather != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🌤️",
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${weather.temp}°C  ${weather.description}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Actividad sugerida:",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text(weather.suggestedActivity) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PendingTaskItem(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                task.dueDate?.let { dueDate ->
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    Text(
                        text = timeFormat.format(Date(dueDate)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "🟡",
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    FocusFlowTheme {
        HomeContent(
            state = HomeUiState(
                userName = "Sebastián",
                photoUrl = "",
                nextTask = Task(
                    title = "Estudiar matemáticas",
                    status = Task.STATUS_ACTIVE,
                    dueDate = System.currentTimeMillis() + 3600000
                ),
                pendingTasks = listOf(
                    Task(title = "Leer capítulo 5", status = Task.STATUS_PENDING, dueDate = System.currentTimeMillis() + 7200000),
                    Task(title = "Hacer ejercicio", status = Task.STATUS_PENDING, dueDate = System.currentTimeMillis() + 10800000),
                    Task(title = "Revisar correo", status = Task.STATUS_PENDING)
                ),
                totalTasks = 5,
                completedTasks = 3,
                progress = 0.6f,
                quote = "El éxito es la suma de pequeños esfuerzos repetidos día tras día.",
                weather = WeatherInfo(
                    temp = "22",
                    description = "Soleado",
                    condition = "Clear",
                    suggestedActivity = "Sal a caminar 15 minutos"
                ),
                isLoading = false
            ),
            onRefreshQuote = {}
        )
    }
}
