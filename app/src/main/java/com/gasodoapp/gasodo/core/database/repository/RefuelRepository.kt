package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.PagingData
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository interface for refueling events.
 */
interface RefuelRepository {
    fun getAllByDatePaged(): Flow<PagingData<RefuelEvent>>

    fun getAllByYearMonth(year: Int, month: Int): Flow<List<RefuelEvent>>

    fun getAllWithinTime(start: Long, end: Long): Flow<List<RefuelEvent>>
    suspend fun getById(id: UUID): RefuelEvent?
    suspend fun upsert(event: RefuelEvent)
    suspend fun update(event: RefuelEvent)
    suspend fun delete(event: RefuelEvent)
}
