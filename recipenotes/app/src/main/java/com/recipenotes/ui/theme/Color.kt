package com.recipenotes.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Static color palette for RecipeNotes.
 *
 * These are used as a fallback when dynamic theming is not available
 * (Android 11 and below). The palette is warm and food-themed:
 * - Primary: Warm orange (appetizing, energetic)
 * - Secondary: Earthy green (fresh ingredients, nature)
 * - Tertiary: Warm brown (baked goods, comfort)
 *
 * On Android 12+, these are overridden by dynamic colors extracted
 * from the user's wallpaper via Material You.
 */

// Light theme colors
val PrimaryLight = Color(0xFFBF5B04)      // Warm orange
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFFDBC9)
val OnPrimaryContainerLight = Color(0xFF321200)

val SecondaryLight = Color(0xFF5D6135)     // Earthy green
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE2E6AF)
val OnSecondaryContainerLight = Color(0xFF1A1D00)

val TertiaryLight = Color(0xFF7B5733)      // Warm brown
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFDCC3)
val OnTertiaryContainerLight = Color(0xFF2C1600)

val BackgroundLight = Color(0xFFFFFBFF)
val SurfaceLight = Color(0xFFFFFBFF)
val ErrorLight = Color(0xFFBA1A1A)

// Dark theme colors
val PrimaryDark = Color(0xFFFFB68B)
val OnPrimaryDark = Color(0xFF522300)
val PrimaryContainerDark = Color(0xFF743400)
val OnPrimaryContainerDark = Color(0xFFFFDBC9)

val SecondaryDark = Color(0xFFC6CA95)
val OnSecondaryDark = Color(0xFF2F320D)
val SecondaryContainerDark = Color(0xFF454920)
val OnSecondaryContainerDark = Color(0xFFE2E6AF)

val TertiaryDark = Color(0xFFEFBD93)
val OnTertiaryDark = Color(0xFF462A0B)
val TertiaryContainerDark = Color(0xFF60401E)
val OnTertiaryContainerDark = Color(0xFFFFDCC3)

val BackgroundDark = Color(0xFF1F1B16)
val SurfaceDark = Color(0xFF1F1B16)
val ErrorDark = Color(0xFFFFB4AB)
