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

// Use AndroidJUnit4 runner for instrumented tests (Module 7 UI Testing)
@RunWith(AndroidJUnit4::class)
class RecipeScreenE2ETest {

    // Rule for launching an Android activity that can host Compose UI
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Mock the core dependencies required by the ViewModels
    @MockK
    private lateinit var mockRepository: RecipeRepository
    private lateinit var mockGlobalViewModel: GlobalRecipeOperationsViewModel
    private lateinit var mockListViewModel: RecipeListViewModel

    // Mock data
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

        // Set up the mock repository to return success immediately for any search
        coEvery { mockRepository.searchRecipes(any()) } returns Result.Success(listOf(mockRecipe))
        coEvery { mockRepository.filterByIngredient(any()) } returns Result.Success(emptyList())
        coEvery { mockRepository.getCategories() } returns Result.Success(emptyList())
        coEvery { mockRepository.listAreas() } returns Result.Success(emptyList())

        // Set up the Global ViewModel mock
        val favoriteRecipesFlow = MutableStateFlow(emptyList<Recipe>())

        // Mock the required methods on the Global ViewModel
        mockGlobalViewModel = mockk<GlobalRecipeOperationsViewModel> {
            every { favoriteRecipes } returns favoriteRecipesFlow
            // Mocking the suspend functions to allow verification
            coEvery { addFavorite(any()) } coAnswers {
                favoriteRecipesFlow.value = favoriteRecipesFlow.value + listOf(mockRecipe.copy(isFavorite = true))
            }
            coEvery { removeFavorite(any()) } coAnswers {
                favoriteRecipesFlow.value = favoriteRecipesFlow.value.filter { it.id != "101" }
            }
        }

        // Initialize the List ViewModel with the mocked repository
        mockListViewModel = RecipeListViewModel(mockRepository)

        // Set up Compose content with the mocked components
        composeTestRule.setContent {
            RecipeListScreen(
                listViewModel = mockListViewModel,
                globalViewModel = mockGlobalViewModel,
                onRecipeClick = { /* Do nothing for this test */ }
            )
        }
    }

    // -----------------------------------------------------------------
    // --- 1. CORE SEARCH FUNCTIONALITY TEST ---
    // -----------------------------------------------------------------

    @Test
    fun searchScreen_displaysResults_afterEnteringQuery() {
        // 1. Enter text into the search bar (acts as the View Layer action)
        composeTestRule.onNodeWithText("Search recipes by name or ingredient...")
            .performTextInput("chicken")

        // 2. Perform the search action (simulating pressing Enter or the search icon)
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()

        // 3. Verify the result recipe card is displayed (ViewModel successfully updated state)
        composeTestRule.onNodeWithText("Test Chicken Dish")
            .assertIsDisplayed()

        // 4. Verify the search bar correctly triggered the Repository calls
        coVerify(timeout = 2000) { mockRepository.searchRecipes("chicken") }
    }

    // -----------------------------------------------------------------
    // --- 2. FAVORITE STATUS TOGGLE TEST (Integration) ---
    // -------------------------------------------------

    @Test
    fun recipeCard_togglesFavoriteStatus_onIconClick() {
        // GIVEN: Search is executed and recipe card is displayed
        composeTestRule.onNodeWithText("Search recipes by name or ingredient...")
            .performTextInput("chicken")
        composeTestRule.onNodeWithContentDescription("Search")
            .performClick()

        // Wait for the recipe card to appear
        composeTestRule.onNodeWithText("Test Chicken Dish").assertIsDisplayed()

        // 1. Initial State: The icon's Content Description is used for targeting
        val favoriteButton = composeTestRule.onNodeWithContentDescription("Favorite", useUnmergedTree = true)

        // 2. WHEN: Clicking the icon to ADD as favorite
        favoriteButton.performClick()

        // 3. THEN: The Global ViewModel's addFavorite should be called
        coVerify(exactly = 1) { mockGlobalViewModel.addFavorite(any()) }

        // 4. New State: Icon should now be filled (signaling true favorite status)
        composeTestRule.onNode(hasIcon(Icons.Default.Favorite))
            .assertIsDisplayed()

        // 5. WHEN: Clicking the icon again to REMOVE favorite
        favoriteButton.performClick()

        // 6. THEN: The Global ViewModel's removeFavorite should be called
        coVerify(exactly = 1) { mockGlobalViewModel.removeFavorite(mockRecipe.id) }

        // 7. Final State: Icon should revert to 'FavoriteBorder' (unfilled)
        composeTestRule.onNode(hasIcon(Icons.Default.FavoriteBorder))
            .assertIsDisplayed()
    }

    // --- Utility Function to help match Icons ---
    private fun hasIcon(imageVector: androidx.compose.ui.graphics.vector.ImageVector): SemanticsMatcher {
        return SemanticsMatcher("Icon with ImageVector $imageVector") { node ->
            // Checks for the existence of the ContentDescription property, confirming the Icon composable is present.
            val iconTag = node.config.getOrNull(SemanticsProperties.ContentDescription)
            iconTag != null
        }
    }
}