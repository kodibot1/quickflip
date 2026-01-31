package com.recipenotes.data.repository

import com.recipenotes.data.local.dao.MealPlanDao
import com.recipenotes.data.local.dao.MealPlanWithRecipe
import com.recipenotes.data.local.dao.RecipeDao
import com.recipenotes.data.local.entity.MealPlanEntity
import com.recipenotes.domain.model.Ingredient
import com.recipenotes.domain.model.MealPlan
import com.recipenotes.domain.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for meal plan operations.
 */
interface MealPlanRepository {
    fun getForWeek(weekStartDate: String): Flow<List<MealPlan>>
    suspend fun assignRecipe(recipeId: Long, dayOfWeek: Int, mealType: String, weekStartDate: String): Long
    suspend fun removeEntry(id: Long)
    suspend fun clearWeek(weekStartDate: String)

    /** Get all recipe IDs planned for a week (for shopping list generation) */
    suspend fun getRecipeIdsForWeek(weekStartDate: String): List<Long>
}

class MealPlanRepositoryImpl @Inject constructor(
    private val mealPlanDao: MealPlanDao
) : MealPlanRepository {

    override fun getForWeek(weekStartDate: String): Flow<List<MealPlan>> =
        mealPlanDao.getForWeek(weekStartDate).map { entries ->
            entries.map { it.toDomain() }
        }

    override suspend fun assignRecipe(
        recipeId: Long, dayOfWeek: Int, mealType: String, weekStartDate: String
    ): Long = mealPlanDao.insert(
        MealPlanEntity(
            recipeId = recipeId,
            dayOfWeek = dayOfWeek,
            mealType = mealType,
            weekStartDate = weekStartDate
        )
    )

    override suspend fun removeEntry(id: Long) = mealPlanDao.delete(id)

    override suspend fun clearWeek(weekStartDate: String) =
        mealPlanDao.deleteForWeek(weekStartDate)

    override suspend fun getRecipeIdsForWeek(weekStartDate: String): List<Long> =
        mealPlanDao.getEntitiesForWeek(weekStartDate).map { it.recipeId }.distinct()

    private fun MealPlanWithRecipe.toDomain() = MealPlan(
        id = id,
        recipe = Recipe(
            id = recipeId,
            title = recipeTitle,
            description = recipeDescription,
            prepTimeMinutes = recipePrepTimeMinutes,
            cookTimeMinutes = recipeCookTimeMinutes,
            servings = recipeServings,
            photoUri = recipePhotoUri,
            isFavourite = recipeIsFavourite
        ),
        dayOfWeek = dayOfWeek,
        mealType = mealType,
        weekStartDate = weekStartDate
    )
}
