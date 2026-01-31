package com.recipenotes.domain.model

/**
 * Domain model for a recipe - the "clean" representation used by UI and business logic.
 *
 * This has NO Room annotations - it's a pure Kotlin data class. The repository layer
 * handles converting between this and RecipeEntity. This keeps the UI layer completely
 * independent of the database implementation.
 *
 * Includes ingredients and steps inline (unlike the entity layer where they're separate tables).
 * This is more natural for the UI - when you display a recipe, you want everything together.
 */
data class Recipe(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val servings: Int = 1,
    val photoUri: String? = null,
    val isFavourite: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Total time to make the recipe */
    val totalTimeMinutes: Int get() = prepTimeMinutes + cookTimeMinutes
}
