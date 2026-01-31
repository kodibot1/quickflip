package com.recipenotes.domain.model

/**
 * Domain model for a meal plan entry - links a recipe to a day and meal type.
 */
data class MealPlan(
    val id: Long = 0,
    val recipe: Recipe,
    val dayOfWeek: Int,
    val mealType: String,
    val weekStartDate: String
)

/**
 * The four meal types supported by the planner.
 * Using an object with constants (rather than an enum) keeps it simple
 * while still providing type-safe references.
 */
object MealType {
    const val BREAKFAST = "breakfast"
    const val LUNCH = "lunch"
    const val DINNER = "dinner"
    const val SNACK = "snack"

    val ALL = listOf(BREAKFAST, LUNCH, DINNER, SNACK)

    /** Human-readable label for display */
    fun displayName(type: String): String = type.replaceFirstChar { it.uppercase() }
}
