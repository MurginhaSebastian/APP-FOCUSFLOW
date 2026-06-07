package com.example.focusflow.viewmodel

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StreakTracker {
    private const val PREFS_NAME = "streak_prefs"
    private const val KEY_LAST_DATE = "last_date"
    private const val KEY_COUNT = "count"

    fun updateStreak(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""

        if (lastDate != today) {
            val yesterday = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                .format(Date(System.currentTimeMillis() - 86400000))

            val count = if (lastDate == yesterday) {
                prefs.getInt(KEY_COUNT, 0) + 1
            } else {
                1
            }

            prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putInt(KEY_COUNT, count)
                .apply()
        }
    }

    fun getStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_COUNT, 0)
    }
}
