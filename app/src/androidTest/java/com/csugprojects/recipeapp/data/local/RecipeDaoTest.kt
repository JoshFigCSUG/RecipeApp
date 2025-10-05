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

// Use AndroidJUnit4 runner for instrumented tests
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RecipeDaoTest {

    // Rule to execute architectural components instantly on the testing thread
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var recipeDao: RecipeDao
    private lateinit var db: RecipeDatabase

    // --- Mock Data ---
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

        // 1. Create an in-memory database for testing (it's destroyed after the process finishes).
        db = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
            // Allow main thread access only for testing purposes
            .allowMainThreadQueries()
            .build()
        recipeDao = db.recipeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        // 2. Close the database after each test
        db.close()
    }

    // -----------------------------------------------------------------
    // --- 1. INSERTION TESTS (CORRECTED NAMES) ---
    // -----------------------------------------------------------------

    @Test
    fun insertRecipe_inserts_a_single_recipe_into_the_database() = runTest {
        // WHEN: inserting the recipe
        recipeDao.insertRecipe(recipe1)

        // THEN: the recipe is present in the database Flow
        val allRecipes = recipeDao.getAllFavoriteRecipes().first()
        assertEquals(1, allRecipes.size)
        assertEquals(recipe1.id, allRecipes.first().id)
        // Verify complex field was correctly converted and stored
        assertEquals(recipe1.ingredients.first().name, allRecipes.first().ingredients.first().name)
    }

    @Test
    fun insertRecipe_replaces_existing_recipe_on_conflict() = runTest {
        // GIVEN: two versions of the same recipe ID
        val updatedRecipe = recipe1.copy(title = "Updated Dish 1")

        // WHEN: inserting the original, then the updated version
        recipeDao.insertRecipe(recipe1)
        recipeDao.insertRecipe(updatedRecipe)

        // THEN: only one recipe exists, and it is the updated version
        val allRecipes = recipeDao.getAllFavoriteRecipes().first()
        assertEquals(1, allRecipes.size)
        assertEquals("Updated Dish 1", allRecipes.first().title)
    }

    // -----------------------------------------------------------------
    // --- 2. DELETION TESTS (CORRECTED NAMES) ---
    // -----------------------------------------------------------------

    @Test
    fun deleteRecipeById_removes_the_correct_recipe() = runTest {
        // GIVEN: two recipes are inserted
        recipeDao.insertRecipe(recipe1)
        recipeDao.insertRecipe(recipe2)
        assertEquals(2, recipeDao.getAllFavoriteRecipes().first().size)

        // WHEN: deleting recipe 1
        recipeDao.deleteRecipeById(recipe1.id)

        // THEN: only recipe 2 remains
        val remainingRecipes = recipeDao.getAllFavoriteRecipes().first()
        assertEquals(1, remainingRecipes.size)
        assertEquals(recipe2.id, remainingRecipes.first().id)
    }

    // -----------------------------------------------------------------
    // --- 3. RETRIEVAL TESTS (Flow and Existence) (CORRECTED NAMES) ---
    // -----------------------------------------------------------------

    @Test
    fun getAllFavoriteRecipes_returns_empty_list_when_database_is_empty() = runTest {
        // WHEN: fetching all recipes
        val allRecipes = recipeDao.getAllFavoriteRecipes().first()

        // THEN: the list is empty
        assertTrue(allRecipes.isEmpty())
    }

    @Test
    fun isFavorite_returns_true_when_recipe_exists() = runTest {
        // GIVEN: a recipe is inserted
        recipeDao.insertRecipe(recipe1)

        // WHEN: checking if it is a favorite
        val isFav = recipeDao.isFavorite(recipe1.id).first()

        // THEN: result is true
        assertTrue(isFav)
    }

    @Test
    fun isFavorite_returns_false_when_recipe_does_not_exist() = runTest {
        // WHEN: checking if a non-existent recipe is a favorite
        val isFav = recipeDao.isFavorite("99999").first()

        // THEN: result is false
        assertFalse(isFav)
    }
}