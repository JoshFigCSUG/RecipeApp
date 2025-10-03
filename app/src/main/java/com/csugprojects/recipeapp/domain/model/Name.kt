package com.csugprojects.recipeapp.domain.model

/**
 * Name is a versatile domain model used for listing filtered criteria (M6 Feature).
 * It represents a single item like an ingredient or an area/cuisine.
 */
data class Name(
    val name: String,
    val type: String
)