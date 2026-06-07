package com.example.focusflow.ui.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusflow.viewmodel.FocusPhase
import com.example.focusflow.viewmodel.FocusViewModel

@Composable
fun FocusScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (state.phase) {
            FocusPhase.IDLE -> IdleContent(state, viewModel)
            FocusPhase.FOCUSING -> ActiveContent(state, viewModel, "ENFOQUE", 0xFF97E3F0)
            FocusPhase.BREAK -> ActiveContent(state, viewModel, "DESCANSO", 0xFFFFDF85)
            FocusPhase.LONG_BREAK -> ActiveContent(state, viewModel, "DESCANSO LARGO", 0xFFFFDF85)
            FocusPhase.FINISHED -> FinishedContent(state, viewModel)
        }
    }
}

@Composable
private fun IdleContent(
    state: com.example.focusflow.viewmodel.FocusUiState,
    viewModel: FocusViewModel
) {
    Text(
        text = "🌙",
        fontSize = 64.sp
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Modo Enfoque",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Concéntrate en lo que quieras lograr",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            SliderSetting("⏱ Enfoque", state.focusDuration, 10, 60) {
                viewModel.updateFocusDuration(it)
            }
            SliderSetting("☕ Descanso", state.breakDuration, 1, 15) {
                viewModel.updateBreakDuration(it)
            }
            SliderSetting("🌀 Descanso largo", state.longBreakDuration, 10, 30) {
                viewModel.updateLongBreak(it)
            }
            SliderSetting("🔄 Ciclos", state.totalCycles, 1, 6) {
                viewModel.updateCycles(it)
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = { viewModel.startFocus() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(30.dp)
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("INICIAR ENFOQUE", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label: ${value}min",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(140.dp)
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = max - min - 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActiveContent(
    state: com.example.focusflow.viewmodel.FocusUiState,
    viewModel: FocusViewModel,
    label: String,
    containerColor: Long
) {
    val progress = if (state.totalSeconds > 0) {
        state.remainingSeconds.toFloat() / state.totalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    val progressColor = when {
        progress > 0.5f -> Color(0xFF689F38)
        progress > 0.2f -> Color(0xFFFFD54F)
        else -> Color(0xFFE57373)
    }

    Spacer(modifier = Modifier.height(16.dp))
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp)
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(state.remainingSeconds),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.1.sp
            )
            if (state.phase == FocusPhase.FOCUSING) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ciclo ${state.currentCycle}/${state.totalCycles}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = { viewModel.cancelFocus() },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cancelar", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = { viewModel.pauseResume() },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(30.dp)
        ) {
            Icon(
                if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (state.isRunning) "Pausa" else "Reanudar", fontWeight = FontWeight.Bold)
        }
    }

    if (state.phase != FocusPhase.FOCUSING) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { viewModel.skipBreak() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp)
        ) {
            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Saltar descanso")
        }
    }
}

@Composable
private fun FinishedContent(
    state: com.example.focusflow.viewmodel.FocusUiState,
    viewModel: FocusViewModel
) {
    Text(
        text = "🎉",
        fontSize = 72.sp
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "¡Enfoque completado!",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Completaste ${state.totalCycles} ciclos. ¡Sigue así!",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = { viewModel.cancelFocus() },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(30.dp)
    ) {
        Text("FINALIZAR", fontWeight = FontWeight.Bold)
    }
}

private fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return String.format("%02d:%02d", min, sec)
}
