package com.example.carrefueltracker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.carrefueltracker.core.database.entity.SavedLocation
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SavedLocation entities.
 */
@Dao
interface SavedLocationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: SavedLocation): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<SavedLocation>)

    @Delete
    suspend fun delete(entity: SavedLocation)

    @Update
    suspend fun update(entity: SavedLocation)

    @Query("SELECT * FROM saved_locations WHERE id = :id")
    suspend fun getById(id: String): SavedLocation?

    @Query("SELECT * FROM saved_locations ORDER BY name ASC")
    fun getAll(): Flow<List<SavedLocation>>

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteById(id: String)
}
