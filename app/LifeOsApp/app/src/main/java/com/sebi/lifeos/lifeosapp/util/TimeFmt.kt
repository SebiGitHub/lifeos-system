package com.sebi.lifeos.lifeosapp.util

import kotlin.math.max
import kotlin.math.roundToInt

object TimeFmt {

    fun msToMinutes(ms: Long): Long = ms / 60000L

    /** 0:05, 1:23, 12:00 */
    fun msToHhMm(ms: Long): String {
        val totalSec = max(0L, ms) / 1000L
        val h = totalSec / 3600L
        val m = (totalSec % 3600L) / 60L
        return if (h > 0) String.format("%d:%02d", h, m) else String.format("0:%02d", m)
    }

    /** 5m, 1h 20m */
    fun msToHuman(ms: Long): String {
        val totalMin = msToMinutes(max(0L, ms))
        val h = totalMin / 60L
        val m = totalMin % 60L
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    /** Para progress bars. */
    fun ratio(partMs: Long, totalMs: Long): Float {
        if (totalMs <= 0L) return 0f
        return (partMs.toDouble() / totalMs.toDouble()).toFloat().coerceIn(0f, 1f)
    }

    /** % redondeado (0..100). */
    fun percent(partMs: Long, totalMs: Long): Int = (ratio(partMs, totalMs) * 100f).roundToInt()
}
