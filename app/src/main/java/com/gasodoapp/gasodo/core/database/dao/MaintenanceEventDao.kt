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
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
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

    @Transaction
    @Query("SELECT * FROM maintenance_events ORDER BY date DESC")
    fun getAllWithServiceTypesOrderByDate(): PagingSource<Int, MaintenanceEventWithServices>

    @Transaction
    @Query("SELECT * FROM maintenance_events WHERE event_id = :id")
    suspend fun getByIdWithServiceTypes(id: UUID): MaintenanceEventWithServices
}
