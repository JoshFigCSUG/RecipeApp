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
import androidx.compose.runtime.collectAsState
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeListViewModel
import com.csugprojects.recipeapp.domain.model.Name
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


/**
 * RecipeListScreen serves as the search and filtering hub (View Layer - M2 Feature).
 * It observes data streams from both the RecipeListViewModel and the GlobalRecipeOperationsViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    listViewModel: RecipeListViewModel,
    globalViewModel: GlobalRecipeOperationsViewModel,
    onRecipeClick: (String) -> Unit
) {
    // Collects states specific to list fetching and search query (M6 State Management).
    val recipes by listViewModel.recipes
    val searchQuery by listViewModel.searchQuery
    val isLoading by listViewModel.isLoading
    val errorMessage by listViewModel.errorMessage
    val categoriesState by listViewModel.categories

    // Collects global favorites state to correctly display favorite status on each card (M6 State Management).
    val favoriteRecipesList by globalViewModel.favoriteRecipes.collectAsState()
    val favoriteIds = remember(favoriteRecipesList) {
        favoriteRecipesList.map { it.id }.toSet()
    }

    // Collects lists used for filter chips (M6 Feature).
    val areasState by listViewModel.areas

    // Tracks which filter chip is currently selected.
    val selectedCategory by listViewModel.selectedCategory
    val selectedArea by listViewModel.selectedArea
    val selectedIngredient by listViewModel.selectedIngredient

    var active by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // SearchBar component handles user input (M2 Component).
        SearchBar(
            query = searchQuery,
            onQueryChange = { listViewModel.onSearchQueryChanged(it) },
            // Triggers the dual search (by name and ingredient) defined in the ViewModel (M6 Implementation).
            onSearch = {
                listViewModel.searchRecipes(favoriteIds)
                active = false
            },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Search recipes by name or ingredient...") },
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

        // Column scrolls to allow space for all filter bars.
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            // Horizontal Filter Bars for navigation/filtering (M6 Feature).
            Text("Categories", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            CategoryFilterBar(
                categoriesState = categoriesState,
                selectedFilter = selectedCategory,
                favoriteIds = favoriteIds,
                listViewModel = listViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Areas", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 16.dp))
            AreaFilterBar(
                areasState = areasState,
                selectedFilter = selectedArea,
                favoriteIds = favoriteIds,
                listViewModel = listViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Common Ingredients", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 16.dp))
            IngredientFilterBar(
                selectedFilter = selectedIngredient,
                favoriteIds = favoriteIds,
                listViewModel = listViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))
        }


        // Main Content display area for search results.
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            // Displays error messages received from the Repository/ViewModel (M4 Error Handling).
            Text(
                text = errorMessage!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            // LazyColumn displays the search and filter results.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = recipes,
                    key = { it.id }
                ) { recipe ->
                    val currentIsFavorite = favoriteIds.contains(recipe.id)
                    // Uses the reusable RecipeCard component (M2 Component).
                    RecipeCard(
                        recipe = recipe.copy(isFavorite = currentIsFavorite),
                        onClick = { onRecipeClick(recipe.id) },
                        // Calls the Global ViewModel to modify persistent favorite status (M2/M6 Operation).
                        onFavoriteClick = { isFavorite ->
                            if (isFavorite) {
                                globalViewModel.addFavorite(recipe)
                            } else {
                                globalViewModel.removeFavorite(recipe.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Filter Composables define the horizontal filter bars (M6 Feature).
 */
@Composable
fun CategoryFilterBar(
    categoriesState: Result<List<Category>>,
    selectedFilter: String?,
    favoriteIds: Set<String>,
    listViewModel: RecipeListViewModel
) {
    // Displays based on the state of the categories fetch (Loading, Success, or Error).
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
                // Creates a clickable FilterChip for each category.
                items(categoriesState.data.take(10), key = { it.id }) { category ->
                    FilterChip(
                        selected = selectedFilter == category.name,
                        onClick = {
                            listViewModel.filterAndDisplayRecipes("category", category.name, favoriteIds)
                        },
                        label = { Text(category.name) }
                    )
                }
            }
        }
        is Result.Error -> {
            Text(
                "Could not load categories.",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun AreaFilterBar(
    areasState: Result<List<Name>>,
    selectedFilter: String?,
    favoriteIds: Set<String>,
    listViewModel: RecipeListViewModel
) {
    when (areasState) {
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
                // Creates a clickable FilterChip for each area.
                items(areasState.data.take(10), key = { it.name }) { area ->
                    FilterChip(
                        selected = selectedFilter == area.name,
                        onClick = {
                            listViewModel.filterAndDisplayRecipes("area", area.name, favoriteIds)
                        },
                        label = { Text(area.name) }
                    )
                }
            }
        }
        is Result.Error -> {
            Text(
                "Could not load areas.",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun IngredientFilterBar(
    selectedFilter: String?,
    favoriteIds: Set<String>,
    listViewModel: RecipeListViewModel
) {
    // Hardcoded common ingredients for a simple horizontal scroll (M6 Feature).
    val commonIngredients = listOf("Chicken", "Beef", "Salmon", "Cheese", "Pasta")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(commonIngredients) { ingredient ->
            FilterChip(
                selected = selectedFilter == ingredient,
                onClick = {
                    listViewModel.filterAndDisplayRecipes("ingredient", ingredient, favoriteIds)
                },
                label = { Text(ingredient) }
            )
        }
    }
}