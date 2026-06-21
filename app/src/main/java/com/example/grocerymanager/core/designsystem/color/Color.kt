package com.example.grocerymanager.core.designsystem.color

import androidx.compose.ui.graphics.Color

/**
 * Brand palette — premium grocery finance dashboard identity.
 *
 * Light theme leans on a deep forest green on a soft off-white surface.
 * Dark theme uses a cool near-black background with a vibrant green accent.
 */
object BrandColors {
    // Greens
    val PrimaryGreen = Color(0xFF2E7D32)        // forest 700 — light mode primary
    val PrimaryGreenDark = Color(0xFF126C2C)   // hero gradient start (light)
    val PrimaryGreenDeep = Color(0xFF0F5F24)   // hero dark areas (light)
    val FreshGreen = Color(0xFF43E681)          // bright accent — dark mode primary
    val FreshGreenDark = Color(0xFF168A45)     // dark hero / hover state
    val MutedGreen = Color(0xFF5BAA64)          // secondary green text/icons

    val DarkNavy = Color(0xFF0F172A)
    val MutedText = Color(0xFF64748B)

    val WarningOrange = Color(0xFFFF8A1F)       // grocery category / alert accent
    val WarningDark = Color(0xFFC07000)
    val OverBudgetRed = Color(0xFFEF4444)       // red 500
    val OverBudgetRedSoft = Color(0xFFF87171)    // red 400 — dark mode

    // Refined "Emerald Gradient" — the sophisticated mint-to-luminous-green
    // sweep that replaces the harsh solid green everywhere a primary surface
    // used to read flat (hero cards, FABs, primary buttons). The same stops
    // are reused by both light and dark themes so the brand green stays
    // consistent; the glow is pinned to the deeper mint end so shadows read
    // green-tinted, never blue.
    val EmeraldStart = Color(0xFF20BF55)     // mint emerald (gradient head)
    val EmeraldEnd = Color(0xFF01BAEF)       // luminous cyan-green (gradient tail)

    // Hero gradient stops — refined Emerald Gradient (light theme).
    val HeroGradientStart = EmeraldStart
    val HeroGradientEnd = EmeraldEnd
    val HeroGlow = EmeraldStart

    // Premium dark hero gradient — same Emerald sweep.
    val DarkHeroStart = EmeraldStart
    val DarkHeroEnd = EmeraldEnd
    val DarkHeroGlow = EmeraldStart

    // Surface tints (light theme)
    val FilledFieldBg = Color(0xFFF1F5F9)        // slate 100
    val SurfaceMint = Color(0xFFE8F3EA)           // selected nav / chip
    val SurfaceGreenSoft = Color(0xFFDDEEE2)      // icon circle backgrounds
    val SurfaceOrangeSoft = Color(0xFFFFF0DC)
    val SurfaceRedSoft = Color(0xFFFFE2E5)
    val SurfacePurpleSoft = Color(0xFFEDE3FF)
    val SurfaceBlueSoft = Color(0xFFDDF4FF)
    val SurfaceGraySoft = Color(0xFFE8EEF5)
    val SurfaceTealSoft = Color(0xFFD9F7F5)
    val SurfacePinkSoft = Color(0xFFFFE0F0)
    val SurfaceIndigoSoft = Color(0xFFE4E4FF)

    // Premium category palette — used as a soft reference when a category
    // has no override color or is new/legacy.
    val CatEmerald = Color(0xFF10B981)
    val CatSky = Color(0xFF0EA5E9)
    val CatAmber = Color(0xFFF59E0B)
    val CatRose = Color(0xFFF43F5E)
    val CatViolet = Color(0xFF8B5CF6)
    val CatTeal = Color(0xFF14B8A6)
    val CatPink = Color(0xFFEC4899)
    val CatIndigo = Color(0xFF6366F1)

    // Fallback applied when a category is missing (deleted) or a new user-added
    // category has no color yet. The hex value lives here so it can't drift
    // between Home, ShoppingList and CategoryDetail.
    const val CategoryFallbackHex: String = "#94A3B8"
    const val CategoryDefaultIcon: String = "Box"
}

/**
 * Light theme tokens — soft off-white surfaces with deep forest green primary.
 */
object LightTokens {
    val Background = Color(0xFFF8FAFC)          // soft off-white
    val Surface = Color(0xFFFFFFFF)              // pure white cards
    val SurfaceVariant = Color(0xFFF1F5F9)      // soft stat cards / input groups
    val OnBackground = Color(0xFF0F172A)
    val OnSurface = Color(0xFF0F172A)
    val OnSurfaceMuted = Color(0xFF64748B)
    val OnSurfaceFaint = Color(0xFF94A3B8)
    val Outline = Color(0xFFE2E8F0)
    val OutlineVariant = Color(0xFFD5DEE9)      // focusable outline border
    val Error = Color(0xFFEF4444)
    val OnError = Color(0xFFFFFFFF)

    val PrimaryContainer = Color(0xFFD7EFE0)
    val OnPrimaryContainer = Color(0xFF14532D)

    val Scrim = Color(0x66000000)
    val OnAccent = Color(0xFFFFFFFF)

