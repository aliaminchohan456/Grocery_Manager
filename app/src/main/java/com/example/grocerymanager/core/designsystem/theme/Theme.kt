package com.example.grocerymanager.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.color.DarkTokens
import com.example.grocerymanager.core.designsystem.color.LightTokens
import com.example.grocerymanager.core.designsystem.color.OledTokens
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.LocalAppMotion
import com.example.grocerymanager.core.designsystem.typography.AppTypography
import com.example.grocerymanager.core.preferences.ThemeMode

/**
 * Centralized premium design-system color scheme.
 *
 * The fields exposed here are what every screen reads through
 * [AppTheme.colors]. Adding a new color token = adding a field here and a
 * mapping in [lightAppColors] / [darkAppColors].
 */
data class AppColorScheme(
    // Brand
    val brand: Color,
    val brandDark: Color,
    val brandDeep: Color,
    val fresh: Color,
    val navy: Color,

    // Text
    val mutedText: Color,
    val onSurfaceFaint: Color,
    val onSurfaceDisabled: Color,

    // Status
    val warning: Color,
    val warningDark: Color,
    val overBudget: Color,
    val success: Color,

    // Surfaces
    val surfaceVariant: Color,
    val surfaceMint: Color,
    val surfaceGreenSoft: Color,
    val surfaceOrangeSoft: Color,
    val surfaceRedSoft: Color,
    val surfacePurpleSoft: Color,
    val surfaceBlueSoft: Color,
    val surfaceGraySoft: Color,
    val surfaceTealSoft: Color,
    val surfacePinkSoft: Color,
    val surfaceIndigoSoft: Color,
    val surfaceSelected: Color,

    // Lines / borders
    val outline: Color,
    val outlineStrong: Color,

    // Containers
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val onAccent: Color,

    // On-surface roles
    val onSurfaceMuted: Color,
    val onBackground: Color,

    // Surfaces
    val background: Color,
    val surface: Color,
    val elevatedCard: Color,
    val inputSurface: Color,

    // Premium soft accent surfaces (used for selected states).
    val accentSurface: Color,
    val accentSurfaceStrong: Color,
)

val LocalAppColors = staticCompositionLocalOf {
    lightAppColors()
}

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.System }

fun lightAppColors(): AppColorScheme = AppColorScheme(
    brand = BrandColors.PrimaryGreen,
    brandDark = BrandColors.PrimaryGreenDark,
    brandDeep = BrandColors.PrimaryGreenDeep,
    fresh = BrandColors.FreshGreen,
    navy = BrandColors.DarkNavy,
    mutedText = LightTokens.OnSurfaceMuted,
    onSurfaceFaint = LightTokens.OnSurfaceFaint,
    onSurfaceDisabled = BrandColors.MutedText,
    warning = BrandColors.WarningOrange,
    warningDark = BrandColors.WarningDark,
    overBudget = BrandColors.OverBudgetRed,
    success = BrandColors.PrimaryGreen,
    surfaceVariant = LightTokens.SurfaceVariant,
    surfaceMint = BrandColors.SurfaceMint,
    surfaceGreenSoft = BrandColors.SurfaceGreenSoft,
    surfaceOrangeSoft = BrandColors.SurfaceOrangeSoft,
    surfaceRedSoft = BrandColors.SurfaceRedSoft,
    surfacePurpleSoft = BrandColors.SurfacePurpleSoft,
    surfaceBlueSoft = BrandColors.SurfaceBlueSoft,
    surfaceGraySoft = BrandColors.SurfaceGraySoft,
    surfaceTealSoft = BrandColors.SurfaceTealSoft,
    surfacePinkSoft = BrandColors.SurfacePinkSoft,
    surfaceIndigoSoft = BrandColors.SurfaceIndigoSoft,
    surfaceSelected = LightTokens.SurfaceSelectedMint,
    outline = LightTokens.Outline,
    outlineStrong = LightTokens.OutlineVariant,
    primaryContainer = LightTokens.PrimaryContainer,
    onPrimaryContainer = LightTokens.OnPrimaryContainer,
    onAccent = LightTokens.OnAccent,
    onSurfaceMuted = LightTokens.OnSurfaceMuted,
    onBackground = LightTokens.OnBackground,
    background = LightTokens.Background,
    surface = LightTokens.Surface,
    elevatedCard = LightTokens.Surface,
    inputSurface = LightTokens.SurfaceVariant,
    accentSurface = LightTokens.SurfaceGreen12,
    accentSurfaceStrong = LightTokens.SurfaceGreen18,
)

