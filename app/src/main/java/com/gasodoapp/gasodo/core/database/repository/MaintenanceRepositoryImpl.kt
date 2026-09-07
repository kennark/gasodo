package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.dao.MaintenanceEventDao
import com.gasodoapp.gasodo.core.database.dao.UsedMaintenanceServiceDao
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import kotlinx.coroutines.flow.Flow
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

    override fun getAllByDatePaged(): Flow<PagingData<MaintenanceEventWithServices>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { eventDao.getAllWithServiceTypesOrderByDate() }
        ).flow
    }


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
