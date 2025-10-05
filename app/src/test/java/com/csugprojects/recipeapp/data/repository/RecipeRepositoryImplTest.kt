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

// Use the Kotlin Coroutines Test API for reliable testing of suspend functions (Module 7).
@ExperimentalCoroutinesApi
class RecipeRepositoryImplTest {

    // Mock dependencies required by the Repository
    @MockK
    private lateinit var mockApiService: RecipeApiService

    @MockK
    private lateinit var mockRecipeDao: RecipeDao

    // Use a test dispatcher to control coroutine execution during testing
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    // Class under test
    private lateinit var repository: RecipeRepositoryImpl

    // --- Mock Data Structures (simulating API response and desired output) ---

    // A sample DTO object simulating a successful API response.
    // All 40 ingredient/measure parameters must be explicitly defined (M4 DTO structure).
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

        // Explicitly defining remaining 36 parameters
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

    // The expected domain model after successful mapping from the DTO
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

    // The expected database entity
    private val expectedRecipeEntity = expectedRecipeDomain.toRecipeEntity()


    @Before
    fun setUp() {
        // Initialize MockK annotations and the repository instance
        MockKAnnotations.init(this, relaxed = true)
        repository = RecipeRepositoryImpl(mockApiService, mockRecipeDao)
    }

    // -----------------------------------------------------------------
    // --- 1. SEARCH RECIPES TESTS (Underscore Convention) ---
    // -----------------------------------------------------------------

    @Test
    fun searchRecipes_returns_Success_with_mapped_data_on_API_success() = runTest(testDispatcher) {
        // GIVEN: Mock API returns a list of meals
        val mockMealListDto = MealListDto(meals = listOf(mockMealDto))
        coEvery { mockApiService.searchMeals(any()) } returns mockMealListDto

        // WHEN: Calling the repository search function
        val result = repository.searchRecipes("test")

        // THEN: Result is a Success, and data is correctly mapped to the domain model
        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        assertEquals(expectedRecipeDomain, result.data.first())
    }

    @Test
    fun searchRecipes_returns_Error_when_API_returns_no_meals() = runTest(testDispatcher) {
        // GIVEN: Mock API returns a list wrapper with a null/empty meal list
        coEvery { mockApiService.searchMeals(any()) } returns MealListDto(meals = emptyList())

        // WHEN: Calling the repository search function
        val result = repository.searchRecipes("no results")

        // THEN: Result is an Error with the correct message (M4 Error Handling)
        assertTrue(result is Result.Error)
        val errorMessage = (result as Result.Error).exception.message
        assertEquals("No recipes found for 'no results'", errorMessage)
    }

    @Test
    fun searchRecipes_returns_Error_on_IOException_Network_Error() = runTest(testDispatcher) {
        // GIVEN: Mock API throws an IOException (simulating no internet/timeout)
        coEvery { mockApiService.searchMeals(any()) } throws IOException("Network failed")

        // WHEN: Calling the repository search function
        val result = repository.searchRecipes("test")

        // THEN: Result is an Error indicating a network issue (M4 Error Handling)
        assertTrue(result is Result.Error)
        val errorMessage = (result as Result.Error).exception.message
        assertTrue(errorMessage!!.startsWith("Network error:"))
    }

    // -----------------------------------------------------------------
    // --- 2. GET RECIPE DETAILS TESTS (Underscore Convention) ---
    // -----------------------------------------------------------------

    @Test
    fun getRecipeDetails_returns_Success_with_single_mapped_recipe() = runTest(testDispatcher) {
        // GIVEN: Mock API returns a single meal in a list wrapper
        coEvery { mockApiService.getMealDetails(any()) } returns MealListDto(meals = listOf(mockMealDto))

        // WHEN: Calling the repository for details
        val result = repository.getRecipeDetails("100")

        // THEN: Result is a Success with the single, correctly mapped domain object
        assertTrue(result is Result.Success)
        assertEquals(expectedRecipeDomain, (result as Result.Success).data)
    }

    @Test
    fun getRecipeDetails_returns_Error_when_recipe_ID_is_not_found() = runTest(testDispatcher) {
        // GIVEN: Mock API returns a null/empty list, even on a lookup
        coEvery { mockApiService.getMealDetails(any()) } returns MealListDto(meals = null)

        // WHEN: Calling the repository for details
        val result = repository.getRecipeDetails("999")

        // THEN: Result is an Error indicating the item was not found
        assertTrue(result is Result.Error)
        val errorMessage = (result as Result.Error).exception.message
        assertEquals("Recipe not found for ID: 999", errorMessage)
    }

    // -----------------------------------------------------------------
    // --- 3. FAVORITES (DATABASE) TESTS (Underscore Convention) ---
    // -----------------------------------------------------------------

    @Test
    fun addFavorite_calls_DAO_insert_with_correct_entity() = runTest(testDispatcher) {
        // GIVEN: Mock DAO is ready
        // WHEN: Calling addFavorite with the domain model
        repository.addFavorite(expectedRecipeDomain)

        // THEN: DAO's insertRecipe suspend function is called exactly once
        // with the correctly mapped RecipeEntity
        coVerify(exactly = 1) { mockRecipeDao.insertRecipe(expectedRecipeEntity) }
    }

    @Test
    fun removeFavorite_calls_DAO_deleteById() = runTest(testDispatcher) {
        // GIVEN: Mock DAO is ready
        // WHEN: Calling removeFavorite
        repository.removeFavorite("100")

        // THEN: DAO's deleteRecipeById suspend function is called exactly once
        // with the correct ID
        coVerify(exactly = 1) { mockRecipeDao.deleteRecipeById("100") }
    }

    @Test
    fun getFavoriteRecipes_emits_Flow_of_mapped_domain_models() = runTest(testDispatcher) {
        // GIVEN: Mock DAO returns a Flow emitting a list of entities
        val entities = listOf(expectedRecipeEntity)
        coEvery { mockRecipeDao.getAllFavoriteRecipes() } returns flowOf(entities)

        // WHEN: Collecting the Flow from the repository
        val resultFlow = repository.getFavoriteRecipes()

        // THEN: The emitted value is a list of correctly mapped domain models
        val resultList = resultFlow.first()
        assertEquals(1, resultList.size)

        // Ensure the Flow successfully mapped the Entity to the Domain Model
        assertEquals(expectedRecipeDomain.copy(isFavorite = true), resultList.first())
    }

}