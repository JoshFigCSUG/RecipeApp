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
import com.csugprojects.recipeapp.util.Result
import kotlin.text.isLetter
// CORRECTED IMPORTS
import androidx.compose.runtime.collectAsState
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeListViewModel
// ADDED IMPORT for RecipeCard
import com.csugprojects.recipeapp.ui.list.RecipeCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    // CHANGED PARAMETERS to use the new ViewModels
    listViewModel: RecipeListViewModel,
    globalViewModel: GlobalRecipeOperationsViewModel,
    onRecipeClick: (String) -> Unit
) {
    // Collect list states from List ViewModel
    val recipes by listViewModel.recipes
    val searchQuery by listViewModel.searchQuery
    val isLoading by listViewModel.isLoading
    val errorMessage by listViewModel.errorMessage
    val categoriesState by listViewModel.categories

    // Collect global favorites state from Global ViewModel
    val favoriteRecipesList by globalViewModel.favoriteRecipes.collectAsState()
    val favoriteIds = remember(favoriteRecipesList) {
        favoriteRecipesList.map { it.id }.toSet()
    }

    var active by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Material 3 SearchBar Implementation ---
        SearchBar(
            query = searchQuery,
            onQueryChange = { listViewModel.onSearchQueryChanged(it) },
            onSearch = {
                // Pass the current favorite IDs to the search function
                listViewModel.searchRecipes(favoriteIds)
                active = false
            },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Search recipes by name or filter...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
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
            // Pass the current favorite IDs to the filter function
            listViewModel.filterAndDisplayRecipes("category", categoryName, favoriteIds)
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
                            // Use Global ViewModel for all favorite modification actions
                            if (isFavorite) {
                                globalViewModel.removeFavorite(recipe.id)
                            } else {
                                globalViewModel.addFavorite(recipe)
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