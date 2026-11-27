package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.InMemoryDatabaseRule
import com.gasodoapp.gasodo.core.database.insertReturning
import com.gasodoapp.gasodo.core.database.inspectablePart
import com.gasodoapp.gasodo.core.database.inspectionEvent
import com.gasodoapp.gasodo.core.database.maintenanceEvent
import com.gasodoapp.gasodo.core.database.refuelEvent
import com.gasodoapp.gasodo.core.database.savedLocation
import com.gasodoapp.gasodo.core.database.serviceType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

/**
 * Exercises the multi-statement repository logic against a real SQLite engine.
 *
 * The JVM repository tests mock the DAOs, so they cannot prove that
 * [MaintenanceRepositoryImpl.insertWithUsedServices] and its inspection counterpart behave
 * transactionally, nor that the junction tables really cascade.
 */
class RepositoryDatabaseTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule()

    private val db: AppDatabase get() = dbRule.database

    private fun maintenanceRepository() = MaintenanceRepositoryImpl(
        db = db,
        eventDao = db.maintenanceEventDao(),
        usedServiceDao = db.usedMaintenanceServiceDao(),
    )

    private fun inspectionRepository() = InspectionRepositoryImpl(
        db = db,
        dao = db.inspectionEventDao(),
        usedPartDao = db.usedInspectablePartDao(),
    )

    // region maintenance

    @Test
    fun insertWithUsedServices_persistsTheEventAndItsJunctionRows(): Unit = runBlocking {
        val repository = maintenanceRepository()
        val oil =
            db.maintenanceServiceTypeDao().insertReturning(serviceType(serviceName = "Oil Change"))
        val filter =
            db.maintenanceServiceTypeDao().insertReturning(serviceType(serviceName = "Air Filter"))
        val event = maintenanceEvent()

        repository.insertWithUsedServices(event, setOf(oil, filter))

        val stored = repository.getByIdWithServiceTypes(event.id)
        assertThat(stored).isNotNull()
        assertThat(stored!!.services.map { it.serviceName })
            .containsExactly("Oil Change", "Air Filter")
    }

    @Test
    fun update_replacesTheJunctionRowsInsteadOfDuplicatingThem(): Unit = runBlocking {
        val repository = maintenanceRepository()
        val oil =
            db.maintenanceServiceTypeDao().insertReturning(serviceType(serviceName = "Oil Change"))
        val filters =
            db.maintenanceServiceTypeDao().insertReturning(serviceType(serviceName = "Air Filter"))
        val event = maintenanceEvent()
        repository.insertWithUsedServices(event, setOf(oil))

        repository.update(event, setOf(filters))

        val stored = repository.getByIdWithServiceTypes(event.id)
        assertThat(stored!!.services.map { it.serviceName }).containsExactly("Air Filter")
    }

    @Test
    fun update_leavesTheJunctionRowsAloneWhenServicesAreNull(): Unit = runBlocking {
        val repository = maintenanceRepository()
        val oil =
            db.maintenanceServiceTypeDao().insertReturning(serviceType(serviceName = "Oil Change"))
        val event = maintenanceEvent()
        repository.insertWithUsedServices(event, setOf(oil))

        repository.update(event.copy(base = event.base.copy(mileage = 9_999L)), services = null)

        val stored = repository.getByIdWithServiceTypes(event.id)
        assertThat(stored!!.event.base.mileage).isEqualTo(9_999L)
        assertThat(stored.services).hasSize(1)
    }

    @Test
    fun deleteByIds_cascadesTheMaintenanceJunctionRows(): Unit = runBlocking {
        val repository = maintenanceRepository()
        val oil = db.maintenanceServiceTypeDao().insertReturning(serviceType())
        val event = maintenanceEvent()
        repository.insertWithUsedServices(event, setOf(oil))

        repository.deleteByIds(listOf(event.id))

        assertThat(repository.getById(event.id)).isNull()
        assertThat(db.usedMaintenanceServiceDao().countByServiceId(oil.id)).isEqualTo(0)
    }

    @Test
    fun deleteByIds_withAnEmptyCollectionDoesNotHitTheDao(): Unit = runBlocking {
        val repository = maintenanceRepository()
        val event = maintenanceEvent()
        db.maintenanceEventDao().insert(event)

        repository.deleteByIds(emptyList())

        assertThat(repository.getById(event.id)).isNotNull()
    }

    @Test
    fun getAllWithinTime_returnsEventsWithTheirServices(): Unit = runBlocking {
        val repository = maintenanceRepository()
        val oil =
            db.maintenanceServiceTypeDao().insertReturning(serviceType(serviceName = "Oil Change"))
        val event = maintenanceEvent(date = LocalDate.of(2025, 3, 15))
        repository.insertWithUsedServices(event, setOf(oil))

        val result = repository.getAllWithinTime(
            start = LocalDate.of(2025, 3, 1),
            end = LocalDate.of(2025, 3, 31),
        ).first()

        assertThat(result).hasSize(1)
        assertThat(result.single().services.map { it.serviceName }).containsExactly("Oil Change")
    }

    // endregion

    // region inspection

    @Test
    fun insertWithUsedParts_persistsTheEventAndItsJunctionRows(): Unit = runBlocking {
        val repository = inspectionRepository()
        val brakes = db.inspectablePartDao().insertReturning(inspectablePart(partName = "Brakes"))
        val tires = db.inspectablePartDao().insertReturning(inspectablePart(partName = "Tires"))
        val event = inspectionEvent()

        repository.insertWithUsedParts(event, setOf(brakes, tires))

        val stored = repository.getByIdWithParts(event.id)
        assertThat(stored).isNotNull()
        assertThat(stored!!.parts.map { it.partName }).containsExactly("Brakes", "Tires")
    }

    @Test
    fun update_replacesTheInspectionJunctionRowsInsteadOfDuplicatingThem(): Unit = runBlocking {
        val repository = inspectionRepository()
        val brakes = db.inspectablePartDao().insertReturning(inspectablePart(partName = "Brakes"))
        val tires = db.inspectablePartDao().insertReturning(inspectablePart(partName = "Tires"))
        val event = inspectionEvent()
        repository.insertWithUsedParts(event, setOf(brakes))

        repository.update(event, setOf(tires))

        val stored = repository.getByIdWithParts(event.id)
        assertThat(stored!!.parts.map { it.partName }).containsExactly("Tires")
    }

    @Test
    fun deleteByIds_cascadesTheInspectionJunctionRows(): Unit = runBlocking {
        val repository = inspectionRepository()
        val brakes = db.inspectablePartDao().insertReturning(inspectablePart())
        val event = inspectionEvent()
        repository.insertWithUsedParts(event, setOf(brakes))

        repository.deleteByIds(listOf(event.id))

        assertThat(repository.getById(event.id)).isNull()
        assertThat(db.usedInspectablePartDao().countByPartId(brakes.id)).isEqualTo(0)
    }

    // endregion

    // region saved location

    @Test
    fun savedLocationRepository_countUsage_bridgesUuidAndString(): Unit = runBlocking {
        val repository = SavedLocationRepositoryImpl(db.savedLocationDao())
        val location = savedLocation()
        repository.insert(location)
        db.refuelEventDao().upsert(refuelEvent(savedLocationId = location.id))
        db.maintenanceEventDao().insert(maintenanceEvent(savedLocationId = location.id))

        assertThat(repository.countUsage(location.id)).isEqualTo(2)
        assertThat(repository.countUsage(UUID.randomUUID())).isEqualTo(0)
    }

    // endregion
}
