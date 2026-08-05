package com.example.carrefueltracker.core.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.carrefueltracker.core.database.dao.RefuelEventDao
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import javax.inject.Inject
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Implementation of [RefuelRepository] using Room DAO.
 */
class RefuelRepositoryImpl @Inject constructor(
    private val dao: RefuelEventDao,
) : RefuelRepository {

    override fun getAllByDatePaged(): Flow<PagingData<RefuelEvent>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { dao.getAllOrderByDate() }
        ).flow
    }

    override fun getAllByYearMonth(year: Int, month: Int): Flow<List<RefuelEvent>> {
        val startDate = LocalDate.of(year, month, 0)
        val endDate = LocalDate.of(year, month + 1, 0)

        return dao.getAllInDateRange(startDate.toEpochDay(), endDate.toEpochDay())
    }

    override fun getAllWithinTime(
        start: Long,
        end: Long
    ): Flow<List<RefuelEvent>> {
        return dao.getAllInDateRange(start, end)
    }

    override suspend fun getById(id: UUID): RefuelEvent? =
        dao.getById(id)

    override suspend fun insert(event: RefuelEvent) {
        dao.insert(event)
    }

    override suspend fun update(event: RefuelEvent) {
        dao.update(event)
    }

    override suspend fun delete(event: RefuelEvent) {
        dao.delete(event)
    }
}
