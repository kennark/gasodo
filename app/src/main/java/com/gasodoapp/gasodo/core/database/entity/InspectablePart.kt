package com.gasodoapp.gasodo.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("inspectable_parts")
data class InspectablePart(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("service_id") var id: Long = 0,
    @ColumnInfo("service_name") val partName: String,
    @ColumnInfo("notes") val notes: String? = null
)
