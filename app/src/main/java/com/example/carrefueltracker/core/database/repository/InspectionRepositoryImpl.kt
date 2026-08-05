package com.example.carrefueltracker.core.database.repository

import com.example.carrefueltracker.core.database.dao.InspectionEventDao
import com.example.carrefueltracker.core.database.entity.InspectionEvent
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.flow.Flow

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
