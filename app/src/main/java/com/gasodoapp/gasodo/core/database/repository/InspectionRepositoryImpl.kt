package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.dao.InspectionEventDao
import com.gasodoapp.gasodo.core.database.dao.UsedInspectablePartDao
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.entity.UsedInspectablePart
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of [InspectionRepository] using Room DAO.
 */
class InspectionRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val dao: InspectionEventDao,
    private val usedPartDao: UsedInspectablePartDao,
) : InspectionRepository {

    override fun getAll(): Flow<List<InspectionEvent>> =
        dao.getAll()

    override fun getAllByDatePaged(): Flow<PagingData<InspectionEventWithParts>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { dao.getAllWithPartsOrderByDate() }
        ).flow
    }

    override suspend fun getById(id: UUID): InspectionEvent? =
        dao.getById(id)

    override suspend fun getByIdWithParts(id: UUID): InspectionEventWithParts? =
        dao.getByIdWithParts(id)

    override suspend fun insert(event: InspectionEvent) {
        dao.insert(event)
    }

    override suspend fun update(event: InspectionEvent, parts: Set<InspectablePart>?) {
        dao.update(event)

        if (parts != null) {
            usedPartDao.deleteByInspectionEventId(event.id)
            parts.map { UsedInspectablePart(event.id, it.id) }.let {
                usedPartDao.insertAll(it)
            }
        }
    }

    override suspend fun delete(event: InspectionEvent) {
        dao.delete(event)
    }

    override suspend fun insertWithUsedParts(
        event: InspectionEvent,
        parts: Set<InspectablePart>
    ) {
        db.withTransaction {
            insert(event)
            val listToInsert = parts.map { UsedInspectablePart(event.id, it.id) }
            usedPartDao.insertAll(listToInsert)
        }
    }

    override fun getAllWithinTime(
        start: Long,
        end: Long
    ): Flow<List<InspectionEventWithParts>> =
        dao.getAllInDateRange(start, end)
}