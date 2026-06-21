package com.example.grocerymanager.feature.setup

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.common.Currencies
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UnitSystem
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.usecase.SetMonthlyBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SetupUiState(
    val currencyCode: String = Currencies.defaultForLocale() ?: "USD",
    val currencySymbol: String = "$",
    val budgetInput: String = "",
    val budgetError: String? = null,
    val alertThreshold: Float = 0.8f,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val themeMode: ThemeMode = ThemeMode.System,
)

private data class EditableFields(
    val budgetInput: String = "",
    val budgetError: String? = null,
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val setBudget: SetMonthlyBudgetUseCase,
) : ViewModel() {

    // Local edit-only state. Held in its own MutableStateFlow so screen edits
    // don't fight with the preference flow on the combine.
    private val editable = MutableStateFlow(EditableFields())

    /**
     * Single `combine` for the screen. The previous implementation also ran
     * a second `viewModelScope.launch { combine(...).collect(...) }` in
     * `init`, which double-collected the same data (P30).
     */
    val state: StateFlow<SetupUiState> = combine(
        preferences.preferences,
        editable,
    ) { prefs, fields ->
        SetupUiState(
            currencyCode = prefs.currencyCode,
            currencySymbol = MoneyUtils.symbolFor(prefs.currencyCode),
            budgetInput = fields.budgetInput,
            budgetError = fields.budgetError,
            alertThreshold = prefs.alertThreshold,
            unitSystem = prefs.unitSystem,
            themeMode = prefs.themeMode,
        )
    }.stateIn(
        viewModelScope,
        // Eagerly so the state always reflects the latest preferences even
        // when no UI subscriber is active (e.g. unit tests). The original
        // screen is one-shot and short-lived so the hot-flow cost is
        // negligible.
        SharingStarted.Eagerly,
        SetupUiState(),
    )

    fun selectCurrency(code: String) {
        viewModelScope.launch { preferences.setCurrencyCode(code) }
    }

    fun setBudget(input: String) {
        val parsed = MoneyUtils.parse(input)
        editable.update {
            it.copy(
                budgetInput = input,
                budgetError = if (parsed == null && input.isNotBlank()) "Enter a valid amount" else null,
            )
        }
    }

    fun clearBudget() {
        editable.update { it.copy(budgetInput = "", budgetError = null) }
    }

    fun setThreshold(value: Float) {
        viewModelScope.launch { preferences.setAlertThreshold(value) }
    }

    fun setUnit(system: UnitSystem) {
        viewModelScope.launch { preferences.setUnitSystem(system) }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun completeSetup() {
        viewModelScope.launch {
            val s = state.value
            val budgetMinor = MoneyUtils.parse(s.budgetInput)
            if (budgetMinor != null && budgetMinor > 0L) {
                val (month, year) = com.example.grocerymanager.core.common.DateUtils.monthAndYear(
                    com.example.grocerymanager.core.common.DateUtils.now(),
                )
                setBudget(month, year, budgetMinor, s.alertThreshold)
            }
            // Persist setup-complete before returning so the router sees
            // the destination change on the very next preference emission
            // (B11).
            preferences.setSetupComplete(true)
        }
    }
}
