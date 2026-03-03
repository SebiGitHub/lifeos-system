package com.sebi.lifeos.lifeosapp.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sebi.lifeos.lifeosapp.data.AppUsageAgg
import com.sebi.lifeos.lifeosapp.ui.components.AppIcon
import com.sebi.lifeos.lifeosapp.ui.components.AppUsageRow
import com.sebi.lifeos.lifeosapp.ui.components.ScreenTitle
import com.sebi.lifeos.lifeosapp.ui.components.SectionTitle
import com.sebi.lifeos.lifeosapp.ui.components.rememberAppDominantColor
import com.sebi.lifeos.lifeosapp.util.TimeFmt

@Composable
fun RankingScreen(vm: RankingViewModel, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle(text = "Ranking") }

        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(text = "Top 3 del día")

                    if (state.topDay.isEmpty()) {
                        Text("No hay datos para el ranking de hoy.")
                    } else {
                        Podium(top3 = state.topDay.take(3))
                    }
                }
            }
        }

        item {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle(text = "Más usadas hoy")
                    if (state.rankList.isEmpty()) Text("No hay apps con uso > 5 min hoy.")
                }
            }
        }

        items(state.rankList, key = { it.packageName }) { item ->
            val dom = rememberAppDominantColor(
                packageName = item.packageName,
                fallback = MaterialTheme.colorScheme.surfaceVariant
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = dom.copy(alpha = 0.18f)
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    AppUsageRow(
                        packageName = item.packageName,
                        totalMs = item.totalMs
                    )
                }
            }
        }

        item { Spacer(Modifier.height(6.dp)) }
    }
}

private fun podiumOrder(items: List<AppUsageAgg>): List<AppUsageAgg?> {
    val first = items.getOrNull(0)
    val second = items.getOrNull(1)
    val third = items.getOrNull(2)
    return listOf(second, first, third)
}

@Composable
private fun Podium(top3: List<AppUsageAgg>) {
    val ordered = podiumOrder(top3)
    val inf = rememberInfiniteTransition(label = "podiumInf")

    val pulse1 = inf.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse1"
    ).value

    val floatIcon: Dp = inf.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "floatIcon"
    ).value.dp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        PodiumColumn(
            position = 2,
            item = ordered[0],
            stepHeight = 62.dp,
            scale = 1f,
            iconFloat = floatIcon,
            modifier = Modifier.weight(1f)
        )

        PodiumColumn(
            position = 1,
            item = ordered[1],
            stepHeight = 82.dp,
            scale = pulse1,
            iconFloat = floatIcon,
            modifier = Modifier.weight(1f)
        )

        PodiumColumn(
            position = 3,
            item = ordered[2],
            stepHeight = 56.dp,
            scale = 1f,
            iconFloat = floatIcon,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PodiumColumn(
    position: Int,
    item: AppUsageAgg?,
    stepHeight: Dp,
    scale: Float,
    iconFloat: Dp,
    modifier: Modifier = Modifier
) {
    if (item == null || item.totalMs <= 0L) {
        Spacer(modifier = modifier.height(stepHeight + 56.dp))
        return
    }

    val appColor = rememberAppDominantColor(
        packageName = item.packageName,
        fallback = when (position) {
            1 -> MaterialTheme.colorScheme.primaryContainer
            2 -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        }
    )

    // ✅ Contraste automático: si el fondo es oscuro -> texto blanco, si es claro -> negro
    val numberColor: Color = if (appColor.luminance() < 0.45f) Color.White else Color.Black

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(stepHeight + 52.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = appColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stepHeight)
                    .scale(scale)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = numberColor
                    )

                    if (position == 1) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = iconFloat)
                    .padding(top = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIcon(
                    packageName = item.packageName,
                    size = 52.dp,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = TimeFmt.msToHuman(item.totalMs),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
