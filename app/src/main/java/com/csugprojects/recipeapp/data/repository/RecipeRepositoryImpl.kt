package com.csugprojects.recipeapp.data.repository

import com.csugprojects.recipeapp.data.api.RecipeApiService
import com.csugprojects.recipeapp.data.local.RecipeDao
import com.csugprojects.recipeapp.data.model.MealListDto
import com.csugprojects.recipeapp.data.model.MealDto
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

class RecipeRepositoryImpl(
    private val apiService: RecipeApiService,
    private val recipeDao: RecipeDao
) : RecipeRepository {

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
                Result.Error(Exception("Network error: ${e.message}", e))
            } catch (e: Exception) {
                Result.Error(Exception("Failed to fetch recipes: ${e.message}", e))
            }
        }
    }

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

    override fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllFavoriteRecipes().map { entities ->
            entities.map { it.toRecipe().copy(isFavorite = true) }
        }
    }

    override suspend fun addFavorite(recipe: Recipe) {
        withContext(Dispatchers.IO) {
            recipeDao.insertRecipe(recipe.toRecipeEntity())
        }
    }

    override suspend fun removeFavorite(recipeId: String) {
        withContext(Dispatchers.IO) {
            recipeDao.deleteRecipeById(recipeId)
        }
    }
}