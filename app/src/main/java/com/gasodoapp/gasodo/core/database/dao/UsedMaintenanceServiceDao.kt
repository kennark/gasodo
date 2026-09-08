package com.gasodoapp.gasodo.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService
import java.util.UUID

@Dao
interface UsedMaintenanceServiceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: UsedMaintenanceService): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<UsedMaintenanceService>)

    @Delete
    suspend fun delete(entity: UsedMaintenanceService)

    @Query("DELETE FROM used_maintenance_services WHERE event_id = :id")
    suspend fun deleteByEventId(id: UUID)
}