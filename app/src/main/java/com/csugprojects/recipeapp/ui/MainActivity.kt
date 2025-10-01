package com.csugprojects.recipeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.csugprojects.recipeapp.AppContainer
import com.csugprojects.recipeapp.MyApp
import com.csugprojects.recipeapp.ui.RecipeViewModelFactory
import com.csugprojects.recipeapp.ui.detail.RecipeDetailScreen
import com.csugprojects.recipeapp.ui.list.RecipeListScreen
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

    NavHost(navController = navController, startDestination = "recipeList") {
        composable("recipeList") {
            RecipeListScreen(
                viewModel = viewModel,
                onRecipeClick = { recipeId ->
                    navController.navigate("recipeDetail/$recipeId")
                }
            )
        }
        composable("recipeDetail/{recipeId}") { backStackEntry ->
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