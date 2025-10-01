package com.csugprojects.recipeapp.domain.model

data class Name(
    val name: String,
    val type: String // E.g., "Ingredient" or "Area" to help with UI logic
)