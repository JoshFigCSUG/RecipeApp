package com.csugprojects.recipeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.csugprojects.recipeapp.AppContainer
import com.csugprojects.recipeapp.MyApp
import com.csugprojects.recipeapp.ui.navigation.AppBottomNav // NEW IMPORT
import com.csugprojects.recipeapp.ui.navigation.AppNavHost // NEW IMPORT
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
                    RootScreen(appContainer = appContainer)
                }
            }
        }
    }
}

// New top-level composable to encapsulate the Scaffold and navigation logic
@Composable
fun RootScreen(appContainer: AppContainer) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomNav(navController = navController) }
    ) { paddingValues ->
        // AppNavHost receives the padding from Scaffold
        AppNavHost(appContainer = appContainer, paddingValues = paddingValues)
    }
}

// REMOVED: The old RecipeAppNavGraph function