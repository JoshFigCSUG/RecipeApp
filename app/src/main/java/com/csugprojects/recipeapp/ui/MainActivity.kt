package com.csugprojects.recipeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.csugprojects.recipeapp.AppContainer
import com.csugprojects.recipeapp.MyApp
import com.csugprojects.recipeapp.ui.detail.RecipeDetailScreen
import com.csugprojects.recipeapp.ui.list.FavoriteRecipeScreen
import com.csugprojects.recipeapp.ui.list.HomeScreen // NEW IMPORT for the true home screen
import com.csugprojects.recipeapp.ui.list.RecipeListScreen // This file now serves as the Search/Filter screen
import com.csugprojects.recipeapp.ui.navigation.AppBottomNav // NEW IMPORT for the Bottom Bar UI
import com.csugprojects.recipeapp.ui.navigation.Screen // NEW IMPORT for routes (Home, Favorites, Search, Detail)
import com.csugprojects.recipeapp.ui.theme.RecipeAppTheme

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

    // Scaffold wraps the entire navigation structure to place the persistent bottom bar
    Scaffold(
        bottomBar = { AppBottomNav(navController = navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 1. HOME SCREEN (The resettable landing page with random recipe)
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                    },
                    // Navigate to the Search Screen when the button is pressed
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            // 2. SEARCH RESULTS SCREEN (The primary list/filter view)
            composable(Screen.Search.route) {
                // RecipeListScreen.kt's composable is used here for the filtering logic
                RecipeListScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                    }
                )
            }

            // 3. FAVORITES SCREEN
            composable(Screen.Favorites.route) {
                FavoriteRecipeScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                    }
                )
            }

            // 4. DETAIL SCREEN (Deep link destination)
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