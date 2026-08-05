package com.example.carrefueltracker.core.database

import androidx.room.ColumnInfo
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class BaseColumns(
    @ColumnInfo("date") val date: Long? = null,
    @ColumnInfo("mileage") val mileage: Long? = null,
    @ColumnInfo("saved_location_id") val savedLocationId: UUID? = null,
    @ColumnInfo("photo_uris") val photoUris: List<String> = emptyList(),
    @ColumnInfo("notes") val notes: String = "",
)