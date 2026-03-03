package com.sebi.lifeos.lifeosapp.repo

import android.content.Context
import android.net.Uri
import com.sebi.lifeos.lifeosapp.data.*
import com.sebi.lifeos.lifeosapp.domain.DEFAULT_TOP_LIMIT
import com.sebi.lifeos.lifeosapp.domain.MIN_APP_MS
import com.sebi.lifeos.lifeosapp.usage.AppMetaMapper
import com.sebi.lifeos.lifeosapp.usage.UsageCollector
import com.sebi.lifeos.lifeosapp.util.DayKey
import com.sebi.lifeos.lifeosapp.util.Downloads
import java.util.Locale
import com.sebi.lifeos.lifeosapp.data.AppWithCategoryTotalRow

class UsageRepository(private val context: Context, private val db: LifeOsDb) {

    private val categoryDao = db.categoryDao()
    private val appDao = db.appDao()
    private val usageDao = db.usageDao()

    private val defaultCategories = listOf(
        "Estudio",
        "Juegos",
        "Música",
        "Entretenimiento",
        "Otros"
    )

    suspend fun ensureCategories() {
        val existing = categoryDao.getAll().map { it.name }.toSet()
        val toInsert = defaultCategories.filter { it !in existing }
        if (toInsert.isNotEmpty()) {
            categoryDao.insertAll(toInsert.map { CategoryEntity(name = it) })
        }
    }

    private suspend fun ensureCategoryId(name: String, fallbackId: Long): Long {
        val clamped = if (defaultCategories.contains(name)) name else "Otros"
        val existing = categoryDao.getIdByName(clamped)
        if (existing != null) return existing
        ensureCategories()
        return categoryDao.getIdByName(clamped) ?: fallbackId
    }

    suspend fun collectAndStoreDay(dayKey: String, startMs: Long, endMs: Long) {
        ensureCategories()

        val otherId = categoryDao.getIdByName("Otros")
            ?: run {
                ensureCategories()
                categoryDao.getIdByName("Otros") ?: error("No existe categoría 'Otros'")
            }

        val pm = context.packageManager
        val statsMap = UsageCollector.aggregateUsage(context, startMs, endMs)

        val existingApps = appDao.getAll().associateBy { it.packageName }
        val usageRows = mutableListOf<UsageDayEntity>()

        for ((packageName, stats) in statsMap) {
            val fg = stats.totalTimeInForeground
            if (fg <= 0) continue

            val label = AppMetaMapper.resolveLabel(pm, packageName)
            val categoryName = AppMetaMapper.resolveCategory(pm, packageName)
            val mappedCategoryId = ensureCategoryId(categoryName, otherId)

            val prev = existingApps[packageName]
            val finalCategoryId =
                if (prev != null && prev.categoryId != otherId) prev.categoryId
                else mappedCategoryId

            appDao.upsert(
                AppEntity(
                    packageName = packageName,
                    label = label,
                    categoryId = finalCategoryId
                )
            )

            usageRows.add(
                UsageDayEntity(
                    dayKey = dayKey,
                    packageName = packageName,
                    foregroundMs = fg
                )
            )
        }

        usageDao.upsertAll(usageRows)
    }

    suspend fun categories(): List<CategoryEntity> = categoryDao.getAll()

    suspend fun appsWithCategoryForLastDays(
        days: Int,
        minTotalMs: Long = 5 * 60 * 1000L
    ): List<AppWithCategoryTotalRow> {
        val keys = DayKey.lastNDaysKeys(days)
        return usageDao.appsWithCategoryForDaysMinTotal(keys, minTotalMs)
    }

    suspend fun appsWithCategoryForYear(
        year: String,
        minTotalMs: Long = 5 * 60 * 1000L
    ): List<AppWithCategoryTotalRow> {
        return usageDao.appsWithCategoryForYearMinTotal(year, minTotalMs)
    }

