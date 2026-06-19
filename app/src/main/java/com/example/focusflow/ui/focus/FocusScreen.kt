package com.example.focusflow.ui.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import android.view.HapticFeedbackConstants
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusflow.R
import com.example.focusflow.ui.components.BrazucaGuide
import com.example.focusflow.ui.components.FocusFlowCard
import com.example.focusflow.ui.theme.Spacing
import com.example.focusflow.viewmodel.FocusPhase
import com.example.focusflow.viewmodel.FocusViewModel
import androidx.compose.animation.AnimatedVisibility

@Composable
fun FocusScreen(
    modifier: Modifier = Modifier,
    viewModel: FocusViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val view = LocalView.current

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            AnimatedContent(
                targetState = state.phase,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 }) togetherWith
                            (fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 4 })
                },
                label = "focus_phase",
            ) { phase ->
                when (phase) {
                    FocusPhase.IDLE -> IdleContent(state, viewModel)
                    FocusPhase.FOCUSING -> ActiveContent(state, viewModel, "ENFOQUE")
                    FocusPhase.BREAK, FocusPhase.LONG_BREAK -> ActiveContent(
                        state, viewModel,
                        if (phase == FocusPhase.LONG_BREAK) "DESCANSO LARGO" else "DESCANSO",
                    )
                    FocusPhase.FINISHED -> FinishedContent(state, viewModel)
                }
            }
        }

        BrazucaGuide(
            visible = state.showWelcomeBrazuca,
            message = "Entra en tu zona de máxima concentración y trabaja sin distracciones.",
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun IdleContent(
    state: com.example.focusflow.viewmodel.FocusUiState,
    viewModel: FocusViewModel,
) {
    val view = LocalView.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            Icons.Default.NightsStay,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "Modo Enfoque",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = "Concéntrate en lo que quieras lograr",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.xl))

        FocusFlowCard {
            Column {
                SliderSetting("Enfoque", state.focusDuration, 10, 60, Icons.Default.Timer) {
                    viewModel.updateFocusDuration(it)
                }
                SliderSetting("Descanso", state.breakDuration, 1, 15, Icons.Default.Coffee) {
                    viewModel.updateBreakDuration(it)
                }
                SliderSetting("Descanso largo", state.longBreakDuration, 10, 30, Icons.Default.NightsStay) {
                    viewModel.updateLongBreak(it)
                }
                SliderSetting("Ciclos", state.totalCycles, 1, 6, Icons.Default.Refresh) {
                    viewModel.updateCycles(it)
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Button(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                viewModel.startFocus()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("INICIAR ENFOQUE", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValueChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = "$label: ${value}min",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = min.toFloat()..max.toFloat(),
            steps = max - min - 1,
        )
    }
}

@Composable
private fun ActiveContent(
    state: com.example.focusflow.viewmodel.FocusUiState,
    viewModel: FocusViewModel,
    label: String,
) {
    val view = LocalView.current
    val progress = if (state.totalSeconds > 0) {
        state.remainingSeconds.toFloat() / state.totalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress",
    )

    val progressColor = when {
        progress > 0.5f -> MaterialTheme.colorScheme.primary
        progress > 0.2f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.height(Spacing.md))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(260.dp),
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(state.remainingSeconds),
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.phase == FocusPhase.FOCUSING) {
                    Text(
                        text = "Ciclo ${state.currentCycle}/${state.totalCycles}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.cancelFocus()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancelar", style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.pauseResume()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (state.isRunning) "Pausa" else "Reanudar",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (state.phase != FocusPhase.FOCUSING) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            OutlinedButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.skipBreak()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Saltar descanso", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun FinishedContent(
    state: com.example.focusflow.viewmodel.FocusUiState,
    viewModel: FocusViewModel,
) {
    val view = LocalView.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "¡Enfoque completado!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(
            text = "Completaste ${state.totalCycles} ciclos. ¡Sigue así!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
        Button(
            onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                viewModel.cancelFocus()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text("FINALIZAR", style = MaterialTheme.typography.labelLarge)
        }
    }
}

private fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return String.format("%02d:%02d", min, sec)
}
