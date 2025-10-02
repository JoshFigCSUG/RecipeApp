package com.csugprojects.recipeapp.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppBottomNav(navController: NavHostController) {
    val navItems = listOf(Screen.Home, Screen.Favorites)

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        navItems.forEach { screen ->
            val isSelected = currentRoute == screen.route ||
                    // Highlight the correct tab even when on the Detail screen
                    (currentRoute?.startsWith(Screen.Detail.route.substringBefore("/{")) == true && navController.previousBackStackEntry?.destination?.route == screen.route)

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        // FIX: Pop up to the start destination of the graph, clearing any intermediate Detail Screens.
                        // We use inclusive=false (by default) to ensure the Home screen remains on the stack.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }

                        // Prevents adding a duplicate instance to the back stack
                        launchSingleTop = true
                        // Restores state of the screen (e.g., scroll position)
                        restoreState = true
                    }
                }
            )
        }
    }
}