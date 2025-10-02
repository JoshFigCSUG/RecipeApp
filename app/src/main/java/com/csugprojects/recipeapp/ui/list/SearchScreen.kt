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
import androidx.compose.runtime.collectAsState
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeListViewModel
import com.csugprojects.recipeapp.ui.list.RecipeCard
import com.csugprojects.recipeapp.domain.model.Name
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
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
    val areasState by listViewModel.areas

    // NEW: Collect selected filter states
    val selectedCategory by listViewModel.selectedCategory
    val selectedArea by listViewModel.selectedArea
    val selectedIngredient by listViewModel.selectedIngredient

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

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            // --- 1. Horizontal Category Filter Bar ---
            Text("Categories", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            CategoryFilterBar(
                categoriesState = categoriesState,
                selectedFilter = selectedCategory, // Pass selected state
                favoriteIds = favoriteIds,
                listViewModel = listViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Horizontal Area Filter Bar ---
            Text("Areas", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 16.dp))
            AreaFilterBar(
                areasState = areasState,
                selectedFilter = selectedArea, // Pass selected state
                favoriteIds = favoriteIds,
                listViewModel = listViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. Horizontal Ingredient Filter Bar ---
            Text("Common Ingredients", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 16.dp))
            IngredientFilterBar(
                selectedFilter = selectedIngredient, // Pass selected state
                favoriteIds = favoriteIds,
                listViewModel = listViewModel
            )

            Spacer(modifier = Modifier.height(16.dp))
        }


        // --- Main Content Display (LazyColumn for Search Results) ---
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
                items(
                    items = recipes,
                    key = { it.id }
                ) { recipe ->
                    val currentIsFavorite = favoriteIds.contains(recipe.id)
                    RecipeCard(
                        recipe = recipe.copy(isFavorite = currentIsFavorite),
                        onClick = { onRecipeClick(recipe.id) },
                        onFavoriteClick = { isFavorite ->
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

// --- 1. Category Filter Bar (FIXED & PERSISTENT) ---
@Composable
fun CategoryFilterBar(
    categoriesState: Result<List<Category>>,
    selectedFilter: String?,
    favoriteIds: Set<String>,
    listViewModel: RecipeListViewModel
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
                items(categoriesState.data.take(10), key = { it.id }) { category ->
                    FilterChip(
                        selected = selectedFilter == category.name, // PERSISTENCE FIX
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


// --- 2. Area Filter Bar (FIXED & PERSISTENT) ---
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
                items(areasState.data.take(10), key = { it.name }) { area ->
                    FilterChip(
                        selected = selectedFilter == area.name, // PERSISTENCE FIX
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

// --- 3. Ingredient Filter Bar (FIXED & PERSISTENT) ---
@Composable
fun IngredientFilterBar(
    selectedFilter: String?,
    favoriteIds: Set<String>,
    listViewModel: RecipeListViewModel
) {
    // Hardcoded common ingredients for a simple horizontal scroll
    val commonIngredients = listOf("Chicken", "Beef", "Salmon", "Cheese", "Pasta")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(commonIngredients) { ingredient ->
            FilterChip(
                selected = selectedFilter == ingredient, // PERSISTENCE FIX
                onClick = {
                    listViewModel.filterAndDisplayRecipes("ingredient", ingredient, favoriteIds)
                },
                label = { Text(ingredient) }
            )
        }
    }
}