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
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.text.style.TextAlign


/**
 * FavoriteRecipeScreen displays the list of user-saved recipes (View Layer - M2 Feature).
 * This screen demonstrates the application's offline support (M4 Persistence).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipeScreen(
    viewModel: GlobalRecipeOperationsViewModel,
    onRecipeClick: (String) -> Unit
) {
    // Collects the list of favorite recipes from the Global ViewModel (M6 State Management).
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Prominent header using custom typography.
        Text(
            text = "Your Favorites",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp)
        )
        // Adds a visual separation line under the title (M6 UX Enhancement).
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), thickness = 1.dp)

        // Conditional rendering: shows a message if no recipes are saved.
        if (favoriteRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Engaging empty state with an icon and styled text (M6 UX Enhancement).
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "No Favorites",
                        modifier = Modifier.size(64.dp).padding(bottom = 8.dp),
                        // Uses the Tertiary color for a soft, themed accent on the empty state.
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        "You haven't saved any recipes yet. Tap the heart icon on any recipe to add it here!",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // LazyColumn efficiently renders the scrollable list of recipe cards.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteRecipes) { recipe ->
                    // Uses the reusable RecipeCard component (M2 Component Description).
                    RecipeCard(
                        // Ensures the card always shows the filled favorite icon.
                        recipe = recipe.copy(isFavorite = true),
                        onClick = { onRecipeClick(recipe.id) },
                        // Handles removal/addition of the favorite item via the ViewModel (M6 Operation).
                        onFavoriteClick = { isFavorite ->
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