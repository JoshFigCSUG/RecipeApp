package com.csugprojects.recipeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM favorite_recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)

    @Query("SELECT * FROM favorite_recipes")
    fun getAllFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT EXISTS(SELECT * FROM favorite_recipes WHERE id = :recipeId)")
    fun isFavorite(recipeId: String): Flow<Boolean>
}