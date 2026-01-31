package com.recipenotes.di

import com.recipenotes.data.repository.MealPlanRepository
import com.recipenotes.data.repository.MealPlanRepositoryImpl
import com.recipenotes.data.repository.RecipeRepository
import com.recipenotes.data.repository.RecipeRepositoryImpl
import com.recipenotes.data.repository.ShoppingRepository
import com.recipenotes.data.repository.ShoppingRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to their implementations.
 *
 * @Binds vs @Provides:
 * - @Provides: You write the factory function yourself (used in AppModule for Room)
 * - @Binds: Tells Hilt "when someone asks for the interface, give them this implementation"
 *   More concise and efficient — Hilt doesn't need to generate a factory method.
 *
 * This is the module that makes testing easy: in a test, you could install a different
 * module that binds FakeRecipeRepository instead of RecipeRepositoryImpl.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindMealPlanRepository(impl: MealPlanRepositoryImpl): MealPlanRepository

    @Binds
    @Singleton
    abstract fun bindShoppingRepository(impl: ShoppingRepositoryImpl): ShoppingRepository
}
