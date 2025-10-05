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

// This rule allows testing of architecture components, a core part of the Testing Strategy.
@get:Rule
val instantTaskExecutorRule = InstantTaskExecutorRule()

@ExperimentalCoroutinesApi
class RecipeListViewModelTest {

    // The RecipeRepository dependency is mocked to isolate and test the ViewModel's logic (Testing Strategy).
    @MockK
    private lateinit var mockRepository: RecipeRepository

    private lateinit var viewModel: RecipeListViewModel
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    // Mock data structures simulate the clean Domain Layer data (Milestone 4/6 Design).
    private val mockRecipe1 = Recipe(id = "1", title = "Chicken Curry", imageUrl = null, instructions = null, ingredients = emptyList(), category = "Chicken", area = "Indian", isFavorite = false)
    private val mockRecipe2 = Recipe(id = "2", title = "Beef Stew", imageUrl = null, instructions = null, ingredients = emptyList(), category = "Beef", area = "American", isFavorite = false)
    private val mockRecipe3 = Recipe(id = "3", title = "Chicken Soup", imageUrl = null, instructions = null, ingredients = emptyList(), category = "Chicken", area = "American", isFavorite = false)
    // This list simulates recipes already saved in the local database (Persistence Feature - Maintenance Plan).
    private val mockFavoriteIds = setOf("2", "3")

    private val mockCategories = listOf(Category(id="c1", name="Beef", thumbUrl=null, description=null), Category(id="c2", name="Chicken", thumbUrl=null, description=null))
    private val mockAreas = listOf(Name(name="Indian", type="Area"), Name(name="American", type="Area"))

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        // Sets up a controlled dispatcher for testing Kotlin Coroutines (Testing Strategy/Performance).
        Dispatchers.setMain(testDispatcher)

        // Mocks the initial setup calls made by the ViewModel on creation (Testing Strategy).
        coEvery { mockRepository.getCategories() } returns Result.Success(mockCategories)
        coEvery { mockRepository.listAreas() } returns Result.Success(mockAreas)

        // Initializes the ViewModel instance for testing.
        viewModel = RecipeListViewModel(mockRepository)

        // Runs pending coroutines to ensure setup finishes before tests begin.
        runTest(testDispatcher.scheduler) { }
    }

    @After
    fun tearDown() {
        // Resets the coroutine environment after each test.
        Dispatchers.resetMain()
    }

    // This test verifies the core dual-search feature (Milestone 6 Implementation/Performance).
    @Test
    fun searchRecipes_returns_combined_unique_results_and_updates_favorite_status() = runTest(testDispatcher) {
        // Mock the two concurrent API responses (Performance - Concurrency).
        val nameResults = Result.Success(listOf(mockRecipe1, mockRecipe2))
        val ingredientResults = Result.Success(listOf(mockRecipe2, mockRecipe3))

        coEvery { mockRepository.searchRecipes("chicken") } returns nameResults
        coEvery { mockRepository.filterByIngredient("chicken") } returns ingredientResults

        // Execute the search method in the ViewModel.
        viewModel.onSearchQueryChanged("chicken")
        viewModel.searchRecipes(mockFavoriteIds)

        // Verify the ViewModel correctly merges and de-duplicates the lists.
        assertEquals(3, viewModel.recipes.value.size)

        // Verifies the ViewModel correctly cross-references the global favorites state.
        val resultRecipes = viewModel.recipes.value
        assertTrue(resultRecipes.find { it.id == "1" }?.isFavorite == false)
        assertTrue(resultRecipes.find { it.id == "2" }?.isFavorite == true)
    }

    // This test validates the error handling strategy when the network fails (Testing Strategy/Maintenance Plan).
    @Test
    fun searchRecipes_returns_Error_when_both_API_calls_fail() = runTest(testDispatcher) {
        // Mocks both calls to return a Result.Error, simulating a network timeout (Milestone 4 Error Handling).
        val error = Result.Error(IOException("Network timeout"))
        coEvery { mockRepository.searchRecipes(any()) } returns error
        coEvery { mockRepository.filterByIngredient(any()) } returns error

        // Execute the search function.
        viewModel.onSearchQueryChanged("fail")
        viewModel.searchRecipes(emptySet())

        // Verify that the UI state variables are updated to reflect the error for the user.
        assertFalse(viewModel.isLoading.value)
        assertEquals(0, viewModel.recipes.value.size)
        assertTrue(viewModel.errorMessage.value?.startsWith("Network timeout") == true)
    }

    // This test ensures the category filter correctly updates the ViewModel's state.
    @Test
    fun filterAndDisplayRecipes_sets_selectedCategory_and_filters_correctly() = runTest(testDispatcher) {
        // Mocks the Repository response for a category filter call.
        val filterResults = Result.Success(listOf(mockRecipe1, mockRecipe3))
        coEvery { mockRepository.filterByCategory("Chicken") } returns filterResults

        // Apply the filter.
        viewModel.filterAndDisplayRecipes("category", "Chicken", emptySet())

        // Verify the correct filter is marked as selected for the UI (State Management).
        assertEquals("Chicken", viewModel.selectedCategory.value)
        assertEquals(null, viewModel.selectedArea.value)

        // Verify the recipe list state is correctly updated with the filtered data.
        assertEquals(2, viewModel.recipes.value.size)
        assertTrue(viewModel.recipes.value.contains(mockRecipe1))
    }

    // This test ensures clicking an active filter chip correctly clears the filter.
    @Test
    fun filterAndDisplayRecipes_toggles_filter_off_when_clicking_selected_chip() = runTest(testDispatcher) {
        // Sets the initial filter state.
        viewModel.filterAndDisplayRecipes("category", "Chicken", emptySet())
        coEvery { mockRepository.filterByCategory("Chicken") } returns Result.Success(listOf(mockRecipe1))

        // Toggle the same filter off.
        viewModel.filterAndDisplayRecipes("category", "Chicken", emptySet())

        // Verify that the filter state and the recipe list are cleared.
        assertEquals(null, viewModel.selectedCategory.value)
        assertEquals(0, viewModel.recipes.value.size)
    }

    // This test verifies that selecting a new filter type resets all previous filter states, ensuring consistent filtering logic.
    @Test
    fun filterAndDisplayRecipes_clears_other_filters_upon_new_selection() = runTest(testDispatcher) {
        // Set an initial filter (Area).
        coEvery { mockRepository.filterByArea(any()) } returns Result.Success(listOf(mockRecipe1))
        viewModel.filterAndDisplayRecipes("area", "Indian", emptySet())

        // Apply a new filter (Category).
        coEvery { mockRepository.filterByCategory(any()) } returns Result.Success(listOf(mockRecipe2))
        viewModel.filterAndDisplayRecipes("category", "Beef", emptySet())

        // Verify the old filter (Area) is successfully cleared.
        assertEquals(null, viewModel.selectedArea.value)

        // Verify the new filter (Category) is successfully set.
        assertEquals("Beef", viewModel.selectedCategory.value)
    }
}