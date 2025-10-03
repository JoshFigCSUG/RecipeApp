package com.csugprojects.recipeapp.ui.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import com.csugprojects.recipeapp.di.AppContainer
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeListViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeDetailViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeViewModelFactory
import com.csugprojects.recipeapp.ui.detail.RecipeDetailScreen
import com.csugprojects.recipeapp.ui.list.FavoriteRecipeScreen
import com.csugprojects.recipeapp.ui.list.HomeScreen
import com.csugprojects.recipeapp.ui.list.RecipeListScreen

/**
 * AppNavHost sets up the Navigation Compose graph and links destinations to their ViewModels (M2/M4 Architecture).
 */
@Composable
fun AppNavHost(navController: NavHostController, appContainer: AppContainer, paddingValues: PaddingValues) {

    // Creates the ViewModel Factory using the shared Repository (M4 Dependency Injection).
    val factory = RecipeViewModelFactory(appContainer.recipeRepository)

    // Instantiates shared ViewModels (Global and List) scoped to the entire NavHost lifetime.
    val globalViewModel: GlobalRecipeOperationsViewModel = viewModel(factory = factory)
    val listViewModel: RecipeListViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        // HOME SCREEN (M2 Primary Destination)
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = globalViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        // SEARCH RESULTS SCREEN (M2/M6 Search and Filtering Hub)
        composable(Screen.Search.route) {
            RecipeListScreen(
                listViewModel = listViewModel,
                globalViewModel = globalViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                }
            )
        }

        // FAVORITES SCREEN (M2/M4 Persistence Feature)
        composable(Screen.Favorites.route) {
            FavoriteRecipeScreen(
                viewModel = globalViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                }
            )
        }

        // DETAIL SCREEN (M2 Deep Link Destination)
        composable(
            Screen.Detail.route,
            // Defines the recipeId argument required for fetching details.
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Instantiates the Detail ViewModel, scoped only to this composable (M4 MVVM).
            val detailViewModel: RecipeDetailViewModel = viewModel(factory = factory)

            val recipeId = backStackEntry.arguments?.getString("recipeId")
            if (recipeId != null) {
                RecipeDetailScreen(
                    recipeId = recipeId,
                    detailViewModel = detailViewModel,
                    globalViewModel = globalViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}