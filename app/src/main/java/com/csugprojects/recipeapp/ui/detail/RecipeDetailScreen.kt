package com.csugprojects.recipeapp.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.* // Correct import for collectAsState()
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Ingredient
import com.csugprojects.recipeapp.domain.model.Name
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeDetailViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeViewModelFactory
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import androidx.compose.runtime.collectAsState // Explicit, correct import

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    detailViewModel: RecipeDetailViewModel, // ViewModel for fetching recipe details
    globalViewModel: GlobalRecipeOperationsViewModel, // ViewModel for favorites and recently viewed
    onBackClick: () -> Unit
) {
    // OBSERVE STATES FROM SEPARATE VIEWMODELS
    val favoriteRecipes by globalViewModel.favoriteRecipes.collectAsState()
    val recipeState by detailViewModel.recipeState.collectAsState() // Observe StateFlow directly

    // Calculate favorite status based on global state
    val isFavorite = remember(favoriteRecipes) {
        favoriteRecipes.any { it.id == recipeId }
    }

    // --- Data Fetching and Side Effects ---
    LaunchedEffect(recipeId) {
        detailViewModel.getRecipeDetails(recipeId)
    }

    // Log recipe to "Recently Viewed" upon successful fetch
    LaunchedEffect(recipeState) {
        if (recipeState is Result.Success) {
            val recipe = (recipeState as Result.Success<Recipe>).data
            globalViewModel.addRecentlyViewedRecipe(recipe)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Recipe Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = recipeState) {
                is Result.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Result.Success -> {
                    val recipe = state.data
                    RecipeDetailContent(
                        recipe = recipe,
                        isFavorite = isFavorite,
                        onFavoriteClick = {
                            if (isFavorite) {
                                // Calls globalViewModel for shared action
                                globalViewModel.removeFavorite(recipe.id)
                            } else {
                                // Calls globalViewModel for shared action
                                globalViewModel.addFavorite(recipe)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is Result.Error -> {
                    Text(
                        state.exception.message ?: "An error occurred",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// --- Mock Repository for Preview (FIXED) ---
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
        category = "Chicken", // FIXED: Added value for category
        area = "Japanese",     // FIXED: Added value for area
        isFavorite = false
    )

    override suspend fun searchRecipes(query: String): Result<List<Recipe>> = Result.Success(listOf(mockRecipe))
    override suspend fun getRecipeDetails(id: String): Result<Recipe> = Result.Success(mockRecipe)
    override fun getFavoriteRecipes(): Flow<List<Recipe>> = flow { emit(emptyList()) }
    override suspend fun addFavorite(recipe: Recipe) {}
    override suspend fun removeFavorite(recipeId: String) {}
    override suspend fun getRandomRecipe(): Result<Recipe> = Result.Success(mockRecipe)
    override suspend fun getCategories(): Result<List<Category>> = Result.Success(emptyList())
    override suspend fun listIngredients(): Result<List<Name>> = Result.Success(emptyList())
    override suspend fun listAreas(): Result<List<Name>> = Result.Success(emptyList())
    override suspend fun filterByCategory(category: String): Result<List<Recipe>> = Result.Success(emptyList())
    override suspend fun filterByArea(area: String): Result<List<Recipe>> = Result.Success(emptyList())
    override suspend fun filterByIngredient(ingredient: String): Result<List<Recipe>> = Result.Success(emptyList())
}

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
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

            // Display Category and Area if available
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                recipe.category?.let {
                    Text(
                        text = "Category: $it",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                recipe.area?.let {
                    Text(
                        text = "Cuisine: $it",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }


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
    val mockRepo = MockRecipeRepository()
    val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            // Updated to instantiate the new ViewModels
            if (modelClass.isAssignableFrom(GlobalRecipeOperationsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GlobalRecipeOperationsViewModel(mockRepo) as T
            }
            if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeDetailViewModel(mockRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    RecipeDetailScreen(
        recipeId = "52772",
        detailViewModel = viewModel(factory = factory),
        globalViewModel = viewModel(factory = factory),
        onBackClick = {}
    )
}