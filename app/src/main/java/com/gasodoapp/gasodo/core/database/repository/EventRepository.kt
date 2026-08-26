package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.projections.DateMileage

/**
 * Repository interface for reading common data of all events.
 */
interface EventRepository {

    suspend fun getDateWithHigherMileage(mileage: Long): DateMileage?
    suspend fun getDateWithLowerMileage(mileage: Long): DateMileage?
}