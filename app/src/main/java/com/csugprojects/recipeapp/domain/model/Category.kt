package com.csugprojects.recipeapp.domain.model

/**
 * Category is a core domain model (Model Layer - M2/M4).
 * It represents a single meal category, converted from a network DTO.
 */
data class Category(
    val id: String,
    val name: String,
    val thumbUrl: String?,
    val description: String?
)