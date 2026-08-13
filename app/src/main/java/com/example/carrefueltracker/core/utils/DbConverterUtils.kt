package com.example.carrefueltracker.core.utils

import androidx.room.TypeConverter
import java.math.BigDecimal
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

    // List<String> ↔ String (comma-separated)
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> = value.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? =
        value?.setScale(3, BigDecimalUtils.ROUNDING_MODE)?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.toBigDecimalOrNull()
}