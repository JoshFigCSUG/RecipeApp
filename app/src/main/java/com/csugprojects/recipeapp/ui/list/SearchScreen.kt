package com.csugprojects.recipeapp.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.ui.viewmodel.RecipeViewModel
import com.csugprojects.recipeapp.util.Result
import kotlin.text.isLetter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeViewModel,
    onRecipeClick: (String) -> Unit
    // Removed: onFavoritesClick: () -> Unit
) {
    val recipes by viewModel.recipes
    val searchQuery by viewModel.searchQuery
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val categoriesState by viewModel.categories

    // Functional Fix: Collect the live state of favorites for immediate UI updates
    val favoriteRecipesList by viewModel.favoriteRecipes.collectAsState()
    val favoriteIds = remember(favoriteRecipesList) {
        favoriteRecipesList.map { it.id }.toSet()
    }

    var active by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Material 3 SearchBar Implementation ---
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onSearch = {
                val searchType = if (it.length == 1 && it.first().isLetter()) "firstLetter" else "name"
                viewModel.searchRecipes()
                active = false
            },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Search recipes by name or filter...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            // Removed: trailingIcon for Favorites, as it is now in the Bottom Bar
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Type to search or use filters below.",
                modifier = Modifier.padding(16.dp)
            )
        }
        // --- End SearchBar ---

        // --- Horizontal Filter Bar (New Feature) ---
        CategoryFilterBar(categoriesState = categoriesState, onFilterSelected = { categoryName ->
            viewModel.filterAndDisplayRecipes("category", categoryName)
        })

        // --- Main Content Display ---
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recipes) { recipe ->
                    val currentIsFavorite = favoriteIds.contains(recipe.id)
                    RecipeCard(
                        recipe = recipe.copy(isFavorite = currentIsFavorite),
                        onClick = { onRecipeClick(recipe.id) },
                        onFavoriteClick = { isFavorite ->
                            if (isFavorite) {
                                viewModel.removeFavorite(recipe.id)
                            } else {
                                viewModel.addFavorite(recipe)
                            }
                        }
                    )
                }
            }
        }
    }
}
// CategoryFilterBar composable remains unchanged
@Composable
fun CategoryFilterBar(
    categoriesState: Result<List<Category>>,
    onFilterSelected: (String) -> Unit
) {
    when (categoriesState) {
        is Result.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .size(24.dp),
                strokeWidth = 2.dp
            )
        }
        is Result.Success -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categoriesState.data.take(10).forEach { category ->
                    item(key = category.id) {
                        FilterChip(
                            selected = false,
                            onClick = { onFilterSelected(category.name) },
                            label = { Text(category.name) }
                        )
                    }
                }
            }
        }
        is Result.Error -> {
            Text(
                "Could not load filters.",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}