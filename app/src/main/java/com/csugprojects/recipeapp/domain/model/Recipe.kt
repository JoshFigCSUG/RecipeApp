package com.csugprojects.recipeapp.domain.model

import com.csugprojects.recipeapp.data.local.RecipeEntity

data class Recipe(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val instructions: String?,
    val ingredients: List<Ingredient>,
    val category: String?, // NEW FIELD
    val area: String?,      // NEW FIELD (Cuisine)
    val isFavorite: Boolean = false
) {
    fun toRecipeEntity(): RecipeEntity {
        return RecipeEntity(
            id = this.id,
            title = this.title,
            imageUrl = this.imageUrl,
            instructions = this.instructions,
            ingredients = this.ingredients,
            category = this.category, // NEW FIELD
            area = this.area          // NEW FIELD
        )
    }
}

data class Ingredient(
    val name: String,
    val measure: String
)