package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.InspectionEventDao
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of [InspectionRepository] using Room DAO.
 */
class InspectionRepositoryImpl @Inject constructor(
    private val dao: InspectionEventDao,
) : InspectionRepository {

    override fun getAll(): Flow<List<InspectionEvent>> =
        dao.getAll()

    override suspend fun getById(id: UUID): InspectionEvent? =
        dao.getById(id)

    override suspend fun insert(event: InspectionEvent) {
        dao.insert(event)
    }

    override suspend fun update(event: InspectionEvent) {
        dao.update(event)
    }

    override suspend fun delete(event: InspectionEvent) {
        dao.delete(event)
    }
}
