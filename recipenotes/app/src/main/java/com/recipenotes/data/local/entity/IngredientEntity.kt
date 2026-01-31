package com.recipenotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a single ingredient belonging to a recipe.
 *
 * @ForeignKey enforces referential integrity: you can't insert an ingredient
 * for a recipe that doesn't exist. CASCADE delete means when a recipe is deleted,
 * all its ingredients are automatically removed too.
 *
 * @Index on recipeId speeds up queries that filter by recipe (which is most queries).
 * Without this index, Room would do a full table scan every time you ask for
 * "all ingredients for recipe X".
 */
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipeId"])]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    val name: String,
    val quantity: Double = 0.0,
    val unit: String = "",
    /** sortOrder preserves the user's intended ingredient ordering */
    val sortOrder: Int = 0
)
