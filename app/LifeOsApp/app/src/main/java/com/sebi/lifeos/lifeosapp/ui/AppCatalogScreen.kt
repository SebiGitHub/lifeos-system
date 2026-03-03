package com.sebi.lifeos.lifeosapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sebi.lifeos.lifeosapp.ui.components.AppIcon
import com.sebi.lifeos.lifeosapp.ui.components.ScreenTitle
import com.sebi.lifeos.lifeosapp.ui.components.SectionTitle
import com.sebi.lifeos.lifeosapp.ui.components.rememberAppDominantColor
import com.sebi.lifeos.lifeosapp.usage.AppMetaMapper
import com.sebi.lifeos.lifeosapp.util.TimeFmt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCatalogScreen(
    vm: AppCatalogViewModel,
    modifier: Modifier = Modifier
) {
    val state by vm.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val ctx = LocalContext.current
    val pm = ctx.packageManager

    LaunchedEffect(Unit) { vm.load() }

    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        scope.launch { snack.showSnackbar(msg) }
        vm.consumeMessage()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ScreenTitle("Catálogo") }

            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SectionTitle("Periodo")

                            val chipColors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = state.period == CatalogPeriod.LAST_30_DAYS,
                                    onClick = { vm.setPeriod(CatalogPeriod.LAST_30_DAYS) },
                                    label = { Text("Últimos 30 días") },
                                    colors = chipColors
                                )
                                FilterChip(
                                    selected = state.period == CatalogPeriod.YEAR,
                                    onClick = { vm.setPeriod(CatalogPeriod.YEAR) },
                                    label = { Text("Año") },
                                    colors = chipColors
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            var expanded by remember { mutableStateOf(false) }
                            val selectedCatName = state.categoryFilterId?.let { id ->
                                state.categories.firstOrNull { it.id == id }?.name
                            } ?: "Todas"

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedCatName,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Categoría") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Todas") },
                                        onClick = {
                                            expanded = false
                                            vm.setCategoryFilter(null)
                                        }
                                    )
                                    state.categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.name) },
                                            onClick = {
                                                expanded = false
                                                vm.setCategoryFilter(cat.id)
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = state.query,
                                onValueChange = { vm.setQuery(it) },
                                label = { Text("Buscar") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionTitle("Apps")
                        if (state.apps.isEmpty()) Text("Sin datos todavía.")
                    }
                }
            }

            items(state.apps, key = { it.packageName }) { app ->
                val dom = rememberAppDominantColor(
                    packageName = app.packageName,
                    fallback = MaterialTheme.colorScheme.surfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = dom.copy(alpha = 0.18f)
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ✅ IZQUIERDA con weight para que no “aplasten” el dropdown
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppIcon(packageName = app.packageName, size = 40.dp)
                                Spacer(Modifier.width(12.dp))

                                val displayLabel = remember(app.packageName, app.label) {
                                    if (app.label.contains(".")) AppMetaMapper.resolveLabel(pm, app.packageName)
                                    else app.label
                                }

                                Column {
                                    Text(
                                        text = displayLabel,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        // ✅ si es largo, que haga wrap (no encoge el dropdown)
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = TimeFmt.msToHuman(app.totalMs),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            // Dropdown categoría por app (ancho fijo)
                            var expanded by remember(app.packageName) { mutableStateOf(false) }
                            val selectedCat = state.categories.firstOrNull { it.id == app.categoryId }
                                ?: state.categories.firstOrNull { it.name == "Otros" }
                                ?: state.categories.firstOrNull()

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedCat?.name ?: "Otros",
                                    onValueChange = { },
                                    readOnly = true,
                                    singleLine = true,
                                    label = { Text("Categoría") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .width(170.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    state.categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat.name) },
                                            onClick = {
                                                expanded = false
                                                vm.setCategory(app.packageName, cat.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
