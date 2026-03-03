package com.sebi.lifeos.lifeosapp.data

data class YearAppRow(
    val packageName: String,
    val label: String,
    val totalMs: Long
)

data class YearCategoryRow(
    val category: String,
    val totalMs: Long
)
