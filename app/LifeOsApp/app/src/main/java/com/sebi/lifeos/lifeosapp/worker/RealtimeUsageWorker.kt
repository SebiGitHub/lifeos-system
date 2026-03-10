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

        val zone = ZoneId.of("Europe/Madrid")
        val now = ZonedDateTime.now(zone)
        val today = now.toLocalDate()

        val dayKey = today.toString()
        val start = today.atStartOfDay(zone)

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