package com.csugprojects.recipeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.csugprojects.recipeapp.AppContainer
import com.csugprojects.recipeapp.MyApp
import com.csugprojects.recipeapp.ui.detail.RecipeDetailScreen
import com.csugprojects.recipeapp.ui.list.FavoriteRecipeScreen
import com.csugprojects.recipeapp.ui.list.RecipeListScreen
import com.csugprojects.recipeapp.ui.theme.RecipeAppTheme

// Define all top-level destinations
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("recipeList", "Home", Icons.Default.Home)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object Detail : Screen("recipeDetail/{recipeId}", "Details", Icons.Default.Favorite)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as MyApp).container

        setContent {
            RecipeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecipeAppNavGraph(appContainer)
                }
            }
        }
    }
}

@Composable
fun RecipeAppNavGraph(appContainer: AppContainer) {
    val navController = rememberNavController()
    val viewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(appContainer.recipeRepository)
    )

    val navItems = listOf(Screen.Home, Screen.Favorites)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        // Check if the current route matches a primary bottom bar destination
                        selected = currentRoute == screen.route ||
                                (currentRoute?.startsWith("recipeDetail") == true && screen.route == Screen.Home.route),
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to avoid building a stack of destinations
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                RecipeListScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate("recipeDetail/$recipeId")
                    }
                    // NOTE: onFavoritesClick is removed, navigation is done via the Bottom Bar
                )
            }
            composable(Screen.Favorites.route) {
                FavoriteRecipeScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate("recipeDetail/$recipeId")
                    }
                    // NOTE: onBackClick is removed, back navigation is native or via Bottom Bar
                )
            }
            composable(
                Screen.Detail.route,
                arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")
                if (recipeId != null) {
                    RecipeDetailScreen(
                        recipeId = recipeId,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}