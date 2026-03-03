package com.sebi.lifeos.lifeosapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sebi.lifeos.lifeosapp.data.AppUsageAgg
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import com.sebi.lifeos.lifeosapp.util.DayKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RankingUiState(
    val dayKey: String = "",
    val topDay: List<AppUsageAgg> = emptyList(),
    val rankList: List<AppUsageAgg> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class RankingViewModel(
    private val repo: UsageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RankingUiState())
    val state: StateFlow<RankingUiState> = _state

    init {
        refresh()
    }

    fun refresh() {
        val today = DayKey.todayKey()
        _state.update { it.copy(dayKey = today, loading = true, error = null) }

        viewModelScope.launch {
            runCatching {
                repo.topAppsForDay(today, limit = 50)
            }.onSuccess { list ->
                _state.update {
                    it.copy(
                        topDay = list,
                        rankList = list,
                        loading = false
                    )
                }
            }.onFailure { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Error") }
            }
        }
    }
}
