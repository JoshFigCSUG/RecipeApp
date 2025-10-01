package com.csugprojects.recipeapp.data.api

import com.csugprojects.recipeapp.data.model.CategoryListDto // New DTO needed for categories
import com.csugprojects.recipeapp.data.model.MealListDto
import com.csugprojects.recipeapp.data.model.NameListDto // New DTO needed for area/ingredient lists
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApiService {
    // --- Existing Search/Lookup ---
    @GET("search.php")
    suspend fun searchMeals(@Query("s") mealName: String): MealListDto

    @GET("lookup.php")
    suspend fun getMealDetails(@Query("i") idMeal: String): MealListDto

    // --- New Free API Capabilities ---

    // 1. Lookup a Random Meal
    @GET("random.php")
    suspend fun getRandomMeal(): MealListDto

    // 2. List All Meal Categories
    @GET("categories.php")
    suspend fun getCategories(): CategoryListDto

    // 3. List All Categories, Areas, Ingredients (using list.php)
    @GET("list.php")
    suspend fun listIngredients(@Query("i") list: String = "list"): NameListDto

    @GET("list.php")
    suspend fun listAreas(@Query("a") list: String = "list"): NameListDto

    // 4. Filter by Category
    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealListDto

    // 5. Filter by Area
    @GET("filter.php")
    suspend fun filterByArea(@Query("a") area: String): MealListDto

    // 6. Filter by Ingredient
    @GET("filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealListDto
}