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
        viewModelScope.launch {
            _errorMessage.value = null
            when (val result = repository.searchRecipes(_searchQuery.value)) {
                is Result.Success -> {
                    // Update recipe list, maintaining current favorite status
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

    /**
     * Handles all filtering types (by category, area, or ingredient).
     */
    fun filterAndDisplayRecipes(filterType: String, query: String, favoriteIds: Set<String>) {
        _isLoading.value = true
        viewModelScope.launch {
            _errorMessage.value = null
            val result = when (filterType) {
                "category" -> repository.filterByCategory(query)
                "area" -> repository.filterByArea(query)
                "ingredient" -> repository.filterByIngredient(query)
                else -> return@launch
            }

            when (result) {
                is Result.Success -> {
                    // Update recipe list, maintaining current favorite status
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