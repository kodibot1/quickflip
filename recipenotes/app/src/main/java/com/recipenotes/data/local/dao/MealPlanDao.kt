package com.recipenotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.recipenotes.data.local.entity.MealPlanEntity
import com.recipenotes.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data class to hold a meal plan entry joined with its recipe data.
 * Room can populate this from a JOIN query - it matches column names to properties.
 */
data class MealPlanWithRecipe(
    // MealPlan fields
    val id: Long,
    val recipeId: Long,
    val dayOfWeek: Int,
    val mealType: String,
    val weekStartDate: String,
    // Recipe fields (prefixed to avoid column name conflicts)
    val recipeTitle: String,
    val recipeDescription: String,
    val recipePrepTimeMinutes: Int,
    val recipeCookTimeMinutes: Int,
    val recipeServings: Int,
    val recipePhotoUri: String?,
    val recipeIsFavourite: Boolean
)

/**
 * DAO for meal plan operations.
 */
@Dao
interface MealPlanDao {

    /** Get all meal plan entries for a specific week, joined with recipe details */
    @Query("""
        SELECT mp.id, mp.recipeId, mp.dayOfWeek, mp.mealType, mp.weekStartDate,
               r.title AS recipeTitle, r.description AS recipeDescription,
               r.prepTimeMinutes AS recipePrepTimeMinutes, r.cookTimeMinutes AS recipeCookTimeMinutes,
               r.servings AS recipeServings, r.photoUri AS recipePhotoUri,
               r.isFavourite AS recipeIsFavourite
        FROM meal_plans mp
        INNER JOIN recipes r ON mp.recipeId = r.id
        WHERE mp.weekStartDate = :weekStartDate
        ORDER BY mp.dayOfWeek, mp.mealType
    """)
    fun getForWeek(weekStartDate: String): Flow<List<MealPlanWithRecipe>>

    /** Get raw meal plan entities for a week (used for shopping list generation) */
    @Query("SELECT * FROM meal_plans WHERE weekStartDate = :weekStartDate")
    suspend fun getEntitiesForWeek(weekStartDate: String): List<MealPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealPlan: MealPlanEntity): Long

    @Query("DELETE FROM meal_plans WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM meal_plans WHERE weekStartDate = :weekStartDate")
    suspend fun deleteForWeek(weekStartDate: String)
}
