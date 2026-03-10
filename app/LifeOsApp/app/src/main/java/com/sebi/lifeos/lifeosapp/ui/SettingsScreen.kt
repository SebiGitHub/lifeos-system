package com.sebi.lifeos.lifeosapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sebi.lifeos.lifeosapp.ui.components.ScreenTitle
import com.sebi.lifeos.lifeosapp.ui.components.SectionTitle
import com.sebi.lifeos.lifeosapp.usage.UsageAccess
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    realtimeTracking: Boolean,
    onRealtimeTrackingChange: (Boolean) -> Unit,
    onOpenUsageAccess: () -> Unit,
    onSyncNow: () -> Unit,
    onRunDailyCaptureNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var hasPerm by remember { mutableStateOf(UsageAccess.hasUsageAccess(ctx)) }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPerm = UsageAccess.hasUsageAccess(ctx)
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        modifier = modifier
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            ScreenTitle("Ajustes")
            Spacer(Modifier.height(12.dp))

            // Permisos
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionTitle("Permisos")
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (hasPerm) "✅ Acceso a uso concedido" else "❌ Falta acceso a uso",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onOpenUsageAccess()
                            scope.launch { snackbar.showSnackbar("Abriendo ajustes de Acceso a uso…") }
                        }
                    ) { Text("Abrir Acceso a uso") }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Captura / Sync
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    SectionTitle("Captura diaria")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "• Corte del día: 00:00\n• Sync hoy: actualiza el uso del día en curso\n• Capturar cierre: guarda el día anterior completo",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onSyncNow()
                                scope.launch { snackbar.showSnackbar("Sync de hoy lanzado.") }
                            },
                            enabled = hasPerm
                        ) { Text("Sync hoy") }

                        OutlinedButton(
                            onClick = {
                                onRunDailyCaptureNow()
                                scope.launch { snackbar.showSnackbar("Captura de cierre lanzada.") }
                            },
                            enabled = hasPerm
                        ) { Text("Capturar cierre") }
                    }

                    if (!hasPerm) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Necesitas conceder “Acceso a uso” para que funcione la captura.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tracking en tiempo real
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onRealtimeTrackingChange(!realtimeTracking)
                                scope.launch {
                                    snackbar.showSnackbar(
                                        if (!realtimeTracking) {
                                            "Tracking en tiempo real activado."
                                        } else {
                                            "Tracking en tiempo real desactivado."
                                        }
                                    )
                                }
                            }
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Tracking en tiempo real", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text(
                            text = "Actualiza el uso cada 15 minutos",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = realtimeTracking,
                        onCheckedChange = {
                            onRealtimeTrackingChange(it)
                            scope.launch {
                                snackbar.showSnackbar(if (it) "Activado." else "Desactivado.")
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Apariencia
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onDarkModeChange(!darkMode)
                                scope.launch {
                                    snackbar.showSnackbar(
                                        if (!darkMode) "Modo oscuro activado." else "Modo oscuro desactivado."
                                    )
                                }
                            }
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Modo oscuro", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text(
                            text = "Cambia el tema de la app",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = {
                            onDarkModeChange(it)
                            scope.launch { snackbar.showSnackbar(if (it) "Modo oscuro activado." else "Modo oscuro desactivado.") }
                        }
                    )
                }
            }
        }
    }
}
