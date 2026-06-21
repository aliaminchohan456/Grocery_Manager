package com.example.grocerymanager.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object AppShapes {
    val Tiny = RoundedCornerShape(8.dp)
    val Small = RoundedCornerShape(12.dp)
    val Medium = RoundedCornerShape(16.dp)
    val CardSmall = RoundedCornerShape(18.dp)
    val Card = RoundedCornerShape(24.dp)
    val LargeCard = RoundedCornerShape(30.dp)
    val HeroCard = RoundedCornerShape(32.dp)
    val HeroLarge = RoundedCornerShape(36.dp)
    val Button = RoundedCornerShape(24.dp)
    val ButtonSmall = RoundedCornerShape(14.dp)
    val Input = RoundedCornerShape(22.dp)
    val InputSmall = RoundedCornerShape(14.dp)
    val Chip = RoundedCornerShape(50)
    val BottomSheet = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val Avatar = RoundedCornerShape(16.dp)
    val BottomNav = RoundedCornerShape(50)

    /**
     * Concentric inner radius for the Double-Bezel pattern. Subtracts the
     * outer shell's padding from [CardSmall] so nested curves look intentional.
     */
    val CardSmallInner = RoundedCornerShape(12.dp)
    val CardInner = RoundedCornerShape(18.dp)
    val HeroCardInner = RoundedCornerShape(26.dp)
}
