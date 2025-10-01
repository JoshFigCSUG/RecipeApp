package com.csugprojects.recipeapp.domain.repository

import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun searchRecipes(query: String): Result<List<Recipe>>
    suspend fun getRecipeDetails(id: String): Result<Recipe>
    fun getFavoriteRecipes(): Flow<List<Recipe>>
    suspend fun addFavorite(recipe: Recipe)
    suspend fun removeFavorite(recipeId: String)
}