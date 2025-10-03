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
 * RecipeDetailViewModel manages the state and business logic specific to displaying one recipe (ViewModel Layer - M2/M4).
 * It is scoped to the lifecycle of the detail screen.
 */
class RecipeDetailViewModel(private val repository: RecipeRepository) : ViewModel() {

    // Mutable flow holds the detailed recipe data wrapped in a Result class (M4 Error Handling).
    private val _recipeState = MutableStateFlow<Result<Recipe>>(Result.Loading)
    // Public state flow for the UI to observe (M6 State Management).
    val recipeState: StateFlow<Result<Recipe>> = _recipeState

    /**
     * Initiates the asynchronous request to fetch the complete recipe details from the Repository (M4 Data Flow).
     */
    fun getRecipeDetails(recipeId: String) {
        viewModelScope.launch {
            // Sets the state to Loading while the network request is processed.
            _recipeState.value = Result.Loading

            // Calls the Repository, which handles the I/O off the main thread (M6 Performance).
            val result = repository.getRecipeDetails(recipeId)

            // Updates the public state with the final result (Success or Error).
            _recipeState.value = result
        }
    }
}