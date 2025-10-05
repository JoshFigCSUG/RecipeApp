package com.csugprojects.recipeapp.ui.theme

import androidx.compose.ui.graphics.Color

// Denver Broncos Colors (Primary Palette)
val BroncosOrange = Color(0xFFFB4F14)
val BroncosBlueDark = Color(0xFF002244)
val BroncosSilver = Color(0xFFC8C9CA)
val BroncosWhite = Color(0xFFFFFFFF)

// --- Default Light Theme Colors (Based on Broncos Palette) ---
// Primary color, used for buttons and key accents.
val LightPrimary = BroncosOrange
// Secondary color, used for subtle accents and containers.
val LightSecondary = Color(0xFF6B5848)
// Tertiary color, used for high-contrast accents like the empty state icon.
val LightTertiary = Color(0xFF8C7359)
// Screen background color.
val LightBackground = BroncosWhite
// Color for components like cards and surfaces.
val LightSurface = Color(0xFFFCFCFC)

// --- Default Dark Theme Colors (Lighter versions of Light Theme colors) ---
// Primary color for the dark theme, optimized for contrast.
val DarkPrimary = Color(0xFFFC7A4E)
// Secondary color for dark theme accents.
val DarkSecondary = Color(0xFF9D8370)
// Tertiary color for dark theme accents.
val DarkTertiary = Color(0xFFB39885)
// Deep dark background color.
val DarkBackground = Color(0xFF1C1B1F)
// Color for components like cards and surfaces in dark mode.
val DarkSurface = Color(0xFF2C2B2F)