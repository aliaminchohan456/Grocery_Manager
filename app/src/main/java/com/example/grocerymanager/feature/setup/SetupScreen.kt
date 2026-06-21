package com.example.grocerymanager.feature.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.Currencies
import com.example.grocerymanager.core.common.CurrencyOption
import com.example.grocerymanager.core.designsystem.components.AmountInputField
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.PrimaryButton
import com.example.grocerymanager.core.designsystem.components.SecondaryButton
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.SegmentedChip
import com.example.grocerymanager.core.designsystem.components.TextLink
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UnitSystem
import com.example.grocerymanager.core.preferences.icon
import com.example.grocerymanager.core.preferences.label
import com.example.grocerymanager.navigation.HomeRoute
import com.example.grocerymanager.navigation.SelectCurrencyRoute

@Composable
fun SetupScreen(
    navController: NavHostController,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Double-tap guard: a rapid second tap on Finish would otherwise call
    // `viewModel.completeSetup()` twice (inserting two budget rows for the
    // same month) and then `navigate(HomeRoute) { popUpTo(0) ... }` on an
    // already-empty back stack. The flag resets on screen dispose so a
    // re-entry into Setup starts fresh.
    var inFlight by remember { mutableStateOf(false) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { inFlight = false }
    }

    Scaffold(
        bottomBar = {
            // Finish is always reachable. The Scaffold resizes the content area
            // when the IME shows, so the budget input is never hidden behind the
            // bottom bar. Phase 1 P0: explicit imePadding + navigationBarsPadding
            // on the sticky CTA Box so the keyboard never overlaps the button.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.action_finish),
                    onClick = {
                        // Guard against a double-tap that would otherwise
                        // call `viewModel.completeSetup()` twice (inserting
                        // two budget rows for the same month) and run
                        // `navController.navigate(HomeRoute) { popUpTo(0) ... }`
                        // a second time on an empty back stack.
                        if (!inFlight) {
                            inFlight = true
                            viewModel.completeSetup()
                            navController.navigate(HomeRoute) { popUpTo(0) { inclusive = true } }
                        }
                    },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.setup_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        stringResource(R.string.setup_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceMuted,
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(title = stringResource(R.string.setup_currency_section))
                    CurrencyCard(
                        currencyCode = state.currencyCode,
                        onClick = { navController.navigate(SelectCurrencyRoute) },
                    )
                }
            }

            item {
                BudgetCard(
                    budgetInput = state.budgetInput,
                    budgetError = state.budgetError,
                    currencySymbol = state.currencySymbol,
                    currencyCode = state.currencyCode,
                    alertThreshold = state.alertThreshold,
                    onBudgetChange = viewModel::setBudget,
                    onClear = viewModel::clearBudget,
                    onThresholdChange = viewModel::setThreshold,
                )
            }

            item {
                UnitSection(state.unitSystem, onSelect = viewModel::setUnit)
            }

            item {
                ThemeSection(state.themeMode, onSelect = viewModel::setTheme)
            }
        }
    }
}

@Composable
private fun CurrencyCard(currencyCode: String, onClick: () -> Unit) {
    val currency: CurrencyOption = remember(currencyCode) {
        Currencies.supported.firstOrNull { it.code == currencyCode }
            ?: Currencies.supported.first { it.code == "USD" }
    }
    GroceryCard(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.brand.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CurrencyExchange,
                    contentDescription = null,
                    tint = AppTheme.colors.brand,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = currency.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceMuted,
                )
            }
            Text(
                text = stringResource(R.string.setup_currency_change),
                style = MaterialTheme.typography.labelLarge,
                color = AppTheme.colors.brand,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.size(4.dp))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.brand,
            )
        }
    }
}

@Composable
private fun BudgetCard(
    budgetInput: String,
    budgetError: String?,
    currencySymbol: String,
    currencyCode: String,
    alertThreshold: Float,
    onBudgetChange: (String) -> Unit,
    onClear: () -> Unit,
    onThresholdChange: (Float) -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val hasBudget = budgetInput.isNotBlank()
    val isValid = budgetError == null

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = stringResource(R.string.setup_budget_section))
        GroceryCard(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AmountInputField(
                    value = budgetInput,
                    onValueChange = onBudgetChange,
                    label = "${currencyCode} ${stringResource(R.string.setup_budget_label)}",
                    currencySymbol = currencySymbol,
                    isError = budgetError != null,
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                    ),
                    // Emerald-tinted border so the budget input stands out from
                    // a distance — the only "active" field on this screen.
                    borderColor = AppTheme.colors.brand,
                )
                Text(
                    text = stringResource(R.string.setup_budget_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceMuted,
                )
                // The Skip / Clear action uses the design-system
                // SecondaryButton with a tone flip: emerald for the
                // pre-budget "Skip — set later" affordance, destructive
                // red once a budget value is entered so the user sees
                // the action will remove their input.
                SecondaryButton(
                    text = if (hasBudget) stringResource(R.string.setup_budget_clear)
                    else stringResource(R.string.action_skip_set_later),
                    onClick = {
                        onClear()
                        keyboard?.hide()
                    },
                    tone = if (hasBudget) com.example.grocerymanager.core.designsystem.theme.ButtonTone.Destructive
                    else com.example.grocerymanager.core.designsystem.theme.ButtonTone.Neutral,
                )
                if (budgetError != null) {
                    Text(
                        text = stringResource(R.string.setup_budget_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.overBudget,
                    )
                }
                // Alert slider only when a valid budget is entered.
                AnimatedVisibility(visible = hasBudget && isValid) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.setup_budget_alert, (alertThreshold * 100).toInt()),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Slider(
                            value = alertThreshold,
                            onValueChange = onThresholdChange,
                            valueRange = 0.5f..1.0f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = AppTheme.colors.brand,
                                activeTrackColor = AppTheme.colors.brand,
                                inactiveTrackColor = AppTheme.colors.outline,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitSection(selected: UnitSystem, onSelect: (UnitSystem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = stringResource(R.string.setup_units_section))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SegmentedChip(
                label = stringResource(R.string.unit_metric),
                selected = selected == UnitSystem.Metric,
                onClick = { onSelect(UnitSystem.Metric) },
            )
            SegmentedChip(
                label = stringResource(R.string.unit_imperial),
                selected = selected == UnitSystem.Imperial,
                onClick = { onSelect(UnitSystem.Imperial) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeSection(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = stringResource(R.string.setup_theme_section))
        // FlowRow wraps chips to a second line on narrow screens so OLED is
        // never clipped and no horizontal scroll is needed.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ThemeMode.entries.forEach { mode ->
                SegmentedChip(
                    label = mode.label(),
                    selected = selected == mode,
                    onClick = { onSelect(mode) },
                    leadingIcon = mode.icon(),
                )
            }
        }
    }
}
