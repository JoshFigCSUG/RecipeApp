package com.csugprojects.recipeapp.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.csugprojects.recipeapp.AppContainer
import com.csugprojects.recipeapp.ui.RecipeViewModel
import com.csugprojects.recipeapp.ui.RecipeViewModelFactory
import com.csugprojects.recipeapp.ui.detail.RecipeDetailScreen
import com.csugprojects.recipeapp.ui.list.FavoriteRecipeScreen
import com.csugprojects.recipeapp.ui.list.HomeScreen
import com.csugprojects.recipeapp.ui.list.RecipeListScreen // Using this for the SearchScreen logic

@Composable
fun AppNavHost(appContainer: AppContainer, paddingValues: PaddingValues) {
    val navController = rememberNavController()
    val viewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(appContainer.recipeRepository)
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        // 1. HOME SCREEN (The true resettable landing page)
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                },
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

        // 4. DETAIL SCREEN
        composable(
            Screen.Detail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            if (recipeId != null) {
                RecipeDetailScreen(
                    recipeId = recipeId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() } // Dynamic back action
                )
            }
        }
    }
}