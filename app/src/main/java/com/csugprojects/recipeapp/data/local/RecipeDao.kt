package com.csugprojects.recipeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * RecipeDao serves as the Data Access Object for the Room database (Model Layer - M4 Design).
 * It defines methods for performing local database operations on favorite recipes (M2 Feature).
 */
@Dao
interface RecipeDao {

    /**
     * Inserts a recipe entity into the local database (M4 Persistence).
     * Uses suspend to ensure the operation is run asynchronously (M6 Performance).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    /**
     * Deletes a specific recipe from the database using its unique ID.
     */
    @Query("DELETE FROM favorite_recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)

    /**
     * Retrieves all recipes marked as favorites.
     * Returns a Flow for reactive, real-time updates to the UI (M4/M6 State Management).
     */
    @Query("SELECT * FROM favorite_recipes")
    fun getAllFavoriteRecipes(): Flow<List<RecipeEntity>>

    /**
     * Checks if a specific recipe ID exists in the favorites table.
     * This function is often mocked for Unit Testing (M8 Testing Strategy).
     */
    @Query("SELECT EXISTS(SELECT * FROM favorite_recipes WHERE id = :recipeId)")
    fun isFavorite(recipeId: String): Flow<Boolean>
}