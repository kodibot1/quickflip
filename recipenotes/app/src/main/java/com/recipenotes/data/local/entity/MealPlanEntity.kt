package com.recipenotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a meal plan entry - one recipe assigned to a
 * specific day and meal type within a week.
 *
 * Design decisions:
 * - dayOfWeek is 0-6 (Mon-Sun) stored as Int for easy arithmetic
 * - mealType is a String ("breakfast", "lunch", "dinner", "snack") - using
 *   String instead of an enum makes the DB schema more flexible
 * - weekStartDate is ISO format "2025-01-27" (always a Monday) - this groups
 *   meal plans into weeks for easy querying
 */
@Entity(
    tableName = "meal_plans",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipeId"]), Index(value = ["weekStartDate"])]
)
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    /** 0 = Monday, 1 = Tuesday, ..., 6 = Sunday */
    val dayOfWeek: Int,
    /** One of: "breakfast", "lunch", "dinner", "snack" */
    val mealType: String,
    /** ISO date string of the Monday that starts this week, e.g. "2025-01-27" */
    val weekStartDate: String
)
