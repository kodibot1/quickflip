package com.recipenotes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for RecipeNotes.
 *
 * @HiltAndroidApp triggers Hilt's code generation at compile time, creating a base class
 * that serves as the application-level dependency container. This is the root of the
 * Hilt dependency graph - all injected objects ultimately trace back to here.
 *
 * Key concept: In Hilt's DI system, the Application is the "parent" container.
 * Activities, Fragments, and ViewModels each have their own scoped containers that
 * inherit from this one. This means a @Singleton-scoped object (like our database)
 * lives as long as the Application does.
 */
@HiltAndroidApp
class RecipeNotesApp : Application()
