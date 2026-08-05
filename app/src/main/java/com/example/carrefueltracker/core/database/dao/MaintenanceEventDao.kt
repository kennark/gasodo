package com.example.carrefueltracker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.carrefueltracker.core.database.entity.MaintenanceEvent
import kotlinx.coroutines.flow.Flow
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

    @Query("SELECT * FROM maintenance_events WHERE id = :id")
    suspend fun getById(id: UUID): MaintenanceEvent?

    @Query("DELETE FROM maintenance_events WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("SELECT * FROM maintenance_events ORDER BY date DESC")
    fun getAll(): Flow<List<MaintenanceEvent>>
}
