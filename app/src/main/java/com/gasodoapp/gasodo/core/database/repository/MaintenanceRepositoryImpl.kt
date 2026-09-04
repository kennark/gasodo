package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.dao.MaintenanceEventDao
import com.gasodoapp.gasodo.core.database.dao.UsedMaintenanceServiceDao
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of [MaintenanceRepository] using Room DAO.
 */
class MaintenanceRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val eventDao: MaintenanceEventDao,
    private val usedServiceDao: UsedMaintenanceServiceDao
) : MaintenanceRepository {

    override fun getAll(): PagingSource<Int, MaintenanceEventWithServices> =
        eventDao.getAllWithServiceTypesOrderByDate()

    override suspend fun getById(id: UUID): MaintenanceEvent? =
        eventDao.getById(id)

    override suspend fun insert(event: MaintenanceEvent) {
        eventDao.insert(event)
    }

    override suspend fun update(event: MaintenanceEvent) {
        eventDao.update(event)
    }

    override suspend fun delete(event: MaintenanceEvent) {
        eventDao.delete(event)
    }

    override suspend fun insertWithUsedServices(
        event: MaintenanceEvent,
        services: Set<MaintenanceServiceType>
    ) {
        db.withTransaction {
            insert(event)
            val listToInsert = services.map { UsedMaintenanceService(event.id, it.id) }
            usedServiceDao.insertAll(listToInsert)
        }
    }
}
