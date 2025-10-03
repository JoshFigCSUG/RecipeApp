package com.csugprojects.recipeapp

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
import com.csugprojects.recipeapp.di.AppContainer
import com.csugprojects.recipeapp.di.MyApp
import com.csugprojects.recipeapp.ui.nav.AppBottomNav
import com.csugprojects.recipeapp.ui.nav.AppNavHost
import com.csugprojects.recipeapp.ui.theme.RecipeAppTheme

/**
 * MainActivity serves as the single Activity in this application (View Layer - M2).
 * Its role is to host the Jetpack Compose UI and initialize the application's dependencies.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Accesses the AppContainer (Dependency Injection setup - M4/M6) from the Application class.
        val appContainer = (application as MyApp).container

        setContent {
            // Applies the Material Theme (Theming Approach - M2).
            RecipeAppTheme {
                // Sets the background surface, covering the entire screen (M8 Deployment).
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Starts the main composable structure.
                    RootScreen(appContainer = appContainer)
                }
            }
        }
    }
}

/**
 * RootScreen composable sets up the fundamental UI navigation structure.
 * It is the highest-level representation of the View in the MVVM architecture (M2).
 */
@Composable
fun RootScreen(appContainer: AppContainer) {
    // The NavController manages the navigation state throughout the application (Navigation Flow - M2/M4).
    val navController = rememberNavController()

    // Scaffold provides the basic visual structure, including the bottom navigation bar.
    Scaffold(
        // Implements the Bottom Navigation component (Navigation Flow - M2).
        bottomBar = { AppBottomNav(navController = navController) }
    ) { paddingValues ->

        // AppNavHost defines the navigation graph and routes.
        AppNavHost(
            navController = navController,
            // Passes the AppContainer to allow ViewModels access to the Repository (M4/M6).
            appContainer = appContainer,
            paddingValues = paddingValues
        )
    }
}