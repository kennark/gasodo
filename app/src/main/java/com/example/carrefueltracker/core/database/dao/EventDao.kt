package com.example.carrefueltracker.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.carrefueltracker.core.database.projections.DateMileage

/**
 * Data Access Object for Event view.
 */
@Dao
interface EventDao {

    @Query("SELECT date, mileage FROM alleventsbasecolumnsview WHERE mileage >= :mileage ORDER BY mileage ASC LIMIT 1")
    suspend fun getDateWithHigherMileage(mileage: Long): DateMileage?

    @Query("SELECT date, mileage FROM alleventsbasecolumnsview WHERE mileage <= :mileage ORDER BY mileage DESC LIMIT 1")
    suspend fun getDateWithLowerMileage(mileage: Long): DateMileage?
}