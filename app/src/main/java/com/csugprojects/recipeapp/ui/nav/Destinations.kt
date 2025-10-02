package com.csugprojects.recipeapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

// Define all top-level destinations and the detail route
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home) // Changed route from recipeList to home
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object Search : Screen("search", "Search Results", Icons.Default.Search) // New dedicated search route
    object Detail : Screen("recipeDetail/{recipeId}", "Details", Icons.Default.Favorite)
}