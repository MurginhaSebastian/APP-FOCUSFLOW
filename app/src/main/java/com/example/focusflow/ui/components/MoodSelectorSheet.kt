package com.example.focusflow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class Mood {
    FELIZ, SERIO, ENOJADO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodSelectorSheet(
    onDismiss: () -> Unit,
    onMoodSelected: (Mood) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                text = "¿Cómo te sientes hoy?",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(24.dp))

            MoodOption(
                icon = Icons.Default.Favorite,
                label = "Feliz",
                color = Color(0xFF66BB6A),
                onClick = { onMoodSelected(Mood.FELIZ) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            MoodOption(
                icon = Icons.Default.Visibility,
                label = "Serio",
                color = Color(0xFFFFA726),
                onClick = { onMoodSelected(Mood.SERIO) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            MoodOption(
                icon = Icons.Default.Warning,
                label = "Enojado",
                color = Color(0xFFEF5350),
                onClick = { onMoodSelected(Mood.ENOJADO) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MoodOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = color,
            )
        }
    }
}
