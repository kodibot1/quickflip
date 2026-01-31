package com.recipenotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a recipe in the database.
 *
 * Why separate Entity from Domain Model?
 * - Entities are tied to Room (annotations, column names, database concerns)
 * - Domain models are pure Kotlin data classes (no framework dependencies)
 * - This separation means:
 *   1. UI layer never depends on Room (could swap to a different DB)
 *   2. Database schema can evolve independently of business logic
 *   3. Easier to test - domain models don't need a database context
 *
 * Room uses @Entity to auto-generate the SQLite CREATE TABLE statement.
 * Each property becomes a column. @PrimaryKey(autoGenerate = true) makes Room
 * auto-increment the ID for new rows.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val servings: Int = 1,
    /** URI string pointing to a photo in the device's gallery (null if no photo) */
    val photoUri: String? = null,
    val isFavourite: Boolean = false,
    /** Timestamps stored as epoch millis for easy sorting and comparison */
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
