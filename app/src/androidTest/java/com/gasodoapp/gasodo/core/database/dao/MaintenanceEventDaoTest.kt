package com.gasodoapp.gasodo.core.database.dao

import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.InMemoryDatabaseRule
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService
import com.gasodoapp.gasodo.core.database.insertReturning
import com.gasodoapp.gasodo.core.database.maintenanceEvent
import com.gasodoapp.gasodo.core.database.savedLocation
import com.gasodoapp.gasodo.core.database.serviceType
import com.gasodoapp.gasodo.core.enums.PaymentMethod
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Tests [MaintenanceEventDao], including the `@Relation` through `used_maintenance_services` and
 * the `ON DELETE CASCADE` behaviour of the junction table.
 */
class MaintenanceEventDaoTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule()

    private val db: AppDatabase get() = dbRule.database
    private val dao: MaintenanceEventDao get() = dbRule.database.maintenanceEventDao()
    private val usedServiceDao: UsedMaintenanceServiceDao get() = dbRule.database.usedMaintenanceServiceDao()

    @Test
    fun insert_andGetById_roundTripsTheEventIdColumn(): Unit = runBlocking {
        val location = savedLocation()
        db.savedLocationDao().insert(location)
        val event = maintenanceEvent(
            date = LocalDate.of(2025, 4, 2),
            mileage = 88_000L,
            savedLocationId = location.id,
            totalCost = BigDecimal("120.5"),
            paymentMethod = PaymentMethod.CASH,
            notes = "Brake pads",
        )

        dao.insert(event)
        val stored = dao.getById(event.id)

        assertThat(stored).isNotNull()
        assertThat(stored!!.id).isEqualTo(event.id)
        assertThat(stored.base.mileage).isEqualTo(88_000L)
        assertThat(stored.totalCost).isEqualTo(BigDecimal("120.500"))
        assertThat(stored.paymentMethod).isEqualTo(PaymentMethod.CASH)
    }

    @Test
    fun getByIdWithServiceTypes_resolvesTheManyToManyRelation(): Unit = runBlocking {
        val event = maintenanceEvent()
        val oil =
            db.maintenanceServiceTypeDao().insertReturning(serviceType(serviceName = "Oil Change"))
        val brakes = db.maintenanceServiceTypeDao()
            .insertReturning(serviceType(serviceName = "Brake Change"))

        dao.insert(event)
        usedServiceDao.insertAll(
            listOf(
                UsedMaintenanceService(maintenanceEventId = event.id, serviceId = oil.id),
                UsedMaintenanceService(maintenanceEventId = event.id, serviceId = brakes.id),
            )
        )

        val stored = dao.getByIdWithServiceTypes(event.id)

        assertThat(stored).isNotNull()
        assertThat(stored!!.event.id).isEqualTo(event.id)
        assertThat(stored.services.map { it.serviceName })
            .containsExactly("Oil Change", "Brake Change")
    }

    @Test
    fun getByIdWithServiceTypes_returnsAnEmptyListWhenThereAreNoJunctionRows(): Unit = runBlocking {
        val event = maintenanceEvent()
        dao.insert(event)

        val stored = dao.getByIdWithServiceTypes(event.id)

        assertThat(stored).isNotNull()
        assertThat(stored!!.services).isEmpty()
    }

    @Test
    fun getByIdWithServiceTypes_returnsNullForAnUnknownId(): Unit = runBlocking {
        assertThat(dao.getByIdWithServiceTypes(UUID.randomUUID())).isNull()
    }

    @Test
    fun deleteByIds_cascadesIntoTheJunctionTable(): Unit = runBlocking {
        val event = maintenanceEvent()
        val service = db.maintenanceServiceTypeDao().insertReturning(serviceType())
        dao.insert(event)
        usedServiceDao.insert(
            UsedMaintenanceService(
                maintenanceEventId = event.id,
                serviceId = service.id
            )
        )

        dao.deleteByIds(listOf(event.id))

        assertThat(dao.getById(event.id)).isNull()
        assertThat(usedServiceDao.countByServiceId(service.id)).isEqualTo(0)
    }

    @Test
    fun deletingTheEventCascadesIntoTheJunctionTable(): Unit = runBlocking {
        val event = maintenanceEvent()
        val service = db.maintenanceServiceTypeDao().insertReturning(serviceType())
        dao.insert(event)
        usedServiceDao.insert(
            UsedMaintenanceService(
                maintenanceEventId = event.id,
                serviceId = service.id
            )
        )

        dao.delete(event)

        assertThat(usedServiceDao.countByServiceId(service.id)).isEqualTo(0)
    }

    @Test
    fun getAllWithServiceTypesOrderByDate_returnsEachEventWithItsServices(): Unit = runBlocking {
        val oil =
            db.maintenanceServiceTypeDao().insertReturning(serviceType(serviceName = "Oil Change"))
        val older = maintenanceEvent(date = LocalDate.of(2025, 1, 1), mileage = 1_000L)
        val newer = maintenanceEvent(date = LocalDate.of(2025, 6, 1), mileage = 2_000L)
        dao.insertAll(listOf(older, newer))
        usedServiceDao.insert(
            UsedMaintenanceService(
                maintenanceEventId = older.id,
                serviceId = oil.id
            )
        )

        val result =
            dao.getAllInDateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2030, 1, 1)).first()

        assertThat(result.map { it.event.id }).containsExactly(newer.id, older.id).inOrder()
        assertThat(result.last().services.map { it.serviceName }).containsExactly("Oil Change")
    }

    @Test
    fun getAllInDateRange_isInclusiveOnBothBoundsAndOrderedByMileageDesc(): Unit = runBlocking {
        val start = LocalDate.of(2025, 2, 1)
        val end = LocalDate.of(2025, 2, 28)
        dao.insert(maintenanceEvent(date = start, mileage = 1_000L))
        dao.insert(maintenanceEvent(date = end, mileage = 3_000L))
        dao.insert(maintenanceEvent(date = LocalDate.of(2025, 2, 10), mileage = 2_000L))
        dao.insert(maintenanceEvent(date = LocalDate.of(2025, 1, 31), mileage = 9_000L))

        val result = dao.getAllInDateRange(start, end).first()

        assertThat(result.map { it.event.base.mileage }).containsExactly(3_000L, 2_000L, 1_000L)
            .inOrder()
    }

    @Test
    fun update_replacesTheStoredValues(): Unit = runBlocking {
        val event = maintenanceEvent(mileage = 1_000L)
        dao.insert(event)

        dao.update(event.copy(base = event.base.copy(mileage = 2_000L)))

        assertThat(dao.getById(event.id)!!.base.mileage).isEqualTo(2_000L)
    }

    @Test
    fun deleteByIds_withAnEmptyCollectionDeletesNothing(): Unit = runBlocking {
        val event = maintenanceEvent()
        dao.insert(event)

        dao.deleteByIds(emptyList())

        assertThat(dao.getById(event.id)).isNotNull()
    }
}
