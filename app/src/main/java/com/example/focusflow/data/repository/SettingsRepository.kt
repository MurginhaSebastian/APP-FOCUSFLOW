package com.example.focusflow.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val FOCUS_DURATION_KEY = intPreferencesKey("focus_duration")
        val BREAK_DURATION_KEY = intPreferencesKey("break_duration")
        val LONG_BREAK_KEY = intPreferencesKey("long_break")
        val CYCLES_KEY = intPreferencesKey("cycles")
        private val LINKED_USER_EMAIL_KEY = androidx.datastore.preferences.core.stringPreferencesKey("linked_user_email")
    }

    val linkedUserEmail: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LINKED_USER_EMAIL_KEY]
    }

    suspend fun setLinkedUserEmail(email: String?) {
        context.dataStore.edit { prefs ->
            if (email == null) {
                prefs.remove(LINKED_USER_EMAIL_KEY)
            } else {
                prefs[LINKED_USER_EMAIL_KEY] = email
            }
        }
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: false
    }

    val focusDuration: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[FOCUS_DURATION_KEY] ?: 25
    }

    val breakDuration: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BREAK_DURATION_KEY] ?: 5
    }

    val longBreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[LONG_BREAK_KEY] ?: 15
    }

    val cycles: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[CYCLES_KEY] ?: 4
    }

    val isDynamicColor: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DYNAMIC_COLOR_KEY] ?: false
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setFocusDuration(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[FOCUS_DURATION_KEY] = minutes
        }
    }

    suspend fun setBreakDuration(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[BREAK_DURATION_KEY] = minutes
        }
    }

    suspend fun setLongBreak(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[LONG_BREAK_KEY] = minutes
        }
    }

    suspend fun setCycles(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[CYCLES_KEY] = count
        }
    }
}
