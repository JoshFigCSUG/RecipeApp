@file:Suppress("DEPRECATION")

package com.csugprojects.recipeapp.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.csugprojects.recipeapp.data.api.RecipeApiService
import com.csugprojects.recipeapp.data.local.RecipeDatabase
import com.csugprojects.recipeapp.data.repository.RecipeRepositoryImpl
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * AppContainer manages the application's dependencies (Dependency Injection - M4 Design).
 * This class creates single instances of services like the API client and database,
 * making them available to ViewModels across the application.
 */
class AppContainer(context: Context) {
    private val baseUrl = "https://www.themealdb.com/api/json/v1/1/"

    // Retrofit instance used for all network calls (M4 Technical Specification).
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Service implementation that defines network endpoints (Model Layer - M4 Design).
    private val recipeApiService: RecipeApiService by lazy {
        retrofit.create(RecipeApiService::class.java)
    }

    // Room Database instance for local persistence (Model Layer - M4 Design).
    private val database: RecipeDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            RecipeDatabase::class.java,
            "recipe-database"
        ).build()
    }

    // --- MILESTONE 6: SECURITY IMPLEMENTATION ---

    // Creates or gets the master key for encrypting local preferences (M6 Security).
    @Suppress("DEPRECATION")
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    // Initializes the EncryptedSharedPreferences object for secure storage (M6 Security).
    // The @Suppress("UNUSED") tag ignores the "never used" warning, as this property is meant
    // for future external use by other components (e.g., ViewModels).
    @Suppress("DEPRECATION", "UNUSED")
    val securePreferences by lazy {
        EncryptedSharedPreferences.create(
            context.applicationContext,
            "secure_app_prefs", // File name for encrypted preferences
            masterKey, // The securely generated MasterKey object
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    // -----------------------------------------------------------------

    /**
     * The central Repository instance, combining API and local database access (M4 Repository Pattern).
     */
    val recipeRepository: RecipeRepository by lazy {
        RecipeRepositoryImpl(
            apiService = recipeApiService,
            recipeDao = database.recipeDao()
        )
    }
}