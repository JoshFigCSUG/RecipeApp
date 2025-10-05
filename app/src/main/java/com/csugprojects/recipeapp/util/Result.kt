package com.csugprojects.recipeapp.util

/**
 * Result is a sealed class used throughout the application to manage the state of asynchronous data (M4 Error Handling).
 * It forces the UI (View Layer) to handle three possible states: loading, success, or error (M6 State Management).
 * @param T The type of data being loaded (e.g., Recipe, List<Category>).
 */
sealed class Result<out T> {
    /**
     * Represents the transient state when data fetching is in progress.
     */
    object Loading : Result<Nothing>()

    /**
     * Represents a successful operation, holding the final data payload.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation, holding the exception or error message.
     */
    data class Error(val exception: Exception) : Result<Nothing>()
}