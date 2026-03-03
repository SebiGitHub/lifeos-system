package com.sebi.lifeos.lifeosapp.usage

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object UsageCollector {
    fun aggregateUsage(context: Context, startMs: Long, endMs: Long): Map<String, UsageStats> {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usm.queryAndAggregateUsageStats(startMs, endMs) ?: emptyMap()
    }
}
