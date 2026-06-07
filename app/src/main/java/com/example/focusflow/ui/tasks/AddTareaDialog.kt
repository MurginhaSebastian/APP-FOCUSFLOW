package com.example.focusflow.ui.tasks

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.focusflow.data.model.Rutina
import com.example.focusflow.ui.theme.FocusPrimary
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTareaDialog(
    rutinas: List<Rutina>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, dueDate: Long?, rutinaId: Int, location: String) -> Unit,
    onPickLocation: () -> Unit,
    initialLocation: String = "" // Añadido para recibir la ubicación del ViewModel
) {
    var title by remember { mutableStateOf("") }
    var selectedRutina by remember { mutableStateOf<Rutina?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    // Sincronizar la ubicación interna con la recibida
    var location by remember(initialLocation) { mutableStateOf(initialLocation) }
    
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedRutina != null) {
                        val dueDate = selectedTime?.let { (h, m) ->
                            val now = Calendar.getInstance()
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, h)
                                set(Calendar.MINUTE, m)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            
                            // Si la hora seleccionada ya pasó hace más de 1 minuto, programar para mañana
                            // Esto evita que tareas creadas para el minuto actual se pasen a mañana por segundos
                            if (cal.timeInMillis < now.timeInMillis - 60000) {
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                            }
                            cal.timeInMillis
                        }
                        onConfirm(title.trim(), dueDate, selectedRutina!!.id, location)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && selectedRutina != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Guardar tarea")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                text = "Nueva tarea",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la tarea") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FocusPrimary,
                        focusedLabelColor = FocusPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRutina?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rutina") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusPrimary,
                            focusedLabelColor = FocusPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        rutinas.forEach { rutina ->
                            DropdownMenuItem(
                                text = { Text(rutina.name) },
                                onClick = {
                                    selectedRutina = rutina
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedTime?.let { formatTime(it.first, it.second) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora") },
                        trailingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val now = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        selectedTime = Pair(hour, minute)
                                    },
                                    now.get(Calendar.HOUR_OF_DAY),
                                    now.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FocusPrimary,
                            focusedLabelColor = FocusPrimary
                        ),
                        enabled = false
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onPickLocation,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FocusPrimary
                        )
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Text("  Ubicación")
                    }
                }
                
                if (location.isNotEmpty()) {
                    Text(
                        text = "📍 $location",
                        style = MaterialTheme.typography.bodySmall,
                        color = FocusPrimary,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1
                    )
                }
            }
        }
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val h = if (hour < 10) "0$hour" else "$hour"
    val m = if (minute < 10) "0$minute" else "$minute"
    return "$h:$m"
}
