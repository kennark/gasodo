package com.gasodoapp.gasodo.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Number of milliseconds in a day, used to convert the legacy stored representation.
 */
private const val MILLIS_PER_DAY = 86_400_000L

/**
 * Converts the `date` column of the event tables from UTC-midnight epoch milliseconds to
 * epoch days.
 *
 * Every stored value was written from the Material3 date pickers, which always represent a
 * selected day as midnight UTC, so each value is an exact multiple of [MILLIS_PER_DAY] and the
 * division is lossless.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf("refuel_events", "maintenance_events", "inspection_events").forEach { table ->
            db.execSQL(
                "UPDATE $table SET date = date / $MILLIS_PER_DAY WHERE date IS NOT NULL"
            )
        }
    }
}
