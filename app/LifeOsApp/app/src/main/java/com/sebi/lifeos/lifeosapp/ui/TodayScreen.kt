package com.sebi.lifeos.lifeosapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sebi.lifeos.lifeosapp.ui.components.AppUsageRow
import com.sebi.lifeos.lifeosapp.ui.components.ScreenTitle
import com.sebi.lifeos.lifeosapp.ui.components.SectionTitle
import com.sebi.lifeos.lifeosapp.ui.components.SelectableBarChart
import com.sebi.lifeos.lifeosapp.usage.UsageAccess
import com.sebi.lifeos.lifeosapp.util.DayKey
import com.sebi.lifeos.lifeosapp.util.TimeFmt
import com.sebi.lifeos.lifeosapp.worker.DailyUsageScheduler
import kotlinx.coroutines.launch

@Composable
fun TodayScreen(
    vm: TodayViewModel,
    modifier: Modifier = Modifier
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.message) {
        state.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            vm.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenTitle(text = "Hoy") }

            item {
                Card {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionTitle(text = "Permisos")

                        if (!state.hasUsagePermission) {
                            Text(
                                "Para ver el tracking, activa \"Acceso a uso\" en Ajustes.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = { UsageAccess.openUsageAccessSettings(context) }) {
                                Text("Ir a Ajustes")
                            }
                        } else {
                            Text(
                                text = "Tracking activo ✓",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (state.hasUsagePermission) {
                item {
                    Card {
                        Column(
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SectionTitle(text = "Últimos 7 días")

                            val labels = state.dayKeys.map { dayKey ->
                                DayKey.dayKeyToShortLabel(dayKey)
                            }

                            val selectedTotal =
                                state.dayTotalsMs.getOrNull(state.selectedIndex) ?: 0L

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = { vm.moveSelection(-1) },
                                    enabled = state.selectedIndex > 0
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronLeft,
                                        contentDescription = "Día anterior"
                                    )
                                }

                                Text(
                                    text = TimeFmt.msToHuman(selectedTotal),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                IconButton(
                                    onClick = { vm.moveSelection(+1) },
                                    enabled = state.selectedIndex < state.dayKeys.lastIndex
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = "Día siguiente"
                                    )
                                }
                            }

                            // ✅ firma correcta del componente
                            SelectableBarChart(
                                valuesMs = state.dayTotalsMs,
                                labels = labels,
                                selectedIndex = state.selectedIndex,
                                onSelect = { index -> vm.selectIndex(index) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Card {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SectionTitle(text = "Más usadas (día seleccionado)")

                            if (state.loading) {
                                CircularProgressIndicator()
                            } else if (state.topApps.isEmpty()) {
                                Text("No hay apps con uso > 5 min en este día.")
                            } else {
                                val maxMs = state.topApps.maxOf { it.totalMs }.coerceAtLeast(1L)
                                state.topApps.forEach { app ->
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

                item {
                    Card {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SectionTitle(text = "Sincronización")

                            Button(
                                onClick = {
                                    scope.launch {
                                        DailyUsageScheduler.refreshTodayNow(context)
                                        vm.notifySyncStarted()
                                        vm.refresh()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sincronizar ahora")
                            }
                        }
                    }
                }
            }
        }
    }
}
