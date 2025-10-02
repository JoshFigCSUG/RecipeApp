package com.csugprojects.recipeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GlobalRecipeOperationsViewModel(private val repository: RecipeRepository) : ViewModel() {

    // --- FAVORITES STATES ---
    private val _favoriteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val favoriteRecipes: StateFlow<List<Recipe>> = _favoriteRecipes

    // --- RECENTLY VIEWED STATES ---
    private val _recentlyViewed = MutableStateFlow<List<Recipe>>(emptyList())
    val recentlyViewed: StateFlow<List<Recipe>> = _recentlyViewed

    init {
        // Start collecting favorite recipes immediately for UI status updates
        viewModelScope.launch {
            repository.getFavoriteRecipes().collect { favorites ->
                _favoriteRecipes.value = favorites
            }
        }
    }

    // --- RANDOM RECIPE (HOME SCREEN) ---
    fun fetchRandomRecipe(onResult: (Result<Recipe>) -> Unit) {
        viewModelScope.launch {
            onResult(Result.Loading)
            val result = repository.getRandomRecipe()
            onResult(result)
        }
    }

    // --- FAVORITES OPERATIONS (Shared across screens) ---
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

    // --- RECENTLY VIEWED OPERATIONS (Called by Detail ViewModel) ---
    fun addRecentlyViewedRecipe(recipe: Recipe) {
        viewModelScope.launch {
            // Remove the recipe if it already exists, then add it to the front.
            val updatedList = listOf(recipe) + _recentlyViewed.value.filter { it.id != recipe.id }
            // Limit the list to the 5 most recent recipes
            _recentlyViewed.value = updatedList.take(5)
        }
    }
}