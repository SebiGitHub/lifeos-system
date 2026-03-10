package com.sebi.lifeos.lifeosapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sebi.lifeos.lifeosapp.data.LifeOsDb
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import com.sebi.lifeos.lifeosapp.usage.UsageAccess
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyUsageWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext

        if (!UsageAccess.hasUsageAccess(ctx)) {
            DailyUsageScheduler.scheduleNext(ctx)
            return Result.success()
        }

        val zone = ZoneId.of("Europe/Madrid")
        val now = ZonedDateTime.now(zone)

        val todayStart = now.toLocalDate().atStartOfDay(zone)
        val yesterdayStart = todayStart.minusDays(1)

        val dayKey = yesterdayStart.toLocalDate().toString()
        val startMs = yesterdayStart.toInstant().toEpochMilli()
        val endMs = todayStart.toInstant().toEpochMilli()

        val db = LifeOsDb.get(ctx)
        val repo = UsageRepository(ctx, db)

        repo.collectAndStoreDay(dayKey, startMs, endMs)

        DailyUsageScheduler.scheduleNext(ctx)
        return Result.success()
    }
}