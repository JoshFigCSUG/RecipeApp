package com.csugprojects.recipeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.model.Ingredient

/**
 * RecipeEntity represents the schema for the local Room Database table (Model Layer - M4 Design).
 * It holds the details for recipes saved as favorites by the user (M2 Feature).
 */
@Entity(tableName = "favorite_recipes")
data class RecipeEntity(
    // The recipe ID serves as the primary key for quick lookup (M8 Testing).
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String?,
    val instructions: String?,
    // The List<Ingredient> field requires a TypeConverter for Room to save it (M4 Design).
    val ingredients: List<Ingredient>,
    // Fields for category and area are included for full detail mapping.
    val category: String?,
    val area: String?
) {
    /**
     * Converts the database entity back into the domain layer's Recipe object (M4 Design).
     */
    fun toRecipe(): Recipe {
        return Recipe(
            id = this.id,
            title = this.title,
            imageUrl = this.imageUrl,
            instructions = this.instructions,
            ingredients = this.ingredients,
            category = this.category,
            area = this.area,
        )
    }
}