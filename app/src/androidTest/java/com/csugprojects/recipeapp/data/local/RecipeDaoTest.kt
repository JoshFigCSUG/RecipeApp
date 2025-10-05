package com.csugprojects.recipeapp.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.csugprojects.recipeapp.domain.model.Ingredient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// This Instrumented Test runs on an Android device/emulator to verify Room Database operations.
// This supports the project's overall **Testing Strategy** (M8).
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RecipeDaoTest {

    // Ensures test operations run instantly and in order, crucial for testing Coroutines (Testing Strategy).
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var recipeDao: RecipeDao
    private lateinit var db: RecipeDatabase

    // Mock data represents a Recipe Entity, which is the structure for **Data Persistence** (M4).
    private val mockIngredient = Ingredient("Tomato", "1 can")
    private val recipe1 = RecipeEntity(
        id = "52771",
        title = "Test Dish 1",
        imageUrl = "url1",
        instructions = "Do step 1",
        ingredients = listOf(mockIngredient),
        category = "Pasta",
        area = "Italian"
    )
    private val recipe2 = RecipeEntity(
        id = "52772",
        title = "Test Dish 2",
        imageUrl = "url2",
        instructions = "Do step 2",
        ingredients = listOf(mockIngredient),
        category = "Dessert",
        area = "American"
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Sets up an in-memory Room Database that is destroyed after testing.
        // This isolates the tests for reliability (Testing Strategy).
        db = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        recipeDao = db.recipeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        // Ensures the temporary database is closed after each test run (Testing Strategy).
        db.close()
    }

    // --- INSERTION TESTS: Verifies adding new favorite recipes (M2 Feature/M4 Persistence) ---

    @Test
    fun insertRecipe_inserts_a_single_recipe_into_the_database() = runTest {
        // Action: Inserts a single mock recipe.
        recipeDao.insertRecipe(recipe1)

        // Assertion: Retrieves all data via Flow and checks that the item was successfully saved.
        val allRecipes = recipeDao.getAllFavoriteRecipes().first()
        assertEquals(1, allRecipes.size)
        assertEquals(recipe1.id, allRecipes.first().id)
        // Checks that complex fields (like the Ingredient list) are saved correctly using TypeConverters (M4 Design).
        assertEquals(recipe1.ingredients.first().name, allRecipes.first().ingredients.first().name)
    }

    @Test
    fun insertRecipe_replaces_existing_recipe_on_conflict() = runTest {
        // Setup: Creates a modified version of the same recipe ID.
        val updatedRecipe = recipe1.copy(title = "Updated Dish 1")

        // Action: Inserts the original, then the updated version.
        recipeDao.insertRecipe(recipe1)
        recipeDao.insertRecipe(updatedRecipe)

        // Assertion: Confirms Room's REPLACE strategy worked, preventing duplicate IDs (Maintenance Plan).
        val allRecipes = recipeDao.getAllFavoriteRecipes().first()
        assertEquals(1, allRecipes.size)
        assertEquals("Updated Dish 1", allRecipes.first().title)
    }

    // --- DELETION TESTS: Verifies removing favorite recipes (M2/M4 Persistence) ---

    @Test
    fun deleteRecipeById_removes_the_correct_recipe() = runTest {
        // Setup: Inserts two unique recipes.
        recipeDao.insertRecipe(recipe1)
        recipeDao.insertRecipe(recipe2)
        assertEquals(2, recipeDao.getAllFavoriteRecipes().first().size)

        // Action: Deletes the first recipe by its ID.
        recipeDao.deleteRecipeById(recipe1.id)

        // Assertion: Confirms only the second recipe remains in the database.
        val remainingRecipes = recipeDao.getAllFavoriteRecipes().first()
        assertEquals(1, remainingRecipes.size)
        assertEquals(recipe2.id, remainingRecipes.first().id)
    }

    // --- RETRIEVAL TESTS: Verifies the integrity of the data stream (M4/M6 State Management) ---

    @Test
    fun getAllFavoriteRecipes_returns_empty_list_when_database_is_empty() = runTest {
        // Action: Fetches all recipes when the database is fresh.
        val allRecipes = recipeDao.getAllFavoriteRecipes().first()

        // Assertion: Confirms the list is empty.
        assertTrue(allRecipes.isEmpty())
    }

    @Test
    fun isFavorite_returns_true_when_recipe_exists() = runTest {
        // Setup: A recipe is saved as a favorite.
        recipeDao.insertRecipe(recipe1)

        // Action/Assertion: Checks the `isFavorite` query for the saved recipe ID.
        val isFav = recipeDao.isFavorite(recipe1.id).first()
        assertTrue(isFav)
    }

    @Test
    fun isFavorite_returns_false_when_recipe_does_not_exist() = runTest {
        // Action/Assertion: Checks the `isFavorite` query for a non-existent ID.
        val isFav = recipeDao.isFavorite("99999").first()
        assertFalse(isFav)
    }
}