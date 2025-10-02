package com.csugprojects.recipeapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

// The file location shown in the error is 'ui/nav', but I will use the established 'ui/navigation' package
// based on previous context. If the file is in 'ui/nav', you must change the package line below.

// FIX: Ensure all primary constructor parameters have explicit types (String, String, ImageVector)
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {

    // Primary Bottom Bar Destinations
    // FIX: Icons.Default.Home/Search/Favorite are correctly passed as ImageVector types.
    object Home : Screen(
        route = "home",
        label = "Home",
        icon = Icons.Default.Home
    )

    object Search : Screen(
        route = "search",
        label = "Search",
        icon = Icons.Default.Search
    )

    object Favorites : Screen(
        route = "favorites",
        label = "Favorites",
        icon = Icons.Default.Favorite
    )

    // Child/Deep Link Destination
    object Detail : Screen(
        route = "recipeDetail/{recipeId}",
        label = "Details",
        icon = Icons.Default.Favorite // Placeholder icon, but correctly typed
    )
}