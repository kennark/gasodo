package com.gasodoapp.gasodo.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gasodoapp.gasodo.core.database.entity.UsedInspectablePart
import java.util.UUID

@Dao
interface UsedInspectablePartDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: UsedInspectablePart): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<UsedInspectablePart>)

    @Delete
    suspend fun delete(entity: UsedInspectablePart)

    @Query("DELETE FROM used_inspectable_parts WHERE inspection_event_id = :id")
    suspend fun deleteByInspectionEventId(id: UUID)
}