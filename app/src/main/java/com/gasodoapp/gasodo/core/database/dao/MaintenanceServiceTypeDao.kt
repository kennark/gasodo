package com.gasodoapp.gasodo.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Maintenance Service Types.
 */
@Dao
interface MaintenanceServiceTypeDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MaintenanceServiceType): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<MaintenanceServiceType>)

    @Delete
    suspend fun delete(entity: MaintenanceServiceType)

    @Update
    suspend fun update(entity: MaintenanceServiceType)

    @Query("SELECT * FROM maintenance_service_types WHERE service_id = :id")
    suspend fun getById(id: Long): MaintenanceServiceType?

    @Query("DELETE FROM maintenance_service_types WHERE service_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM maintenance_service_types")
    fun getAll(): Flow<List<MaintenanceServiceType>>
}