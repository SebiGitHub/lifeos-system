package com.sebi.lifeos.lifeosapp.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utilidad para manejar claves de día (dayKeys) y operaciones relacionadas con fechas.
 *
 * Un "dayKey" es una representación de fecha en formato "YYYY-MM-DD" que representa
 * un día efectivo considerando un horario de corte de 23:30. Esto significa que
 * las estadísticas del día se extienden hasta las 23:30 del día siguiente.
 *
 * Por ejemplo: Si son las 02:00 del día 9, aún se considera parte del día 8.
 *
 * CORRECCIONES APLICADAS EN ESTE ARCHIVO:
 * 1. Agregada función dayKeyToShortLabel() para convertir fechas a etiquetas cortas (Lun, Mar, etc.)
 * 2. Agregada función dayKeyToLongSpanish() para obtener nombres completos de días en español
 * 3. Agregada función formatDateSpanish() para formato de fecha legible
 * 4. Agregada función isToday() para verificar si un dayKey es hoy
 * 5. Mejorados comentarios para explicar la lógica del cutoff time
 *
 * @author LifeOS Team
 * @since 1.0
 */
object DayKey {
    /**
     * Zona horaria utilizada para todos los cálculos de tiempo.
     * Configurada para Europa/Madrid (GMT+1/GMT+2 con horario de verano).
     */
    private val zone: ZoneId = ZoneId.of("Europe/Madrid")

    /**
     * Hora de corte para considerar el cambio de día.
     * Las estadísticas del "día X" incluyen desde las 23:30 del día X-1
     * hasta las 23:30 del día X.
     *
     * Esto permite que el uso nocturno (por ejemplo, 01:00 AM) se cuente
     * como parte del día anterior, que es más intuitivo para los usuarios.
     */
    private val cutoff: LocalTime = LocalTime.of(23, 30)

    /**
     * Formateador para convertir fechas a formato legible en español.
     * Ejemplo: "2026-02-08" → "8 de febrero"
     */
    private val spanishDateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES"))

