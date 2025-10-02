package com.csugprojects.recipeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeDetailViewModel(private val repository: RecipeRepository) : ViewModel() {

    // State to hold the result of the detail fetch
    private val _recipeState = MutableStateFlow<Result<Recipe>>(Result.Loading)
    val recipeState: StateFlow<Result<Recipe>> = _recipeState

    // Function to initiate the detail fetch (can be called from the Composable's LaunchedEffect)
    fun getRecipeDetails(recipeId: String) {
        viewModelScope.launch {
            _recipeState.value = Result.Loading
            val result = repository.getRecipeDetails(recipeId)
            _recipeState.value = result
        }
    }
}