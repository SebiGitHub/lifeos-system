package com.sebi.lifeos.lifeosapp.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val categoryId: Long
)

@Entity(
    tableName = "usage_day",
    primaryKeys = ["dayKey", "packageName"]
)
data class UsageDayEntity(
    val dayKey: String,          // "YYYY-MM-DD" según corte 23:30
    val packageName: String,
    val foregroundMs: Long
)

@Entity(tableName = "app_catalog")
data class AppCatalogEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val category: String,        // auto
    val userCategory: String? = null // override opcional
)

@Entity(
    tableName = "usage_entries",
    indices = [Index(value = ["dayKey", "packageName"], unique = true)]
)
data class UsageEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayKey: String,           // "2026-01-29"
    val packageName: String,
    val minutes: Double
)