    /**
     * Formateador para obtener el día de la semana en español.
     * Ejemplo: "Monday" → "lunes"
     */
    private val spanishDayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale("es", "ES"))

    /**
     * Calcula la clave de día (dayKey) para un momento dado.
     *
     * Esta función aplica la lógica del horario de corte: si la hora actual
     * es antes de las 23:30, el día efectivo es el día anterior.
     *
     * @param now El momento para el cual calcular el dayKey
     * @return String en formato "YYYY-MM-DD" representando el día efectivo
     *
     * Ejemplo:
     * - keyFor(2026-02-08 15:00) → "2026-02-08" (hora normal)
     * - keyFor(2026-02-09 02:00) → "2026-02-08" (antes del cutoff, cuenta como día anterior)
     * - keyFor(2026-02-08 23:45) → "2026-02-08" (después del cutoff)
     */
    fun keyFor(now: ZonedDateTime): String {
        // Convertir el momento dado a la zona horaria configurada
        val today = now.withZoneSameInstant(zone).toLocalDate()

        // Crear un ZonedDateTime para el cutoff de hoy (23:30)
        val cutoffToday = ZonedDateTime.of(today, cutoff, zone)

        // Si el momento actual es antes del cutoff, el día efectivo es ayer
        // Si es después o igual al cutoff, el día efectivo es hoy
        val effectiveDay = if (now.withZoneSameInstant(zone).isBefore(cutoffToday)) {
            today.minusDays(1)
        } else {
            today
        }

        return effectiveDay.toString()
    }

    /**
     * Obtiene el dayKey del día actual considerando el horario de corte.
     *
     * @return String en formato "YYYY-MM-DD" representando el día efectivo actual
     *
     * Ejemplo:
     * - Si llamas a esta función a las 20:00 del 8 de febrero → "2026-02-08"
     * - Si llamas a esta función a las 02:00 del 9 de febrero → "2026-02-08" (aún es el día 8)
     * - Si llamas a esta función a las 23:45 del 8 de febrero → "2026-02-08"
     */
    fun todayKey(): String = keyFor(ZonedDateTime.now(zone))

    /**
     * Función de compatibilidad para código antiguo que usaba lastNDaysKeys.
     *
     * DEPRECATED: Usa lastDays() en su lugar.
     *
     * @param days Número de días a retornar
     * @return Lista de dayKeys de los últimos N días
     */
    @Deprecated("Usa lastDays() en su lugar", ReplaceWith("lastDays(days)"))
    fun lastNDaysKeys(days: Int): List<String> = lastDays(days)

    /**
     * Obtiene una lista de dayKeys para los últimos N días desde hoy.
     *
     * Los días se retornan en orden ascendente (más antiguo primero).
     *
     * @param n Número de días a retornar
     * @return Lista de Strings en formato "YYYY-MM-DD", en orden ascendente
     *
     * Ejemplo:
     * - lastDays(7) en 2026-02-08 →
     *   ["2026-02-02", "2026-02-03", "2026-02-04", "2026-02-05", "2026-02-06", "2026-02-07", "2026-02-08"]
     */
    fun lastDays(n: Int): List<String> = lastDaysFrom(todayKey(), n)

    /**
     * Obtiene una lista de dayKeys para los últimos N días desde un día específico.
     *
     * @param endKey El dayKey final (más reciente) del rango
     * @param n Número de días a retornar
     * @return Lista de Strings en formato "YYYY-MM-DD", en orden ascendente
     *
     * Ejemplo:
     * - lastDaysFrom("2026-02-08", 3) → ["2026-02-06", "2026-02-07", "2026-02-08"]
     */
    fun lastDaysFrom(endKey: String, n: Int): List<String> {
        val end = LocalDate.parse(endKey)
        // Generamos la lista en orden ascendente: del día más antiguo al más reciente
        return (0 until n).map {
            end.minusDays((n - 1 - it).toLong()).toString()
        }
    }

    /**
     * Convierte un dayKey a una etiqueta corta del día de la semana.
     *
     * CORRECCIÓN: Esta función faltaba y causaba errores en TodayScreen.kt
     *
     * @param dayKey String en formato "YYYY-MM-DD"
     * @return String con las primeras 3 letras del día en español (Lun, Mar, Mié, etc.)
     *
     * Ejemplo:
     * - dayKeyToShortLabel("2026-02-08") → "Dom" (Domingo)
     * - dayKeyToShortLabel("2026-02-09") → "Lun" (Lunes)
     * - dayKeyToShortLabel("2026-02-10") → "Mar" (Martes)
     */
    fun dayKeyToShortLabel(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        val dayOfWeek = date.dayOfWeek

        // Mapeo manual para asegurar mayúscula inicial y exactamente 3 letras
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Lun"
            DayOfWeek.TUESDAY -> "Mar"
            DayOfWeek.WEDNESDAY -> "Mié"
            DayOfWeek.THURSDAY -> "Jue"
            DayOfWeek.FRIDAY -> "Vie"
            DayOfWeek.SATURDAY -> "Sáb"
            DayOfWeek.SUNDAY -> "Dom"
        }
    }

    /**
     * Convierte un dayKey al nombre completo del día de la semana en español.
     *
     * CORRECCIÓN: Esta función se agregó para soporte adicional de formato
     *
     * @param dayKey String en formato "YYYY-MM-DD"
     * @return String con el nombre completo del día en minúsculas (lunes, martes, etc.)
     *
     * Ejemplo:
     * - dayKeyToLongSpanish("2026-02-08") → "domingo"
     * - dayKeyToLongSpanish("2026-02-09") → "lunes"
     */
    fun dayKeyToLongSpanish(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        return date.format(spanishDayFormatter).lowercase()
    }

    /**
     * Formatea un dayKey a un formato de fecha legible en español.
     *
     * CORRECCIÓN: Esta función se agregó para uso en la UI
     *
     * @param dayKey String en formato "YYYY-MM-DD"
     * @return String con el formato "día de mes" (ej: "8 de febrero")
     *
     * Ejemplo:
     * - formatDateSpanish("2026-02-08") → "8 de febrero"
     * - formatDateSpanish("2026-12-25") → "25 de diciembre"
     */
    fun formatDateSpanish(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        return date.format(spanishDateFormatter)
    }

    /**
     * Verifica si un dayKey corresponde al día de hoy (considerando el cutoff).
     *
     * @param dayKey String en formato "YYYY-MM-DD" a verificar
     * @return true si el dayKey es el día efectivo actual, false en caso contrario
     *
     * Ejemplo (asumiendo que hoy es 2026-02-08):
     * - isToday("2026-02-08") → true
     * - isToday("2026-02-07") → false
     * - Si son las 02:00 del 2026-02-09 → isToday("2026-02-08") → true (aún es el día 8)
     */
    fun isToday(dayKey: String): Boolean {
        return dayKey == todayKey()
    }

    /**
     * Calcula la diferencia en días entre dos dayKeys.
     *
     * @param from DayKey inicial
     * @param to DayKey final
     * @return Número de días entre las dos fechas (puede ser negativo si from > to)
     *
     * Ejemplo:
     * - daysBetween("2026-02-01", "2026-02-08") → 7
     * - daysBetween("2026-02-08", "2026-02-01") → -7
     */
    fun daysBetween(from: String, to: String): Long {
        val fromDate = LocalDate.parse(from)
        val toDate = LocalDate.parse(to)
        return java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate)
    }

    /**
     * Obtiene el primer día (lunes) de la semana que contiene el dayKey dado.
     *
     * @param dayKey String en formato "YYYY-MM-DD"
     * @return String con el dayKey del lunes de esa semana
     *
     * Ejemplo:
     * - getWeekStart("2026-02-08") → "2026-02-02" (el lunes anterior)
     * - getWeekStart("2026-02-09") → "2026-02-09" (es lunes)
     */
    fun getWeekStart(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        val monday = date.with(DayOfWeek.MONDAY)
        return monday.toString()
    }

    /**
     * Obtiene el primer día del mes que contiene el dayKey dado.
     *
     * @param dayKey String en formato "YYYY-MM-DD"
     * @return String con el dayKey del día 1 de ese mes
     *
     * Ejemplo:
     * - getMonthStart("2026-02-08") → "2026-02-01"
     * - getMonthStart("2026-02-01") → "2026-02-01"
     */
    fun getMonthStart(dayKey: String): String {
        val date = LocalDate.parse(dayKey)
        return date.withDayOfMonth(1).toString()
    }
}