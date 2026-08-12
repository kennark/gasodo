package com.example.carrefueltracker.core.utils

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

/**
 * Type converters for Room to handle types that are not directly supported.
 */
class DbConverterUtils {

    // UUID ↔ String
    @TypeConverter
    fun fromUuid(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUuid(value: String?): UUID? = value?.let { UUID.fromString(it) }

    // LocalDate ↔ Long (epoch day)
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toDateFromLong(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    // LocalTime ↔ Long (nano of day)
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): Long? = time?.toNanoOfDay()

    @TypeConverter
    fun toTimeFromLong(value: Long?): LocalTime? = value?.let { LocalTime.ofNanoOfDay(it) }

    // List<String> ↔ String (comma-separated)
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> = value.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.setScale(3)?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }
}