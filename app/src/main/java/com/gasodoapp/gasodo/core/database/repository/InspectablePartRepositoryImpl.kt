package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.InspectablePartDao
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InspectablePartRepositoryImpl @Inject constructor(
    private val dao: InspectablePartDao,
) : InspectablePartRepository {

    override fun getAll(): Flow<List<InspectablePart>> =
        dao.getAll()

    override suspend fun getById(id: Long): InspectablePart? =
        dao.getById(id)

    override suspend fun insert(entity: InspectablePart): Long =
        dao.insert(entity)

    override suspend fun update(entity: InspectablePart) {
        dao.update(entity)
    }

    override suspend fun delete(entity: InspectablePart) {
        dao.delete(entity)
    }
}