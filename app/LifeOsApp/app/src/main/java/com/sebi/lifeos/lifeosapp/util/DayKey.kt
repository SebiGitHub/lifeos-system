package com.sebi.lifeos.lifeosapp.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DayKey {
    // Recomendación: fija Europe/Madrid para que móvil y emulador se comporten igual.
    // Si prefieres que siga la zona del dispositivo, cambia esto por ZoneId.systemDefault()
    private val zone: ZoneId = ZoneId.of("Europe/Madrid")

    private val spanishDateFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES"))

    private val spanishDayFormatter =
        DateTimeFormatter.ofPattern("EEEE", Locale("es", "ES"))

    fun keyFor(now: ZonedDateTime): String {
        return now.withZoneSameInstant(zone).toLocalDate().toString()
    }

    fun todayKey(): String = keyFor(ZonedDateTime.now(zone))

    @Deprecated("Usa lastDays() en su lugar", ReplaceWith("lastDays(days)"))
    fun lastNDaysKeys(days: Int): List<String> = lastDays(days)

    fun lastDays(n: Int): List<String> = lastDaysFrom(todayKey(), n)

    fun lastDaysFrom(endKey: String, n: Int): List<String> {
        val end = LocalDate.parse(endKey)
        return (0 until n).map {
            end.minusDays((n - 1 - it).toLong()).toString()
        }
    }

    fun dayKeyToShortLabel(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "Lun"
            DayOfWeek.TUESDAY -> "Mar"
            DayOfWeek.WEDNESDAY -> "Mié"
            DayOfWeek.THURSDAY -> "Jue"
            DayOfWeek.FRIDAY -> "Vie"
            DayOfWeek.SATURDAY -> "Sáb"
            DayOfWeek.SUNDAY -> "Dom"
        }
    }

    fun dayKeyToLongSpanish(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        return date.format(spanishDayFormatter).lowercase()
    }

    fun formatDateSpanish(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        return date.format(spanishDateFormatter)
    }

    fun isToday(dayKey: String): Boolean {
        return dayKey == todayKey()
    }

    fun daysBetween(from: String, to: String): Long {
        val fromDate = LocalDate.parse(from)
        val toDate = LocalDate.parse(to)
        return java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate)
    }

    fun getWeekStart(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        val monday = date.with(DayOfWeek.MONDAY)
        return monday.toString()
    }

    fun getMonthStart(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        return date.withDayOfMonth(1).toString()
    }
}