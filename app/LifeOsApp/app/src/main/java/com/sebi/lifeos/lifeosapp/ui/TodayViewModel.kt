package com.sebi.lifeos.lifeosapp.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sebi.lifeos.lifeosapp.data.AppUsageAgg
import com.sebi.lifeos.lifeosapp.domain.Series
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import com.sebi.lifeos.lifeosapp.usage.UsageAccess
import com.sebi.lifeos.lifeosapp.util.DayKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodayUiState(
    val hasUsagePermission: Boolean = false,
    val loading: Boolean = false,
    val dayKeys: List<String> = emptyList(),     // 7 días
    val dayTotalsMs: List<Long> = emptyList(),   // alineado con dayKeys
    val selectedIndex: Int = 0,
    val topApps: List<AppUsageAgg> = emptyList(),
    val message: String? = null
)

class TodayViewModel(
    private val repo: UsageRepository,
    private val contextProvider: () -> Context
) : ViewModel() {

    private val _state = MutableStateFlow(TodayUiState())
    val state: StateFlow<TodayUiState> = _state

    init { refresh() }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun refresh() {
        viewModelScope.launch {
            val ctx = contextProvider()
            val hasPerm = UsageAccess.hasUsageAccess(ctx)

            _state.update { it.copy(hasUsagePermission = hasPerm, loading = true) }

            if (!hasPerm) {
                _state.update { it.copy(loading = false, dayKeys = emptyList(), dayTotalsMs = emptyList(), topApps = emptyList()) }
                return@launch
            }

            runCatching {
                val keys = DayKey.lastDays(7)
                val rows = repo.dayTotals(keys)
                val totals = Series.buildDailyTotals(keys, rows)
                val selected = (keys.size - 1).coerceAtLeast(0)
                val dayKey = keys.getOrNull(selected) ?: DayKey.todayKey()
                val top = repo.topAppsForDay(dayKey)

                _state.update {
                    it.copy(
                        loading = false,
                        dayKeys = keys,
                        dayTotalsMs = totals,
                        selectedIndex = selected,
                        topApps = top
                    )
                }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        loading = false,
                        message = "Error cargando Hoy: ${e.message ?: "desconocido"}"
                    )
                }
            }
        }
    }

    fun moveSelection(delta: Int) {
        val st = _state.value
        if (st.dayKeys.isEmpty()) return
        val newIndex = (st.selectedIndex + delta).coerceIn(0, st.dayKeys.lastIndex)
        selectIndex(newIndex)
    }

    fun selectIndex(index: Int) {
        val st = _state.value
        if (st.dayKeys.isEmpty()) return

        val idx = index.coerceIn(0, st.dayKeys.lastIndex)
        val key = st.dayKeys[idx]

        viewModelScope.launch {
            _state.update { it.copy(selectedIndex = idx, loading = true) }

            runCatching {
                val top = repo.topAppsForDay(key)
                _state.update { it.copy(loading = false, topApps = top) }
            }.onFailure { e ->
                _state.update { it.copy(loading = false, message = "Error cargando apps: ${e.message ?: "desconocido"}") }
            }
        }
    }

    fun notifySyncStarted() {
        _state.update { it.copy(message = "Sincronización lanzada") }
    }
}
