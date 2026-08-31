package com.gasodoapp.gasodo.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity representing a saved location (e.g., gas station, repair shop).
 */
@Entity(tableName = "saved_locations")
data class SavedLocation(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    @ColumnInfo("name") val name: String,
    @ColumnInfo("latitude") val latitude: Double? = null,
    @ColumnInfo("longitude") val longitude: Double? = null,
    @ColumnInfo("address") val address: String? = null
)