    suspend fun setAppCategory(packageName: String, categoryId: Long) {
        appDao.setAppCategory(packageName, categoryId)
    }

    suspend fun topAppsForDay(dayKey: String, limit: Int = DEFAULT_TOP_LIMIT): List<AppUsageAgg> {
        return usageDao.topAppsForDayMinMs(dayKey = dayKey, limit = limit, minMs = MIN_APP_MS)
    }

    private fun likeMonthPrefix(monthPrefix: String): String =
        if (monthPrefix.endsWith("%")) monthPrefix else "$monthPrefix%"

    suspend fun topAppsForMonth(monthPrefix: String, limit: Int = DEFAULT_TOP_LIMIT): List<AppUsageAgg> {
        return usageDao.topAppsForMonthMinMs(
            monthPrefix = likeMonthPrefix(monthPrefix),
            limit = limit,
            minMs = MIN_APP_MS
        )
    }

    suspend fun topAppsForYear(year: String, limit: Int = 50): List<AppUsageAgg> {
        return usageDao.topAppsForYearMinMs(year = year, limit = limit, minMs = MIN_APP_MS)
    }

    suspend fun totalsByCategoryForYear(year: String): List<CategoryUsageAgg> {
        return usageDao.totalsByCategoryForYearMinMs(year = year, minMs = MIN_APP_MS)
    }

    suspend fun dayTotals(dayKeys: List<String>): List<DayTotalAgg> {
        return if (dayKeys.isEmpty()) emptyList() else usageDao.dayTotals(dayKeys)
    }

    suspend fun monthTotals(year: String): List<MonthTotalAgg> {
        return usageDao.monthTotals(year)
    }

    suspend fun buildCsvForYear(year: String): String {
        val rows = usageDao.csvRowsForYearMinTotal(year = year, minTotalMs = MIN_APP_MS)
        if (rows.isEmpty()) return ""

        val header = "dayKey,packageName,label,category,totalMinutes\n"
        val body = buildString {
            for (r in rows) {
                val minutes = r.totalMs / 60000.0
                fun esc(s: String) = '"' + s.replace("\"", "\"\"") + '"'
                append(r.dayKey); append(',')
                append(esc(r.packageName)); append(',')
                append(esc(r.label)); append(',')
                append(esc(r.categoryName)); append(',')
                append(String.format(Locale.US, "%.2f", minutes))
                append('\n')
            }
        }
        return header + body
    }

    private suspend fun buildCsvForMonth(year: String, month2: String): String {
        val prefix = "$year-$month2-"
        val rows = usageDao.csvRowsForYearMinTotal(year = year, minTotalMs = MIN_APP_MS)
            .filter { it.dayKey.startsWith(prefix) }

        if (rows.isEmpty()) return ""

        val header = "dayKey,packageName,label,category,totalMinutes\n"
        val body = buildString {
            for (r in rows) {
                val minutes = r.totalMs / 60000.0
                fun esc(s: String) = '"' + s.replace("\"", "\"\"") + '"'
                append(r.dayKey); append(',')
                append(esc(r.packageName)); append(',')
                append(esc(r.label)); append(',')
                append(esc(r.categoryName)); append(',')
                append(String.format(Locale.US, "%.2f", minutes))
                append('\n')
            }
        }
        return header + body
    }

    /**
     * ✅ Exporta el MES seleccionado a Descargas con nombre: lifeos_usage_MM_YYYY.csv
     */
    suspend fun exportMonthCsvToDownloads(year: String, monthIdx: Int): Result<Uri> {
        val mm = (monthIdx + 1).toString().padStart(2, '0')
        val csv = buildCsvForMonth(year, mm)
        if (csv.isBlank()) return Result.failure(IllegalStateException("No hay datos para exportar"))

        val fileName = "lifeos_usage_${mm}_${year}.csv"
        return Downloads.saveCsvToDownloads(context, fileName, csv)
    }
}
