package com.sebi.lifeos.lifeosapp.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object DailyUsageScheduler {

    private const val UNIQUE_DAILY = "daily_usage_capture"
    private const val UNIQUE_REALTIME = "realtime_usage_refresh"
    private const val UNIQUE_REALTIME_ONCE = "realtime_usage_refresh_once"

    /**
     * Programa la captura diaria para la próxima 00:10 (hora local).
     * Si ya pasó, programa para mañana.
     */
    fun scheduleNext(context: Context) {
        val now = ZonedDateTime.now()
        val todayAt0010 = now.toLocalDate().atTime(0, 10).atZone(now.zone)
        val next = if (now.isBefore(todayAt0010)) todayAt0010 else todayAt0010.plusDays(1)

        val delayMs = Duration.between(now, next).toMillis().coerceAtLeast(0L)

        val req = OneTimeWorkRequestBuilder<DailyUsageWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_DAILY, ExistingWorkPolicy.REPLACE, req)
    }

    /**
     * Fuerza ejecutar YA la captura “cierre” (DailyUsageWorker).
     * Útil si quieres guardar el día anterior completo.
     */
    fun runDailyCaptureNow(context: Context) {
        val req = OneTimeWorkRequestBuilder<DailyUsageWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_DAILY, ExistingWorkPolicy.REPLACE, req)
    }

    /**
     * Sync “hoy” (one-shot) usando RealtimeUsageWorker.
     * Esto actualiza el día en curso.
     */
    fun refreshTodayNow(context: Context) {
        val req = OneTimeWorkRequestBuilder<RealtimeUsageWorker>().build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_REALTIME_ONCE, ExistingWorkPolicy.REPLACE, req)
    }

    /**
     * Activa/desactiva el tracking en tiempo real (cada 15 min).
     */
    fun setRealtimeEnabled(context: Context, enabled: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (enabled) {
            val req = PeriodicWorkRequestBuilder<RealtimeUsageWorker>(15, TimeUnit.MINUTES).build()
            wm.enqueueUniquePeriodicWork(UNIQUE_REALTIME, ExistingPeriodicWorkPolicy.UPDATE, req)
        } else {
            wm.cancelUniqueWork(UNIQUE_REALTIME)
            wm.cancelUniqueWork(UNIQUE_REALTIME_ONCE)
        }
    }
}
