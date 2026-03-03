package com.sebi.lifeos.lifeosapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sebi.lifeos.lifeosapp.ui.BottomTab
import com.sebi.lifeos.lifeosapp.ui.theme.GreyTab
import com.sebi.lifeos.lifeosapp.ui.theme.GreyTabDark
import com.sebi.lifeos.lifeosapp.ui.theme.Lilac
import com.sebi.lifeos.lifeosapp.ui.theme.LilacDark

@Composable
fun TabIcon(tab: BottomTab, selected: Boolean) {
    val dark = isSystemInDarkTheme()

    // ✅ Colores asignados:
    // Hoy = morado (primary)
    // Año = azul (secondary)
    // Catálogo = lila (Lilac)
    // Ranking = amarillo (tertiary)
    // Ajustes = gris (GreyTab)
    val base: Color = when (tab) {
        BottomTab.TODAY -> MaterialTheme.colorScheme.primary
        BottomTab.YEAR -> MaterialTheme.colorScheme.secondary
        BottomTab.CATALOG -> if (dark) LilacDark else Lilac
        BottomTab.RANKING -> MaterialTheme.colorScheme.tertiary
        BottomTab.SETTINGS -> if (dark) GreyTabDark else GreyTab
    }

    val bg = if (selected) base.copy(alpha = if (dark) 0.35f else 0.28f)
    else base.copy(alpha = if (dark) 0.18f else 0.14f)

    val tint = if (selected) base else base.copy(alpha = 0.85f)

    // ✅ Contenedor fijo => misma “huella” visual => se ve todo alineado
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(MaterialTheme.shapes.small)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = tint
        )
    }
}
