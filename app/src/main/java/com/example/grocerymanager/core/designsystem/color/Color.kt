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
    val PrimaryGreen = Color(0xFF1A8242)        // Softened for premium look
    val PrimaryGreenDark = Color(0xFF126C2C)
    val PrimaryGreenDeep = Color(0xFF0F5F24)
    val FreshGreen = Color(0xFF3DDC84)          // Apple's systemGreen for dark mode
    val FreshGreenDark = Color(0xFF168A45)
    val MutedGreen = Color(0xFF5BAA64)

    // Replaced harsh navy with a softer premium slate
    val DarkNavy = Color(0xFF1C1C1E)            // Apple's dark background base
    val MutedText = Color(0xFF8E8E93)           // iOS secondary label color

    val WarningOrange = Color(0xFFFF9F0A)       // Apple's systemOrange
    val WarningDark = Color(0xFFC07000)
    val OverBudgetRed = Color(0xFFFF3B30)       // Apple's systemRed
    val OverBudgetRedSoft = Color(0xFFFF453A)

    // Refined "Emerald Gradient"
    val EmeraldStart = Color(0xFF20BF55)
    val EmeraldEnd = Color(0xFF01BAEF)

    val HeroGradientStart = EmeraldStart
    val HeroGradientEnd = EmeraldEnd
    val HeroGlow = EmeraldStart

    val DarkHeroStart = EmeraldStart
    val DarkHeroEnd = EmeraldEnd
    val DarkHeroGlow = EmeraldStart

    // Surface tints (light theme) - Notion style soft backgrounds
    val FilledFieldBg = Color(0xFFF7F7F5)        // Notion's light gray
    val SurfaceMint = Color(0xFFE8F3EA)
    val SurfaceGreenSoft = Color(0xFFE3F2E8)      // Softened
    val SurfaceOrangeSoft = Color(0xFFFFF4E0)
    val SurfaceRedSoft = Color(0xFFFFE5E7)
    val SurfacePurpleSoft = Color(0xFFF1ECFF)
    val SurfaceBlueSoft = Color(0xFFE6F6FF)
    val SurfaceGraySoft = Color(0xFFEFF1F4)
    val SurfaceTealSoft = Color(0xFFE0F7F5)
    val SurfacePinkSoft = Color(0xFFFFE6F2)
    val SurfaceIndigoSoft = Color(0xFFECECFF)

    // Premium category palette
    val CatEmerald = Color(0xFF10B981)
    val CatSky = Color(0xFF0EA5E9)
    val CatAmber = Color(0xFFF59E0B)
    val CatRose = Color(0xFFF43F5E)
    val CatViolet = Color(0xFF8B5CF6)
    val CatTeal = Color(0xFF14B8A6)
    val CatPink = Color(0xFFEC4899)
    val CatIndigo = Color(0xFF6366F1)

    const val CategoryFallbackHex: String = "#94A3B8"
    const val CategoryDefaultIcon: String = "Box"
}

/**
 * Light theme tokens — soft off-white surfaces (Notion/Apple style).
 */
object LightTokens {
    val Background = Color(0xFFFAFAFA)          // Pure white se halka sa warm (Notion style)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF2F2F2)      // Clean separator
    val OnBackground = Color(0xFF1C1C1E)        // Apple's label color
    val OnSurface = Color(0xFF1C1C1E)
    val OnSurfaceMuted = Color(0xFF6E6E73)      // Apple's secondaryLabel
    val OnSurfaceFaint = Color(0xFFA0A0A5)
    val Outline = Color(0xFFE5E5EA)             // Apple's separator
    val OutlineVariant = Color(0xFFD1D1D6)
    val Error = Color(0xFFFF3B30)
    val OnError = Color(0xFFFFFFFF)

    val PrimaryContainer = Color(0xFFE3F6E8)
    val OnPrimaryContainer = Color(0xFF0F5F24)

    val Scrim = Color(0x66000000)
    val OnAccent = Color(0xFFFFFFFF)

    val SurfaceGreen12 = Color(0x1F1A8242)
    val SurfaceGreen18 = Color(0x2D1A8242)
    val SurfaceSelectedMint = Color(0xFFE8F3EA)
}

/**
 * Premium dark theme tokens — Apple/Notion True Dark.
 * Replaced the harsh blue tints with a neutral, soothing dark gray scale.
 */
object DarkTokens {
    val Background = Color(0xFF000000)            // True Black (for OLED default feel)
    val Surface = Color(0xFF1C1C1E)               // Apple's secondarySystemBackground
    val ElevatedCard = Color(0xFF2C2C2E)          // Apple's tertiarySystemBackground
    val InputSurface = Color(0xFF2C2C2E)
    val MatteSlate = Color(0xFF2C2C2E)
    val OnBackground = Color(0xFFFFFFFF)          // Pure white for primary text in dark
    val OnSurface = Color(0xFFFFFFFF)
    val OnSurfaceMuted = Color(0xFFEBEBF5)        // 60% white (Apple secondary label)
    val OnSurfaceFaint = Color(0xFFA0A0A5)        // 30% white
    val Outline = Color(0xFF38383A)               // Dark mode separator
    val OutlineVariant = Color(0xFF545458)
    val Error = Color(0xFFFF453A)
    val OnError = Color(0xFFFFFFFF)

    val PrimaryContainer = Color(0xFF143E26)
    val OnPrimaryContainer = Color(0xFFA7F3D0)

    val Scrim = Color(0x99000000)
    val OnAccent = Color(0xFF04101F)

    val SurfaceGreen12 = Color(0x1F3DDC84)
    val SurfaceGreen18 = Color(0x2E3DDC84)
    val SurfaceGreen24 = Color(0x3D3DDC84)
    val SurfaceSelectedMint = Color(0xFF143E26)

    // Tuned for premium dark surfaces
    val CatGreen = Color(0xFF3DDC84)
    val CatAmber = Color(0xFFFFD60A)
    val CatEmerald = Color(0xFF34D399)
    val CatRose = Color(0xFFFF375F)
    val CatOrange = Color(0xFFFF9F0A)
    val CatViolet = Color(0xFFBF5AF2)
    val CatCyan = Color(0xFF64D2FF)
    val CatPink = Color(0xFFFF375F)
    val CatTeal = Color(0xFF40CBE0)
    val CatIndigo = Color(0xFF5E5CE6)
}

object OledTokens {
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF000000)
    val ElevatedCard = Color(0xFF1C1C1E)  // Slightly elevated from pure black
    val OnAccent = Color(0xFF04101F)
}

/**
 * Ambient depth colors for the [AmbientBackdrop] gradient orbs.
 */
object AmbientColors {
    val EmeraldGlowDark = Color(0x333DDC84)   // Matched to new fresh green
    val EmeraldGlowOled = Color(0x1A3DDC84)
    val EmeraldGlowLight = Color(0x141A8242)
    val SlateGlowDark = Color(0x1AEBEBF5)
    val SlateGlowLight = Color(0x186E6E73)
}

/**
 * Frosted-glass surface tokens for premium dark surfaces.
 */
object GlassColors {
    val Primary = Color(0x14FFFFFF)
    val Strong = Color(0x1FFFFFFF)
    val Hairline = Color(0x1AFFFFFF)
    val HairlineStrong = Color(0x33FFFFFF)
    val Highlight = Color(0x26FFFFFF)
    val ScrimDark = Color(0x99000000)
    val ScrimLight = Color(0x66000000)

    val TrueFill = Color(0x0DFFFFFF)
    val TrueBorder = Color(0x14FFFFFF)
    val TrueFillFrosted = Color(0x26FFFFFF)
}