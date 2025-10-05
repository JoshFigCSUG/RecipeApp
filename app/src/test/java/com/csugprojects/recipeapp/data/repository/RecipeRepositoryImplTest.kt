package com.csugprojects.recipeapp.data.repository

import com.csugprojects.recipeapp.data.api.RecipeApiService
import com.csugprojects.recipeapp.data.local.RecipeDao
import com.csugprojects.recipeapp.data.model.MealDto
import com.csugprojects.recipeapp.data.model.MealListDto
import com.csugprojects.recipeapp.domain.model.Ingredient
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

// This file contains Unit Tests for the Repository layer, supporting the overall Testing Strategy (Module 8).
@ExperimentalCoroutinesApi
class RecipeRepositoryImplTest {

    // Mock the API service to isolate the repository's logic (Testing Strategy).
    @MockK
    private lateinit var mockApiService: RecipeApiService

    // Mock the local database DAO, which is responsible for persistence (Testing Strategy/Module 4).
    @MockK
    private lateinit var mockRecipeDao: RecipeDao

    // Uses a test dispatcher for reliable coroutine execution, a requirement for modern Android testing.
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    // The class under test is the Recipe Repository implementation.
    private lateinit var repository: RecipeRepositoryImpl

    // Mock DTO simulates the raw data structure from the external API (Module 4 Design).
    private val mockMealDto = MealDto(
        idMeal = "100",
        strMeal = "Test Recipe",
        strMealThumb = "test_image.jpg",
        strInstructions = "Step 1. Step 2.",
        strCategory = "Dessert",
        strArea = "American",
        strIngredient1 = "Sugar",
        strMeasure1 = "1 cup",
        strIngredient2 = null,
        strMeasure2 = null,

        // Placeholder fields for a full DTO structure (Module 4 Data Models).
        strIngredient3 = null, strMeasure3 = null,
        strIngredient4 = null, strMeasure4 = null,
        strIngredient5 = null, strMeasure5 = null,
        strIngredient6 = null, strMeasure6 = null,
        strIngredient7 = null, strMeasure7 = null,
        strIngredient8 = null, strMeasure8 = null,
        strIngredient9 = null, strMeasure9 = null,
        strIngredient10 = null, strMeasure10 = null,
        strIngredient11 = null, strMeasure11 = null,
        strIngredient12 = null, strMeasure12 = null,
        strIngredient13 = null, strMeasure13 = null,
        strIngredient14 = null, strMeasure14 = null,
        strIngredient15 = null, strMeasure15 = null,
        strIngredient16 = null, strMeasure16 = null,
        strIngredient17 = null, strMeasure17 = null,
        strIngredient18 = null, strMeasure18 = null,
        strIngredient19 = null, strMeasure19 = null,
        strIngredient20 = null, strMeasure20 = null,
    )

    // Expected Domain Model shows correct mapping from DTO (Module 4 Design).
    private val expectedRecipeDomain = Recipe(
        id = "100",
        title = "Test Recipe",
        imageUrl = "test_image.jpg",
        instructions = "Step 1. Step 2.",
        ingredients = listOf(Ingredient("Sugar", "1 cup")),
        category = "Dessert",
        area = "American",
        isFavorite = false
    )

    // Expected database entity for local persistence (Module 4 Persistence).
    private val expectedRecipeEntity = expectedRecipeDomain.toRecipeEntity()


    @Before
    fun setUp() {
        // Initializes the mocked dependencies.
        MockKAnnotations.init(this, relaxed = true)
        // Creates the repository instance for testing.
        repository = RecipeRepositoryImpl(mockApiService, mockRecipeDao)
    }

    // This section verifies search functionality and data mapping (Testing Strategy).

    @Test
    fun searchRecipes_returns_Success_with_mapped_data_on_API_success() = runTest(testDispatcher) {
        // Arrange: API returns successful meal data.
        val mockMealListDto = MealListDto(meals = listOf(mockMealDto))
        coEvery { mockApiService.searchMeals(any()) } returns mockMealListDto

        // Act: Perform the search operation.
        val result = repository.searchRecipes("test")

        // Assert: Verify data is correctly mapped to the clean domain model.
        assertTrue(result is Result.Success)
        assertEquals(expectedRecipeDomain, (result as Result.Success).data.first())
    }