    // Premium soft accents for category icon backgrounds.
    val SurfaceGreen12 = Color(0x1F2E7D32)        // 12% forest
    val SurfaceGreen18 = Color(0x2D2E7D32)        // 18% forest
    val SurfaceSelectedMint = Color(0xFFE8F3EA)
}

/**
 * Premium dark theme tokens — vibrant green on a cool near-black surface.
 *
 * Surfaces use a step-up hierarchy so cards float above the background.
 * Inputs use a deeper surface so the user can tell what is editable at a glance.
 */
object DarkTokens {
    val Background = Color(0xFF050A14)            // main dark background
    val Surface = Color(0xFF0B1220)               // base surface
    val ElevatedCard = Color(0xFF111827)          // cards
    val InputSurface = Color(0xFF1C2738)          // text fields
    val MatteSlate = Color(0xFF1C2738)            // soft stat cards / chip groups
    val OnBackground = Color(0xFFF8FAFC)
    val OnSurface = Color(0xFFF8FAFC)
    val OnSurfaceMuted = Color(0xFFAAB4C5)
    val OnSurfaceFaint = Color(0xFF6F7B8E)
    val Outline = Color(0xFF2A374A)
    val OutlineVariant = Color(0xFF33425A)
    val Error = Color(0xFFF87171)
    val OnError = Color(0xFF0B1220)

    val PrimaryContainer = Color(0xFF143E26)       // dark surface mint
    val OnPrimaryContainer = Color(0xFFA7F3D0)

    val Scrim = Color(0x99000000)
    val OnAccent = Color(0xFF04101F)               // very dark navy text on bright green

    // Soft premium green tints for selected states.
    val SurfaceGreen12 = Color(0x1F43E681)         // rgba(67,230,129,0.12)
    val SurfaceGreen18 = Color(0x2E43E681)         // rgba(67,230,129,0.18)
    val SurfaceGreen24 = Color(0x3D43E681)         // rgba(67,230,129,0.24)
    val SurfaceSelectedMint = Color(0xFF143E26)

    // Premium category palette tuned for dark surfaces — slightly desaturated
    // so the icons read well without burning the eyes.
    val CatGreen = Color(0xFF43E681)
    val CatAmber = Color(0xFFFBBF24)
    val CatEmerald = Color(0xFF34D399)
    val CatRose = Color(0xFFFB7185)
    val CatOrange = Color(0xFFFB923C)
    val CatViolet = Color(0xFFA78BFA)
    val CatCyan = Color(0xFF22D3EE)
    val CatPink = Color(0xFFF472B6)
    val CatTeal = Color(0xFF2DD4BF)
    val CatIndigo = Color(0xFF818CF8)
}

object OledTokens {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF000000)
    val ElevatedCard = Color(0xFF0B1220)
    val OnAccent = Color(0xFF04101F)
}

/**
 * Ambient depth colors for the [AmbientBackdrop] gradient orbs.
 *
 * These are very low-alpha versions of the brand / cool-slate palette used
 * exclusively by the fixed, pointer-events-none background layer in dark
 * and OLED themes. Light mode renders the orbs in a subtle forest tint so
 * the same depth language still reads in a bright environment.
 */
object AmbientColors {
    val EmeraldGlowDark = Color(0x3343E681)   // 20% vibrant green for primary orb (dark)
    val EmeraldGlowOled = Color(0x1A43E681)   // 10% on pure black (OLED)
    val EmeraldGlowLight = Color(0x142E7D32)  // 8% forest for light mode primary orb
    val SlateGlowDark = Color(0x1AAAB4C5)     // 10% cool slate for secondary orb
    val SlateGlowLight = Color(0x1864748B)    // subtle slate for light mode
}

/**
 * Frosted-glass surface tokens for premium dark surfaces.
 *
 * Used by `GlassCard`, frosted top app bar, frosted bottom nav and the
 * sticky bottom-sheet header. All values are alpha-layered white or black
 * so they blend correctly against both `DarkTokens.Background` and the
 * deep OLED pure-black background.
 */
object GlassColors {
    val Primary = Color(0x14FFFFFF)          // 8% white — primary glass fill
    val Strong = Color(0x1FFFFFFF)           // 12% white — emphasized glass fill
    val Hairline = Color(0x1AFFFFFF)         // 10% white — outer shell border
    val HairlineStrong = Color(0x33FFFFFF)   // 20% white — emphasized border
    val Highlight = Color(0x26FFFFFF)        // 15% white — top-edge inset highlight
    val ScrimDark = Color(0x99000000)        // 60% scrim (dark themes)
    val ScrimLight = Color(0x66000000)       // 40% scrim (light theme)

    // "True glass" recipe — the premium frosted-plate read:
    //   fill      = 3% white (Color.White @ alpha 0.03)
    //   hairline  = 8% white inner border (Color.White @ alpha 0.08)
    //   shadow    = 50% black drop shadow (handled at the call site).
    // Keeping these as named tokens stops each call site from drifting to a
    // different alpha and breaking the unified glass language.
    val TrueFill = Color(0x0DFFFFFF)         // Color.White @ alpha 0.03 (8% = 0x14; 3% ≈ 0x0D)
    val TrueBorder = Color(0x14FFFFFF)       // Color.White @ alpha 0.08
    val TrueFillFrosted = Color(0x26FFFFFF)  // Color.White @ alpha 0.15 — selected chip frost
}
