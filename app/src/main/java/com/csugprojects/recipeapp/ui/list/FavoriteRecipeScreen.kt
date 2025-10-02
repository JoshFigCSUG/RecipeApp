package com.csugprojects.recipeapp.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// UPDATED IMPORT: Use the new Global ViewModel
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
// ADDED IMPORT: Needed for the RecipeCard composable
import com.csugprojects.recipeapp.ui.list.RecipeCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipeScreen(
    // CHANGED PARAMETER: Use the GlobalRecipeOperationsViewModel
    viewModel: GlobalRecipeOperationsViewModel,
    onRecipeClick: (String) -> Unit
    // Removed: onBackClick: () -> Unit
) {
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsState()

    // UI Fix: Use a simple Column/Box structure as Scaffold is handled in MainActivity
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Use a simple text title instead of TopAppBar for this inner screen
        Text(
            text = "Your Favorites",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp)
        )

        if (favoriteRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f), // Take remaining space
                contentAlignment = Alignment.Center
            ) {
                Text("You haven't saved any recipes yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe.copy(isFavorite = true),
                        onClick = { onRecipeClick(recipe.id) },
                        onFavoriteClick = { isFavorite ->
                            // FIX: The logic was inverted. If 'isFavorite' is true, ADD it; otherwise, REMOVE it.
                            if (isFavorite) {
                                viewModel.addFavorite(recipe)
                            } else {
                                viewModel.removeFavorite(recipe.id)
                            }
                        }
                    )
                }
            }
        }
    }
}