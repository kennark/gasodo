package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.gasodoapp.gasodo.core.database.dao.RefuelEventDao
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

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
        val startDate = LocalDate.of(year, month, 1)
        val endDate = LocalDate.of(year, month + 1, 1)

        return getAllWithinTime(
            startDate.toEpochDay().times(86400000L),
            endDate.toEpochDay().times(86400000L)
        )
    }

    override fun getAllWithinTime(
        start: Long,
        end: Long
    ): Flow<List<RefuelEvent>> {
        return dao.getAllInDateRange(start, end)
    }

    override suspend fun getById(id: UUID): RefuelEvent? =
        dao.getById(id)

    override suspend fun upsert(event: RefuelEvent) {
        dao.upsert(event)
    }

    override suspend fun update(event: RefuelEvent) {
        dao.update(event)
    }

    override suspend fun delete(event: RefuelEvent) {
        dao.delete(event)
    }
}
