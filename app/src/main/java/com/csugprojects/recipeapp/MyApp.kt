package com.csugprojects.recipeapp

import android.app.Application

class MyApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}