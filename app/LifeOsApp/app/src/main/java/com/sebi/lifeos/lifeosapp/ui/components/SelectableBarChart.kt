package com.sebi.lifeos.lifeosapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun SelectableBarChart(
    valuesMs: List<Long>,
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    gridStepHours: Int = 4,
    adaptiveGrid: Boolean = true,
    modifier: Modifier = Modifier,
    barHeight: Dp = 140.dp,
    barGap: Dp = 8.dp,
    axisWidth: Dp = 46.dp,
    axisSpacer: Dp = 10.dp,
    sidePadding: Dp = 6.dp
) {
    if (valuesMs.isEmpty()) return

    val maxMs = valuesMs.maxOrNull() ?: 0L
    val maxHoursRaw = ceil(maxMs / 3_600_000.0).toInt().coerceAtLeast(1)

    val maxHours = if (adaptiveGrid) {
        // ✅ escala acorde al máximo real (sin forzar 20h)
        maxHoursRaw
    } else {
        val steps = ceil(maxHoursRaw.toDouble() / gridStepHours.toDouble()).toInt().coerceAtLeast(1)
        max(steps * gridStepHours, gridStepHours)
    }

    val axisSteps: List<Int> = run {
        val raw = listOf(
            0,
            (maxHours * 0.25f).toInt(),
            (maxHours * 0.50f).toInt(),
            (maxHours * 0.75f).toInt(),
            maxHours
        )
        raw.distinct().sorted().let { steps ->
            if (steps.lastOrNull() == maxHours) steps else (steps + maxHours).distinct().sorted()
        }
    }

    val barColor = MaterialTheme.colorScheme.primary
    val selColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val axisText = MaterialTheme.colorScheme.onSurfaceVariant

    val density = LocalDensity.current

    Column(modifier = modifier) {
        // --- Chart row (barras + eje Y) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Canvas (solo el área de barras, no incluye el eje Y)
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(barHeight)
            ) {
                val w = size.width
                val h = size.height

                val count = valuesMs.size.coerceAtLeast(1)
                val gapPx = with(density) { barGap.toPx() }
                val sidePx = with(density) { sidePadding.toPx() }

                // ✅ encogemos un poco el área efectiva para dejar aire (y evitar “choque” visual)
                val effectiveW = (w - 2f * sidePx).coerceAtLeast(1f)
                val barW = (effectiveW - gapPx * (count - 1)) / count

                // grid lines
                val lines = (axisSteps.size - 1).coerceAtLeast(1)
                for (i in 0..lines) {
                    val y = h * (i.toFloat() / lines.toFloat())
                    drawRect(
                        color = gridColor,
                        topLeft = Offset(0f, y),
                        size = Size(w, 1.5f)
                    )
                }

                // bars
                valuesMs.forEachIndexed { idx, v ->
                    val ratio =
                        if (maxMs <= 0L) 0f else (v.toFloat() / maxMs.toFloat()).coerceIn(0f, 1f)
                    val bh = h * ratio

                    val x = sidePx + idx * (barW + gapPx)
                    val y = h - bh

                    val c = if (idx == selectedIndex) selColor else barColor

                    drawRoundRect(
                        color = c,
                        topLeft = Offset(x, y),
                        size = Size(barW, bh),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
            }

            Spacer(Modifier.width(axisSpacer))

            // eje Y (ancho fijo para reservar hueco)
            Column(
                modifier = Modifier
                    .width(axisWidth)
                    .height(barHeight),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                axisSteps.reversed().forEach { v ->
                    Text(
                        text = "${v}h",
                        style = MaterialTheme.typography.labelSmall,
                        color = axisText,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // --- Labels row (MISMO ancho que el área de barras, dejando hueco para eje Y) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // zona labels = weight(1f) (igual que canvas), y dentro aplicamos sidePadding + spacedBy(barGap)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = sidePadding),
                horizontalArrangement = Arrangement.spacedBy(barGap)
            ) {
                val count = valuesMs.size.coerceAtLeast(1)
                // ancho de cada label = cálculo equivalente al de barras (pero en Dp)
                // (usamos BoxWithConstraints implícito? no; aquí lo hacemos aproximado con fillMaxWidth+weight)
                // -> usamos weights iguales para que encaje con los gaps (visualmente clavado).
                labels.take(count).forEachIndexed { idx, l ->
                    val isSelected = idx == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable { onSelect(idx) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = l,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else axisText
                        )
                    }
                }
            }

            Spacer(Modifier.width(axisSpacer))

            // placeholder del eje Y para que el último label nunca “invada” esa zona
            Spacer(Modifier.width(axisWidth))
        }
    }
}
