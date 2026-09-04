package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.PagingSource
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import java.util.UUID

/**
 * Repository interface for maintenance events.
 */
interface MaintenanceRepository {
    fun getAll(): PagingSource<Int, MaintenanceEventWithServices>
    suspend fun getById(id: UUID): MaintenanceEvent?
    suspend fun insert(event: MaintenanceEvent)
    suspend fun update(event: MaintenanceEvent)
    suspend fun delete(event: MaintenanceEvent)
    suspend fun insertWithUsedServices(
        event: MaintenanceEvent,
        services: Set<MaintenanceServiceType> = emptySet()
    )
}
