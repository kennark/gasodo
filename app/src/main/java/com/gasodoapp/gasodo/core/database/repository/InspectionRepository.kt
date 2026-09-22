package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.PagingData
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for inspection events.
 */
interface InspectionRepository {
    fun getAll(): Flow<List<InspectionEvent>>
    fun getAllByDatePaged(): Flow<PagingData<InspectionEventWithParts>>
    suspend fun getById(id: UUID): InspectionEvent?
    suspend fun getByIdWithParts(id: UUID): InspectionEventWithParts?
    suspend fun insert(event: InspectionEvent)
    suspend fun update(event: InspectionEvent, parts: Set<InspectablePart>? = null)
    suspend fun delete(event: InspectionEvent)
    suspend fun insertWithUsedParts(
        event: InspectionEvent,
        parts: Set<InspectablePart> = emptySet()
    )
    fun getAllWithinTime(start: Long, end: Long): Flow<List<InspectionEventWithParts>>
}