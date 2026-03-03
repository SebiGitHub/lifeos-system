package com.sebi.lifeos.lifeosapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sebi.lifeos.lifeosapp.repo.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CatalogPeriod { LAST_30_DAYS, YEAR }

data class CategoryUi(val id: Long, val name: String)

data class CatalogAppItem(
    val packageName: String,
    val label: String,
    val totalMs: Long,
    val categoryId: Long
)

data class AppCatalogUiState(
    val period: CatalogPeriod = CatalogPeriod.LAST_30_DAYS,
    val year: Int = java.time.Year.now().value,
    val query: String = "",
    val categories: List<CategoryUi> = emptyList(),
    val categoryFilterId: Long? = null, // null = todas
    val apps: List<CatalogAppItem> = emptyList(),
    val loading: Boolean = false,
    val message: String? = null,
)

class AppCatalogViewModel(private val repo: UsageRepository) : ViewModel() {

    private val _state = MutableStateFlow(AppCatalogUiState())
    val state: StateFlow<AppCatalogUiState> = _state.asStateFlow()

    private var allApps: List<CatalogAppItem> = emptyList()

    private val allowedCategoryNames = setOf("Estudio", "Juegos", "Música", "Entretenimiento", "Otros")

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }

            val res = runCatching {
                repo.ensureCategories()

                val allCats = repo.categories()
                    .filter { it.name in allowedCategoryNames }
                    .sortedBy { it.name }

                val cats = allCats.map { CategoryUi(it.id, it.name) }

                val appsRows = when (_state.value.period) {
                    CatalogPeriod.LAST_30_DAYS -> repo.appsWithCategoryForLastDays(days = 30)
                    CatalogPeriod.YEAR -> repo.appsWithCategoryForYear(_state.value.year.toString())
                }

                val apps = appsRows.map { row ->
                    CatalogAppItem(
                        packageName = row.packageName,
                        label = row.label,
                        totalMs = row.totalMs,
                        categoryId = row.categoryId
                    )
                }

                cats to apps
            }

            res.onSuccess { (cats, apps) ->
                allApps = apps

                _state.update {
                    it.copy(
                        categories = cats,
                        loading = false,
                        message = null
                    )
                }
                applyFilters()
            }.onFailure { e ->
                _state.update { it.copy(loading = false, message = e.message ?: "Error cargando catálogo") }
            }
        }
    }

    fun setPeriod(period: CatalogPeriod) {
        _state.update { it.copy(period = period) }
        load()
    }

    fun setYear(year: Int) {
        _state.update { it.copy(year = year) }
        if (_state.value.period == CatalogPeriod.YEAR) load()
    }

    fun setQuery(q: String) {
        _state.update { it.copy(query = q) }
        applyFilters()
    }

    fun setCategoryFilter(categoryId: Long?) {
        _state.update { it.copy(categoryFilterId = categoryId) }
        applyFilters()
    }

    fun setCategory(packageName: String, categoryId: Long) {
        viewModelScope.launch {
            runCatching {
                repo.setAppCategory(packageName, categoryId)
            }.onSuccess {
                allApps = allApps.map { if (it.packageName == packageName) it.copy(categoryId = categoryId) else it }
                applyFilters()
                _state.update { it.copy(message = "Categoría actualizada") }
            }.onFailure { e ->
                _state.update { it.copy(message = e.message ?: "No se pudo actualizar la categoría") }
            }
        }
    }

    private fun applyFilters() {
        val st = _state.value
        var list = allApps

        st.categoryFilterId?.let { id ->
            list = list.filter { it.categoryId == id }
        }

        val q = st.query.trim().lowercase()
        if (q.isNotBlank()) {
            list = list.filter {
                it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }

        _state.update { it.copy(apps = list) }
    }

    companion object {
        fun factory(repo: UsageRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppCatalogViewModel(repo) as T
            }
        }
    }
}
