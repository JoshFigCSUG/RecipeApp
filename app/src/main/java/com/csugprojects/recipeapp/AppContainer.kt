package com.csugprojects.recipeapp

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey // UPDATED: Use MasterKey instead of deprecated MasterKeys
import com.csugprojects.recipeapp.data.api.RecipeApiService
import com.csugprojects.recipeapp.data.local.RecipeDatabase
import com.csugprojects.recipeapp.data.repository.RecipeRepositoryImpl
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val baseUrl = "https://www.themealdb.com/api/json/v1/1/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
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

    // --- MILESTONE 6: SECURITY IMPLEMENTATION (Modern/Non-Deprecated) ---

    // 1. Create or get the master key using the modern Builder pattern
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    // 2. Initialize EncryptedSharedPreferences using the MasterKey object
    val securePreferences by lazy {
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "secure_app_prefs", // The name of the file to store your encrypted preferences
            masterKey, // Use the non-deprecated MasterKey object
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    // -----------------------------------------------------------------

    val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(
            apiService = recipeApiService,
            recipeDao = database.recipeDao()
        )
    }
}