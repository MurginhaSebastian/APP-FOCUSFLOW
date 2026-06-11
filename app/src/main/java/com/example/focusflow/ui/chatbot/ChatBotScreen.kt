package com.example.focusflow.ui.chatbot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusflow.ui.components.generateQRCode
import com.example.focusflow.viewmodel.SmartUiState
import com.example.focusflow.viewmodel.SmartViewModel

private data class Rutina(
    val codigo: String,
    val especialista: String,
    val titulo: String,
    val descripcion: String,
)

private val rutinas = listOf(
    Rutina(
        "FOCUS_01", "Dra. Luna", "Rutina de enfoque suave",
        "1. Respira profundo durante 2 minutos.\n2. Ordena tu escritorio por 3 minutos.\n3. Estudia una sola tarea durante 15 minutos.\n4. Descansa 5 minutos sin celular.\n5. Marca tu avance en la app.",
    ),
    Rutina(
        "FOCUS_02", "Dr. Leo", "Rutina anti-distracción",
        "1. Elige una tarea pequeña.\n2. Guarda objetos que te distraigan.\n3. Activa un temporizador de 20 minutos.\n4. Trabaja solo en esa tarea.\n5. Al terminar, date una pausa corta.",
    ),
    Rutina(
        "FOCUS_03", "Dra. Sol", "Rutina de calma y organización",
        "1. Toma agua.\n2. Respira lentamente 5 veces.\n3. Escribe 3 cosas que debes hacer.\n4. Empieza por la tarea más fácil.\n5. Felicítate al terminar.",
    ),
    Rutina(
        "FOCUS_04", "Dr. Mateo", "Rutina Pomodoro para TDAH",
        "1. Estudia 10 minutos.\n2. Descansa 3 minutos.\n3. Repite el ciclo 2 veces.\n4. Evita cambiar de tarea.\n5. Registra cómo te sentiste.",
    ),
)

@Composable
fun ChatBotScreen(
    modifier: Modifier = Modifier,
    smartViewModel: SmartViewModel = hiltViewModel()
) {
    var prompt by remember { mutableStateOf("") }
    val uiState by smartViewModel.uiState.collectAsState()
    var selectedRutinaLocal by remember { mutableStateOf<Rutina?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Icon(
            Icons.Default.SmartToy,
            contentDescription = "Chatbot",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )

        Text(
            text = "Asistente Chompibiris",
            style = MaterialTheme.typography.headlineMedium,
        )

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("¿Cómo te sientes o qué quieres lograr?") },
            placeholder = { Text("Ej: Quiero una rutina para estudiar matemáticas") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (prompt.isNotBlank()) {
                            smartViewModel.generarRutina(prompt)
                        }
                    },
                    enabled = uiState !is SmartUiState.Loading
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        when (val state = uiState) {
            is SmartUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                Text("Generando tu rutina personalizada...")
            }
            is SmartUiState.Success -> {
                SmartRoutineContent(
                    rutina = state.rutina,
                    onAplicar = { smartViewModel.aplicarRutina(state.rutina) }
                )
            }
            is SmartUiState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
            is SmartUiState.RutinaAplicada -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        "¡Rutina aplicada con éxito! Puedes verla en la sección de Tareas.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Button(onClick = { smartViewModel.resetState() }) {
                    Text("Generar otra")
                }
            }
            else -> {
                // Mostrar rutinas clásicas (sorpresa) si no hay nada activo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "O prueba una rutina sorpresa",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { selectedRutinaLocal = rutinas.random() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null)
                        Text("  Rutina Sorpresa", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        selectedRutinaLocal?.let { rutina ->
            // ... (resto del código de la rutina sorpresa anterior)
            val qrBitmap = remember(rutina.codigo) { generateQRCode(rutina.codigo, 500) }
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR de la rutina",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                )
            }

            Text(
                text = "Código: ${rutina.codigo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = rutina.titulo,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = rutina.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
