package com.gasodoapp.gasodo.core.database.dao

import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.InMemoryDatabaseRule
import com.gasodoapp.gasodo.core.database.entity.UsedMaintenanceService
import com.gasodoapp.gasodo.core.database.maintenanceEvent
import com.gasodoapp.gasodo.core.database.serviceType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

/**
 * Tests [MaintenanceServiceTypeDao] and the [UsedMaintenanceServiceDao] counts that back the
 * "in use" guard in the Manage screens.
 */
class MaintenanceServiceTypeDaoTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule()

    private val db: AppDatabase get() = dbRule.database
    private val dao: MaintenanceServiceTypeDao get() = dbRule.database.maintenanceServiceTypeDao()
    private val usedServiceDao: UsedMaintenanceServiceDao get() = dbRule.database.usedMaintenanceServiceDao()

    @Test
    fun insert_returnsTheGeneratedServiceIdAndGetByIdReadsItBack(): Unit = runBlocking {
        val id = dao.insert(serviceType(serviceName = "Tire Rotation", notes = "every 10k km"))

        val stored = dao.getById(id)

        assertThat(id).isGreaterThan(0L)
        assertThat(stored).isNotNull()
        assertThat(stored!!.serviceName).isEqualTo("Tire Rotation")
        assertThat(stored.notes).isEqualTo("every 10k km")
    }

    @Test
    fun insert_withAnExplicitIdIsHonoured(): Unit = runBlocking {
        val id = dao.insert(serviceType(serviceName = "Pinned").copy(id = 500L))

        assertThat(id).isEqualTo(500L)
        assertThat(dao.getById(500L)).isNotNull()
    }

    @Test
    fun getAll_returnsEveryServiceType(): Unit = runBlocking {
        dao.insertAll(listOf(serviceType(serviceName = "A"), serviceType(serviceName = "B")))

        assertThat(dao.getAll().first()).hasSize(2)
    }

    @Test
    fun update_replacesTheStoredValues(): Unit = runBlocking {
        val id = dao.insert(serviceType(serviceName = "Old"))
        val stored = dao.getById(id)!!

        dao.update(stored.copy(serviceName = "New"))

        assertThat(dao.getById(id)!!.serviceName).isEqualTo("New")
    }

    @Test
    fun deleteById_removesTheRow(): Unit = runBlocking {
        val id = dao.insert(serviceType())

        dao.deleteById(id)

        assertThat(dao.getById(id)).isNull()
    }

    @Test
    fun getById_returnsNullForAnUnknownId(): Unit = runBlocking {
        assertThat(dao.getById(-1L)).isNull()
    }

    // region junction counts

    @Test
    fun countByServiceId_countsTheJunctionRows(): Unit = runBlocking {
        val id = dao.insert(serviceType())
        val first = maintenanceEvent()
        val second = maintenanceEvent()
        db.maintenanceEventDao().insertAll(listOf(first, second))
        usedServiceDao.insertAll(
            listOf(
                UsedMaintenanceService(maintenanceEventId = first.id, serviceId = id),
                UsedMaintenanceService(maintenanceEventId = second.id, serviceId = id),
            ),
        )

        assertThat(usedServiceDao.countByServiceId(id)).isEqualTo(2)
    }

    @Test
    fun countByServiceId_returnsZeroWhenTheServiceIsUnused(): Unit = runBlocking {
        val id = dao.insert(serviceType())

        assertThat(usedServiceDao.countByServiceId(id)).isEqualTo(0)
    }

    @Test
    fun deleteByEventId_clearsOnlyThatEventsJunctionRows(): Unit = runBlocking {
        val keptService = dao.insert(serviceType(serviceName = "Kept"))
        val droppedService = dao.insert(serviceType(serviceName = "Dropped"))
        val first = maintenanceEvent()
        val second = maintenanceEvent()
        db.maintenanceEventDao().insertAll(listOf(first, second))
        usedServiceDao.insertAll(
            listOf(
                UsedMaintenanceService(maintenanceEventId = first.id, serviceId = droppedService),
                UsedMaintenanceService(maintenanceEventId = second.id, serviceId = keptService),
            )
        )

        usedServiceDao.deleteByEventId(first.id)

        assertThat(usedServiceDao.countByServiceId(droppedService)).isEqualTo(0)
        assertThat(usedServiceDao.countByServiceId(keptService)).isEqualTo(1)
    }

    // endregion
}
