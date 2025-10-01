package com.csugprojects.recipeapp.domain.repository

import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.Flow
import com.csugprojects.recipeapp.domain.model.Category // Simple domain models needed
import com.csugprojects.recipeapp.domain.model.Name

interface RecipeRepository {
    // --- Existing Data Operations ---
    suspend fun searchRecipes(query: String): Result<List<Recipe>>
    suspend fun getRecipeDetails(id: String): Result<Recipe>
    fun getFavoriteRecipes(): Flow<List<Recipe>>
    suspend fun addFavorite(recipe: Recipe)
    suspend fun removeFavorite(recipeId: String)

    // --- New Search/Filter/List Functions ---
    suspend fun getRandomRecipe(): Result<Recipe>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun listIngredients(): Result<List<Name>>
    suspend fun listAreas(): Result<List<Name>>
    suspend fun filterByCategory(category: String): Result<List<Recipe>>
    suspend fun filterByArea(area: String): Result<List<Recipe>>
    suspend fun filterByIngredient(ingredient: String): Result<List<Recipe>>
}