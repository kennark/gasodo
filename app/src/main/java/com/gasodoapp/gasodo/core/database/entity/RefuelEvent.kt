package com.gasodoapp.gasodo.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import java.math.BigDecimal
import java.util.UUID

/**
 * Room entity for refueling events.
 */
@Entity(
    tableName = "refuel_events",
    foreignKeys = [
        ForeignKey(
            entity = SavedLocation::class,
            parentColumns = ["id"],
            childColumns = ["saved_location_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("saved_location_id")]
)
data class RefuelEvent(
    @PrimaryKey var id: UUID = UUID.randomUUID(),
    @Embedded val base: BaseColumns,

    @ColumnInfo("amount_liters") val amountLiters: BigDecimal?,
    @ColumnInfo("price_per_liter") val pricePerLiter: BigDecimal?,
    @ColumnInfo("total_cost") val totalCost: BigDecimal?,
    @ColumnInfo("payment_method") val paymentMethod: PaymentMethod?,
    @ColumnInfo("full_fill_up") val fullFillUp: Boolean
)
