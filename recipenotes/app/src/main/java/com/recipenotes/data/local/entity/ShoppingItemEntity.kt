package com.recipenotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a shopping list item.
 *
 * Items can come from two sources:
 * 1. Auto-generated from meal plan recipes (isManual = false, sourceRecipeId set)
 * 2. Manually added by the user (isManual = true, sourceRecipeId = null)
 *
 * This distinction lets us clear auto-generated items when regenerating
 * the list without losing the user's manual additions.
 */
@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val quantity: Double = 0.0,
    val unit: String = "",
    val isChecked: Boolean = false,
    /** Links back to the recipe this ingredient came from (null for manual items) */
    val sourceRecipeId: Long? = null,
    val isManual: Boolean = false
)
