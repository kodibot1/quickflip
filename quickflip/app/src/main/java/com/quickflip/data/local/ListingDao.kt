package com.quickflip.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE id = :id")
    fun getById(id: Long): Flow<Listing?>

    @Query("SELECT * FROM listings WHERE status = :status ORDER BY createdAt DESC")
    fun getByStatus(status: String): Flow<List<Listing>>

    @Insert
    suspend fun insert(listing: Listing): Long

    @Update
    suspend fun update(listing: Listing)

    @Delete
    suspend fun delete(listing: Listing)
}
