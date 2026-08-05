package com.example.carrefueltracker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.carrefueltracker.core.database.entity.InspectionEvent
import kotlinx.coroutines.flow.Flow
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

    @Query("SELECT * FROM inspection_events ORDER BY date DESC")
    fun getAll(): Flow<List<InspectionEvent>>
}
