package com.example.carrefueltracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.carrefueltracker.core.database.dao.InspectionEventDao
import com.example.carrefueltracker.core.database.dao.MaintenanceEventDao
import com.example.carrefueltracker.core.database.dao.RefuelEventDao
import com.example.carrefueltracker.core.database.dao.SavedLocationDao
import com.example.carrefueltracker.core.database.entity.InspectionEvent
import com.example.carrefueltracker.core.database.entity.MaintenanceEvent
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import com.example.carrefueltracker.core.database.entity.SavedLocation
import com.example.carrefueltracker.core.utils.ConverterUtils

@Database(
    entities = [
        SavedLocation::class,
        RefuelEvent::class,
        MaintenanceEvent::class,
        InspectionEvent::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(ConverterUtils::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun refuelEventDao(): RefuelEventDao
    abstract fun maintenanceEventDao(): MaintenanceEventDao
    abstract fun inspectionEventDao(): InspectionEventDao
}
