package com.csugprojects.recipeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.model.Ingredient

@Entity(tableName = "favorite_recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String?,
    val instructions: String?,
    val ingredients: List<Ingredient>,
    val category: String?, // NEW FIELD
    val area: String?      // NEW FIELD
) {
    fun toRecipe(): Recipe {
        return Recipe(
            id = this.id,
            title = this.title,
            imageUrl = this.imageUrl,
            instructions = this.instructions,
            ingredients = this.ingredients,
            category = this.category, // NEW MAPPING
            area = this.area,         // NEW MAPPING
        )
    }
}