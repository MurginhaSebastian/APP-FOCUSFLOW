package com.example.focusflow.ui.enlace

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.focusflow.ui.components.generateQRCode
import com.example.focusflow.ui.qr.QRViewModel
import com.example.focusflow.ui.tasks.AddChoiceDialog
import com.example.focusflow.ui.tasks.AddRutinaDialog
import com.example.focusflow.viewmodel.HomeViewModel

@Composable
fun EnlaceScreen(
    modifier: Modifier = Modifier,
    onNavigateToQR: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    qrViewModel: QRViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val qrUiState by qrViewModel.uiState.collectAsState()
    val linkedEmail by qrViewModel.linkedEmail.collectAsState(initial = null)

    var showAddOptions by remember { mutableStateOf(false) }
    var showAddRutinaDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val qrContent = "focusflow:enlace:${state.userEmail.replace(".", "_")}"
    val qrBitmap = remember(qrContent) {
        if (state.userEmail.isNotEmpty()) {
            generateQRCode(qrContent)
        } else {
            null
        }
    }

    LaunchedEffect(qrUiState.infoMessage) {
        qrUiState.infoMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            qrViewModel.clearInfoMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Bienvenido, ${state.userName}!",
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (state.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = state.photoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.extraLarge),
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Tu Código de Enlace Único",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Usa este código para vincular tu cuenta con otros dispositivos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (qrBitmap != null) {
                            Surface(
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(MaterialTheme.shapes.large),
                                color = Color.White,
                                tonalElevation = 4.dp,
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Código QR de usuario",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(220.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ID: ${state.userId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            if (linkedEmail != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Vinculado",
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Vinculado con:",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Text(
                                    text = linkedEmail!!.replace("_", "."),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Acciones rápidas",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (linkedEmail == null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (linkedEmail == null) "Vincular Dispositivo" else "Gestionar Tareas",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = if (linkedEmail == null) "Escanea un QR para conectar" else "Asigna rutinas o tareas al usuario",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                        ) {
                            Icon(
                                imageVector = if (linkedEmail == null) Icons.Default.QrCodeScanner else Icons.Default.Add,
                                contentDescription = if (linkedEmail == null) "Escanear QR" else "Agregar tarea",
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
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showAddOptions) {
        AddChoiceDialog(
            onDismiss = { showAddOptions = false },
            onAddRutina = {
                showAddOptions = false
                showAddRutinaDialog = true
            },
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
            description = "Esta rutina aparecerá en el dispositivo vinculado.",
        )
    }

}
