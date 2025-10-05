package com.csugprojects.recipeapp.ui.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Ingredient
import com.csugprojects.recipeapp.domain.model.Name
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeDetailViewModel
import com.csugprojects.recipeapp.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * This composable function displays the full recipe details (View Layer - M2/M4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    detailViewModel: RecipeDetailViewModel,
    globalViewModel: GlobalRecipeOperationsViewModel,
    onBackClick: () -> Unit
) {
    // Observes the list of favorites for immediate status updates (M6 State Management).
    val favoriteRecipes by globalViewModel.favoriteRecipes.collectAsState()
    // Observes the recipe data, which is wrapped in a Result class (M4 Error Handling).
    val recipeState by detailViewModel.recipeState.collectAsState()

    // Checks the current favorite status against the global state.
    val isFavorite = remember(favoriteRecipes) {
        favoriteRecipes.any { it.id == recipeId }
    }

    // Triggers the network request when the screen first loads (M4 Data Flow).
    LaunchedEffect(recipeId) {
        detailViewModel.getRecipeDetails(recipeId)
    }

    // Adds the successfully loaded recipe to the "Recently Viewed" list (M6 Feature).
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
                    // Implements navigation control for returning to the previous screen (M2 Navigation Flow).
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Uses the Result sealed class to show content based on the data state (M4 Error Handling).
            when (val state = recipeState) {
                is Result.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Result.Success -> {
                    val recipe = state.data
                    RecipeDetailContent(
                        recipe = recipe,
                        isFavorite = isFavorite,
                        // Delegates the favorite toggle operation to the Global ViewModel.
                        onFavoriteClick = {
                            if (isFavorite) {
                                globalViewModel.removeFavorite(recipe.id)
                            } else {
                                globalViewModel.addFavorite(recipe)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is Result.Error -> {
                    // Displays the error message, preventing app crash (M4 Error Handling/Maintenance Plan).
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

/**
 * Mock data repository for use in Compose Previews.
 * This supports the **Testing Strategy** for UI components without running the actual network layer (M8).
 */
private class MockRecipeRepository : RecipeRepository {
    private val mockRecipe = Recipe(
        id = "52772",
        title = "Chicken Teriyaki",
        imageUrl = "https://www.themealdb.com/images/media/meals/g046bb1663960946.jpg",
        instructions = "Step one is to prepare the chicken by trimming the fat. Next, whisk the marinade ingredients in a large bowl and add the chicken. Marinate for at least one hour or overnight. Finally, preheat your grill to medium-high heat and cook the chicken for 5-7 minutes per side until the internal temperature reaches 165°F.",
        ingredients = listOf(
            Ingredient("Chicken Breast", "1 lb"),
            Ingredient("Soy Sauce", "1/4 cup"),
            Ingredient("Brown Sugar", "2 tbsp"),
            Ingredient("Garlic Cloves", "2"),
            Ingredient("Ginger, grated", "1 tsp")
        ),
        category = "Chicken",
        area = "Japanese",
        isFavorite = false
    )

    // Overrides all required data methods with mock return values.
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

/**
 * Composable for rendering the main content block, incorporating styling for visual appeal.
 */
@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        // Enables smooth transitions when content size changes (M6 UX Enhancement).
        modifier = modifier
            .verticalScroll(scrollState)
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            AsyncImage(
                model = recipe.imageUrl,
                // Content description supports accessibility (M2 Accessibility).
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
                    contentDescription = "Favorite button for ${recipe.title}",
                    // Uses theme colors, ensuring consistency with the Denver Broncos/Dynamic Theme.
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Displays the recipe title using a prominent style.
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Displays metadata chips for Category and Area (M6 Features).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                recipe.category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                recipe.area?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }


            // Section Header: Ingredients
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Renders the ingredient list with explicit separation between measure and name.
            recipe.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        // Uses the primary theme color for the list bullet accent.
                        text = "• ",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        // Formats as "Measure Name" to ensure correct spacing.
                        text = "${ingredient.measure} ${ingredient.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // Section Header: Instructions
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Splits the instruction text into distinct steps/paragraphs.
            val steps = recipe.instructions
                ?.split(delimiters = arrayOf("."), ignoreCase = false, limit = 0)
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: listOf("No instructions available.")

            // Iterates and renders each step inside a visually distinct color block (M6 UX Enhancement).
            steps.forEachIndexed { index, step ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    // Uses a subtle background color for the step block.
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Step ${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            // Uses the primary theme color for the step number accent.
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Preview function uses the MockRepository to show the UI design for validation (M8 Testing Strategy).
 */
@Preview
@Composable
fun RecipeDetailScreenPreview() {
    val mockRepo = MockRecipeRepository()
    val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
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