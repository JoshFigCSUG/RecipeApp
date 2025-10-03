package com.csugprojects.recipeapp.domain.repository

import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.Flow
import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Name

/**
 * RecipeRepository defines the contract for all data operations (Model Layer - M4 Repository Pattern).
 * This interface abstracts data sources (API or database) from the ViewModels.
 */
interface RecipeRepository {

    /**
     * Searches for recipes by a query (M2 Core Feature).
     */
    suspend fun searchRecipes(query: String): Result<List<Recipe>>

    /**
     * Fetches the full details for a single recipe (M2 Core Feature).
     */
    suspend fun getRecipeDetails(id: String): Result<Recipe>

    /**
     * Retrieves the stream of favorite recipes from the local database (M4 Persistence).
     * The Flow ensures reactive UI updates (M6 State Management).
     */
    fun getFavoriteRecipes(): Flow<List<Recipe>>

    /**
     * Saves a recipe to the persistent favorites list (M2/M4 Persistence).
     */
    suspend fun addFavorite(recipe: Recipe)

    /**
     * Deletes a recipe from the persistent favorites list (M2/M4 Persistence).
     */
    suspend fun removeFavorite(recipeId: String)

    // --- Expanded Search, Filter, and List Functions (M6 Feature Expansion) ---

    /**
     * Fetches a single random recipe (M6 Feature).
     */
    suspend fun getRandomRecipe(): Result<Recipe>

    /**
     * Retrieves the list of all available meal categories (M6 Filtering).
     */
    suspend fun getCategories(): Result<List<Category>>

    /**
     * Retrieves the list of all available ingredients (M6 Filtering).
     */
    suspend fun listIngredients(): Result<List<Name>>

    /**
     * Retrieves the list of all available meal areas (cuisines) (M6 Filtering).
     */
    suspend fun listAreas(): Result<List<Name>>

    /**
     * Filters the list of recipes by a specific category (M6 Filtering).
     */
    suspend fun filterByCategory(category: String): Result<List<Recipe>>

    /**
     * Filters the list of recipes by a specific area/cuisine (M6 Filtering).
     */
    suspend fun filterByArea(area: String): Result<List<Recipe>>

    /**
     * Filters the list of recipes by a specific main ingredient (M6 Filtering).
     */
    suspend fun filterByIngredient(ingredient: String): Result<List<Recipe>>
}