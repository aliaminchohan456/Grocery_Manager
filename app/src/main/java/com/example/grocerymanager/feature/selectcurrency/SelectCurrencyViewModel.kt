package com.example.grocerymanager.feature.selectcurrency

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.common.Currencies
import com.example.grocerymanager.core.common.CurrencyOption
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SelectCurrencyUiState(
    val query: String = "",
    val selectedCode: String = "USD",
    val filtered: List<CurrencyOption> = EmptyAll,
)

private val EmptyAll: List<CurrencyOption> = emptyList()

@HiltViewModel
class SelectCurrencyViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    /**
     * Eagerly so the state is always live for unit tests that read
     * `state.value` directly. B6 was a perf concern but the test
     * contract is more important to preserve; the eager cost is a
     * single combined read of the supported list, which is cached.
     */
    val state: StateFlow<SelectCurrencyUiState> = combine(
        preferences.preferences,
        query,
    ) { prefs, q ->
        val trimmed = q.trim()
        val filtered = if (trimmed.isEmpty()) {
            Currencies.supported
        } else {
            Currencies.supported.filter {
                it.code.contains(trimmed, ignoreCase = true) ||
                    it.displayName.contains(trimmed, ignoreCase = true) ||
                    it.symbol.contains(trimmed, ignoreCase = true)
            }
        }
        SelectCurrencyUiState(
            query = q,
            selectedCode = prefs.currencyCode,
            filtered = filtered,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SelectCurrencyUiState(),
    )

    fun setQuery(value: String) {
        query.value = value
    }

    fun selectCurrency(code: String) {
        viewModelScope.launch { preferences.setCurrencyCode(code) }
    }

    /**
     * Persist a user-entered currency code (e.g. "NZD", "CHF", "BTC") that
     * is not in the static [Currencies.supported] list. `setCurrencyCode`
     * accepts any string and the display path falls back to the code itself
     * for unknown codes (see [com.example.grocerymanager.core.common.MoneyUtils]).
     */
    fun selectCustomCurrency(code: String) {
        val normalised = code.trim().uppercase()
        if (normalised.isEmpty()) return
        viewModelScope.launch { preferences.setCurrencyCode(normalised) }
    }
}
