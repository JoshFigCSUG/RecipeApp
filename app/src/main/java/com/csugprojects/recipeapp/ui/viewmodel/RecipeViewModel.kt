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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    // --- Existing States ---
    private val _recipes = mutableStateOf<List<Recipe>>(emptyList())
    val recipes: State<List<Recipe>> = _recipes

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _favoriteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val favoriteRecipes: StateFlow<List<Recipe>> = _favoriteRecipes

    // --- NEW: Recently Viewed State ---
    private val _recentlyViewed = MutableStateFlow<List<Recipe>>(emptyList())
    val recentlyViewed: StateFlow<List<Recipe>> = _recentlyViewed

    // --- Existing States for Filter/List Capabilities ---
    private val _categories = mutableStateOf<Result<List<Category>>>(Result.Loading)
    val categories: State<Result<List<Category>>> = _categories

    private val _areas = mutableStateOf<Result<List<Name>>>(Result.Loading)
    val areas: State<Result<List<Name>>> = _areas

    // --- Initialization ---
    init {
        viewModelScope.launch {
            // Start collecting favorite recipes immediately for UI status updates
            repository.getFavoriteRecipes().collect { favorites ->
                _favoriteRecipes.value = favorites
            }
        }
        // Fetch initial lists for filter options (e.g., categories, areas)
        fetchCategories()
        fetchAreas()
    }

    // --- Core Functions ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun searchRecipes() {
        if (_searchQuery.value.isBlank()) return
        _isLoading.value = true
        viewModelScope.launch {
            _errorMessage.value = null
            // Calls the standard search function in the repository
            when (val result = repository.searchRecipes(_searchQuery.value)) {
                is Result.Success -> {
                    val currentFavorites = _favoriteRecipes.value.map { it.id }.toSet()
                    _recipes.value = result.data.map { recipe ->
                        recipe.copy(isFavorite = currentFavorites.contains(recipe.id))
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

    fun addFavorite(recipe: Recipe) {
        viewModelScope.launch {
            repository.addFavorite(recipe)
        }
    }

    fun removeFavorite(recipeId: String) {
        viewModelScope.launch {
            repository.removeFavorite(recipeId)
        }
    }

    fun getRecipeDetails(recipeId: String, onResult: (Result<Recipe>) -> Unit) {
        viewModelScope.launch {
            onResult(Result.Loading)
            val result = repository.getRecipeDetails(recipeId)
            onResult(result)

            // NEW LOGIC: Add to recently viewed list after successful fetch
            if (result is Result.Success) {
                addRecentlyViewedRecipe(result.data)
            }
        }
    }

    // --- NEW: Recently Viewed Helper Function ---
    fun addRecentlyViewedRecipe(recipe: Recipe) {
        viewModelScope.launch {
            // Remove the recipe if it already exists, then add it to the front.
            val updatedList = listOf(recipe) + _recentlyViewed.value.filter { it.id != recipe.id }
            // Limit the list to the 5 most recent recipes
            _recentlyViewed.value = updatedList.take(5)
        }
    }
    // ---------------------------------------------

    // --- New Filter/List Functions ---

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

    /**
     * Handles all filtering types (by category, area, or ingredient)
     * and updates the main recipe list.
     */
    fun filterAndDisplayRecipes(filterType: String, query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            _errorMessage.value = null
            val result = when (filterType) {
                "category" -> repository.filterByCategory(query)
                "area" -> repository.filterByArea(query)
                "ingredient" -> repository.filterByIngredient(query)
                else -> repository.searchRecipes(query)
            }

            when (result) {
                is Result.Success -> {
                    val currentFavorites = _favoriteRecipes.value.map { it.id }.toSet()
                    _recipes.value = result.data.map { recipe ->
                        recipe.copy(isFavorite = currentFavorites.contains(recipe.id))
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

    fun fetchRandomRecipe(onResult: (Result<Recipe>) -> Unit) {
        viewModelScope.launch {
            onResult(Result.Loading)
            val result = repository.getRandomRecipe()
            onResult(result)
        }
    }
}