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
import com.csugprojects.recipeapp.ui.list.RecipeListScreen
import com.csugprojects.recipeapp.ui.navigation.AppBottomNav // NEW IMPORT
import com.csugprojects.recipeapp.ui.navigation.Screen // NEW IMPORT
import com.csugprojects.recipeapp.ui.theme.RecipeAppTheme

// REMOVED: Screen sealed class definition is now in ui.navigation.Destinations.kt

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

    Scaffold(
        bottomBar = { AppBottomNav(navController = navController) } // CALL NEW COMPOSABLE
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
                        navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                    }
                )
            }
            composable(Screen.Favorites.route) {
                FavoriteRecipeScreen(
                    viewModel = viewModel,
                    onRecipeClick = { recipeId ->
                        navController.navigate(Screen.Detail.route.replace("{recipeId}", recipeId))
                    }
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