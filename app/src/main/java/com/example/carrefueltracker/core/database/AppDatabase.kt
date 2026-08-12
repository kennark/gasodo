package com.example.carrefueltracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.carrefueltracker.core.database.dao.EventDao
import com.example.carrefueltracker.core.database.dao.InspectionEventDao
import com.example.carrefueltracker.core.database.dao.MaintenanceEventDao
import com.example.carrefueltracker.core.database.dao.RefuelEventDao
import com.example.carrefueltracker.core.database.dao.SavedLocationDao
import com.example.carrefueltracker.core.database.entity.InspectionEvent
import com.example.carrefueltracker.core.database.entity.MaintenanceEvent
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import com.example.carrefueltracker.core.database.entity.SavedLocation
import com.example.carrefueltracker.core.database.views.AllEventsBaseColumnsView
import com.example.carrefueltracker.core.utils.DbConverterUtils

@Database(
    entities = [
        SavedLocation::class,
        RefuelEvent::class,
        MaintenanceEvent::class,
        InspectionEvent::class
    ],
    views = [
        AllEventsBaseColumnsView::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(DbConverterUtils::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun refuelEventDao(): RefuelEventDao
    abstract fun maintenanceEventDao(): MaintenanceEventDao
    abstract fun inspectionEventDao(): InspectionEventDao
    abstract fun eventDao(): EventDao
}
