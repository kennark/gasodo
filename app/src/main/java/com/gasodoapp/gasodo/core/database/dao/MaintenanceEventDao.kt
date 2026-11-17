package com.gasodoapp.gasodo.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

/**
 * Data Access Object for MaintenanceEvent entities.
 */
@Dao
interface MaintenanceEventDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MaintenanceEvent): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<MaintenanceEvent>)

    @Delete
    suspend fun delete(entity: MaintenanceEvent)

    @Update
    suspend fun update(entity: MaintenanceEvent)

    @Query("SELECT * FROM maintenance_events WHERE event_id = :id")
    suspend fun getById(id: UUID): MaintenanceEvent?

    @Query("DELETE FROM maintenance_events WHERE event_id = :id")
    suspend fun deleteById(id: UUID)

    /**
     * Deletes every event whose id is in [ids] in a single statement.
     *
     * The [UUID] type converter is applied to each element of the collection, so Room can bind the
     * list to the `IN` clause directly. Linked rows in `used_maintenance_services` are removed by
     * the `onDelete = CASCADE` foreign key on [UsedMaintenanceService].
     */
    @Query("DELETE FROM maintenance_events WHERE event_id IN (:ids)")
    suspend fun deleteByIds(ids: Collection<UUID>)

    @Transaction
    @Query("SELECT * FROM maintenance_events ORDER BY date DESC")
    fun getAllWithServiceTypesOrderByDate(): PagingSource<Int, MaintenanceEventWithServices>

    @Transaction
    @Query("SELECT * FROM maintenance_events WHERE event_id = :id")
    suspend fun getByIdWithServiceTypes(id: UUID): MaintenanceEventWithServices?

    @Transaction
    @Query("SELECT * FROM maintenance_events WHERE date BETWEEN :startRange AND :endRange ORDER BY mileage DESC")
    fun getAllInDateRange(
        startRange: LocalDate,
        endRange: LocalDate
    ): Flow<List<MaintenanceEventWithServices>>
}
