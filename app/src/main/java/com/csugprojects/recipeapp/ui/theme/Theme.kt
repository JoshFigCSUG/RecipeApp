package com.csugprojects.recipeapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// Custom Material 3 Light Color Scheme using Broncos colors.
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = BroncosWhite, // Text/icons on primary color
    onSecondary = BroncosWhite,
    onTertiary = BroncosWhite,
    onBackground = Color(0xFF1C1B1F), // Dark text on light background
    onSurface = Color(0xFF1C1B1F),
)

// Custom Material 3 Dark Color Scheme using Broncos colors.
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = BroncosSilver, // Text/icons on primary color
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = BroncosWhite, // Light text on dark background
    onSurface = BroncosSilver
)

/**
 * Applies the RecipeApp Material 3 theme.
 * Enables Dynamic Color (Android 12+) by default to allow the user's system theme to take control,
 * falling back to the custom Denver Broncos color schemes otherwise.
 */
@Composable
fun RecipeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is true by default, enabling system color takeover (M6 UX Enhancement).
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Logic to select the Color Scheme:
    val colorScheme = when {
        // 1. If Dynamic Color is available (Android S+) and enabled, use the system colors.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 2. Otherwise, fall back to the custom defined Bronco Dark or Light theme.
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
