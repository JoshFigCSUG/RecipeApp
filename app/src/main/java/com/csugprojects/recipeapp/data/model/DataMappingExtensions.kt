package com.csugprojects.recipeapp.data.model

import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Name

/**
 * DataMappingExtensions provides functions to convert data layer DTOs into clean domain models (Model Layer - M4 Design).
 * This separates the API's technical structure (DTO) from the app's business logic (Domain Model).
 */

/**
 * Converts a CategoryDto received from the API into the application's domain Category object.
 */
fun CategoryDto.toDomain(): Category {
    return Category(
        id = this.idCategory,
        name = this.strCategory,
        thumbUrl = this.strCategoryThumb,
        description = this.strCategoryDescription
    )
}

/**
 * Converts a NameDto (used for listing ingredients or areas) into the application's Name domain object.
 */
fun NameDto.toDomainName(type: String): Name {
    return Name(
        // Uses null-coalescing to select either the ingredient name or the area name field.
        name = this.strIngredient ?: this.strArea ?: "",
        type = type
    )
}