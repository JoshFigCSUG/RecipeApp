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
        // Fetch initial lists for filter options (e.g., categories, areas)
        fetchCategories()
        fetchAreas()
    }

    // --- SEARCH & FILTER OPERATIONS ---

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Executes a text search against the meal database.
     */
    fun searchRecipes(favoriteIds: Set<String>) {
        if (_searchQuery.value.isBlank()) return
        _isLoading.value = true
        // Clear active filters when performing a new text search
        clearAllFilters()

        viewModelScope.launch {
            _errorMessage.value = null
            when (val result = repository.searchRecipes(_searchQuery.value)) {
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
        // FIX: newQuery is defined here, making it accessible below.
        val newQuery = if (currentState.value == query) null else query

        // 3. Clear ALL filters first for single-selection across all filter groups
        clearAllFilters()

        // 4. Set the new selection state (null if toggled off, or the new value)
        currentState.value = newQuery

        // 5. Execute API call only if a new query is active
        if (newQuery != null) {
            viewModelScope.launch {

                // FIX: Corrected ingredient filtering to use searchRecipes
                val result = when (filterType) {
                    "category" -> repository.filterByCategory(newQuery)
                    "area" -> repository.filterByArea(newQuery)
                    "ingredient" -> repository.searchRecipes(newQuery) // Uses full search for ingredients
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