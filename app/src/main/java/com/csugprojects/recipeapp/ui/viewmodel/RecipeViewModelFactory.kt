package com.csugprojects.recipeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.csugprojects.recipeapp.domain.repository.RecipeRepository

class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlobalRecipeOperationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GlobalRecipeOperationsViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(RecipeListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecipeDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}