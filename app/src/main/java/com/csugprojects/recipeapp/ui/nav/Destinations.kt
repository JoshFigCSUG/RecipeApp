package com.csugprojects.recipeapp.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen defines the sealed class for all possible navigation routes (M2 Navigation Flow).
 * This structure makes navigation type-safe and centralizes route definitions (M4 Design).
 */
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {

    // --- Primary Bottom Bar Destinations ---

    /**
     * The initial landing page route.
     */
    object Home : Screen(
        route = "home",
        label = "Home",
        icon = Icons.Default.Home
    )

    /**
     * The primary screen for searching and filtering recipes (M2 Feature).
     */
    object Search : Screen(
        route = "search",
        label = "Search",
        icon = Icons.Default.Search
    )

    /**
     * The screen for viewing locally saved favorite recipes (M2 Feature/M4 Persistence).
     */
    object Favorites : Screen(
        route = "favorites",
        label = "Favorites",
        icon = Icons.Default.Favorite
    )

    // --- Child/Deep Link Destination ---

    /**
     * The detail view for a single recipe, accepting a recipe ID as an argument.
     */
    object Detail : Screen(
        route = "recipeDetail/{recipeId}",
        label = "Details",
        icon = Icons.Default.Favorite
    )
}