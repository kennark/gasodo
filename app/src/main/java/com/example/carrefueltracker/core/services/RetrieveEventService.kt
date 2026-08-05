package com.example.carrefueltracker.core.services

import com.example.carrefueltracker.core.database.repository.InspectionRepository
import com.example.carrefueltracker.core.database.repository.MaintenanceRepository
import com.example.carrefueltracker.core.database.repository.RefuelRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for retrieving old events of all types.
 */
@Singleton
class RetrieveEventService @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val inspectionRepository: InspectionRepository
){

}