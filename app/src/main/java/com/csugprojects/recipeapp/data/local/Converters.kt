package com.csugprojects.recipeapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.csugprojects.recipeapp.domain.model.Ingredient

/**
 * This class provides TypeConverters to the Room Database (M4 Data Persistence).
 * Room cannot natively store complex objects like List<Ingredient>.
 * These functions use the Gson library to convert the list to a storable JSON string and back again.
 */
class Converters {

    /**
     * Converts a list of Ingredient objects into a single JSON string for database storage.
     */
    @TypeConverter
    fun fromIngredientList(list: List<Ingredient>): String {
        return Gson().toJson(list)
    }

    /**
     * Converts a JSON string retrieved from the database back into a List<Ingredient> object.
     * This restores the original format for the Model layer logic.
     */
    @TypeConverter
    fun toIngredientList(json: String): List<Ingredient> {
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return Gson().fromJson(json, type)
    }
}