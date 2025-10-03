package com.csugprojects.recipeapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * NameListDto and NameDto define the Data Transfer Objects (DTOs) for listing categories, areas, or ingredients (Model Layer - M4 Design).
 * This structure is used because the TheMealDB API reuses the "meals" key for these lists (M6 Implementation).
 */
data class NameListDto(
    // The API uses the confusing key "meals" for these lists, requiring explicit mapping.
    @SerializedName("meals")
    val names: List<NameDto>?
)

data class NameDto(
    // This DTO must hold either an ingredient string or an area string, as required by the API.
    @SerializedName("strIngredient")
    val strIngredient: String?,
    @SerializedName("strArea")
    val strArea: String?
)