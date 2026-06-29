package com.example.focusflow.viewmodel

import android.app.Application
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.repository.SettingsRepository
import com.example.focusflow.service.FocusBlockerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FocusPhase { IDLE, FOCUSING, BREAK, LONG_BREAK, FINISHED }

data class FocusUiState(
    val phase: FocusPhase = FocusPhase.IDLE,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val currentCycle: Int = 1,
    val totalCycles: Int = 4,
    val focusDuration: Int = 25,
    val breakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val isRunning: Boolean = false,
    val showWelcomeBrazuca: Boolean = false,
    val showPermissionDialog: Boolean = false
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    private var welcomeShown = false

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.focusDuration.collect { _uiState.value = _uiState.value.copy(focusDuration = it) }
        }
        viewModelScope.launch {
            settingsRepository.breakDuration.collect { _uiState.value = _uiState.value.copy(breakDuration = it) }
        }
        viewModelScope.launch {
            settingsRepository.longBreak.collect { _uiState.value = _uiState.value.copy(longBreakDuration = it) }
        }
        viewModelScope.launch {
            settingsRepository.cycles.collect { _uiState.value = _uiState.value.copy(totalCycles = it) }
        }
    }

    fun triggerWelcomeBrazuca() {
        if (!welcomeShown) {
            welcomeShown = true
            _uiState.value = _uiState.value.copy(showWelcomeBrazuca = true)
            viewModelScope.launch {
                delay(6000)
                _uiState.value = _uiState.value.copy(showWelcomeBrazuca = false)
            }
        }
    }

    fun updateFocusDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(focusDuration = minutes)
        viewModelScope.launch { settingsRepository.setFocusDuration(minutes) }
    }

    fun updateBreakDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(breakDuration = minutes)
        viewModelScope.launch { settingsRepository.setBreakDuration(minutes) }
    }

    fun updateLongBreak(minutes: Int) {
        _uiState.value = _uiState.value.copy(longBreakDuration = minutes)
        viewModelScope.launch { settingsRepository.setLongBreak(minutes) }
    }

    fun updateCycles(count: Int) {
        _uiState.value = _uiState.value.copy(totalCycles = count)
        viewModelScope.launch { settingsRepository.setCycles(count) }
    }

    fun startFocus() {
        if (!hasUsageStatsPermission() || !hasOverlayPermission()) {
            _uiState.value = _uiState.value.copy(showPermissionDialog = true)
            return
        }

        val s = _uiState.value
        val totalSec = s.focusDuration * 60
        _uiState.value = s.copy(
            phase = FocusPhase.FOCUSING,
            remainingSeconds = totalSec,
            totalSeconds = totalSec,
            currentCycle = 1,
            isRunning = true
        )
        startTimer()
        updateBlockerService()
    }

    fun dismissPermissionDialog() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = false)
    }

    fun openSettings() {
        if (!hasUsageStatsPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            application.startActivity(intent)
        } else if (!hasOverlayPermission()) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${application.packageName}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            application.startActivity(intent)
        }
        dismissPermissionDialog()
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = application.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), application.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), application.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(application)
    }

    private fun updateBlockerService() {
        val s = _uiState.value
        val intent = Intent(application, FocusBlockerService::class.java)
        if (s.isRunning && s.phase == FocusPhase.FOCUSING) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                application.startForegroundService(intent)
            } else {
                application.startService(intent)
            }
        } else {
            application.stopService(intent)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.isRunning) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }
            if (_uiState.value.isRunning) {
                playSound()
                onPhaseComplete()
                updateBlockerService()
            }
        }
    }

    private fun onPhaseComplete() {
        val s = _uiState.value
        when (s.phase) {
            FocusPhase.FOCUSING -> {
                if (s.currentCycle >= s.totalCycles) {
                    _uiState.value = s.copy(
                        phase = FocusPhase.LONG_BREAK,
                        remainingSeconds = s.longBreakDuration * 60,
                        totalSeconds = s.longBreakDuration * 60
                    )
                } else {
                    _uiState.value = s.copy(
                        phase = FocusPhase.BREAK,
                        remainingSeconds = s.breakDuration * 60,
                        totalSeconds = s.breakDuration * 60
                    )
                }
                startTimer()
            }
            FocusPhase.BREAK, FocusPhase.LONG_BREAK -> {
                val nextCycle = s.currentCycle + 1
                if (nextCycle > s.totalCycles) {
                    _uiState.value = s.copy(phase = FocusPhase.FINISHED, isRunning = false)
                } else {
                    val totalSec = s.focusDuration * 60
                    _uiState.value = s.copy(
                        phase = FocusPhase.FOCUSING,
                        remainingSeconds = totalSec,
                        totalSeconds = totalSec,
                        currentCycle = nextCycle
                    )
                    startTimer()
                }
            }
            else -> {}
        }
    }

    fun skipBreak() {
        timerJob?.cancel()
        onPhaseComplete()
    }

    fun cancelFocus() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            phase = FocusPhase.IDLE,
            isRunning = false,
            remainingSeconds = 0,
            currentCycle = 1
        )
        updateBlockerService()
    }

    fun pauseResume() {
        val s = _uiState.value
        if (s.isRunning) {
            timerJob?.cancel()
            _uiState.value = s.copy(isRunning = false)
        } else {
            _uiState.value = s.copy(isRunning = true)
            startTimer()
        }
        updateBlockerService()
    }

    private fun playSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(application, notification)
            ringtone.play()
        } catch (_: Exception) {}
    }
}
