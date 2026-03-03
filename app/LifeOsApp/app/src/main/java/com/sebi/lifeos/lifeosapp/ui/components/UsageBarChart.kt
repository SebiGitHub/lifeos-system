package com.lifeos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@Composable
fun UsageBarChart(labels: List<String>, values: List<Int>, modifier: Modifier = Modifier) {
    val maxValue = values.maxOrNull() ?: 0
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        values.forEachIndexed { index, value ->
            // Calcular fracción de altura
            val fraction = if (maxValue > 0) value.toFloat() / maxValue.toFloat() else 0f
            val barFraction = when {
                fraction <= 0f -> 0f
                fraction < 0.03f -> 0.03f  // altura mínima para ser visible
                else -> fraction
            }
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f),
            ) {
                // Barra
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight(barFraction)
                        .background(MaterialTheme.colorScheme.primary)
                )
                // Etiqueta debajo de la barra
                Text(
                    text = labels.getOrNull(index) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
