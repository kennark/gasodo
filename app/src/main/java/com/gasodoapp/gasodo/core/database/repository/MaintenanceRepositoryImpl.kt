package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.MaintenanceEventDao
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

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
