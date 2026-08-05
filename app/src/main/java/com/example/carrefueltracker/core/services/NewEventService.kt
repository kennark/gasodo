package com.example.carrefueltracker.core.services

import com.example.carrefueltracker.core.database.entity.InspectionEvent
import com.example.carrefueltracker.core.database.entity.MaintenanceEvent
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import com.example.carrefueltracker.core.database.repository.InspectionRepository
import com.example.carrefueltracker.core.database.repository.MaintenanceRepository
import com.example.carrefueltracker.core.database.repository.RefuelRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling all that is related to adding new events.
 * Validation, and storing.
 */
@Singleton
class NewEventService @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val inspectionRepository: InspectionRepository,
    private val maintenanceRepository: MaintenanceRepository
){

    fun addRefuelEvent(refuelEvent: RefuelEvent) {

    }

    fun addInspectionEvent(inspectionEvent: InspectionEvent) {

    }

    fun addMaintenanceEvent(maintenanceEvent: MaintenanceEvent) {

    }
}