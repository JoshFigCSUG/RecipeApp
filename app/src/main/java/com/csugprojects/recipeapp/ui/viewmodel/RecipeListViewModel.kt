package com.csugprojects.recipeapp.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Name
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.util.Result
// NEW IMPORTS for concurrent operations
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RecipeListViewModel(private val repository: RecipeRepository) : ViewModel() {

    // --- LIST STATES (Results of search/filter) ---
    private val _recipes = mutableStateOf<List<Recipe>>(emptyList())
    val recipes: State<List<Recipe>> = _recipes

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // --- FILTER/CATEGORY STATES (Lookups) ---
    private val _categories = mutableStateOf<Result<List<Category>>>(Result.Loading)
    val categories: State<Result<List<Category>>> = _categories

    private val _areas = mutableStateOf<Result<List<Name>>>(Result.Loading)
    val areas: State<Result<List<Name>>> = _areas

    // --- Selected Filter States for Persistence ---
    private val _selectedCategory = mutableStateOf<String?>(null)
    val selectedCategory: State<String?> = _selectedCategory

    private val _selectedArea = mutableStateOf<String?>(null)
    val selectedArea: State<String?> = _selectedArea

    private val _selectedIngredient = mutableStateOf<String?>(null)
    val selectedIngredient: State<String?> = _selectedIngredient

    init {
        fetchCategories()
        fetchAreas()
    }

    // --- SEARCH & FILTER OPERATIONS ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Executes a DUAL search (by name AND by ingredient) to overcome API strictness.
     */
    fun searchRecipes(favoriteIds: Set<String>) {
        if (_searchQuery.value.isBlank()) return
        _isLoading.value = true
        clearAllFilters()

        viewModelScope.launch {
            _errorMessage.value = null

            val query = _searchQuery.value

            // Execute both API calls concurrently
            val nameSearchResultDeferred = async { repository.searchRecipes(query) }
            val ingredientFilterResultDeferred = async { repository.filterByIngredient(query) } // Use ingredient filter as broad search

            val nameResult = nameSearchResultDeferred.await()
            val ingredientResult = ingredientFilterResultDeferred.await()

            val combinedRecipes = mutableListOf<Recipe>()
            val seenIds = mutableSetOf<String>()

            // 1. Process Name Search Results (Higher detail)
            if (nameResult is Result.Success) {
                nameResult.data.forEach { recipe ->
                    if (seenIds.add(recipe.id)) {
                        combinedRecipes.add(recipe)
                    }
                }
            }

            // 2. Process Ingredient Filter Results (More reliable hits)
            if (ingredientResult is Result.Success) {
                ingredientResult.data.forEach { recipe ->
                    if (seenIds.add(recipe.id)) {
                        combinedRecipes.add(recipe)
                    }
                }
            }

            // 3. Handle combined result states
            val finalResult = when {
                combinedRecipes.isNotEmpty() -> Result.Success(combinedRecipes)
                nameResult is Result.Error -> nameResult // Prioritize error from main search
                ingredientResult is Result.Error -> ingredientResult
                else -> Result.Error(Exception("No recipes found for '$query'"))
            }

            when (finalResult) {
                is Result.Success -> {
                    _recipes.value = finalResult.data.map { recipe ->
                        recipe.copy(isFavorite = favoriteIds.contains(recipe.id))
                    }
                }
                is Result.Error -> {
                    _recipes.value = emptyList()
                    _errorMessage.value = finalResult.exception.message
                }
                is Result.Loading -> { /* Should not happen here */ }
            }

            _isLoading.value = false
        }
    }

    private fun clearAllFilters() {
        _selectedCategory.value = null
        _selectedArea.value = null
        _selectedIngredient.value = null
    }

    /**
     * Handles all filtering types (by category, area, or ingredient) including toggling.
     */
    fun filterAndDisplayRecipes(filterType: String, query: String, favoriteIds: Set<String>) {
        _isLoading.value = true
        _errorMessage.value = null

        // 1. Determine which state variable to check/update
        val currentState = when (filterType) {
            "category" -> _selectedCategory
            "area" -> _selectedArea
            "ingredient" -> _selectedIngredient
            else -> return
        }

        // 2. Toggling Logic: If the clicked chip is already selected, clear it (set to null)
        val newQuery = if (currentState.value == query) null else query

        // 3. Clear ALL filters first for single-selection across all filter groups
        clearAllFilters()

        // 4. Set the new selection state (null if toggled off, or the new value)
        currentState.value = newQuery

        // 5. Execute API call only if a new query is active
        if (newQuery != null) {
            viewModelScope.launch {

                val result = when (filterType) {
                    "category" -> repository.filterByCategory(newQuery)
                    "area" -> repository.filterByArea(newQuery)
                    "ingredient" -> repository.searchRecipes(newQuery) // Ingredient filter uses full search
                    else -> return@launch
                }

                when (result) {
                    is Result.Success -> {
                        _recipes.value = result.data.map { recipe ->
                            recipe.copy(isFavorite = favoriteIds.contains(recipe.id))
                        }
                    }
                    is Result.Error -> {
                        _recipes.value = emptyList()
                        _errorMessage.value = result.exception.message
                    }
                    is Result.Loading -> { /* Handled by _isLoading */ }
                }
                _isLoading.value = false
            }
        } else {
            // If filter is cleared (toggled off), show no results.
            _recipes.value = emptyList()
            _isLoading.value = false
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            _categories.value = Result.Loading
            _categories.value = repository.getCategories()
        }
    }

    private fun fetchAreas() {
        viewModelScope.launch {
            _areas.value = Result.Loading
            _areas.value = repository.listAreas()
        }
    }
}