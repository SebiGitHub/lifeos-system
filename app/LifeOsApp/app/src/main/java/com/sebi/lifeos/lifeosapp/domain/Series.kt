package com.sebi.lifeos.lifeosapp.domain

import com.sebi.lifeos.lifeosapp.data.DayTotalAgg
import com.sebi.lifeos.lifeosapp.data.MonthTotalAgg

object Series {

    fun buildDailyTotals(dayKeys: List<String>, rows: List<DayTotalAgg>): List<Long> {
        val map = rows.associate { it.dayKey to it.totalMs }
        return dayKeys.map { map[it] ?: 0L }
    }

    fun buildMonthlyTotals(year: String, rows: List<MonthTotalAgg>): List<Long> {
        val map = rows.associate { it.monthKey to it.totalMs }
        return (1..12).map { m ->
            val key = "%s-%02d".format(year, m)
            map[key] ?: 0L
        }
    }
}
