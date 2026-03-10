package com.sebi.lifeos.lifeosapp

import com.sebi.lifeos.lifeosapp.data.DayTotalAgg
import com.sebi.lifeos.lifeosapp.data.MonthTotalAgg
import com.sebi.lifeos.lifeosapp.domain.Series
import com.sebi.lifeos.lifeosapp.util.DayKey
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DayKeyAndSeriesTest {

    private val zone = ZoneId.of("Europe/Madrid")

    @Test
    fun `DayKey keyFor - before cutoff returns yesterday`() {
        val now = ZonedDateTime.of(2026, 2, 5, 23, 29, 0, 0, zone)
        val key = DayKey.keyFor(now)
        assertEquals("2026-02-04", key)
    }

    @Test
    fun `DayKey keyFor - at cutoff returns today`() {
        val now = ZonedDateTime.of(2026, 2, 5, 23, 30, 0, 0, zone)
        val key = DayKey.keyFor(now)
        assertEquals("2026-02-05", key)
    }

    @Test
    fun `DayKey lastDaysFrom returns ascending list`() {
        val keys = DayKey.lastDaysFrom("2026-02-05", 7)
        assertEquals(
            listOf(
                "2026-01-30",
                "2026-01-31",
                "2026-02-01",
                "2026-02-02",
                "2026-02-03",
                "2026-02-04",
                "2026-02-05",
            ),
            keys
        )
    }

    @Test
    fun `Series buildDailyTotals aligns rows and fills gaps`() {
        val dayKeys = listOf("2026-02-01", "2026-02-02", "2026-02-03")
        val rows = listOf(
            DayTotalAgg(dayKey = "2026-02-01", totalMs = 1000L),
            DayTotalAgg(dayKey = "2026-02-03", totalMs = 3000L),
        )
        val totals = Series.buildDailyTotals(dayKeys, rows)
        assertEquals(listOf(1000L, 0L, 3000L), totals)
    }

    @Test
    fun `Series buildMonthlyTotals returns 12 values and fills gaps`() {
        val rows = listOf(
            MonthTotalAgg(monthKey = "2026-01", totalMs = 111L),
            MonthTotalAgg(monthKey = "2026-03", totalMs = 333L),
        )
        val totals = Series.buildMonthlyTotals("2026", rows)
        assertEquals(12, totals.size)
        assertEquals(111L, totals[0]) // Jan
        assertEquals(0L, totals[1])   // Feb
        assertEquals(333L, totals[2]) // Mar
    }
}
