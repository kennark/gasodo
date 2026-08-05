package com.example.carrefueltracker.core.database.repository

import com.example.carrefueltracker.core.database.dao.MaintenanceEventDao
import com.example.carrefueltracker.core.database.entity.MaintenanceEvent
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [MaintenanceRepository] using Room DAO.
 */
class MaintenanceRepositoryImpl @Inject constructor(
    private val dao: MaintenanceEventDao,
) : MaintenanceRepository {

    override fun getAll(): Flow<List<MaintenanceEvent>> =
        dao.getAll()

    override suspend fun getById(id: UUID): MaintenanceEvent? =
        dao.getById(id)

    override suspend fun insert(event: MaintenanceEvent) {
        dao.insert(event)
    }

    override suspend fun update(event: MaintenanceEvent) {
        dao.update(event)
    }

    override suspend fun delete(event: MaintenanceEvent) {
        dao.delete(event)
    }
}
