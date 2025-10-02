package com.csugprojects.recipeapp.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csugprojects.recipeapp.ui.RecipeViewModel
import com.csugprojects.recipeapp.util.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    // State to hold the result of the random recipe fetch
    var randomRecipeState by remember { mutableStateOf<Result<com.csugprojects.recipeapp.domain.model.Recipe>>(Result.Loading) }

    // Fetch a random recipe immediately when the screen is composed
    LaunchedEffect(Unit) {
        viewModel.fetchRandomRecipe { result ->
            randomRecipeState = result
        }
    }

    // Scaffold is handled in MainActivity, so this is just a Column content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Recipe of the Moment",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )

        // Display Random Recipe or Loading/Error State
        when (val state = randomRecipeState) {
            is Result.Loading -> {
                CircularProgressIndicator()
            }
            is Result.Success -> {
                state.data?.let { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) },
                        onFavoriteClick = { isFavorite ->
                            if (isFavorite) viewModel.removeFavorite(recipe.id) else viewModel.addFavorite(recipe)
                        }
                    )
                } ?: Text("No random recipe found. Try searching!")
            }
            is Result.Error -> {
                Text("Error loading recipe: ${state.exception.message}", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Button to navigate to the full Search Screen
        Button(
            onClick = onSearchClick,
            contentPadding = PaddingValues(16.dp),
        ) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Full Search & Filters")
        }
    }
}