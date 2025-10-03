package com.csugprojects.recipeapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * CategoryListDto and CategoryDto define the Data Transfer Objects (DTOs) for the Model Layer (M4 Design).
 * These classes map directly to the JSON structure received from the TheMealDB API's category endpoint.
 */
data class CategoryListDto(
    // CategoryListDto acts as a wrapper for the list of categories returned by the API.
    @SerializedName("categories")
    val categories: List<CategoryDto>
)

data class CategoryDto(
    // CategoryDto holds the detailed information for a single meal category.
    // The @SerializedName annotation ensures correct mapping from the JSON fields.
    @SerializedName("idCategory")
    val idCategory: String,
    @SerializedName("strCategory")
    val strCategory: String,
    @SerializedName("strCategoryThumb")
    val strCategoryThumb: String?,
    @SerializedName("strCategoryDescription")
    val strCategoryDescription: String?
)