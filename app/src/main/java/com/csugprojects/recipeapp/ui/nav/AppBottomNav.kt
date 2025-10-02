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
                    // This handles selection when the Detail screen is active
                    (currentRoute?.startsWith("recipeDetail") == true && navController.previousBackStackEntry?.destination?.route == screen.route)

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        // FIX: Clears the back stack up to the initial screen
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies when reselecting
                        launchSingleTop = true
                        // Restore state when reselecting
                        restoreState = true
                    }
                }
            )
        }
    }
}