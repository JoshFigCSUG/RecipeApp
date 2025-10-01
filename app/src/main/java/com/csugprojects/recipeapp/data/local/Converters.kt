package com.csugprojects.recipeapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.csugprojects.recipeapp.domain.model.Ingredient

class Converters {
    @TypeConverter
    fun fromIngredientList(list: List<Ingredient>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toIngredientList(json: String): List<Ingredient> {
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return Gson().fromJson(json, type)
    }
}