package com.csugprojects.recipeapp

import android.content.Context
import androidx.room.Room
import com.csugprojects.recipeapp.data.api.RecipeApiService
import com.csugprojects.recipeapp.data.local.RecipeDatabase
import com.csugprojects.recipeapp.data.repository.RecipeRepositoryImpl
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val recipeApiService: RecipeApiService by lazy {
        retrofit.create(RecipeApiService::class.java)
    }

    private val database: RecipeDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            RecipeDatabase::class.java,
            "recipe-database"
        ).build()
    }

    val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(
            apiService = recipeApiService,
            recipeDao = database.recipeDao()
        )
    }
}