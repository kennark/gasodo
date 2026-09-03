package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.MaintenanceServiceTypeDao
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MaintenanceServiceTypeRepositoryImpl @Inject constructor(
    private val dao: MaintenanceServiceTypeDao,
) : MaintenanceServiceTypeRepository {

    override fun getAll(): Flow<List<MaintenanceServiceType>> =
        dao.getAll()

    override suspend fun getById(id: Long): MaintenanceServiceType? =
        dao.getById(id)

    override suspend fun insert(entity: MaintenanceServiceType) {
        dao.insert(entity)
    }

    override suspend fun update(entity: MaintenanceServiceType) {
        dao.update(entity)
    }

    override suspend fun delete(entity: MaintenanceServiceType) {
        dao.delete(entity)
    }
}