package com.csugprojects.recipeapp.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csugprojects.recipeapp.ui.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipeScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    // Collect the live stream of favorite recipes
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Favorite Recipes") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (favoriteRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("You haven't saved any recipes yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteRecipes) { recipe ->
                    RecipeCard(
                        // Since this list only contains favorites, isFavorite is always true
                        recipe = recipe.copy(isFavorite = true),
                        onClick = { onRecipeClick(recipe.id) },
                        onFavoriteClick = { isFavorite ->
                            if (isFavorite) {
                                viewModel.removeFavorite(recipe.id)
                            } else {
                                // Should not happen in this screen, but included for completeness
                                viewModel.addFavorite(recipe)
                            }
                        }
                    )
                }
            }
        }
    }
}