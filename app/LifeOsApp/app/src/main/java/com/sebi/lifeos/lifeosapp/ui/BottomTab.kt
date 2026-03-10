package com.sebi.lifeos.lifeosapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(
    val label: String,
    val icon: ImageVector
) {
    TODAY("Hoy", Icons.Filled.Home),
    YEAR("Año", Icons.Filled.CalendarMonth),
    CATALOG("Catálogo", Icons.Filled.ListAlt),
    RANKING("Ranking", Icons.Filled.BarChart),
    SETTINGS("Ajustes", Icons.Filled.Settings),
}
