package com.csugprojects.recipeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * GlobalRecipeOperationsViewModel manages application-wide state and operations (ViewModel Layer - M2/M6).
 * This includes favorites and recently viewed recipes, shared across all screens.
 */
class GlobalRecipeOperationsViewModel(private val repository: RecipeRepository) : ViewModel() {

    // --- FAVORITES STATES ---
    // Mutable flow to hold the list of user favorite recipes (M2 Feature).
    private val _favoriteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    // Public state flow for the UI to observe (M6 State Management).
    val favoriteRecipes: StateFlow<List<Recipe>> = _favoriteRecipes

    // --- RECENTLY VIEWED STATES ---
    // Mutable flow to hold the list of recipes recently viewed by the user (M6 Feature).
    private val _recentlyViewed = MutableStateFlow<List<Recipe>>(emptyList())
    val recentlyViewed: StateFlow<List<Recipe>> = _recentlyViewed

    init {
        // Subscribes to the database flow immediately to keep the favorites list current (M4/M6 State Management).
        viewModelScope.launch {
            repository.getFavoriteRecipes().collect { favorites ->
                _favoriteRecipes.value = favorites
            }
        }
    }

    // --- RANDOM RECIPE (HOME SCREEN) ---
    /**
     * Fetches a single random recipe from the network.
     * Uses viewModelScope.launch for concurrency (M6 Performance/Battery).
     */
    fun fetchRandomRecipe(onResult: (Result<Recipe>) -> Unit) {
        viewModelScope.launch {
            onResult(Result.Loading)
            val result = repository.getRandomRecipe()
            onResult(result)
        }
    }

    // --- FAVORITES OPERATIONS (Shared across screens) ---
    /**
     * Adds a recipe to the persistent favorites list (M2 Operation).
     */
    fun addFavorite(recipe: Recipe) {
        viewModelScope.launch {
            repository.addFavorite(recipe)
        }
    }

    /**
     * Removes a recipe from the persistent favorites list (M2 Operation).
     */
    fun removeFavorite(recipeId: String) {
        viewModelScope.launch {
            repository.removeFavorite(recipeId)
        }
    }

    // --- RECENTLY VIEWED OPERATIONS ---
    /**
     * Adds a recipe to the recents list, limiting the total count to 5 (M6 Feature).
     */
    fun addRecentlyViewedRecipe(recipe: Recipe) {
        viewModelScope.launch {
            // Logic to move the viewed recipe to the front of the list.
            val updatedList = listOf(recipe) + _recentlyViewed.value.filter { it.id != recipe.id }
            // Limits the list size to the 5 most recent recipes.
            _recentlyViewed.value = updatedList.take(5)
        }
    }
}