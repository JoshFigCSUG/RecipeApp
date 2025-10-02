package com.csugprojects.recipeapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

// Define all top-level destinations and the detail route
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("recipeList", "Home", Icons.Default.Home)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    // Detail is not a main tab, but its route is defined here
    object Detail : Screen("recipeDetail/{recipeId}", "Details", Icons.Default.Favorite)
}