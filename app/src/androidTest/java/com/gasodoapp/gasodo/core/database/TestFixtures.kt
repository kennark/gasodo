package com.gasodoapp.gasodo.core.database

import com.gasodoapp.gasodo.core.database.dao.InspectablePartDao
import com.gasodoapp.gasodo.core.database.dao.MaintenanceServiceTypeDao
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.entity.RefuelEvent
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Builders used by the database tests, so each test only spells out the values it asserts on.
 */

internal val DEFAULT_LOCAL_DATE: LocalDate = LocalDate.of(2025, 1, 1)

fun baseColumns(
    date: LocalDate? = DEFAULT_LOCAL_DATE,
    mileage: Long? = 1_000L,
    savedLocationId: UUID? = null,
    photoUris: List<String> = emptyList(),
    notes: String = "",
): BaseColumns = BaseColumns(
    date = date,
    mileage = mileage,
    savedLocationId = savedLocationId,
    photoUris = photoUris,
    notes = notes,
)

fun refuelEvent(
    id: UUID = UUID.randomUUID(),
    date: LocalDate? = DEFAULT_LOCAL_DATE,
    mileage: Long? = 1_000L,
    savedLocationId: UUID? = null,
    amountLiters: BigDecimal? = null,
    pricePerLiter: BigDecimal? = null,
    totalCost: BigDecimal? = null,
    paymentMethod: PaymentMethod? = null,
    fullFillUp: Boolean = true,
    photoUris: List<String> = emptyList(),
    notes: String = "",
): RefuelEvent = RefuelEvent(
    id = id,
    base = baseColumns(date, mileage, savedLocationId, photoUris, notes),
    amountLiters = amountLiters,
    pricePerLiter = pricePerLiter,
    totalCost = totalCost,
    paymentMethod = paymentMethod,
    fullFillUp = fullFillUp,
)

fun maintenanceEvent(
    id: UUID = UUID.randomUUID(),
    date: LocalDate? = DEFAULT_LOCAL_DATE,
    mileage: Long? = 1_000L,
    savedLocationId: UUID? = null,
    totalCost: BigDecimal? = null,
    paymentMethod: PaymentMethod? = null,
    photoUris: List<String> = emptyList(),
    notes: String = "",
): MaintenanceEvent = MaintenanceEvent(
    id = id,
    base = baseColumns(date, mileage, savedLocationId, photoUris, notes),
    totalCost = totalCost,
    paymentMethod = paymentMethod,
)

fun inspectionEvent(
    id: UUID = UUID.randomUUID(),
    date: LocalDate? = DEFAULT_LOCAL_DATE,
    mileage: Long? = 1_000L,
    savedLocationId: UUID? = null,
    status: InspectionStatus? = null,
    findings: List<String> = emptyList(),
    photoUris: List<String> = emptyList(),
    notes: String = "",
): InspectionEvent = InspectionEvent(
    id = id,
    base = baseColumns(date, mileage, savedLocationId, photoUris, notes),
    status = status,
    findings = findings,
)

fun savedLocation(
    id: UUID = UUID.randomUUID(),
    name: String = "Station",
    latitude: Double? = null,
    longitude: Double? = null,
    address: String? = null,
): SavedLocation = SavedLocation(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    address = address,
)

fun serviceType(
    serviceName: String = "Oil Change",
    notes: String? = null,
): MaintenanceServiceType = MaintenanceServiceType(
    serviceName = serviceName,
    notes = notes,
)

fun inspectablePart(
    partName: String = "Brakes",
    notes: String? = null,
): InspectablePart = InspectablePart(
    partName = partName,
    notes = notes,
)

/**
 * Persists [entity] and returns a copy carrying the auto-generated id.
 *
 * `insertAll` does not write the generated ids back into the passed entities, so a fixture that
 * is only inserted through `insertAll` still has `id = 0` and cannot be referenced from a
 * junction table. Always use this helper when the id is needed afterwards.
 */
suspend fun MaintenanceServiceTypeDao.insertReturning(
    entity: MaintenanceServiceType,
): MaintenanceServiceType = entity.copy(id = insert(entity))

/**
 * Persists [entity] and returns a copy carrying the auto-generated id.
 *
 * See [MaintenanceServiceTypeDao.insertReturning] for why this is needed.
 */
suspend fun InspectablePartDao.insertReturning(
    entity: InspectablePart,
): InspectablePart = entity.copy(id = insert(entity))
