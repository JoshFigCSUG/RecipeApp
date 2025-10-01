package com.csugprojects.recipeapp.domain.model

import com.csugprojects.recipeapp.data.local.RecipeEntity

data class Recipe(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val instructions: String?,
    val ingredients: List<Ingredient>,
    val isFavorite: Boolean = false // Added to handle UI state
) {
    fun toRecipeEntity(): RecipeEntity {
        return RecipeEntity(
            id = this.id,
            title = this.title,
            imageUrl = this.imageUrl,
            instructions = this.instructions,
            ingredients = this.ingredients
        )
    }
}

data class Ingredient(
    val name: String,
    val measure: String
)