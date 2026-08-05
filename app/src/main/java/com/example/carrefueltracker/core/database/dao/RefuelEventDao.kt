package com.example.carrefueltracker.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Data Access Object for RefuelEvent entities.
 */
@Dao
interface RefuelEventDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: RefuelEvent): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<RefuelEvent>)

    @Delete
    suspend fun delete(entity: RefuelEvent)

    @Update
    suspend fun update(entity: RefuelEvent)

    @Query("SELECT * FROM refuel_events WHERE id = :id")
    suspend fun getById(id: UUID): RefuelEvent?

    @Query("DELETE FROM refuel_events WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("SELECT * FROM refuel_events ORDER BY date DESC")
    fun getAllOrderByDate(): PagingSource<Int, RefuelEvent>

    @Query("SELECT * FROM refuel_events WHERE date BETWEEN :startRange AND :endRange ORDER BY mileage DESC")
    fun getAllInDateRange(startRange: Long, endRange: Long): Flow<List<RefuelEvent>>
}
