package com.sebi.lifeos.lifeosapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sebi.lifeos.lifeosapp.data.LifeOsDb
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import com.sebi.lifeos.lifeosapp.usage.UsageAccess
import java.time.ZoneId
import java.time.ZonedDateTime

class RealtimeUsageWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext

        if (!UsageAccess.hasUsageAccess(ctx)) return Result.success()

        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val today = now.toLocalDate()
        val cutoffToday = today.atTime(23, 30).atZone(zone)

        // “Hoy” = intervalo que acaba en el PRÓXIMO corte (23:30)
        val (dayKey, start) = if (now.isBefore(cutoffToday)) {
            today.toString() to cutoffToday.minusDays(1)
        } else {
            today.plusDays(1).toString() to cutoffToday
        }

        val startMs = start.toInstant().toEpochMilli()
        val endMs = now.toInstant().toEpochMilli()

        val db = LifeOsDb.get(ctx)
        val repo = UsageRepository(ctx, db)

        return runCatching {
            repo.collectAndStoreDay(dayKey, startMs, endMs)
            Result.success()
        }.getOrElse { Result.success() }
    }
}
