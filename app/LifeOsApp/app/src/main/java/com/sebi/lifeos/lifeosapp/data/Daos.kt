package com.sebi.lifeos.lifeosapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

// --- Aggregations used by UI / export ---

data class AppUsageAgg(
    val packageName: String,
    val label: String,
    val totalMs: Long
)

data class CategoryUsageAgg(
    val categoryId: Long,
    val categoryName: String,
    val totalMs: Long
)

data class DayTotalAgg(
    val dayKey: String,
    val totalMs: Long
)

data class MonthTotalAgg(
    val monthKey: String,
    val totalMs: Long
)

data class CsvUsageAgg(
    val dayKey: String,
    val packageName: String,
    val label: String,
    val categoryName: String,
    val totalMs: Long
)

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<CategoryEntity>)

    @Query("SELECT id FROM categories WHERE name = :name LIMIT 1")
    suspend fun getIdByName(name: String): Long?
}

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: AppEntity)

    @Query("SELECT * FROM apps ORDER BY label COLLATE NOCASE")
    suspend fun getAll(): List<AppEntity>

    @Query("UPDATE apps SET categoryId = :categoryId WHERE packageName = :packageName")
    suspend fun setAppCategory(packageName: String, categoryId: Long)
}

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<UsageDayEntity>)

    // --- TOP apps ---

    @Transaction
    @Query(
        """
        SELECT a.packageName AS packageName, a.label AS label, SUM(u.foregroundMs) AS totalMs
        FROM usage_day u
        JOIN apps a ON a.packageName = u.packageName
        WHERE u.dayKey = :dayKey
        GROUP BY a.packageName, a.label
        HAVING totalMs >= :minMs
        ORDER BY totalMs DESC
        LIMIT :limit
        """
    )
    suspend fun topAppsForDayMinMs(dayKey: String, limit: Int, minMs: Long): List<AppUsageAgg>

    @Transaction
    @Query(
        """
        SELECT a.packageName AS packageName, a.label AS label, SUM(u.foregroundMs) AS totalMs
        FROM usage_day u
        JOIN apps a ON a.packageName = u.packageName
        WHERE u.dayKey LIKE :monthPrefix
        GROUP BY a.packageName, a.label
        HAVING totalMs >= :minMs
        ORDER BY totalMs DESC
        LIMIT :limit
        """
    )
    suspend fun topAppsForMonthMinMs(monthPrefix: String, limit: Int, minMs: Long): List<AppUsageAgg>

    @Transaction
    @Query(
        """
        SELECT a.packageName AS packageName, a.label AS label, SUM(u.foregroundMs) AS totalMs
        FROM usage_day u
        JOIN apps a ON a.packageName = u.packageName
        WHERE substr(u.dayKey, 1, 4) = :year
        GROUP BY a.packageName, a.label
        HAVING totalMs >= :minMs
        ORDER BY totalMs DESC
        LIMIT :limit
        """
    )
    suspend fun topAppsForYearMinMs(year: String, limit: Int, minMs: Long): List<AppUsageAgg>

    // --- Totals ---

    @Transaction
    @Query(
        """
        SELECT c.id AS categoryId, c.name AS categoryName, SUM(u.foregroundMs) AS totalMs
        FROM usage_day u
        JOIN apps a ON a.packageName = u.packageName
        JOIN categories c ON c.id = a.categoryId
        WHERE substr(u.dayKey, 1, 4) = :year
        GROUP BY c.id, c.name
        HAVING totalMs >= :minMs
        ORDER BY totalMs DESC
        """
    )
    suspend fun totalsByCategoryForYearMinMs(year: String, minMs: Long): List<CategoryUsageAgg>

    @Query(
        """
        SELECT u.dayKey AS dayKey, SUM(u.foregroundMs) AS totalMs
        FROM usage_day u
        WHERE u.dayKey IN (:dayKeys)
        GROUP BY u.dayKey
        ORDER BY u.dayKey ASC
        """
    )
    suspend fun dayTotals(dayKeys: List<String>): List<DayTotalAgg>

    @Query(
        """
        SELECT substr(u.dayKey, 1, 7) AS monthKey, SUM(u.foregroundMs) AS totalMs
        FROM usage_day u
        WHERE substr(u.dayKey, 1, 4) = :year
        GROUP BY monthKey
        ORDER BY monthKey ASC
        """
    )
    suspend fun monthTotals(year: String): List<MonthTotalAgg>

    // --- Filtering for Catalog / CSV: total acumulado del periodo > X ms ---

    @Query(
        """
    SELECT a.packageName AS packageName,
           a.label       AS label,
           a.categoryId  AS categoryId,
           t.totalMs     AS totalMs
    FROM apps a
    JOIN (
        SELECT packageName, SUM(foregroundMs) AS totalMs
        FROM usage_day
        WHERE dayKey IN (:dayKeys)
        GROUP BY packageName
        HAVING SUM(foregroundMs) >= :minTotalMs
    ) t ON t.packageName = a.packageName
    ORDER BY a.label COLLATE NOCASE
    """
    )
    suspend fun appsWithCategoryForDaysMinTotal(
        dayKeys: List<String>,
        minTotalMs: Long
    ): List<AppWithCategoryTotalRow>


    @Query(
        """
    SELECT a.packageName AS packageName,
           a.label       AS label,
           a.categoryId  AS categoryId,
           SUM(u.foregroundMs) AS totalMs
    FROM usage_day u
    JOIN apps a ON a.packageName = u.packageName
    WHERE substr(u.dayKey, 1, 4) = :year
    GROUP BY a.packageName, a.label, a.categoryId
    HAVING SUM(u.foregroundMs) >= :minTotalMs
    ORDER BY a.label COLLATE NOCASE
    """
    )
    suspend fun appsWithCategoryForYearMinTotal(
        year: String,
        minTotalMs: Long
    ): List<AppWithCategoryTotalRow>


    @Transaction
    @Query(
        """
        SELECT u.dayKey AS dayKey,
               a.packageName AS packageName,
               a.label AS label,
               c.name AS categoryName,
               SUM(u.foregroundMs) AS totalMs
        FROM usage_day u
        JOIN apps a ON a.packageName = u.packageName
        JOIN categories c ON c.id = a.categoryId
        WHERE u.dayKey IN (:dayKeys)
          AND u.packageName IN (
              SELECT packageName
              FROM usage_day
              WHERE dayKey IN (:dayKeys)
              GROUP BY packageName
              HAVING SUM(foregroundMs) >= :minTotalMs
          )
        GROUP BY u.dayKey, a.packageName, a.label, c.name
        ORDER BY u.dayKey ASC, totalMs DESC
        """
    )
    suspend fun csvRowsForDaysMinTotal(dayKeys: List<String>, minTotalMs: Long): List<CsvUsageAgg>

    @Transaction
    @Query(
        """
        SELECT u.dayKey AS dayKey,
               a.packageName AS packageName,
               a.label AS label,
               c.name AS categoryName,
               SUM(u.foregroundMs) AS totalMs
        FROM usage_day u
        JOIN apps a ON a.packageName = u.packageName
        JOIN categories c ON c.id = a.categoryId
        WHERE substr(u.dayKey, 1, 4) = :year
          AND u.packageName IN (
              SELECT packageName
              FROM usage_day
              WHERE substr(dayKey, 1, 4) = :year
              GROUP BY packageName
              HAVING SUM(foregroundMs) >= :minTotalMs
          )
        GROUP BY u.dayKey, a.packageName, a.label, c.name
        ORDER BY u.dayKey ASC, totalMs DESC
        """
    )
    suspend fun csvRowsForYearMinTotal(year: String, minTotalMs: Long): List<CsvUsageAgg>
}
