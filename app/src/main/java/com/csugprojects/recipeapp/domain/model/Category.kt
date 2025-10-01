package com.csugprojects.recipeapp.domain.model

data class Category(
    val id: String,
    val name: String,
    val thumbUrl: String?,
    val description: String?
)