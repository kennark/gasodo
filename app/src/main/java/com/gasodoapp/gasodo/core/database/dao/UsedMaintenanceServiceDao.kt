package com.gasodoapp.gasodo.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService

@Dao
interface UsedMaintenanceServiceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: UsedMaintenanceService): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<UsedMaintenanceService>)

    @Delete
    suspend fun delete(entity: UsedMaintenanceService)
}