package com.recipenotes.di

import android.content.Context
import androidx.room.Room
import com.recipenotes.data.local.RecipeNotesDatabase
import com.recipenotes.data.local.dao.MealPlanDao
import com.recipenotes.data.local.dao.RecipeDao
import com.recipenotes.data.local.dao.ShoppingItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides database-related dependencies.
 *
 * How Hilt Dependency Injection works:
 * 1. You define @Module classes that tell Hilt "how to create" objects
 * 2. @Provides functions are factories — Hilt calls them when something needs that type
 * 3. @Singleton means only one instance is created and shared app-wide
 * 4. @InstallIn(SingletonComponent::class) means these live as long as the app does
 *
 * When a ViewModel has @Inject constructor(recipeDao: RecipeDao), Hilt:
 * 1. Looks for a @Provides function that returns RecipeDao
 * 2. Finds provideRecipeDao, which needs RecipeNotesDatabase
 * 3. Looks for a @Provides for RecipeNotesDatabase
 * 4. Finds provideDatabase (which is @Singleton, so it reuses the existing instance)
 * 5. Calls provideRecipeDao(existingDb) and injects the result
 *
 * This chain of automatic object creation is the core of dependency injection.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Creates the Room database singleton.
     *
     * Room.databaseBuilder() creates a builder that:
     * - Takes the app context (for file system access)
     * - Takes the abstract database class (Room generates the implementation)
     * - Takes a database file name (stored in the app's private directory)
     *
     * .build() creates the database (or opens existing one) on first access.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RecipeNotesDatabase =
        Room.databaseBuilder(
            context,
            RecipeNotesDatabase::class.java,
            "recipenotes.db"
        ).build()

    @Provides
    @Singleton
    fun provideRecipeDao(database: RecipeNotesDatabase): RecipeDao =
        database.recipeDao()

    @Provides
    @Singleton
    fun provideMealPlanDao(database: RecipeNotesDatabase): MealPlanDao =
        database.mealPlanDao()

    @Provides
    @Singleton
    fun provideShoppingItemDao(database: RecipeNotesDatabase): ShoppingItemDao =
        database.shoppingItemDao()
}
