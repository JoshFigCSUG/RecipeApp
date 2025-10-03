package com.csugprojects.recipeapp.domain.model

import com.csugprojects.recipeapp.data.local.RecipeEntity

/**
 * Recipe is the primary domain model of the application (Model Layer - M2/M4).
 * It holds the complete, clean structure of a meal, used throughout the ViewModels and UI.
 */
data class Recipe(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val instructions: String?,
    val ingredients: List<Ingredient>,
    // These fields support data filtering and detail display (M6 Implementation).
    val category: String?,
    val area: String?,
    // Flag managed by ViewModels to reflect persistence status (M2/M6 State).
    val isFavorite: Boolean = false
) {
    /**
     * Converts the domain model into the database entity model for local storage (M4 Mapping).
     */
    fun toRecipeEntity(): RecipeEntity {
        return RecipeEntity(
            id = this.id,
            title = this.title,
            imageUrl = this.imageUrl,
            instructions = this.instructions,
            ingredients = this.ingredients,
            category = this.category,
            area = this.area
        )
    }
}

/**
 * Ingredient is a simple data class defining the component parts of a recipe.
 */
data class Ingredient(
    val name: String,
    val measure: String
)