package com.gasodoapp.gasodo.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

/**
 * Data Access Object for InspectionEvent entities.
 */
@Dao
interface InspectionEventDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: InspectionEvent): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<InspectionEvent>)

    @Delete
    suspend fun delete(entity: InspectionEvent)

    @Update
    suspend fun update(entity: InspectionEvent)

    @Query("SELECT * FROM inspection_events WHERE id = :id")
    suspend fun getById(id: UUID): InspectionEvent?

    @Query("DELETE FROM inspection_events WHERE id = :id")
    suspend fun deleteById(id: UUID)

    /**
     * Deletes every event whose id is in [ids] in a single statement.
     *
     * The [UUID] type converter is applied to each element of the collection, so Room can bind the
     * list to the `IN` clause directly. Linked rows in `used_inspectable_parts` are removed by the
     * `onDelete = CASCADE` foreign key on [com.gasodoapp.gasodo.core.database.entity.UsedInspectablePart].
     */
    @Query("DELETE FROM inspection_events WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: Collection<UUID>)

    @Transaction
    @Query("SELECT * FROM inspection_events ORDER BY date DESC")
    fun getAllWithPartsOrderByDate(): PagingSource<Int, InspectionEventWithParts>

    @Transaction
    @Query("SELECT * FROM inspection_events WHERE id = :id")
    suspend fun getByIdWithParts(id: UUID): InspectionEventWithParts?

    @Query("SELECT * FROM inspection_events")
    fun getAll(): Flow<List<InspectionEvent>>

    @Transaction
    @Query("SELECT * FROM inspection_events WHERE date BETWEEN :start AND :end ORDER BY mileage DESC")
    fun getAllInDateRange(start: LocalDate, end: LocalDate): Flow<List<InspectionEventWithParts>>
}