    @Test
    fun searchRecipes_returns_Error_when_API_returns_no_meals() = runTest(testDispatcher) {
        // Arrange: API returns no results.
        coEvery { mockApiService.searchMeals(any()) } returns MealListDto(meals = emptyList())

        // Act: Perform the search.
        val result = repository.searchRecipes("no results")

        // Assert: Verify the custom "not found" error message is returned (Module 4 Error Handling).
        assertTrue(result is Result.Error)
        val errorMessage = (result as Result.Error).exception.message
        assertEquals("No recipes found for 'no results'", errorMessage)
    }

    @Test
    fun searchRecipes_returns_Error_on_IOException_Network_Error() = runTest(testDispatcher) {
        // Arrange: Mock API throws an exception simulating a network failure.
        coEvery { mockApiService.searchMeals(any()) } throws IOException("Network failed")

        // Act: Perform the search.
        val result = repository.searchRecipes("test")

        // Assert: Verify the generic network error is handled (Module 4 Error Handling/Maintenance Plan).
        assertTrue(result is Result.Error)
        val errorMessage = (result as Result.Error).exception.message
        assertTrue(errorMessage!!.startsWith("Network error:"))
    }

    // This section verifies recipe detail fetching and error handling.

    @Test
    fun getRecipeDetails_returns_Success_with_single_mapped_recipe() = runTest(testDispatcher) {
        // Arrange: API returns a single meal detail.
        coEvery { mockApiService.getMealDetails(any()) } returns MealListDto(meals = listOf(mockMealDto))

        // Act: Fetch the recipe details.
        val result = repository.getRecipeDetails("100")

        // Assert: Verify the data mapping is correct.
        assertTrue(result is Result.Success)
        assertEquals(expectedRecipeDomain, (result as Result.Success).data)
    }

    @Test
    fun getRecipeDetails_returns_Error_when_recipe_ID_is_not_found() = runTest(testDispatcher) {
        // Arrange: API returns null meals, simulating an invalid ID.
        coEvery { mockApiService.getMealDetails(any()) } returns MealListDto(meals = null)

        // Act: Fetch the details for a non-existent ID.
        val result = repository.getRecipeDetails("999")

        // Assert: Verify the specific "not found" error is returned (Module 4 Error Handling).
        assertTrue(result is Result.Error)
        val errorMessage = (result as Result.Error).exception.message
        assertEquals("Recipe not found for ID: 999", errorMessage)
    }

    // This section verifies local persistence operations for the Favorites feature.

    @Test
    fun addFavorite_calls_DAO_insert_with_correct_entity() = runTest(testDispatcher) {
        // Act: Call the repository to add a favorite.
        repository.addFavorite(expectedRecipeDomain)

        // Assert: Verify that the DAO received the correctly converted RecipeEntity (Module 4 Persistence).
        coVerify(exactly = 1) { mockRecipeDao.insertRecipe(expectedRecipeEntity) }
    }

    @Test
    fun removeFavorite_calls_DAO_deleteById() = runTest(testDispatcher) {
        // Act: Call the repository to remove a favorite.
        repository.removeFavorite("100")

        // Assert: Verify that the DAO's deletion method was called with the correct ID.
        coVerify(exactly = 1) { mockRecipeDao.deleteRecipeById("100") }
    }

    @Test
    fun getFavoriteRecipes_emits_Flow_of_mapped_domain_models() = runTest(testDispatcher) {
        // Arrange: DAO provides a reactive stream (Flow) of database entities.
        val entities = listOf(expectedRecipeEntity)
        coEvery { mockRecipeDao.getAllFavoriteRecipes() } returns flowOf(entities)

        // Act: Collect the first value from the repository's Flow.
        val resultFlow = repository.getFavoriteRecipes()
        val resultList = resultFlow.first()

        // Assert: Verify the database entity was correctly mapped back to the domain model with 'isFavorite' set to true (Module 4 Persistence/Maintenance Plan).
        assertEquals(1, resultList.size)
        assertEquals(expectedRecipeDomain.copy(isFavorite = true), resultList.first())
    }
}