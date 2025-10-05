package com.csugprojects.recipeapp.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.csugprojects.recipeapp.domain.model.Category
import com.csugprojects.recipeapp.domain.model.Name
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.util.Result
import io.mockk.coEvery
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Rule to allow immediate execution of background tasks (required for testing ViewModels)
@get:Rule
val instantTaskExecutorRule = InstantTaskExecutorRule()

@ExperimentalCoroutinesApi
class RecipeListViewModelTest {

    // Mock the dependency (the contract defined in the Domain Layer)
    @MockK
    private lateinit var mockRepository: RecipeRepository

    private lateinit var viewModel: RecipeListViewModel
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    // --- Mock Data ---
    private val mockRecipe1 = Recipe(id = "1", title = "Chicken Curry", imageUrl = null, instructions = null, ingredients = emptyList(), category = "Chicken", area = "Indian", isFavorite = false)
    private val mockRecipe2 = Recipe(id = "2", title = "Beef Stew", imageUrl = null, instructions = null, ingredients = emptyList(), category = "Beef", area = "American", isFavorite = false)
    private val mockRecipe3 = Recipe(id = "3", title = "Chicken Soup", imageUrl = null, instructions = null, ingredients = emptyList(), category = "Chicken", area = "American", isFavorite = false)
    private val mockFavoriteIds = setOf("2", "3") // Beef Stew and Chicken Soup are favorites

    private val mockCategories = listOf(Category(id="c1", name="Beef", thumbUrl=null, description=null), Category(id="c2", name="Chicken", thumbUrl=null, description=null))
    private val mockAreas = listOf(Name(name="Indian", type="Area"), Name(name="American", type="Area"))

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        // Set up the TestDispatcher as the main dispatcher (Module 7 requirement for Coroutine testing)
        Dispatchers.setMain(testDispatcher)

        // Mock Repository calls needed for the ViewModel's init block
        coEvery { mockRepository.getCategories() } returns Result.Success(mockCategories)
        coEvery { mockRepository.listAreas() } returns Result.Success(mockAreas)

        viewModel = RecipeListViewModel(mockRepository)

        // Advance time to allow the init block (fetching categories/areas) to complete
        runTest(testDispatcher.scheduler) { }
    }

    @After
    fun tearDown() {
        // Reset the main dispatcher after each test
        Dispatchers.resetMain()
    }

    // -----------------------------------------------------------------
    // --- 1. SEARCH RECIPES LOGIC TESTS (Dual Search - M6 Feature) ---
    // -----------------------------------------------------------------

    @Test
    fun searchRecipes_returns_combined_unique_results_and_updates_favorite_status() = runTest(testDispatcher) {
        // GIVEN: Repository returns one result by name and two results by ingredient, with overlap
        val nameResults = Result.Success(listOf(mockRecipe1, mockRecipe2)) // Chicken Curry, Beef Stew
        val ingredientResults = Result.Success(listOf(mockRecipe2, mockRecipe3)) // Beef Stew, Chicken Soup (overlap on Beef Stew)

        coEvery { mockRepository.searchRecipes("chicken") } returns nameResults
        coEvery { mockRepository.filterByIngredient("chicken") } returns ingredientResults

        // WHEN: Search is executed
        viewModel.onSearchQueryChanged("chicken")
        viewModel.searchRecipes(mockFavoriteIds)

        // THEN:
        // 1. Loading state is false
        assertFalse(viewModel.isLoading.value)

        // 2. The list contains unique, combined results (1, 2, 3)
        assertEquals(3, viewModel.recipes.value.size)

        // 3. Favorite status is correctly applied (M6 State Management)
        val resultRecipes = viewModel.recipes.value
        assertTrue(resultRecipes.find { it.id == "1" }?.isFavorite == false) // Chicken Curry is NOT a favorite
        assertTrue(resultRecipes.find { it.id == "2" }?.isFavorite == true) // Beef Stew IS a favorite
        assertTrue(resultRecipes.find { it.id == "3" }?.isFavorite == true) // Chicken Soup IS a favorite

        // 4. Error message is null
        assertEquals(null, viewModel.errorMessage.value)
    }

    @Test
    fun searchRecipes_returns_Error_when_both_API_calls_fail() = runTest(testDispatcher) {
        // GIVEN: Both mock API calls fail
        val error = Result.Error(IOException("Network timeout"))
        coEvery { mockRepository.searchRecipes(any()) } returns error
        coEvery { mockRepository.filterByIngredient(any()) } returns error

        // WHEN: Search is executed
        viewModel.onSearchQueryChanged("fail")
        viewModel.searchRecipes(emptySet())

        // THEN: Error message is set, list is empty
        assertFalse(viewModel.isLoading.value)
        assertEquals(0, viewModel.recipes.value.size)
        assertTrue(viewModel.errorMessage.value?.startsWith("Network timeout") == true)
    }

    // -----------------------------------------------------------------
    // --- 2. FILTER LOGIC TESTS (State Management) ---
    // -----------------------------------------------------------------

    @Test
    fun filterAndDisplayRecipes_sets_selectedCategory_and_filters_correctly() = runTest(testDispatcher) {
        // GIVEN: API returns results for the category filter
        val filterResults = Result.Success(listOf(mockRecipe1, mockRecipe3))
        coEvery { mockRepository.filterByCategory("Chicken") } returns filterResults

        // WHEN: Filter is applied
        viewModel.filterAndDisplayRecipes("category", "Chicken", emptySet())

        // THEN:
        // 1. Correct filter state is set, others are cleared
        assertEquals("Chicken", viewModel.selectedCategory.value)
        assertEquals(null, viewModel.selectedArea.value)

        // 2. Recipes are updated
        assertEquals(2, viewModel.recipes.value.size)
        assertTrue(viewModel.recipes.value.contains(mockRecipe1))
    }

    @Test
    fun filterAndDisplayRecipes_toggles_filter_off_when_clicking_selected_chip() = runTest(testDispatcher) {
        // GIVEN: Category filter is currently set
        viewModel.filterAndDisplayRecipes("category", "Chicken", emptySet())
        coEvery { mockRepository.filterByCategory("Chicken") } returns Result.Success(listOf(mockRecipe1))

        // WHEN: The same filter chip is clicked again
        viewModel.filterAndDisplayRecipes("category", "Chicken", emptySet())

        // THEN:
        // 1. The filter state is cleared
        assertEquals(null, viewModel.selectedCategory.value)

        // 2. The recipe list is cleared (toggled off logic)
        assertEquals(0, viewModel.recipes.value.size)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun filterAndDisplayRecipes_clears_other_filters_upon_new_selection() = runTest(testDispatcher) {
        // GIVEN: Area filter is currently selected
        coEvery { mockRepository.filterByArea(any()) } returns Result.Success(listOf(mockRecipe1))
        viewModel.filterAndDisplayRecipes("area", "Indian", emptySet()) // Area filter is set

        // WHEN: A new Category filter is applied
        coEvery { mockRepository.filterByCategory(any()) } returns Result.Success(listOf(mockRecipe2))
        viewModel.filterAndDisplayRecipes("category", "Beef", emptySet())

        // THEN:
        // 1. The previous filter (Area) is cleared
        assertEquals(null, viewModel.selectedArea.value)

        // 2. The new filter (Category) is set
        assertEquals("Beef", viewModel.selectedCategory.value)

        // 3. The search query is guaranteed to be cleared
        assertEquals("", viewModel.searchQuery.value)
    }
}