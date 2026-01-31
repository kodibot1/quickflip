package com.quickflip.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "listings")
@TypeConverters(Converters::class)
data class Listing(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String = "",
    val tradeMeDescription: String = "",
    val facebookDescription: String = "",
    val price: String = "",
    val condition: String = "",
    val category: String = "",
    val pickupLocation: String = "",

    val status: String = "draft", // draft, active, sold

    val photoUris: List<String> = emptyList(),

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val soldAt: Long? = null
)
