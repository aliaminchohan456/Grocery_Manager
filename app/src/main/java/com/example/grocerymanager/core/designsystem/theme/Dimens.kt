package com.example.grocerymanager.core.designsystem.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object AppSpacing {
    /** 2dp — micro-rhythm between tightly related elements (icon → badge gap, etc.). */
    val tiny = 2.dp
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp

    val heroPadding = 24.dp
    val screenTop = 20.dp
}

object AppSizing {
    val MinTouchTarget = 48.dp
    val PrimaryButtonHeight = 60.dp
    val SecondaryButtonHeight = 52.dp
    val InputHeight = 56.dp
    val SearchBarHeight = 56.dp
    val BottomNavHeight = 76.dp
    val FabSize = 58.dp
    val StatCardHeight = 92.dp
    val PurchaseRowHeight = 76.dp
    val CategoryRowHeight = 72.dp
    val OnboardingHeroSize = 132.dp

    // ----- Premium redesign additions -----

    /** Small inline icon badge — used inside `StatCard`, list rows. */
    val IconBadgeSmall = 22.dp

    /** Medium icon badge — used inside `InsightCard`, category rows. */
    val IconBadgeMedium = 28.dp

    /** Large icon badge — used inside `PurchaseCard`, `CurrencySelectorCard`. */
    val IconBadgeLarge = 40.dp

    /** Hero icon badge — outer ring of the empty-state glow icon. */
    val IconBadgeHero = 56.dp

    /** Inline avatar used by editor sheets (icon picker + delete badge). */
    val EditorAvatar = 96.dp

    /** Summary avatar used by detail / preview surfaces (e.g. `ItemTile`). */
    val SummaryAvatar = 80.dp

    /** Minimum height for the category-detail item grid tiles. */
    val ItemTileMinHeight = 104.dp

    /** Standard horizontal screen-edge padding for top-level screens. */
    val ScreenEdgeHorizontal = 24.dp

    /** Tighter screen-edge padding for selection-only sheets / dense flows. */
    val ScreenEdgeHorizontalTight = 20.dp
}

/**
 * Three canonical content-padding profiles for [GroceryCard]. Encodes the
 * most common internal paddings seen across the app so screens stop passing
 * bespoke `PaddingValues(...)` to `GroceryCard`.
 */
enum class CardPadding {
    /** (h = 14dp, v = 12dp) — tight list rows, settings rows. */
    Tight,

    /** 20dp all-around — default for hero / summary cards. */
    Standard,

    /** (h = 24dp, v = 20dp) — spacious surfaces (charts, summaries). */
    Spacious,
    ;

    fun toPaddingValues(): PaddingValues = when (this) {
        Tight -> PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        Standard -> PaddingValues(20.dp)
        Spacious -> PaddingValues(horizontal = 24.dp, vertical = 20.dp)
    }
}

/**
 * Tone for buttons that need to flip between a neutral and destructive
 * visual style without the caller rebuilding the button chrome.
 */
enum class ButtonTone {
    Neutral,
    Destructive,
}