package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import kotlinx.coroutines.flow.Flow
import java.util.UUID

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
