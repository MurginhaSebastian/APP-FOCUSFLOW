package com.example.focusflow.ui.qr

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import androidx.compose.ui.tooling.preview.Preview
import com.example.focusflow.ui.theme.FocusFlowTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun QRScreen(
    modifier: Modifier = Modifier,
    viewModel: QRViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            handleQRResult(result.contents, viewModel)
        } else {
            Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val intArray = IntArray(bitmap.width * bitmap.height)
                    bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                    val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
                    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                    val result = MultiFormatReader().decode(binaryBitmap)
                    handleQRResult(result.text, viewModel)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo leer el QR", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (state.success) {
        LaunchedEffect(Unit) {
            // El mensaje de éxito ahora se gestiona a través de infoMessage o directamente aquí
            viewModel.resetSuccess()
        }
    }

    if (state.infoMessage != null) {
        LaunchedEffect(state.infoMessage) {
            Toast.makeText(context, state.infoMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearInfoMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vincular Dispositivo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        
        Text(
            text = "Escanea el código de otro usuario para establecer un vínculo de tareas",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val options = ScanOptions()
                options.setCaptureActivity(QRScannerActivity::class.java)
                options.setPrompt("Escanea el QR de Enlace de FocusFlow")
                options.setBeepEnabled(true)
                options.setOrientationLocked(true)
                options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                cameraLauncher.launch(options)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Text("  Escanear Enlace", modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Text("  Leer desde galería", modifier = Modifier.padding(start = 8.dp))
        }

        if (state.isAssigning) {
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}

private fun handleQRResult(content: String, viewModel: QRViewModel) {
    when {
        content.startsWith("focusflow:enlace:") -> {
            val email = content.removePrefix("focusflow:enlace:")
            viewModel.setScannedEmail(email)
        }
        content.startsWith("FOCUS_") -> {
            viewModel.showInfoMessage("Código de rutina detectado. Usa la sección de Chatbot para ver detalles.")
        }
        else -> {
            viewModel.showInfoMessage("Código QR no reconocido")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun QRScreenPreview() {
    FocusFlowTheme {
        QRScreen()
    }
}
