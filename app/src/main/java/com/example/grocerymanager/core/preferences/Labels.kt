package com.example.grocerymanager.core.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.icons.AppIcons

/**
 * Friendly labels for [ThemeMode] / [UnitSystem] so the UI never renders raw enum names
 * like "SYSTEM" or "METRIC". Centralized so Settings + Setup show the same text.
 */
@Composable
fun ThemeMode.label(): String = when (this) {
    ThemeMode.System -> stringResource(R.string.theme_system)
    ThemeMode.Light -> stringResource(R.string.theme_light)
    ThemeMode.Dark -> stringResource(R.string.theme_dark)
    ThemeMode.Oled -> stringResource(R.string.theme_oled)
}

fun ThemeMode.icon(): ImageVector = when (this) {
    ThemeMode.System -> AppIcons.PhoneAndroid
    ThemeMode.Light -> AppIcons.LightMode
    ThemeMode.Dark -> AppIcons.Brightness4
    ThemeMode.Oled -> AppIcons.Dark
}

@Composable
fun UnitSystem.label(): String = when (this) {
    UnitSystem.Metric -> stringResource(R.string.unit_metric)
    UnitSystem.Imperial -> stringResource(R.string.unit_imperial)
}
