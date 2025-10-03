package com.csugprojects.recipeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * RecipeDatabase is the abstract class that represents the local data source (Model Layer - M4 Design).
 * It stores recipes marked as favorites, providing offline support (M4 Feature).
 */
@Database(entities = [RecipeEntity::class], version = 2, exportSchema = false)
// The TypeConverters annotation ensures complex types like List<Ingredient> can be stored (M4 Design).
@TypeConverters(Converters::class)
abstract class RecipeDatabase : RoomDatabase() {

    /**
     * Exposes the Data Access Object (DAO) that allows the Repository to perform CRUD operations.
     */
    abstract fun recipeDao(): RecipeDao
}