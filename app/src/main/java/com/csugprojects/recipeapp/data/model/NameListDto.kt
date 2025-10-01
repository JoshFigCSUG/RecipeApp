package com.csugprojects.recipeapp.data.model

import com.google.gson.annotations.SerializedName

// This DTO handles lists from the list.php endpoint (e.g., list all ingredients, areas)
data class NameListDto(
    @SerializedName("meals") // The API confusingly uses 'meals' for these lists
    val names: List<NameDto>?
)

data class NameDto(
    @SerializedName("strIngredient") // Field for ingredient name
    val strIngredient: String?,
    @SerializedName("strArea") // Field for area name
    val strArea: String?
)