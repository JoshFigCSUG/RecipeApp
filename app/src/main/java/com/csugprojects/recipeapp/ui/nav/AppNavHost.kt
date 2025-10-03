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
// UPDATED IMPORTS to the new viewmodel package
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeListViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeDetailViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeViewModelFactory
import com.csugprojects.recipeapp.ui.detail.RecipeDetailScreen
import com.csugprojects.recipeapp.ui.list.FavoriteRecipeScreen
import com.csugprojects.recipeapp.ui.list.HomeScreen
import com.csugprojects.recipeapp.ui.list.RecipeListScreen

@Composable
fun AppNavHost(navController: NavHostController, appContainer: AppContainer, paddingValues: PaddingValues) {

    // 1. Instantiate the factory once
    val factory = RecipeViewModelFactory(appContainer.recipeRepository)

    // 2. Instantiate the two global/shared ViewModels (scoped to the entire NavHost)
    val globalViewModel: GlobalRecipeOperationsViewModel = viewModel(factory = factory)
    val listViewModel: RecipeListViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        // 1. HOME SCREEN (Needs Global VM for random recipe, favorites, and recently viewed)
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

        // 2. SEARCH RESULTS SCREEN (Needs List VM for search/filter, and Global VM for favorite actions/status)
        composable(Screen.Search.route) {
            RecipeListScreen(
                listViewModel = listViewModel,
                globalViewModel = globalViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                }
            )
        }

        // 3. FAVORITES SCREEN (Needs Global VM for the favorites list and manipulation)
        composable(Screen.Favorites.route) {
            FavoriteRecipeScreen(
                viewModel = globalViewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                }
            )
        }

        // 4. DETAIL SCREEN (Needs Detail VM for fetch and Global VM for favorite actions/logging)
        composable(
            Screen.Detail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Instantiate the Detail ViewModel scoped to this composable
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