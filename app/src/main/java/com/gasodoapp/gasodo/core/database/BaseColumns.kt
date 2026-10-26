package com.gasodoapp.gasodo.core.database

import androidx.room.ColumnInfo
import java.time.LocalDate
import java.util.UUID

data class BaseColumns(
    @ColumnInfo("date") val date: LocalDate? = null,
    @ColumnInfo("mileage") val mileage: Long? = null,
    @ColumnInfo("saved_location_id") val savedLocationId: UUID? = null,
    @ColumnInfo("photo_uris") val photoUris: List<String> = emptyList(),
    @ColumnInfo("notes") val notes: String = "",
)