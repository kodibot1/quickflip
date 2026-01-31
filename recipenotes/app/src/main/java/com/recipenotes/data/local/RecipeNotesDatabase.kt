package com.recipenotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.recipenotes.data.local.converter.Converters
import com.recipenotes.data.local.dao.MealPlanDao
import com.recipenotes.data.local.dao.RecipeDao
import com.recipenotes.data.local.dao.ShoppingItemDao
import com.recipenotes.data.local.entity.IngredientEntity
import com.recipenotes.data.local.entity.MealPlanEntity
import com.recipenotes.data.local.entity.RecipeEntity
import com.recipenotes.data.local.entity.ShoppingItemEntity
import com.recipenotes.data.local.entity.StepEntity

/**
 * Room Database definition for RecipeNotes.
 *
 * @Database annotation tells Room:
 * - Which entities (tables) this database contains
 * - The schema version (increment when changing entity structure, add migration)
 * - exportSchema = false skips generating schema JSON files (enable for production)
 *
 * Room generates the actual SQLite database implementation at compile time based
 * on this abstract class and the DAO interfaces. You never write raw SQL for
 * table creation - Room handles it from the @Entity annotations.
 *
 * This class is abstract because Room generates the concrete implementation.
 * The abstract DAO getter methods are implemented by Room's generated code.
 */
@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        StepEntity::class,
        MealPlanEntity::class,
        ShoppingItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RecipeNotesDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun shoppingItemDao(): ShoppingItemDao
}
