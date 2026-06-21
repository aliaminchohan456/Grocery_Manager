package com.example.grocerymanager.feature.budget

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.DateUtils
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.domain.model.Budget
import com.example.grocerymanager.domain.model.BudgetStatus
import com.example.grocerymanager.domain.repository.BudgetRepository
import com.example.grocerymanager.domain.usecase.GetBudgetStatusUseCase
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
data class BudgetUiState(
    val currencyCode: String = "USD",
    val status: BudgetStatus? = null,
    val recent: List<Budget> = emptyList(),
    val showEditor: Boolean = false,
    val editorAmount: String = "",
    val editorError: String? = null,
    val editorThreshold: Float = 0.8f,
    val monthLabel: String = "",
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val budgetRepo: BudgetRepository,
    private val setBudget: SetMonthlyBudgetUseCase,
    private val getStatus: GetBudgetStatusUseCase,
) : ViewModel() {

    // The screen derives everything from a single combine so the
    // editor-draft state and the persistent budget state stay in lock-step.
    // The previous implementation kept a separate MutableStateFlow
    // `_state` that the `state` flow read inside its combine block — that
    // worked at runtime but was racy under StandardTestDispatcher.
    private val showEditor = MutableStateFlow(false)
    private val editor = MutableStateFlow(EditorDraft())

    private data class EditorDraft(
        val amount: String = "",
        val error: String? = null,
        val threshold: Float = 0.8f,
    )

    val state: StateFlow<BudgetUiState> = combine(
        preferences.preferences,
        getStatus.observe(),
        budgetRepo.observeRecent(6),
        showEditor,
        editor,
    ) { prefs, status, recent, isOpen, draft ->
        val monthLabel = DateFormat.monthYear(DateUtils.now())
        BudgetUiState(
            currencyCode = prefs.currencyCode,
            status = status,
            recent = recent,
            showEditor = isOpen,
            editorAmount = draft.amount,
            editorError = draft.error,
            editorThreshold = draft.threshold,
            monthLabel = monthLabel,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        BudgetUiState(),
    )

    fun openEditor() {
        val status = state.value.status
        editor.value = EditorDraft(
            amount = status?.budget?.budgetAmount?.let { amt -> MoneyUtils.toMajorUnits(amt).toPlainString() } ?: "",
            threshold = status?.budget?.alertThreshold ?: 0.8f,
        )
        showEditor.value = true
    }

    fun closeEditor() {
        showEditor.value = false
        editor.value = EditorDraft()
    }

    fun setEditorAmount(value: String) {
        editor.update {
            it.copy(
                amount = value,
                error = if (value.isNotBlank() && MoneyUtils.parse(value) == null) "INVALID" else null,
            )
        }
    }

    fun setEditorThreshold(value: Float) {
        editor.update { it.copy(threshold = value) }
    }

    fun saveEditor() {
        val draft = editor.value
        val amount = MoneyUtils.parse(draft.amount) ?: return
        if (amount <= 0L) return
        viewModelScope.launch {
            val (month, year) = DateUtils.monthAndYear(DateUtils.now())
            setBudget(month, year, amount, draft.threshold)
            showEditor.value = false
            editor.value = EditorDraft()
        }
    }

    fun clearBudget() {
        viewModelScope.launch {
            val current = state.value.status?.budget
            if (current != null) {
                budgetRepo.delete(current.id)
            }
            // Reset the editor draft so the next "Set budget" opens blank
            // (B19). Without this, the editor would still show the deleted
            // budget's threshold.
            editor.value = EditorDraft()
            showEditor.value = false
        }
    }
}
