package com.example.grocerymanager.feature.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.ConfirmDialog
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.MagneticButton
import com.example.grocerymanager.core.designsystem.components.PremiumSnackbarHost
import com.example.grocerymanager.core.designsystem.components.SecondaryButton
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.SegmentedChip
import com.example.grocerymanager.core.designsystem.components.SettingRow
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.ButtonTone
import com.example.grocerymanager.core.designsystem.theme.CardPadding
import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UnitSystem
import com.example.grocerymanager.core.preferences.icon
import com.example.grocerymanager.core.preferences.label
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    onOpenCategories: () -> Unit,
    onOpenBudget: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var supportMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.exportStatus) {
        when (val s = state.exportStatus) {
            is ExportStatus.Success -> snackbar.showSnackbar(
                context.getString(R.string.settings_export_success, s.count, s.path),
            )
            is ExportStatus.Error -> snackbar.showSnackbar(
                context.getString(R.string.settings_export_failed, s.message),
            )
            else -> Unit
        }
    }
    LaunchedEffect(supportMessage) {
        val msg = supportMessage ?: return@LaunchedEffect
        snackbar.showSnackbar(msg)
        supportMessage = null
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_title),
                onBack = { navController.popBackStack() },
            )
        },
        // Phase 5: swap stock `SnackbarHost` for `PremiumSnackbarHost` so
        // the settings snackbar matches the rest of the app.
        snackbarHost = { PremiumSnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .navigationBarsPadding(),
            // Phase 5: standardise to ScreenEdgeHorizontal.
            contentPadding = PaddingValues(
                start = AppSizing.ScreenEdgeHorizontal,
                end = AppSizing.ScreenEdgeHorizontal,
                top = AppSpacing.sm,
                bottom = AppSizing.BottomNavHeight + AppSpacing.xl,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            // ---- Currency ----
            item {
                SectionHeader(
                    title = stringResource(R.string.settings_currency_section),
                )
            }
            item {
                // Phase 5: `SettingRow` for the read-only currency display.
                SettingRow(
                    title = state.currencyCode,
                    subtitle = stringResource(R.string.settings_currency_subtitle),
                    leadingIcon = AppIcons.CurrencyExchange,
                    showChevron = false,
                    onClick = null,
                )
            }
            item {
                GroceryCard(cardPadding = CardPadding.Tight) {
                    SegmentedChoiceRow(
                        options = com.example.grocerymanager.core.common.Currencies.supported.map { it.code },
                        selected = state.currencyCode,
                        onSelect = { viewModel.setCurrency(it) },
                    )
                }
            }

            // ---- Theme ----
            item {
                SectionHeader(
                    title = stringResource(R.string.settings_theme_section),
                )
            }
            item {
                // Phase 5: precompute labels and a lookup map in the
                // composable scope so the @Composable `label()` extension
                // doesn't have to be called from inside a non-Composable
                // lambda later.
                val themeOptions = ThemeMode.entries.map { it.label() }
                val selectedThemeLabel = state.themeMode.label()
                val themeLabelToMode = ThemeMode.entries.associateBy { it.label() }
                GroceryCard(cardPadding = CardPadding.Tight) {
                    SegmentedChoiceRow(
                        options = themeOptions,
                        selected = selectedThemeLabel,
                        onSelect = { label ->
                            themeLabelToMode[label]?.let { viewModel.setTheme(it) }
                        },
                        leadingIcon = { mode ->
                            themeLabelToMode[mode]?.icon()
                        },
                    )
                }
            }

            // ---- Units ----
            item {
                SectionHeader(
                    title = stringResource(R.string.settings_units_section),
                )
            }
            item {
                // Phase 5: precompute labels and a lookup map in the
                // composable scope so the @Composable `label()` extension
                // doesn't have to be called from inside a non-Composable
                // lambda later.
                val unitOptions = UnitSystem.entries.map { it.label() }
                val selectedUnitLabel = state.unitSystem.label()
                val unitLabelToSystem = UnitSystem.entries.associateBy { it.label() }
                GroceryCard(cardPadding = CardPadding.Tight) {
                    SegmentedChoiceRow(
                        options = unitOptions,
                        selected = selectedUnitLabel,
                        onSelect = { label ->
                            unitLabelToSystem[label]?.let { viewModel.setUnitSystem(it) }
                        },
                    )
                }
            }

            // ---- Budget alert ----
            item {
                SectionHeader(
                    title = stringResource(R.string.settings_budget_alert_section),
                )
            }
            item {
                GroceryCard {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        Text(
                            stringResource(
                                R.string.settings_alert_label,
                                (state.alertThreshold * 100).toInt(),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Slider(
                            value = state.alertThreshold,
                            onValueChange = viewModel::setAlertThreshold,
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

            // ---- Manage ----
            item {
                SectionHeader(
                    title = stringResource(R.string.settings_manage_section),
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    SettingRow(
                        title = stringResource(R.string.settings_categories_button, state.categories.size),
                        leadingIcon = AppIcons.Category,
                        showChevron = true,
                        onClick = onOpenCategories,
                        centerContent = true,
                    )
                    SettingRow(
                        title = stringResource(R.string.settings_budget_button),
                        leadingIcon = AppIcons.WalletAlt,
                        showChevron = true,
                        onClick = onOpenBudget,
                        centerContent = true,
                    )
                }
            }

            // ---- Data ----
            item {
                SectionHeader(
                    title = stringResource(R.string.settings_data_section),
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    GroceryCard(cardPadding = CardPadding.Tight) {
                        // Phase 5: export action becomes a SettingRow so
                        // it sits inline with the other actions.
                        SettingRow(
                            title = stringResource(R.string.action_export_csv),
                            leadingIcon = AppIcons.Export,
                            trailing = {
                                if (state.exportStatus is ExportStatus.InProgress) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        color = AppTheme.colors.brand,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(AppSpacing.lg),
                                    )
                                }
                            },
                            showChevron = false,
                            onClick = {
                                if (state.exportStatus !is ExportStatus.InProgress) {
                                    viewModel.exportCsv(context.applicationContext as Application)
                                }
                            },
                        )
                    }
                    SecondaryClearAllRow(
                        title = stringResource(R.string.action_clear_all_data),
                        onClick = viewModel::askClearAll,
                    )
                }
            }

            // ---- Support ----
            item {
                SectionHeader(
                    title = stringResource(R.string.settings_support_title),
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.settings_help_action),
                    leadingIcon = AppIcons.Mail,
                    showChevron = true,
                    onClick = {
                        if (!launchSupportEmail(context)) {
                            supportMessage = context.getString(R.string.settings_help_no_email_app)
                        }
                    },
                )
            }

            item {
                Spacer(modifier = Modifier.padding(top = AppSpacing.md))
                Text(
                    text = stringResource(R.string.settings_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    if (state.showClearConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.settings_clear_confirm_title),
            message = stringResource(R.string.settings_clear_confirm_message),
            confirmLabel = stringResource(R.string.action_delete),
            icon = AppIcons.Delete,
            onConfirm = viewModel::clearAll,
            onDismiss = viewModel::cancelClearAll,
        )
    }
}

/**
 * Inline row of `SegmentedChip` values. Used for single-select pickers
 * (currency / theme / units) inside the Settings screen. Wraps to a second
 * line via FlowRow when the option set is wider than the screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SegmentedChoiceRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    leadingIcon: ((String) -> androidx.compose.ui.graphics.vector.ImageVector?)? = null,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        options.forEach { option ->
            SegmentedChip(
                label = option,
                selected = option == selected,
                onClick = { onSelect(option) },
                leadingIcon = leadingIcon?.invoke(option),
            )
        }
    }
}

/**
 * Destructive clear-all row — uses [MagneticSecondaryButton] directly
 * so the row reads as an action button rather than a settings row.
 */
@Composable
private fun SecondaryClearAllRow(
    title: String,
    onClick: () -> Unit,
) {
    // Phase 5: use SecondaryButton with the new tone param (Magnetic*
    // variants don't take tone). Destructive red so the user reads it
    // as "this wipes everything".
    SecondaryButton(
        text = title,
        onClick = onClick,
        leadingIcon = AppIcons.Delete,
        tone = ButtonTone.Destructive,
    )
}

private fun launchSupportEmail(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:support@grocerymanager.app")
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.settings_help_email_subject))
    }
    return runCatching { context.startActivity(intent) }.isSuccess
}