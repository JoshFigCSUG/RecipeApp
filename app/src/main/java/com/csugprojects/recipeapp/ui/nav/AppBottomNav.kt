package com.csugprojects.recipeapp.ui.nav

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * AppBottomNav defines the persistent navigation bar used at the bottom of the screen (View Layer - M2/M4).
 */
@Composable
fun AppBottomNav(navController: NavHostController) {
    // Defines the primary destinations accessible from the bottom bar (M2 Navigation Flow).
    val navItems = listOf(Screen.Home, Screen.Search, Screen.Favorites)

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        // Identifies the currently visible route to manage selection state.
        val currentRoute = navBackStackEntry?.destination?.route

        navItems.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    val isHome = screen.route == Screen.Home.route // Logic handles unique requirements for Home.

                    // Navigates to the selected destination while managing the back stack (M2/M8 Maintenance).
                    navController.navigate(screen.route) {
                        // Clears the back stack up to the start destination to prevent deep nesting of screens.
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                            // Saves the state (e.g., search results) for non-Home tabs.
                            saveState = !isHome
                        }

                        // Ensures Home screen forces a full recomposition/reset when clicked again.
                        launchSingleTop = !isHome

                        // Restores state only for Search and Favorites tabs.
                        restoreState = !isHome
                    }
                }
            )
        }
    }
}