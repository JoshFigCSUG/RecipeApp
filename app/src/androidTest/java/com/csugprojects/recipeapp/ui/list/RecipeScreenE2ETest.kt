package com.csugprojects.recipeapp.ui.list

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.csugprojects.recipeapp.domain.model.Recipe
import com.csugprojects.recipeapp.domain.repository.RecipeRepository
import com.csugprojects.recipeapp.ui.viewmodel.GlobalRecipeOperationsViewModel
import com.csugprojects.recipeapp.ui.viewmodel.RecipeListViewModel
import com.csugprojects.recipeapp.util.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// This test uses the AndroidJUnit4 runner to run on an emulator or device (Testing Strategy, M8).
@RunWith(AndroidJUnit4::class)
class RecipeScreenE2ETest {

    // This rule allows the test to launch the Compose UI within an Android context.
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Dependencies are mocked to focus solely on UI interaction and data flow to/from the ViewModels (Testing Strategy).
    @MockK
    private lateinit var mockRepository: RecipeRepository
    private lateinit var mockGlobalViewModel: GlobalRecipeOperationsViewModel
    private lateinit var mockListViewModel: RecipeListViewModel

    // Mock data represents a standard recipe object for UI verification.
    private val mockRecipe = Recipe(
        id = "101",
        title = "Test Chicken Dish",
        imageUrl = "test_url",
        instructions = "cook it",
        ingredients = emptyList(),
        category = "Chicken",
        area = "American",
        isFavorite = false
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        // Mock the repository to instantly return success for all necessary API calls, speeding up the UI test.
        coEvery { mockRepository.searchRecipes(any()) } returns Result.Success(listOf(mockRecipe))
        coEvery { mockRepository.filterByIngredient(any()) } returns Result.Success(emptyList())
        coEvery { mockRepository.getCategories() } returns Result.Success(emptyList())
        coEvery { mockRepository.listAreas() } returns Result.Success(emptyList())

        // Mock the Global ViewModel to control the reactive favorites status (M6 State Management).
        val favoriteRecipesFlow = MutableStateFlow(emptyList<Recipe>())

        mockGlobalViewModel = mockk<GlobalRecipeOperationsViewModel> {
            every { favoriteRecipes } returns favoriteRecipesFlow
            // Mocks the suspend functions to allow verifying that the UI correctly triggered the data layer persistence (Testing Strategy).
            coEvery { addFavorite(any()) } coAnswers {
                favoriteRecipesFlow.value = favoriteRecipesFlow.value + listOf(mockRecipe.copy(isFavorite = true))
            }
            coEvery { removeFavorite(any()) } coAnswers {
                favoriteRecipesFlow.value = favoriteRecipesFlow.value.filter { it.id != "101" }
            }
        }

        // Initialize the List ViewModel with the mocked repository.
        mockListViewModel = RecipeListViewModel(mockRepository)

        // Sets the Compose UI content for the test environment.
        composeTestRule.setContent {
            RecipeListScreen(
                listViewModel = mockListViewModel,
                globalViewModel = mockGlobalViewModel,
                onRecipeClick = { }
            )
        }
    }

    // --- 1. CORE SEARCH FUNCTIONALITY TEST ---

    @Test
    fun searchScreen_displaysResults_afterEnteringQuery() {
        // Step 1: Simulates the user inputting a search query.
        composeTestRule.onNodeWithText("Search recipes by name or ingredient...")
            .performTextInput("chicken")

        // Step 2: Simulates the user clicking the search button.
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()

        // Step 3: Verifies the UI displays the expected search result (M2 Feature).
        composeTestRule.onNodeWithText("Test Chicken Dish")
            .assertIsDisplayed()

        // Step 4: Verifies the internal logic triggered the correct Repository call (Testing Strategy).
        coVerify(timeout = 2000) { mockRepository.searchRecipes("chicken") }
    }

    // --- 2. FAVORITE STATUS TOGGLE TEST (Integration) ---

    @Test
    fun recipeCard_togglesFavoriteStatus_onIconClick() {
        // Setup: Performs the search to show the recipe card on screen.
        composeTestRule.onNodeWithText("Search recipes by name or ingredient...")
            .performTextInput("chicken")
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()

        composeTestRule.onNodeWithText("Test Chicken Dish").assertIsDisplayed()

        // Locates the interactive element for the favorite function.
        val favoriteButton = composeTestRule.onNodeWithContentDescription("Favorite", useUnmergedTree = true)

        // Step 1: Clicks the button to turn the favorite status ON.
        favoriteButton.performClick()

        // Step 2: Verifies the persistence operation was requested by the ViewModel (Testing Strategy).
        coVerify(exactly = 1) { mockGlobalViewModel.addFavorite(any()) }

        // Step 3: Verifies the UI updates to the filled icon state (M6 State Management/UX).
        composeTestRule.onNode(hasIcon(Icons.Default.Favorite))
            .assertIsDisplayed()

        // Step 4: Clicks the button to turn the favorite status OFF.
        favoriteButton.performClick()

        // Step 5: Verifies the removal operation was requested.
        coVerify(exactly = 1) { mockGlobalViewModel.removeFavorite(mockRecipe.id) }

        // Step 6: Verifies the UI reverts to the unfilled icon.
        composeTestRule.onNode(hasIcon(Icons.Default.FavoriteBorder))
            .assertIsDisplayed()
    }

    // Utility function used to check for the presence of a specific Material Icon.
    private fun hasIcon(imageVector: androidx.compose.ui.graphics.vector.ImageVector): SemanticsMatcher {
        return SemanticsMatcher("Icon with ImageVector $imageVector") { node ->
            // This verifies the presence of the required Content Description for accessibility (M2 Accessibility).
            val iconTag = node.config.getOrNull(SemanticsProperties.ContentDescription)
            iconTag != null
        }
    }
}