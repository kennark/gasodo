package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for maintenance events.
 */
interface MaintenanceRepository {
    fun getAll(): Flow<List<MaintenanceEvent>>
    suspend fun getById(id: UUID): MaintenanceEvent?
    suspend fun insert(event: MaintenanceEvent)
    suspend fun update(event: MaintenanceEvent)
    suspend fun delete(event: MaintenanceEvent)
}
