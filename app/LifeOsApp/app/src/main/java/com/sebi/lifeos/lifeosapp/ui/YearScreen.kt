package com.sebi.lifeos.lifeosapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sebi.lifeos.lifeosapp.ui.components.AppUsageRow
import com.sebi.lifeos.lifeosapp.ui.components.ScreenTitle
import com.sebi.lifeos.lifeosapp.ui.components.SectionTitle
import com.sebi.lifeos.lifeosapp.ui.components.SelectableBarChart
import com.sebi.lifeos.lifeosapp.util.TimeFmt

@Composable
fun YearScreen(vm: YearViewModel, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsState()

    val snack = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(state.message) {
        val msg = state.message
        if (!msg.isNullOrBlank()) {
            snack.showSnackbar(msg)
            vm.consumeMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenTitle(text = "Año")

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(text = "Selecciona un año")
                    OutlinedTextField(
                        value = state.year,
                        onValueChange = { vm.setYear(it) },
                        label = { Text("YYYY") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { vm.loadYear() }, enabled = !state.loading) {
                            Text(if (state.loading) "Cargando..." else "Cargar")
                        }
                        Button(onClick = { vm.exportSelectedMonthCsv() }, enabled = !state.exporting) {
                            Text(if (state.exporting) "Exportando..." else "Export CSV")
                        }
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(text = "Uso mensual")

                    val labels = listOf("E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
                    val selectedTotal = state.monthTotalsMs.getOrNull(state.selectedMonthIdx) ?: 0L

                    when {
                        state.loading -> CircularProgressIndicator()
                        state.monthTotalsMs.isEmpty() || state.monthTotalsMs.all { it == 0L } ->
                            Text("No hay datos de uso en este año.")
                        else -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { vm.moveSelection(-1) },
                                    enabled = state.selectedMonthIdx > 0
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronLeft,
                                        contentDescription = "Mes anterior"
                                    )
                                }

                                Text(
                                    text = TimeFmt.msToHuman(selectedTotal),
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = { vm.moveSelection(+1) },
                                    enabled = state.selectedMonthIdx < 11
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = "Mes siguiente"
                                    )
                                }
                            }

                            SelectableBarChart(
                                valuesMs = state.monthTotalsMs,
                                labels = labels,
                                selectedIndex = state.selectedMonthIdx,
                                onSelect = { idx -> vm.selectMonth(idx) },
                                adaptiveGrid = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(text = "Top apps (mes seleccionado)")

                    val list = state.topAppsSelectedMonth
                    when {
                        state.loading -> CircularProgressIndicator()
                        list.isEmpty() -> Text("No hay datos (o no hay apps con uso > 5 min).")
                        else -> {
                            val maxMs = list.maxOf { it.totalMs }.coerceAtLeast(1L)
                            list.take(10).forEach { app ->
                                AppUsageRow(
                                    packageName = app.packageName,
                                    label = app.label,
                                    totalMs = app.totalMs,
                                    maxMs = maxMs
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
        }

        SnackbarHost(
            hostState = snack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}
