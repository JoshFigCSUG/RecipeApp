package com.csugprojects.recipeapp.data.repository

import com.csugprojects.recipeapp.data.api.RecipeApiService
import com.csugprojects.recipeapp.data.local.RecipeDao
import com.csugprojects.recipeapp.data.model.MealListDto
import com.csugprojects.recipeapp.data.model.MealDto
import com.csugprojects.recipeapp.data.model.toDomain
import com.csugprojects.recipeapp.data.model.toDomainName
import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Name
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * RecipeRepositoryImpl is the concrete implementation of the Repository interface (Model Layer - M4 Design).
 * It acts as the single source of truth, coordinating data from the API and the local database.
 */
class RecipeRepositoryImpl(
    private val apiService: RecipeApiService,
    private val recipeDao: RecipeDao
) : RecipeRepository {

    /**
     * Searches for recipes by keyword (M2 Core Feature).
     * Uses Dispatchers.IO to ensure network operation is off the main thread (M6 Performance).
     */
    override suspend fun searchRecipes(query: String): Result<List<Recipe>> {
        return withContext(Dispatchers.IO) {
            try {
                val response: MealListDto = apiService.searchMeals(query)
                val meals = response.meals ?: emptyList()
                if (meals.isEmpty()) {
                    Result.Error(Exception("No recipes found for '$query'"))
                } else {
                    val recipes = meals.map { it.toRecipe() }
                    Result.Success(recipes)
                }
            } catch (e: IOException) {
                // Handles network connection issues (M4 Error Handling).
                Result.Error(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                // Handles general API or unexpected errors.
                Result.Error(Exception("Failed to fetch recipes: ${e.message}", e))
            }
        }
    }

    /**
     * Retrieves full details for a single recipe (M2 Core Feature).
     */
    override suspend fun getRecipeDetails(id: String): Result<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val response: MealListDto = apiService.getMealDetails(id)
                val meal: MealDto? = response.meals?.firstOrNull()
                if (meal == null) {
                    Result.Error(Exception("Recipe not found for ID: $id"))
                } else {
                    Result.Success(meal.toRecipe())
                }
            } catch (e: IOException) {
                Result.Error(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                Result.Error(Exception("Failed to get recipe details: ${e.message}", e))
            }
        }
    }

    /**
     * Retrieves all saved favorite recipes from the local Room database (M4 Persistence).
     * Returns a Flow for reactive state management (M6 State Management).
     */
    override fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllFavoriteRecipes().map { entities ->
            entities.map { it.toRecipe().copy(isFavorite = true) }
        }
    }

    /**
     * Saves a recipe to the local database as a favorite.
     */
    override suspend fun addFavorite(recipe: Recipe) {
        withContext(Dispatchers.IO) {
            recipeDao.insertRecipe(recipe.toRecipeEntity())
        }
    }

    /**
     * Removes a recipe from the local favorites database.
     */
    override suspend fun removeFavorite(recipeId: String) {
        withContext(Dispatchers.IO) {
            recipeDao.deleteRecipeById(recipeId)
        }
    }

    // --- New Search/Filter/List Functions (M6 Feature Expansion) ---

    /**
     * Fetches a single random recipe, used primarily for the Home Screen.
     */
    override suspend fun getRandomRecipe(): Result<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val response: MealListDto = apiService.getRandomMeal()
                val meal: MealDto? = response.meals?.firstOrNull()
                if (meal == null) {
                    Result.Error(Exception("No random recipe found."))
                } else {
                    Result.Success(meal.toRecipe())
                }
            } catch (e: IOException) {
                Result.Error(Exception("Network error fetching random recipe: ${e.message}", e))
            } catch (e: Exception) {
                Result.Error(Exception("Failed to fetch random recipe: ${e.message}", e))
            }
        }
    }

    /**
     * Fetches the list of all available categories.
     */
    override suspend fun getCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCategories()
                val categories = response.categories.map { it.toDomain() }
                Result.Success(categories)
            } catch (e: IOException) {
                Result.Error(Exception("Network error fetching categories: ${e.message}", e))
            } catch (e: Exception) {
                Result.Error(Exception("Failed to fetch categories: ${e.message}", e))
            }
        }
    }

    /**
     * Fetches a list of ingredients for use in filtering.
     */
    override suspend fun listIngredients(): Result<List<Name>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.listIngredients()
                val ingredients = response.names
                    ?.filter { !it.strIngredient.isNullOrBlank() }
                    ?.map { it.toDomainName(type = "Ingredient") } ?: emptyList()
                Result.Success(ingredients)
            } catch (e: IOException) {
                Result.Error(Exception("Network error fetching ingredients: ${e.message}", e))
            } catch (e: Exception) {
                Result.Error(Exception("Failed to fetch ingredients: ${e.message}", e))
            }
        }
    }

    /**
     * Fetches a list of areas/cuisines for use in filtering.
     */
    override suspend fun listAreas(): Result<List<Name>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.listAreas()
                val areas = response.names
                    ?.filter { !it.strArea.isNullOrBlank() }
                    ?.map { it.toDomainName(type = "Area") } ?: emptyList()
                Result.Success(areas)
            } catch (e: IOException) {
                Result.Error(Exception("Network error fetching areas: ${e.message}", e))
            } catch (e: Exception) {
                Result.Error(Exception("Failed to fetch areas: ${e.message}", e))
            }
        }
    }

    /**
     * Handles the common logic for filtering API requests, wrapping the result in the Result sealed class.
     * This function uses the standard error handling designed in M4.
     */
    private suspend fun filterMeals(call: suspend () -> MealListDto): Result<List<Recipe>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call.invoke()
                val meals = response.meals ?: emptyList()
                if (meals.isEmpty()) {
                    Result.Error(Exception("No recipes found for this filter."))
                } else {
                    val recipes = meals.map { it.toRecipe() }
                    Result.Success(recipes)
                }
            } catch (e: IOException) {
                Result.Error(Exception("Network error during filtering: ${e.message}", e))
            } catch (e: Exception) {
                Result.Error(Exception("Failed to filter recipes: ${e.message}", e))
            }
        }
    }

    override suspend fun filterByCategory(category: String): Result<List<Recipe>> {
        return filterMeals { apiService.filterByCategory(category) }
    }

    override suspend fun filterByArea(area: String): Result<List<Recipe>> {
        return filterMeals { apiService.filterByArea(area) }
    }

    override suspend fun filterByIngredient(ingredient: String): Result<List<Recipe>> {
        return filterMeals { apiService.filterByIngredient(ingredient) }
    }
}