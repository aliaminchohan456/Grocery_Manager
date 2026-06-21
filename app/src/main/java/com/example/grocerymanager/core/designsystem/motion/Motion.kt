package com.example.grocerymanager.core.designsystem.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Signature motion easings for the design system.
 *
 * Per the high-end-visual-design mandate, all motion uses custom cubic-bezier
 * curves — never default `LinearEasing` or `FastOutSlowInEasing`. The default
 * easing ([Standard]) approximates an iOS / Linear-style decelerate that gives
 * buttons, sheets and toggles their kinetic weight without feeling heavy.
 *
 *  - [Standard]    — general UI transitions (button press, sheet snap, tab switch).
 *  - [Emphasized]  — large surface swaps, hero values, full-screen reveals.
 *  - [Accelerate]  — exits, dismissals, things leaving the screen.
 *  - [Fluid]       — long entry animations (stagger reveal, scroll into view).
 *  - [Magnetic]    — physical-press return; slight overshoot feel.
 */
object AppEasing {
    val Standard: Easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Accelerate: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val Fluid: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val Magnetic: Easing = CubicBezierEasing(0.18f, 0.89f, 0.32f, 1.18f)
}

/**
 * Canonical motion durations. Every transition in the design system should
 * pull from here instead of inlining a `tween(220)`. The named buckets map to
 * common perceptual durations:
 *
 *  - [Micro]   120ms — tap feedback, ripple, focus ring.
 *  - [Short]   220ms — button press, color swap, icon morph.
 *  - [Medium]  320ms — sheet snap, segment shift, badge swap.
 *  - [Long]    480ms — page-level transition, full-screen dialog.
 *  - [Entry]   700ms — staggered fade-up entry into view.
 *  - [OrbCycle] 12s — ambient backdrop orb breath.
 */
object MotionDuration {
    const val Micro = 120
    const val Short = 220
    const val Medium = 320
    const val Long = 480
    const val Entry = 700
    const val OrbCycle = 12_000
}

/**
 * Stagger delays for entry reveals. Pick the smallest value that still reads
 * as a cascade — 40ms feels instant, 80ms feels theatrical.
 */
object StaggerDelay {
    const val Step40 = 40L
    const val Step60 = 60L
    const val Step80 = 80L

    /** Cap on staggered items to keep total delay bounded. */
    const val MaxItems = 8
}

/**
 * Composition-local for per-screen motion overrides. Defaults to [AppEasing.Standard]
 * so call sites can rely on a single global easing unless they explicitly want to
 * override (e.g. a hero entry that wants [AppEasing.Fluid]).
 */
val LocalAppMotion: androidx.compose.runtime.ProvidableCompositionLocal<Easing> =
    staticCompositionLocalOf { AppEasing.Standard }

@Composable
fun rememberAppMotion(): Easing = LocalAppMotion.current
