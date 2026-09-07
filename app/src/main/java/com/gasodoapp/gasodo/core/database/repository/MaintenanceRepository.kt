package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.PagingData
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for maintenance events.
 */
interface MaintenanceRepository {
    fun getAllByDatePaged(): Flow<PagingData<MaintenanceEventWithServices>>
    suspend fun getById(id: UUID): MaintenanceEvent?
    suspend fun getByIdWithServiceTypes(id: UUID): MaintenanceEventWithServices?
    suspend fun insert(event: MaintenanceEvent)
    suspend fun update(event: MaintenanceEvent)
    suspend fun delete(event: MaintenanceEvent)
    suspend fun insertWithUsedServices(
        event: MaintenanceEvent,
        services: Set<MaintenanceServiceType> = emptySet()
    )
}
