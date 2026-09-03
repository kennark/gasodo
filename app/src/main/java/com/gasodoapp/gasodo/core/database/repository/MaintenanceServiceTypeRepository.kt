package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import kotlinx.coroutines.flow.Flow

interface MaintenanceServiceTypeRepository {

    fun getAll(): Flow<List<MaintenanceServiceType>>
    suspend fun getById(id: Long): MaintenanceServiceType?
    suspend fun insert(entity: MaintenanceServiceType)
    suspend fun update(entity: MaintenanceServiceType)
    suspend fun delete(entity: MaintenanceServiceType)
}