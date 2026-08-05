package com.example.carrefueltracker.core.database.repository

import com.example.carrefueltracker.core.database.dao.SavedLocationDao
import com.example.carrefueltracker.core.database.entity.SavedLocation
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [SavedLocationRepository] using Room DAO.
 */
class SavedLocationRepositoryImpl @Inject constructor(
    private val dao: SavedLocationDao,
) : SavedLocationRepository {

    override fun getAll(): Flow<List<SavedLocation>> =
        dao.getAll()

    override suspend fun getById(id: UUID): SavedLocation? =
        dao.getById(id.toString())

    override suspend fun insert(location: SavedLocation) {
        dao.insert(location)
    }

    override suspend fun update(location: SavedLocation) {
        dao.update(location)
    }

    override suspend fun delete(location: SavedLocation) {
        dao.delete(location)
    }
}
