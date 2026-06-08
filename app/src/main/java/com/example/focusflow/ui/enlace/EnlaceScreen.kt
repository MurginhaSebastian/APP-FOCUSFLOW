package com.example.focusflow.ui.enlace

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.focusflow.ui.tasks.AddChoiceDialog
import com.example.focusflow.ui.tasks.AddRutinaDialog
import com.example.focusflow.ui.tasks.AddTareaDialog
import com.example.focusflow.ui.qr.QRViewModel
import com.example.focusflow.viewmodel.HomeViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun EnlaceScreen(
    modifier: Modifier = Modifier,
    onNavigateToQR: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    qrViewModel: QRViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val qrUiState by qrViewModel.uiState.collectAsState()
    val linkedEmail by qrViewModel.linkedEmail.collectAsState(initial = null)
    val context = LocalContext.current
    
    var showAddOptions by remember { mutableStateOf(false) }
    var showAddRutinaDialog by remember { mutableStateOf(false) }
    var showAddTareaDialog by remember { mutableStateOf(false) }
    
    val qrContent = "focusflow:enlace:${state.userEmail.replace(".", "_")}"
    val qrBitmap = remember(qrContent) {
        if (state.userEmail.isNotEmpty()) {
            generateQRCode(qrContent)
        } else {
            null
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Cabecera de bienvenida
        item {
            Spacer(modifier = Modifier.height(16.dp))
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

        // Sección del QR Único
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tu Código de Enlace Único",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Usa este código para vincular tu cuenta con otros dispositivos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (qrBitmap != null) {
                        Surface(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            color = androidx.compose.ui.graphics.Color.White,
                            tonalElevation = 4.dp
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Código QR de usuario",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "ID: ${state.userId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Usuario Vinculado
        if (linkedEmail != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Vinculado con:", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = linkedEmail!!.replace("_", "."),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Acciones rápidas
        item {
            Text(
                text = "Acciones rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (linkedEmail == null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (linkedEmail == null) "Vincular Dispositivo" else "Gestionar Tareas",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (linkedEmail == null) "Escanea un QR para conectar" else "Asigna rutinas o tareas al usuario",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FloatingActionButton(
                        onClick = { 
                            if (linkedEmail == null) onNavigateToQR() 
                            else showAddOptions = true 
                        },
                        containerColor = if (linkedEmail == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(
                            imageVector = if (linkedEmail == null) Icons.Default.QrCodeScanner else Icons.Default.Add,
                            contentDescription = "Acción"
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Nota: Este código es privado. No lo compartas con personas en las que no confíes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
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

    if (showAddRutinaDialog && linkedEmail != null) {
        AddRutinaDialog(
            onDismiss = { showAddRutinaDialog = false },
            onConfirm = { name ->
                qrViewModel.assignRutina(name, linkedEmail!!)
                showAddRutinaDialog = false
            },
            title = "Asignar Rutina",
            description = "Esta rutina aparecerá en el dispositivo vinculado."
        )
    }

    if (showAddTareaDialog && linkedEmail != null) {
        AddTareaDialog(
            rutinas = qrUiState.rutinas,
            onDismiss = { showAddTareaDialog = false },
            onConfirm = { title, dueDate, rutinaId, location ->
                qrViewModel.assignTarea(title, dueDate, rutinaId, location, linkedEmail!!)
                showAddTareaDialog = false
            },
            onPickLocation = { /* Opcional */ }
        )
    }

    if (qrUiState.infoMessage != null) {
        LaunchedEffect(qrUiState.infoMessage) {
            Toast.makeText(context, qrUiState.infoMessage, Toast.LENGTH_SHORT).show()
            qrViewModel.clearInfoMessage()
        }
    }
}

private fun generateQRCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
