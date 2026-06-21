package com.example.grocerymanager.feature.settings

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UnitSystem
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.MaintenanceRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Immutable
data class SettingsUiState(
    val currencyCode: String = "USD",
    val themeMode: ThemeMode = ThemeMode.System,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val alertThreshold: Float = 0.8f,
    val categories: List<Category> = emptyList(),
    val exportStatus: ExportStatus = ExportStatus.Idle,
    val showClearConfirm: Boolean = false,
)

sealed interface ExportStatus {
    data object Idle : ExportStatus
    data object InProgress : ExportStatus
    data class Success(val path: String, val count: Int) : ExportStatus
    data class Error(val message: String) : ExportStatus
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val categories: CategoryRepository,
    private val purchases: PurchaseRepository,
    private val maintenance: MaintenanceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferences.preferences,
                categories.observeCategories(),
            ) { prefs, cats -> prefs to cats }.collect { (prefs, cats) ->
                _state.update {
                    it.copy(
                        currencyCode = prefs.currencyCode,
                        themeMode = prefs.themeMode,
                        unitSystem = prefs.unitSystem,
                        alertThreshold = prefs.alertThreshold,
                        categories = cats,
                    )
                }
            }
        }
    }

    fun setCurrency(code: String) {
        viewModelScope.launch { preferences.setCurrencyCode(code) }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setUnitSystem(system: UnitSystem) {
        viewModelScope.launch { preferences.setUnitSystem(system) }
    }

    fun setAlertThreshold(value: Float) {
        viewModelScope.launch { preferences.setAlertThreshold(value) }
    }

    fun askClearAll() { _state.update { it.copy(showClearConfirm = true) } }
    fun cancelClearAll() { _state.update { it.copy(showClearConfirm = false) } }

    fun clearAll() {
        viewModelScope.launch {
            maintenance.clearAll()
            _state.update { it.copy(showClearConfirm = false) }
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            _state.update { it.copy(exportStatus = ExportStatus.InProgress) }
            // Fetch every purchase + its items in one shot via the @Transaction
            // observeAllWithItems query. No N+1.
            val all = purchases.observeAllWithItems().first()
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "exports").apply { mkdirs() }
                    val name = "grocery_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
                    val out = File(dir, name)
                    val categoryMap = _state.value.categories.associateBy { it.id }
                    out.bufferedWriter().use { w ->
                        w.appendLine("date,shop,item,category,quantity,unit,price_per_unit,total")
                        for (withItems in all) {
                            val purchase = withItems.purchase
                            val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(purchase.purchaseDate))
                            if (withItems.items.isEmpty()) {
                                w.appendLine("${csv(date)},${csv(purchase.shopName)},,,,,")
                            } else {
                                for (item in withItems.items) {
                                    val cat = categoryMap[item.categoryId]?.name ?: ""
                                    w.appendLine(
                                        listOf(
                                            csv(date),
                                            csv(purchase.shopName),
                                            csv(item.itemName),
                                            csv(cat),
                                            item.quantity.toString(),
                                            csv(item.unit),
                                            MoneyUtils.toMajorUnits(item.pricePerUnit ?: 0L).toPlainString(),
                                            MoneyUtils.toMajorUnits(item.totalPrice).toPlainString(),
                                        ).joinToString(","),
                                    )
                                }
                            }
                        }
                    }
                    out.absolutePath to all.size
                }
            }
            result.fold(
                onSuccess = { (path, count) -> _state.update { it.copy(exportStatus = ExportStatus.Success(path, count)) } },
                onFailure = { e -> _state.update { it.copy(exportStatus = ExportStatus.Error(e.message ?: "Export failed")) } },
            )
        }
    }
}

private fun csv(s: String?): String {
    val v = s.orEmpty()
    return if (v.contains(',') || v.contains('"') || v.contains('\n')) {
        "\"" + v.replace("\"", "\"\"") + "\""
    } else v
}
