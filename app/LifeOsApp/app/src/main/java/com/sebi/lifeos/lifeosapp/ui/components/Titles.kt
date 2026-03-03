package com.sebi.lifeos.lifeosapp.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()

    // ✅ En modo claro, oscurecemos el “secondary” mezclándolo con onSurface
    val color = if (dark) {
        MaterialTheme.colorScheme.secondary
    } else {
        lerp(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSurface,
            0.55f
        )
    }

    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = color,
        modifier = modifier
    )
}
