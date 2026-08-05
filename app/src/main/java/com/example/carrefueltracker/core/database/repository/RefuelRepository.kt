package com.example.carrefueltracker.core.database.repository

import android.text.format.DateUtils
import androidx.paging.PagingData
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import java.util.UUID

/**
 * Repository interface for refueling events.
 */
interface RefuelRepository {
    fun getAllByDatePaged(): Flow<PagingData<RefuelEvent>>

    fun getAllByYearMonth(year: Int, month: Int): Flow<List<RefuelEvent>>

    fun getAllWithinTime(start: Long, end: Long): Flow<List<RefuelEvent>>
    suspend fun getById(id: UUID): RefuelEvent?
    suspend fun insert(event: RefuelEvent)
    suspend fun update(event: RefuelEvent)
    suspend fun delete(event: RefuelEvent)
}
