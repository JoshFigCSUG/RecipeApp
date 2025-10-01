package com.csugprojects.recipeapp.data.model

import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Name

fun CategoryDto.toDomain(): Category {
    return Category(
        id = this.idCategory,
        name = this.strCategory,
        thumbUrl = this.strCategoryThumb,
        description = this.strCategoryDescription
    )
}

fun NameDto.toDomainName(type: String): Name {
    return Name(
        // Handles case where DTO might have strIngredient or strArea populated
        name = this.strIngredient ?: this.strArea ?: "",
        type = type
    )
}