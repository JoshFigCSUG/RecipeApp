package com.csugprojects.recipeapp.data.api

import com.csugprojects.recipeapp.data.model.CategoryListDto
import com.csugprojects.recipeapp.data.model.MealListDto
import com.csugprojects.recipeapp.data.model.NameListDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * RecipeApiService is the Retrofit interface (Model Layer) that defines all HTTP requests (M4 Design).
 * The DTO classes imported above handle the direct mapping of JSON data from the TheMealDB API (M4 Design).
 */
interface RecipeApiService {

    /**
     * Searches for meals by name (M2 core requirement).
     */
    @GET("search.php")
    suspend fun searchMeals(@Query("s") mealName: String): MealListDto

    /**
     * Retrieves the full details for a single recipe using its unique ID (M2 core requirement).
     */
    @GET("lookup.php")
    suspend fun getMealDetails(@Query("i") idMeal: String): MealListDto

    // --- Expanded API Functionality (M6 Implementation) ---

    /**
     * Fetches a single random meal, used for the Home Screen.
     */
    @GET("random.php")
    suspend fun getRandomMeal(): MealListDto

    /**
     * Fetches the complete list of all meal categories.
     */
    @GET("categories.php")
    suspend fun getCategories(): CategoryListDto

    /**
     * Fetches a list of all available ingredients.
     */
    @GET("list.php")
    suspend fun listIngredients(@Query("i") list: String = "list"): NameListDto

    /**
     * Fetches a list of all available meal origins (areas).
     */
    @GET("list.php")
    suspend fun listAreas(@Query("a") list: String = "list"): NameListDto

    /**
     * Filters the list of recipes by a specific category.
     */
    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealListDto

    /**
     * Filters the list of recipes by a specific area/cuisine.
     */
    @GET("filter.php")
    suspend fun filterByArea(@Query("a") area: String): MealListDto

    /**
     * Filters the list of recipes by a specific main ingredient.
     */
    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealListDto
}