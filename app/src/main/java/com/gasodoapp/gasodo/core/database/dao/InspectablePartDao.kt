package com.gasodoapp.gasodo.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Inspectable Parts.
 */
@Dao
interface InspectablePartDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: InspectablePart): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<InspectablePart>)

    @Delete
    suspend fun delete(entity: InspectablePart)

    @Update
    suspend fun update(entity: InspectablePart)

    @Query("SELECT * FROM inspectable_parts WHERE service_id = :id")
    suspend fun getById(id: Long): InspectablePart?

    @Query("DELETE FROM inspectable_parts WHERE service_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM inspectable_parts")
    fun getAll(): Flow<List<InspectablePart>>
}