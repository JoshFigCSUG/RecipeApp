package com.csugprojects.recipeapp.ui.theme

import androidx.compose.ui.graphics.Color

// Denver Broncos Colors (Primary Palette)
// Broncos Orange: #FB4F14 (Used for primary color in Light Theme)
val BroncosOrange = Color(0xFFFB4F14)
// Broncos Blue: #002244 (Used for primary color in Dark Theme)
val BroncosBlueDark = Color(0xFF002244)
// Accent White/Silver (Used for secondary/tertiary colors)
val BroncosSilver = Color(0xFFC8C9CA)
val BroncosWhite = Color(0xFFFFFFFF)

// --- Default Light Theme Colors (Based on Broncos Palette) ---
// Primary: The iconic orange
val LightPrimary = BroncosOrange
// Secondary: A dark blue/gray for subtle accent and container backgrounds
val LightSecondary = Color(0xFF6B5848)
// Tertiary: Used for high-contrast accents like the favorite icon tint
val LightTertiary = Color(0xFF8C7359)
// Background: Clean white for high contrast
val LightBackground = BroncosWhite
// Surface: Subtle off-white for cards/surfaces
val LightSurface = Color(0xFFFCFCFC)

// --- Default Dark Theme Colors (Based on Broncos Palette) ---
// Primary: The dark navy blue
val DarkPrimary = BroncosBlueDark
// Secondary: A medium orange for accents
val DarkSecondary = Color(0xFFE48D67)
// Tertiary: A lighter blue/silver for contrast
val DarkTertiary = BroncosSilver
// Background: Deep dark background
val DarkBackground = Color(0xFF1C1B1F)
// Surface: Slightly lighter dark gray for cards
val DarkSurface = Color(0xFF2C2B2F)
