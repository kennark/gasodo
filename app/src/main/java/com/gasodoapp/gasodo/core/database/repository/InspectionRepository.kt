package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for inspection events.
 */
interface InspectionRepository {
    fun getAll(): Flow<List<InspectionEvent>>
    suspend fun getById(id: UUID): InspectionEvent?
    suspend fun insert(event: InspectionEvent)
    suspend fun update(event: InspectionEvent)
    suspend fun delete(event: InspectionEvent)
}
