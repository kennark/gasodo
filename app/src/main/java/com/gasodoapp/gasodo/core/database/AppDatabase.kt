package com.gasodoapp.gasodo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gasodoapp.gasodo.core.database.dao.EventDao
import com.gasodoapp.gasodo.core.database.dao.InspectionEventDao
import com.gasodoapp.gasodo.core.database.dao.MaintenanceEventDao
import com.gasodoapp.gasodo.core.database.dao.MaintenanceServiceTypeDao
import com.gasodoapp.gasodo.core.database.dao.RefuelEventDao
import com.gasodoapp.gasodo.core.database.dao.SavedLocationDao
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService
import com.gasodoapp.gasodo.core.database.views.AllEventsBaseColumnsView
import com.gasodoapp.gasodo.core.utils.DbConverterUtils

@Database(
    entities = [
        SavedLocation::class,
        RefuelEvent::class,
        MaintenanceEvent::class,
        InspectionEvent::class,
        MaintenanceServiceType::class,
        UsedMaintenanceService::class
    ],
    views = [
        AllEventsBaseColumnsView::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(DbConverterUtils::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun refuelEventDao(): RefuelEventDao
    abstract fun maintenanceEventDao(): MaintenanceEventDao
    abstract fun inspectionEventDao(): InspectionEventDao
    abstract fun eventDao(): EventDao
    abstract fun maintenanceServiceTypeDao(): MaintenanceServiceTypeDao
}
