package com.example.focusflow.ui.tasks

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.focusflow.data.model.Rutina
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTareaDialog(
    rutinas: List<Rutina>,
    initialRutina: Rutina? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, dueDate: Long?, rutinaId: Int, location: String) -> Unit,
    onPickLocation: () -> Unit,
    initialLocation: String = "",
    // Nuevos campos para mantener el estado
    initialTitle: String = "",
    initialTime: Pair<Int, Int>? = null,
    onTitleChange: (String) -> Unit = {},
    onRutinaChange: (Rutina) -> Unit = {},
    onTimeChange: (Pair<Int, Int>) -> Unit = {},
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var selectedRutina by remember(initialRutina) { mutableStateOf(initialRutina) }
    var expanded by remember { mutableStateOf(false) }
    var selectedTime by remember(initialTime) { mutableStateOf(initialTime) }
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
                            if (cal.timeInMillis < now.timeInMillis - 60000) {
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                            }
                            cal.timeInMillis
                        }
                        onConfirm(title.trim(), dueDate, selectedRutina!!.id, location)
                    }
                },
                enabled = title.isNotBlank() && selectedRutina != null,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Guardar tarea")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                text = "Nueva tarea",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it 
                        onTitleChange(it)
                    },
                    label = { Text("Nombre de la tarea") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedRutina?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rutina") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        rutinas.forEach { rutina ->
                            DropdownMenuItem(
                                text = { Text(rutina.name) },
                                onClick = {
                                    selectedRutina = rutina
                                    onRutinaChange(rutina)
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedTime?.let { formatTime(it.first, it.second) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora") },
                            trailingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = "Seleccionar hora")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    val now = Calendar.getInstance()
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            selectedTime = Pair(hour, minute)
                                            onTimeChange(Pair(hour, minute))
                                        },
                                        selectedTime?.first ?: now.get(Calendar.HOUR_OF_DAY),
                                        selectedTime?.second ?: now.get(Calendar.MINUTE),
                                        true,
                                    ).show()
                                }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onPickLocation,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ubicación")
                    }
                }

                if (location.isNotEmpty()) {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                    )
                }
            }
        },
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val h = if (hour < 10) "0$hour" else "$hour"
    val m = if (minute < 10) "0$minute" else "$minute"
    return "$h:$m"
}
