package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.PagingSource
import com.gasodoapp.gasodo.core.database.dao.MaintenanceEventDao
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of [MaintenanceRepository] using Room DAO.
 */
class MaintenanceRepositoryImpl @Inject constructor(
    private val dao: MaintenanceEventDao,
) : MaintenanceRepository {

    override fun getAll(): PagingSource<Int, MaintenanceEventWithServices> =
        dao.getAllWithServiceTypesOrderByDate()

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
