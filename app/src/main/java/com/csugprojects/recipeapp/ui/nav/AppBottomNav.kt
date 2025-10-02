package com.csugprojects.recipeapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppBottomNav(navController: NavHostController) {
    // New list of items for the NavigationBar, including Search
    val navItems = listOf(Screen.Home, Screen.Search, Screen.Favorites)

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        // currentRoute captures the base route (e.g., "home", "favorites", "search")
        val currentRoute = navBackStackEntry?.destination?.route

        navItems.forEach { screen ->
            // Check if the current route matches the screen route, OR if the current screen
            // is a detail screen that originated from a main tab (Home/Favorites/Search).
            val isSelected = currentRoute == screen.route
            // Add logic to highlight when on a Detail page that branched from this screen
            // For simplicity, we stick to checking the direct route:

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    // Navigate to the target screen (Home, Search, or Favorites)
                    navController.navigate(screen.route) {
                        // FIX 1: Pop up to the very first destination of the NavHost's graph.
                        // This clears any intermediate Detail screens that were pushed.
                        popUpTo(navController.graph.findStartDestination().id) {
                            // Save state for the other main tabs (Favorites, Search)
                            saveState = true
                        }

                        // FIX 2: Prevents a new instance of the screen from being placed on top of the stack.
                        launchSingleTop = true

                        // FIX 3 (CRITICAL): Restore the state of the screen (e.g., scroll position, but NOT the deep-linked screen)
                        // This is necessary to bring back the RecipeListScreen after the Detail Screen has been popped.
                        restoreState = true
                    }
                }
            )
        }
    }
}