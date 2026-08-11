package com.example.carrefueltracker.core.database.repository

import com.example.carrefueltracker.core.database.dao.EventDao
import com.example.carrefueltracker.core.database.projections.DateMileage
import javax.inject.Inject

/**
 * Implementation of [EventRepository] using Room DAO.
 */
class EventRepositoryImpl @Inject constructor(
    private val dao: EventDao
) : EventRepository {


    override suspend fun getDateWithHigherMileage(mileage: Long): DateMileage? =
        dao.getDateWithHigherMileage(mileage)

    override suspend fun getDateWithLowerMileage(mileage: Long): DateMileage? =
        dao.getDateWithLowerMileage(mileage)
}