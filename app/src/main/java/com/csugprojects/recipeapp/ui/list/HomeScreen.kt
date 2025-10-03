package com.csugprojects.recipeapp.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.csugprojects.recipeapp.util.Result
import androidx.compose.runtime.collectAsState
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.domain.model.Recipe
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight


/**
 * HomeScreen is the application's primary landing page (View Layer - M2).
 * It displays a random recipe and a list of recently viewed items (M6 Features).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GlobalRecipeOperationsViewModel,
    onRecipeClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    // State stores the result of the API call for the random recipe (M4 Error Handling).
    var randomRecipeState by remember { mutableStateOf<Result<Recipe>>(Result.Loading) }

    // Observes the list of recently viewed recipes from the ViewModel (M6 State Management).
    val recentlyViewed by viewModel.recentlyViewed.collectAsState()

    // Observes the global source of truth for favorite status (M6 State Management).
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsState()
    val favoriteIds = remember(favoriteRecipes) {
        favoriteRecipes.map { it.id }.toSet()
    }

    // Triggers the initial fetch for a random recipe when the screen is first composed (M6 Feature).
    LaunchedEffect(Unit) {
        viewModel.fetchRandomRecipe { result ->
            randomRecipeState = result
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Displays a horizontally scrolling list of recently viewed recipes (M6 Feature).
        if (recentlyViewed.isNotEmpty()) {
            Text(
                text = "Recently Viewed",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(recentlyViewed) { recipe ->
                    // Uses SmallRecipeCard component for the condensed view.
                    SmallRecipeCard(
                        recipe = recipe.copy(isFavorite = favoriteIds.contains(recipe.id)),
                        onClick = { onRecipeClick(recipe.id) },
                        // Calls ViewModel to update the persistent favorite status (M2/M6 Operation).
                        onFavoriteClick = { isFavorite ->
                            if (isFavorite) viewModel.addFavorite(recipe) else viewModel.removeFavorite(recipe.id)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = "Recipe of the Moment",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )

        // Handles conditional display based on the API fetch state (M4 Error Handling).
        when (val state = randomRecipeState) {
            is Result.Loading -> {
                CircularProgressIndicator()
            }
            is Result.Success -> {
                // Accesses data directly, as Result.Success guarantees its presence.
                state.data.let { recipe ->
                    val currentIsFavorite = favoriteIds.contains(recipe.id)
                    // Uses the standard RecipeCard component (M2 Component Description).
                    RecipeCard(
                        recipe = recipe.copy(isFavorite = currentIsFavorite),
                        onClick = { onRecipeClick(recipe.id) },
                        onFavoriteClick = { isFavorite ->
                            if (isFavorite) viewModel.addFavorite(recipe) else viewModel.removeFavorite(recipe.id)
                        }
                    )
                }
            }
            is Result.Error -> {
                Text("Error loading recipe: ${state.exception.message}", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Button navigates to the search screen (M2 Navigation Flow).
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

/**
 * SmallRecipeCard defines a compact, reusable UI card for horizontal lists (M2 Component).
 */
@Composable
fun SmallRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    // Content description aids accessibility (M2 Accessibility).
                    contentDescription = "Image of ${recipe.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { onFavoriteClick(!recipe.isFavorite) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}