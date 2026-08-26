package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.SavedLocationDao
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

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
