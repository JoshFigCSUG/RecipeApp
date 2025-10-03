package com.csugprojects.recipeapp.di

import android.app.Application

/**
 * MyApp is the custom Application class, which acts as the global entry point (M4 Design).
 * This class ensures that core dependencies are created only once and live for the entire application lifetime.
 */
class MyApp : Application() {
    // Declares the dependency container, initialized upon application launch (M4 DI).
    lateinit var container: AppContainer

    /**
     * Called when the application process is created.
     */
    override fun onCreate() {
        super.onCreate()
        // Instantiates the dependency container, making the Repository and other services available (M4/M6).
        container = AppContainer(this)
    }
}
