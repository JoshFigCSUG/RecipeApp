package com.csugprojects.recipeapp.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
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

    init {
        viewModelScope.launch {
            repository.getFavoriteRecipes().collect { favorites ->
                _favoriteRecipes.value = favorites
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun searchRecipes() {
        if (_searchQuery.value.isBlank()) return
        _isLoading.value = true
        viewModelScope.launch {
            _errorMessage.value = null
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
                is Result.Loading -> { /* Do nothing, as loading is handled by _isLoading */ }
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
        }
    }
}