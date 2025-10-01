package com.csugprojects.recipeapp.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Ingredient
import com.csugprojects.recipeapp.domain.model.Name
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.ui.RecipeViewModel
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// --- Mock Repository Implementation (Fixes Unimplemented Members Error) ---
private class MockRecipeRepository : RecipeRepository {
    private val mockRecipe = Recipe(
        id = "52772",
        title = "Chicken Teriyaki",
        imageUrl = "https://www.themealdb.com/images/media/meals/g046bb1663960946.jpg",
        instructions = "This is a placeholder recipe for the preview. It includes all the visual elements but no real networking functionality. The main app uses the real repository.",
        ingredients = listOf(
            Ingredient("Chicken", "1 lb"),
            Ingredient("Soy Sauce", "1/4 cup"),
            Ingredient("Sugar", "2 tbsp")
        ),
        isFavorite = false
    )

    override suspend fun searchRecipes(query: String): Result<List<Recipe>> {
        return Result.Success(listOf(mockRecipe))
    }

    override suspend fun getRecipeDetails(id: String): Result<Recipe> {
        return Result.Success(mockRecipe)
    }

    override fun getFavoriteRecipes(): Flow<List<Recipe>> {
        return flow { emit(emptyList()) }
    }

    override suspend fun addFavorite(recipe: Recipe) {}
    override suspend fun removeFavorite(recipeId: String) {}

    // --- New Methods Added to Implement the Updated RecipeRepository Interface ---
    override suspend fun getRandomRecipe(): Result<Recipe> = Result.Success(mockRecipe)
    override suspend fun getCategories(): Result<List<Category>> = Result.Success(emptyList())
    override suspend fun listIngredients(): Result<List<Name>> = Result.Success(emptyList())
    override suspend fun listAreas(): Result<List<Name>> = Result.Success(emptyList())
    override suspend fun filterByCategory(category: String): Result<List<Recipe>> = Result.Success(emptyList())
    override suspend fun filterByArea(area: String): Result<List<Recipe>> = Result.Success(emptyList())
    override suspend fun filterByIngredient(ingredient: String): Result<List<Recipe>> = Result.Success(emptyList())
}
// --- End Mock Repository ---

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    viewModel: RecipeViewModel
) {
    // CollectAsState is an extension function, requires androidx.compose.runtime.collectAsState
    val favoriteRecipes by viewModel.favoriteRecipes.collectAsState()

    var recipeState by remember { mutableStateOf<Result<Recipe>>(Result.Loading) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        viewModel.getRecipeDetails(recipeId) { result ->
            recipeState = result
        }
    }

    // Update isFavorite state whenever the list of favorites changes
    LaunchedEffect(favoriteRecipes) {
        isFavorite = favoriteRecipes.any { it.id == recipeId }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = recipeState) {
            is Result.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is Result.Success -> {
                // The compiler guarantees 'state.data' is a non-null Recipe object
                val recipe = state.data

                // Removed: if (recipe != null) { ... }
                RecipeDetailContent(
                    recipe = recipe, // Use the non-null recipe object directly
                    isFavorite = isFavorite,
                    onFavoriteClick = {
                        if (isFavorite) {
                            viewModel.removeFavorite(recipe.id)
                        } else {
                            viewModel.addFavorite(recipe)
                        }
                    }
                )
            }
            is Result.Error -> {
                Text(
                    state.exception.message ?: "An error occurred",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = recipe.imageUrl,
                contentDescription = "Image of ${recipe.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Ingredients
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleLarge,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            recipe.ingredients.forEach { ingredient ->
                Text(
                    text = "${ingredient.measure} ${ingredient.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Instructions
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleLarge,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = recipe.instructions ?: "No instructions available.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview
@Composable
fun RecipeDetailScreenPreview() {
    // This preview now uses the fully implemented MockRepository
    RecipeDetailScreen(
        recipeId = "52772",
        viewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return RecipeViewModel(MockRecipeRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        })
    )
}
