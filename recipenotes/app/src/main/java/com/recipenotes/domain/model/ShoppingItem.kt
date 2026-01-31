package com.recipenotes.domain.model

/**
 * Domain model for a shopping list item.
 */
data class ShoppingItem(
    val id: Long = 0,
    val name: String,
    val quantity: Double = 0.0,
    val unit: String = "",
    val isChecked: Boolean = false,
    val sourceRecipeId: Long? = null,
    val isManual: Boolean = false
) {
    /** Formatted display like "2 cups" or just the name if no quantity */
    val quantityDisplay: String
        get() = if (quantity > 0) {
            val qty = if (quantity % 1.0 == 0.0) quantity.toInt().toString() else quantity.toString()
            if (unit.isNotBlank()) "$qty $unit" else qty
        } else ""
}
