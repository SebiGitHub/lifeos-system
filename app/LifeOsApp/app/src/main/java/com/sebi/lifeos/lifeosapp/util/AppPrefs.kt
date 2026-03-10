package com.sebi.lifeos.lifeosapp.util

import android.content.Context

object AppPrefs {
    private const val PREFS = "lifeos_prefs"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_REALTIME = "realtime_tracking"

    fun isDarkMode(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, value)
            .apply()
    }

    fun isRealtimeTrackingEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_REALTIME, true) // por defecto ON

    fun setRealtimeTrackingEnabled(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REALTIME, value)
            .apply()
    }
}
