package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import kotlinx.coroutines.flow.Flow

interface MaintenanceServiceTypeRepository {

    fun getAll(): Flow<List<MaintenanceServiceType>>
    suspend fun getById(id: Long): MaintenanceServiceType?
    suspend fun insert(entity: MaintenanceServiceType): Long
    suspend fun update(entity: MaintenanceServiceType)
    suspend fun delete(entity: MaintenanceServiceType)

    /**
     * Number of maintenance events that reference the service type with [id].
     *
     * Deleting a service type cascades to the `used_maintenance_services` reference table, so the
     * UI can use this to warn the user before the type is removed from past events.
     */
    suspend fun countUsage(id: Long): Int
}