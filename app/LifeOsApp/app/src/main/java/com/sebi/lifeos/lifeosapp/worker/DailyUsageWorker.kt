package com.sebi.lifeos.lifeosapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sebi.lifeos.lifeosapp.data.LifeOsDb
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import com.sebi.lifeos.lifeosapp.usage.UsageAccess
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyUsageWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext

        if (!UsageAccess.hasUsageAccess(ctx)) {
            // Reprograma y ya
            DailyUsageScheduler.scheduleNext(ctx)
            return Result.success()
        }

        val zone = ZoneId.of("Europe/Madrid")
        val now = ZonedDateTime.now(zone)

        // Corte diario 23:30:
        val cutoffToday = now.toLocalDate().atTime(23, 30).atZone(zone)
        val end = if (now.isAfter(cutoffToday)) cutoffToday else cutoffToday.minusDays(1)
        val start = end.minusDays(1)

        val dayKey = end.toLocalDate().toString()
        val startMs = start.toInstant().toEpochMilli()
        val endMs = end.toInstant().toEpochMilli()

        val db = LifeOsDb.get(ctx)
        val repo = UsageRepository(ctx, db)

        repo.collectAndStoreDay(dayKey, startMs, endMs)

        // Programa siguiente
        DailyUsageScheduler.scheduleNext(ctx)

        return Result.success()
    }
}
