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

class RecipeRepositoryImpl(
    private val apiService: RecipeApiService,
    private val recipeDao: RecipeDao
) : RecipeRepository {

    // --- Existing Data Operations ---

    override suspend fun searchRecipes(query: String): Result<List<Recipe>> {
        return withContext(Dispatchers.IO) {
            try {
                val response: MealListDto = apiService.searchMeals(query)
                val meals = response.meals ?: emptyList()
                if (meals.isEmpty()) {
                    Result.Error(Exception("No recipes found for '$query'"))
                } else {
                    // Assumes MealDto has a toRecipe() extension function
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

    // --- New Search/Filter/List Functions ---

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

    // --- Filter Implementations (Common Mapping Logic) ---

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