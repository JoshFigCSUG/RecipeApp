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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * RecipeListViewModel manages the state and business logic for the Search screen (ViewModel Layer - M2/M4).
 * It handles search queries, filtering, and API lookup data.
 */
class RecipeListViewModel(private val repository: RecipeRepository) : ViewModel() {

    // --- LIST STATES (Results of search/filter) ---
    // State properties hold the data that the UI observes (M6 State Management).
    private val _recipes = mutableStateOf<List<Recipe>>(emptyList())
    val recipes: State<List<Recipe>> = _recipes

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    // Exposes errors that occur during API calls (M4 Error Handling).
    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    // --- FILTER/CATEGORY STATES (Lookups) ---
    // States hold data used to build the filter chips (M6 Feature).
    private val _categories = mutableStateOf<Result<List<Category>>>(Result.Loading)
    val categories: State<Result<List<Category>>> = _categories

    private val _areas = mutableStateOf<Result<List<Name>>>(Result.Loading)
    val areas: State<Result<List<Name>>> = _areas

    // --- Selected Filter States for Persistence ---
    // These track the currently active filter chip.
    private val _selectedCategory = mutableStateOf<String?>(null)
    val selectedCategory: State<String?> = _selectedCategory

    private val _selectedArea = mutableStateOf<String?>(null)
    val selectedArea: State<String?> = _selectedArea

    private val _selectedIngredient = mutableStateOf<String?>(null)
    val selectedIngredient: State<String?> = _selectedIngredient

    init {
        // Fetches filter options immediately when the ViewModel is created.
        fetchCategories()
        fetchAreas()
    }

    // --- SEARCH & FILTER OPERATIONS ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Executes a dual search (by name AND by ingredient) to improve result reliability (M6 Implementation).
     */
    fun searchRecipes(favoriteIds: Set<String>) {
        if (_searchQuery.value.isBlank()) return
        _isLoading.value = true
        clearAllFilters()

        viewModelScope.launch {
            _errorMessage.value = null

            val query = _searchQuery.value

            // Executes both API calls concurrently using async/await (M6 Concurrency/Performance).
            val nameSearchResultDeferred = async { repository.searchRecipes(query) }
            val ingredientFilterResultDeferred = async { repository.filterByIngredient(query) }

            val nameResult = nameSearchResultDeferred.await()
            val ingredientResult = ingredientFilterResultDeferred.await()

            val combinedRecipes = mutableListOf<Recipe>()
            val seenIds = mutableSetOf<String>()

            // 1. Process Name Search Results
            if (nameResult is Result.Success) {
                nameResult.data.forEach { recipe ->
                    if (seenIds.add(recipe.id)) {
                        combinedRecipes.add(recipe)
                    }
                }
            }

            // 2. Process Ingredient Filter Results
            if (ingredientResult is Result.Success) {
                ingredientResult.data.forEach { recipe ->
                    if (seenIds.add(recipe.id)) {
                        combinedRecipes.add(recipe)
                    }
                }
            }

            // 3. Determines the final state of the search operation (M4 Error Handling).
            val finalResult = when {
                combinedRecipes.isNotEmpty() -> Result.Success(combinedRecipes)
                nameResult is Result.Error -> nameResult
                ingredientResult is Result.Error -> ingredientResult
                else -> Result.Error(Exception("No recipes found for '$query'"))
            }

            when (finalResult) {
                is Result.Success -> {
                    // Updates the recipe list and checks favorite status against global state.
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

        // 1. Determines which state variable to check/update based on the clicked chip.
        val currentState = when (filterType) {
            "category" -> _selectedCategory
            "area" -> _selectedArea
            "ingredient" -> _selectedIngredient
            else -> return
        }

        // 2. Toggling Logic: If the clicked chip is already selected, clear it.
        val newQuery = if (currentState.value == query) null else query

        // 3. Clears ALL filters first to ensure only one filter type is active.
        clearAllFilters()

        // 4. Sets the new selection state.
        currentState.value = newQuery

        // 5. Executes API call only if a new filter is selected.
        if (newQuery != null) {
            viewModelScope.launch {

                val result = when (filterType) {
                    "category" -> repository.filterByCategory(newQuery)
                    "area" -> repository.filterByArea(newQuery)
                    // Ingredient filter uses the broad search API endpoint.
                    "ingredient" -> repository.searchRecipes(newQuery)
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
            // If filter is cleared (toggled off), the recipe list is emptied.
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