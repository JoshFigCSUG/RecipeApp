package com.csugprojects.recipeapp.ui.nav

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
                    val isHome = screen.route == Screen.Home.route // Determine if target is Home

                    // Navigate to the target screen (Home, Search, or Favorites)
                    navController.navigate(screen.route) {
                        // FIX 1: Pop up to the very first destination of the NavHost's graph.
                        // This clears any intermediate Detail screens that were pushed.
                        popUpTo(navController.graph.findStartDestination().id) {
                            // Setting inclusive = false ensures the 'Home' screen is preserved on the stack.
                            inclusive = false
                            // Save state for the other main tabs (Favorites, Search)
                            saveState = !isHome // Only save state for Search/Favorites
                        }

                        // FIX 2 (CRITICAL RESET FIX):
                        // launchSingleTop = false for Home allows navigation to occur even if already on the screen,
                        // forcing a re-composition/reset. For others, it keeps the single-top behavior.
                        launchSingleTop = !isHome

                        // FIX 3: Only restore state for Search/Favorites.
                        restoreState = !isHome
                    }
                }
            )
        }
    }
}