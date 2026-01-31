package com.recipenotes.domain.model

/**
 * Domain model for a recipe ingredient.
 * Clean data class with no database annotations.
 */
data class Ingredient(
    val id: Long = 0,
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = ""
) {
    /** Formatted display string like "2.0 cups flour" */
    val displayText: String
        get() = buildString {
            if (quantity > 0) {
                // Show whole numbers without decimal point
                append(if (quantity % 1.0 == 0.0) quantity.toInt().toString() else quantity.toString())
                if (unit.isNotBlank()) append(" $unit")
                append(" ")
            }
            append(name)
        }
}
