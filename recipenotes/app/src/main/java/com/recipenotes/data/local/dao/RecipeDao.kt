package com.recipenotes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.recipenotes.data.local.entity.IngredientEntity
import com.recipenotes.data.local.entity.RecipeEntity
import com.recipenotes.data.local.entity.StepEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for recipes, ingredients, and steps.
 *
 * DAOs are interfaces where you define database operations using annotations.
 * Room generates the implementation at compile time. This means:
 * - SQL queries are verified at compile time (typos caught early)
 * - Flow return types make queries reactive (UI auto-updates when data changes)
 * - Suspend functions run on background threads automatically with Room KTX
 *
 * Flow vs suspend:
 * - Flow<List<...>>: For queries that should continuously observe changes (lists, searches)
 * - suspend: For one-shot operations (insert, update, delete)
 */
@Dao
interface RecipeDao {

    /** Get all recipes ordered by most recently updated first */
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<RecipeEntity>>

    /** Get a single recipe by ID - Flow so the detail screen auto-updates on changes */
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getById(id: Long): Flow<RecipeEntity?>

    /**
     * Search recipes by title, description, or ingredient name.
     * The '%' || :query || '%' creates a LIKE pattern for substring matching.
     * DISTINCT ensures we don't get duplicate recipes when multiple ingredients match.
     */
    @Query("""
        SELECT DISTINCT r.* FROM recipes r
        LEFT JOIN ingredients i ON r.id = i.recipeId
        WHERE r.title LIKE '%' || :query || '%'
           OR r.description LIKE '%' || :query || '%'
           OR i.name LIKE '%' || :query || '%'
        ORDER BY r.updatedAt DESC
    """)
    fun search(query: String): Flow<List<RecipeEntity>>

    /** Get only favourited recipes */
    @Query("SELECT * FROM recipes WHERE isFavourite = 1 ORDER BY updatedAt DESC")
    fun getFavourites(): Flow<List<RecipeEntity>>

    /** Insert returns the auto-generated ID of the new row */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeEntity): Long

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Delete
    suspend fun delete(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)

    // --- Ingredient operations ---

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY sortOrder")
    fun getIngredientsForRecipe(recipeId: Long): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE recipeId = :recipeId ORDER BY sortOrder")
    suspend fun getIngredientsForRecipeOnce(recipeId: Long): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Long)

    // --- Step operations ---

    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY stepNumber")
    fun getStepsForRecipe(recipeId: Long): Flow<List<StepEntity>>

    @Query("SELECT * FROM steps WHERE recipeId = :recipeId ORDER BY stepNumber")
    suspend fun getStepsForRecipeOnce(recipeId: Long): List<StepEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsForRecipe(recipeId: Long)

    /** Toggle favourite status - more efficient than loading + updating the whole entity */
    @Query("UPDATE recipes SET isFavourite = NOT isFavourite WHERE id = :id")
    suspend fun toggleFavourite(id: Long)
}
