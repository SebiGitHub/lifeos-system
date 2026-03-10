package com.sebi.lifeos.lifeosapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sebi.lifeos.lifeosapp.data.AppUsageAgg
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Year

data class YearUiState(
    val year: String = Year.now().value.toString(),
    val loading: Boolean = false,
    val exporting: Boolean = false,
    val monthTotalsMs: List<Long> = emptyList(),
    val selectedMonthIdx: Int = 0,
    val topAppsSelectedMonth: List<AppUsageAgg> = emptyList(),
    val message: String? = null
)

class YearViewModel(
    private val repo: UsageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(YearUiState())
    val state: StateFlow<YearUiState> = _state

    init {
        loadYear()
    }

    fun setYear(y: String) {
        _state.update { it.copy(year = y) }
    }

    fun loadYear() = viewModelScope.launch {
        val yearStr = _state.value.year.ifBlank { Year.now().value.toString() }

        _state.update {
            it.copy(
                loading = true,
                topAppsSelectedMonth = emptyList()
            )
        }

        try {
            val totalsAgg = repo.monthTotals(yearStr)

            val totals = MutableList(12) { 0L }
            totalsAgg.forEach { row ->
                val mm = row.monthKey.takeLast(2).toIntOrNull()
                if (mm != null && mm in 1..12) totals[mm - 1] = row.totalMs
            }

            val now = LocalDate.now()
            val defaultIdx = if (yearStr == now.year.toString()) (now.monthValue - 1) else 0

            _state.update {
                it.copy(
                    monthTotalsMs = totals,
                    selectedMonthIdx = defaultIdx.coerceIn(0, 11),
                    loading = false
                )
            }

            selectMonth(_state.value.selectedMonthIdx)

        } catch (e: Exception) {
            _state.update { it.copy(loading = false) }
            emitMessage("Error cargando año")
        }
    }

    fun moveSelection(delta: Int) {
        val st = _state.value
        val newIdx = (st.selectedMonthIdx + delta).coerceIn(0, 11)
        if (newIdx == st.selectedMonthIdx) return
        selectMonth(newIdx)
    }

    fun selectMonth(idx: Int) = viewModelScope.launch {
        val yearStr = _state.value.year.ifBlank { Year.now().value.toString() }
        val mm = (idx + 1).toString().padStart(2, '0')
        val monthPrefix = "$yearStr-$mm"

        _state.update { it.copy(selectedMonthIdx = idx, loading = true) }

        try {
            val top = repo.topAppsForMonth(monthPrefix)
            _state.update { it.copy(topAppsSelectedMonth = top, loading = false) }
        } catch (e: Exception) {
            _state.update { it.copy(loading = false) }
            emitMessage("Error cargando mes")
        }
    }

    /**
     * ✅ Exporta el MES seleccionado (nombre: lifeos_usage_MM_YYYY.csv)
     */
    fun exportSelectedMonthCsv() = viewModelScope.launch {
        val st = _state.value
        val yearStr = st.year.ifBlank { Year.now().value.toString() }
        val monthIdx = st.selectedMonthIdx.coerceIn(0, 11)

        _state.update { it.copy(exporting = true) }
        try {
            val res = repo.exportMonthCsvToDownloads(yearStr, monthIdx)
            _state.update { it.copy(exporting = false) }

            res.fold(
                onSuccess = { emitMessage("CSV exportado a Descargas") },
                onFailure = { emitMessage("No hay datos para exportar") }
            )
        } catch (e: Exception) {
            _state.update { it.copy(exporting = false) }
            emitMessage("Error exportando CSV")
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun emitMessage(msg: String) {
        _state.update { it.copy(message = msg) }
        viewModelScope.launch {
            delay(3000)
            _state.update { st -> if (st.message == msg) st.copy(message = null) else st }
        }
    }
}