fun darkAppColors(): AppColorScheme = AppColorScheme(
    brand = BrandColors.FreshGreen,
    brandDark = BrandColors.FreshGreenDark,
    brandDeep = BrandColors.FreshGreenDark,
    fresh = BrandColors.FreshGreen,
    navy = DarkTokens.OnSurface,
    mutedText = DarkTokens.OnSurfaceMuted,
    onSurfaceFaint = DarkTokens.OnSurfaceFaint,
    onSurfaceDisabled = Color(0xFF526071),
    warning = BrandColors.WarningOrange,
    warningDark = BrandColors.WarningDark,
    overBudget = BrandColors.OverBudgetRedSoft,
    success = BrandColors.FreshGreen,
    surfaceVariant = DarkTokens.MatteSlate,
    surfaceMint = DarkTokens.SurfaceSelectedMint,
    surfaceGreenSoft = DarkTokens.SurfaceGreen18,
    surfaceOrangeSoft = Color(0xFF2A1F12),
    surfaceRedSoft = Color(0xFF2C1419),
    surfacePurpleSoft = Color(0xFF1F1A30),
    surfaceBlueSoft = Color(0xFF122230),
    surfaceGraySoft = Color(0xFF1B2333),
    surfaceTealSoft = Color(0xFF112B2A),
    surfacePinkSoft = Color(0xFF2A1424),
    surfaceIndigoSoft = Color(0xFF1A1B30),
    surfaceSelected = DarkTokens.SurfaceSelectedMint,
    outline = DarkTokens.Outline,
    outlineStrong = DarkTokens.OutlineVariant,
    primaryContainer = DarkTokens.PrimaryContainer,
    onPrimaryContainer = DarkTokens.OnPrimaryContainer,
    onAccent = DarkTokens.OnAccent,
    onSurfaceMuted = DarkTokens.OnSurfaceMuted,
    onBackground = DarkTokens.OnBackground,
    background = DarkTokens.Background,
    surface = DarkTokens.Surface,
    elevatedCard = DarkTokens.ElevatedCard,
    inputSurface = DarkTokens.InputSurface,
    accentSurface = DarkTokens.SurfaceGreen12,
    accentSurfaceStrong = DarkTokens.SurfaceGreen18,
)

private val LightMaterial = lightColorScheme(
    primary = BrandColors.PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = LightTokens.PrimaryContainer,
    onPrimaryContainer = LightTokens.OnPrimaryContainer,
    secondary = BrandColors.PrimaryGreen,
    onSecondary = Color.White,
    background = LightTokens.Background,
    onBackground = LightTokens.OnBackground,
    surface = LightTokens.Surface,
    onSurface = LightTokens.OnSurface,
    surfaceVariant = LightTokens.SurfaceVariant,
    onSurfaceVariant = LightTokens.OnSurfaceMuted,
    outline = LightTokens.Outline,
    outlineVariant = LightTokens.OutlineVariant,
    error = LightTokens.Error,
    onError = LightTokens.OnError,
)

private val DarkMaterial = darkColorScheme(
    primary = BrandColors.FreshGreen,
    onPrimary = Color(0xFF04101F),
    primaryContainer = DarkTokens.PrimaryContainer,
    onPrimaryContainer = DarkTokens.OnPrimaryContainer,
    secondary = BrandColors.FreshGreen,
    onSecondary = Color(0xFF04101F),
    background = DarkTokens.Background,
    onBackground = DarkTokens.OnBackground,
    surface = DarkTokens.Surface,
    onSurface = DarkTokens.OnSurface,
    surfaceVariant = DarkTokens.MatteSlate,
    onSurfaceVariant = DarkTokens.OnSurfaceMuted,
    outline = DarkTokens.Outline,
    outlineVariant = DarkTokens.OutlineVariant,
    error = DarkTokens.Error,
    onError = DarkTokens.OnError,
)

private val OledMaterial = darkColorScheme(
    primary = BrandColors.FreshGreen,
    onPrimary = Color(0xFF04101F),
    primaryContainer = DarkTokens.PrimaryContainer,
    onPrimaryContainer = DarkTokens.OnPrimaryContainer,
    secondary = BrandColors.FreshGreen,
    onSecondary = Color(0xFF04101F),
    background = OledTokens.Background,
    onBackground = DarkTokens.OnBackground,
    surface = OledTokens.Surface,
    onSurface = DarkTokens.OnSurface,
    surfaceVariant = DarkTokens.MatteSlate,
    onSurfaceVariant = DarkTokens.OnSurfaceMuted,
    outline = DarkTokens.Outline,
    outlineVariant = DarkTokens.OutlineVariant,
    error = DarkTokens.Error,
    onError = DarkTokens.OnError,
)

@Composable
fun GroceryManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: ThemeMode = ThemeMode.System,
    oledBlack: Boolean = false,
    content: @Composable () -> Unit,
) {
    val resolvedDark = when (themeMode) {
        ThemeMode.System -> darkTheme
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.Oled -> true
    }

    val baseMaterial = if (resolvedDark) DarkMaterial else LightMaterial
    val appColors = if (resolvedDark) darkAppColors() else lightAppColors()

    val isOled = themeMode == ThemeMode.Oled ||
        (themeMode == ThemeMode.System && resolvedDark && oledBlack)
    val materialColorScheme = if (isOled) OledMaterial else baseMaterial
    val finalAppColors = if (isOled) {
        appColors.copy(
            background = OledTokens.Background,
            surface = OledTokens.Surface,
            elevatedCard = OledTokens.ElevatedCard,
        )
    } else appColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            val lightBg = materialColorScheme.background.luminance() > 0.5f
            controller.isAppearanceLightStatusBars = lightBg
            controller.isAppearanceLightNavigationBars = lightBg
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides finalAppColors,
        LocalThemeMode provides themeMode,
        LocalAppMotion provides AppEasing.Standard,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = AppTypography,
            shapes = androidx.compose.material3.Shapes(
                extraSmall = AppShapes.InputSmall,
                small = AppShapes.Input,
                medium = AppShapes.CardSmall,
                large = AppShapes.Card,
                extraLarge = AppShapes.HeroCard,
            ),
            content = content,
        )
    }
}

object AppTheme {
    val colors: AppColorScheme
        @Composable get() = LocalAppColors.current
}

fun AppColorScheme.isLight(): Boolean = background.luminance() > 0.5f
