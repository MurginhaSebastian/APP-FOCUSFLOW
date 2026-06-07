package com.example.focusflow.ui.chatbot

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private data class Rutina(
    val codigo: String,
    val especialista: String,
    val titulo: String,
    val descripcion: String
)

private val rutinas = listOf(
    Rutina("FOCUS_01", "Dra. Luna", "Rutina de enfoque suave",
        "1. Respira profundo durante 2 minutos.\n2. Ordena tu escritorio por 3 minutos.\n3. Estudia una sola tarea durante 15 minutos.\n4. Descansa 5 minutos sin celular.\n5. Marca tu avance en la app."),
    Rutina("FOCUS_02", "Dr. Leo", "Rutina anti-distracción",
        "1. Elige una tarea pequeña.\n2. Guarda objetos que te distraigan.\n3. Activa un temporizador de 20 minutos.\n4. Trabaja solo en esa tarea.\n5. Al terminar, date una pausa corta."),
    Rutina("FOCUS_03", "Dra. Sol", "Rutina de calma y organización",
        "1. Toma agua.\n2. Respira lentamente 5 veces.\n3. Escribe 3 cosas que debes hacer.\n4. Empieza por la tarea más fácil.\n5. Felicítate al terminar."),
    Rutina("FOCUS_04", "Dr. Mateo", "Rutina Pomodoro para TDAH",
        "1. Estudia 10 minutos.\n2. Descansa 3 minutos.\n3. Repite el ciclo 2 veces.\n4. Evita cambiar de tarea.\n5. Registra cómo te sentiste.")
)

@Composable
fun ChatBotScreen(modifier: Modifier = Modifier) {
    var selectedRutina by remember { mutableStateOf<Rutina?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🤖 Chatbot - Rutinas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (selectedRutina == null) {
                        "Hola, gracias por contarme cómo te sientes 😊\n\n" +
                        "He preparado una rutina sorpresa con ayuda de un especialista virtual.\n\n" +
                        "Presiona el botón para generar una rutina."
                    } else {
                        "🎁 Rutina generada por ${selectedRutina!!.especialista}\n\n" +
                        "Escanea el QR para ver los detalles o asigna la rutina como tarea."
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                selectedRutina = rutinas.random()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Casino, contentDescription = null)
            Text("  Generar rutina", modifier = Modifier.padding(start = 8.dp))
        }

        selectedRutina?.let { rutina ->
            Spacer(modifier = Modifier.height(16.dp))

            val qrBitmap = generarQR(rutina.codigo)
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR de la rutina",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Código: ${rutina.codigo}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = rutina.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = rutina.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun generarQR(texto: String): Bitmap {
    val size = 500
    val bits = QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y,
                if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}
