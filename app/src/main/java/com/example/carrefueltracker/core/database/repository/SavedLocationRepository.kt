package com.example.carrefueltracker.core.database.repository

import com.example.carrefueltracker.core.database.entity.SavedLocation
import java.util.UUID
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for saved locations.
 */
interface SavedLocationRepository {
    fun getAll(): Flow<List<SavedLocation>>
    suspend fun getById(id: UUID): SavedLocation?
    suspend fun insert(location: SavedLocation)
    suspend fun update(location: SavedLocation)
    suspend fun delete(location: SavedLocation)
}
