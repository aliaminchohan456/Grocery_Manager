package com.example.grocerymanager.feature.selectcurrency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.Currencies
import com.example.grocerymanager.core.common.CurrencyOption
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.ConfirmDialog
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.PremiumSearchField
import com.example.grocerymanager.core.designsystem.components.TextInputField
import com.example.grocerymanager.core.designsystem.components.TextLink
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme

@Composable
fun SelectCurrencyScreen(
    navController: NavHostController,
    viewModel: SelectCurrencyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Double-tap guard: a rapid second tap on a currency row would
    // otherwise run `viewModel.selectCurrency(...)` twice and then
    // `navController.popBackStack()` twice, which can over-pop and crash.
    var inFlight by remember { mutableStateOf(false) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { inFlight = false }
    }

    // Custom-currency dialog state. When non-null, the dialog is shown and
    // the user's typed code is the draft. On confirm, the code is persisted
    // via `viewModel.selectCustomCurrency(...)` and we pop back.
    var customDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.select_currency_title),
                onBack = { navController.popBackStack() },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
        ) {
            // Search field.
            PremiumSearchField(
                query = state.query,
                onQueryChange = viewModel::setQuery,
                placeholder = stringResource(R.string.select_currency_search_hint),
            )

            if (state.filtered.isEmpty()) {
                // Phase 5: replace bare `Text` empty state with the design-
                // system `EmptyState` primitive so the search no-results
                // screen matches the rest of the app.
                EmptyState(
                    title = stringResource(R.string.select_currency_no_results_title),
                    body = stringResource(R.string.select_currency_no_results_body),
                    icon = AppIcons.Search,
                    eyebrow = stringResource(R.string.select_currency_no_results_eyebrow),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 4.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.filtered, key = { it.code }) { currency ->
                        if (currency.isCustomSentinel) {
                            // The "Custom" entry opens the input dialog
                            // instead of selecting a code. The dialog is
                            // hoisted to the screen scope (not the row)
                            // so the row's `onClick` is a stable
                            // no-arg callback.
                            CurrencyRow(
                                currency = currency,
                                selected = false,
                                onClick = { customDialogOpen = true },
                            )
                        } else {
                            CurrencyRow(
                                currency = currency,
                                selected = currency.code == state.selectedCode,
                                onClick = {
                                    if (inFlight) return@CurrencyRow
                                    inFlight = true
                                    viewModel.selectCurrency(currency.code)
                                    navController.popBackStack()
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (customDialogOpen) {
        CustomCurrencyDialog(
            onDismiss = { customDialogOpen = false },
            onConfirm = { code ->
                customDialogOpen = false
                viewModel.selectCustomCurrency(code)
                navController.popBackStack()
            },
        )
    }
}

/**
 * "Enter a custom currency code" dialog. Validates that the input is
 * exactly 3 ASCII letters; the code is uppercased before being persisted.
 * The dialog mirrors the visual style of the surrounding app (rounded
 * `AppShapes.Dialog`, brand-coloured confirm button, muted cancel).
 */
@Composable
private fun CustomCurrencyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val valid = input.trim().length == 3 && input.all { it.isLetter() }
    val showError = !valid && input.isNotEmpty()
    // Phase 5: replace the stock `AlertDialog` with the design-system
    // `ConfirmDialog` (now extended with a `content` slot) so the custom-
    // currency dialog shares the brand-coloured confirm button + frosted
    // header with the rest of the app.
    ConfirmDialog(
        title = stringResource(R.string.select_currency_custom_title),
        message = stringResource(R.string.select_currency_custom_description),
        confirmLabel = stringResource(R.string.action_select),
        dismissLabel = stringResource(R.string.action_cancel),
        destructive = false,
        icon = AppIcons.CurrencyExchange,
        onConfirm = { if (valid) onConfirm(input.trim()) },
        onDismiss = onDismiss,
        content = {
            TextInputField(
                value = input,
                onValueChange = { input = it.uppercase().take(3) },
                label = stringResource(R.string.select_currency_custom_label),
                placeholder = stringResource(R.string.select_currency_custom_hint),
                singleLine = true,
                isError = showError,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
            )
        },
    )
}

@Composable
private fun CurrencyRow(currency: CurrencyOption, selected: Boolean, onClick: () -> Unit) {
    // Phase 1 P0: CurrencyRow now uses the design-system GroceryCard so
    // it shares the card chrome, border treatment, and selection styling
    // with the rest of the app (previously this row rendered its own
    // bespoke `Row(.background + .clip)` container).
    GroceryCard(
        onClick = onClick,
        selected = selected,
        background = if (selected) AppTheme.colors.brand.copy(alpha = 0.14f)
        else AppTheme.colors.surfaceVariant,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Circular code badge.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) AppTheme.colors.brand.copy(alpha = 0.20f)
                        else AppTheme.colors.brand.copy(alpha = 0.10f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (currency.isCustomSentinel) {
                    // The Custom sentinel has no symbol or code to badge; show
                    // a "+" affordance so the badge is visually meaningful
                    // and the row reads as "add a new one".
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = AppTheme.colors.brand,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(
                        text = currency.symbol,
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.colors.brand,
                    )
                }
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) AppTheme.colors.brand
                    else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = currency.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) AppTheme.colors.brand else AppTheme.colors.onSurfaceMuted,
                )
            }
            if (selected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.action_selected),
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.colors.brand,
                    )
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.brand,
                        modifier = Modifier.size(18.dp),
                    )
                }
            } else {
                TextLink(
                    text = stringResource(R.string.action_select),
                    onClick = onClick,
                )
            }
        }
    }
}
