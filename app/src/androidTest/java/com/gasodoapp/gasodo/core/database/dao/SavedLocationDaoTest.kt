package com.gasodoapp.gasodo.core.database.dao

import android.database.sqlite.SQLiteConstraintException
import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.InMemoryDatabaseRule
import com.gasodoapp.gasodo.core.database.inspectionEvent
import com.gasodoapp.gasodo.core.database.maintenanceEvent
import com.gasodoapp.gasodo.core.database.refuelEvent
import com.gasodoapp.gasodo.core.database.savedLocation
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.util.UUID

/**
 * Tests [SavedLocationDao]: CRUD, ordering and the `countUsage` union that guards deletes.
 */
class SavedLocationDaoTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule()

    private val db: AppDatabase get() = dbRule.database
    private val dao: SavedLocationDao get() = dbRule.database.savedLocationDao()

    @Test
    fun insert_andGetById_roundTripsEveryColumn(): Unit = runBlocking {
        val location = savedLocation(
            name = "Repair shop",
            latitude = 59.4370,
            longitude = 24.7536,
            address = "Tartu mnt 1",
        )

        dao.insert(location)
        val stored = dao.getById(location.id.toString())

        assertThat(stored).isEqualTo(location)
        assertThat(stored!!.latitude).isEqualTo(59.4370)
        assertThat(stored.address).isEqualTo("Tartu mnt 1")
    }

    @Test
    fun getAll_ordersByNameAscending(): Unit = runBlocking {
        dao.insertAll(
            listOf(
                savedLocation(name = "Circle K"),
                savedLocation(name = "Alexela"),
                savedLocation(name = "Shell"),
            ),
        )

        val names = dao.getAll().first().map { it.name }

        assertThat(names).containsExactly("Alexela", "Circle K", "Shell").inOrder()
    }

    @Test
    fun getAll_reEmitsAfterAnInsert(): Unit = runBlocking {
        assertThat(dao.getAll().first()).isEmpty()

        dao.insert(savedLocation(name = "Shell"))

        assertThat(dao.getAll().first()).hasSize(1)
    }

    @Test
    fun insert_throwsWhenTheIdAlreadyExists(): Unit = runBlocking {
        val location = savedLocation()
        dao.insert(location)

        val error = runCatching { dao.insert(location) }.exceptionOrNull()

        assertThat(error).isInstanceOf(SQLiteConstraintException::class.java)
    }

    @Test
    fun update_replacesTheStoredValues(): Unit = runBlocking {
        val location = savedLocation(name = "Old name")
        dao.insert(location)

        dao.update(location.copy(name = "New name", address = "Somewhere"))

        val stored = dao.getById(location.id.toString())
        assertThat(stored!!.name).isEqualTo("New name")
        assertThat(stored.address).isEqualTo("Somewhere")
    }

    @Test
    fun deleteById_removesTheRow(): Unit = runBlocking {
        val location = savedLocation()
        dao.insert(location)

        dao.deleteById(location.id.toString())

        assertThat(dao.getById(location.id.toString())).isNull()
    }

    @Test
    fun getById_returnsNullForAnUnknownId(): Unit = runBlocking {
        assertThat(dao.getById(UUID.randomUUID().toString())).isNull()
    }

    // region countUsage

    @Test
    fun countUsage_sumsReferencesAcrossAllThreeEventTables(): Unit = runBlocking {
        val location = savedLocation()
        dao.insert(location)
        db.refuelEventDao().upsert(refuelEvent(savedLocationId = location.id))
        db.maintenanceEventDao().insert(maintenanceEvent(savedLocationId = location.id))
        db.inspectionEventDao().insert(inspectionEvent(savedLocationId = location.id))
        db.inspectionEventDao().insert(inspectionEvent(savedLocationId = location.id))

        assertThat(dao.countUsage(location.id.toString())).isEqualTo(4)
    }

    @Test
    fun countUsage_countsOnlyTheGivenLocation(): Unit = runBlocking {
        val used = savedLocation(name = "Used")
        val unused = savedLocation(name = "Unused")
        dao.insertAll(listOf(used, unused))
        db.refuelEventDao().upsert(refuelEvent(savedLocationId = used.id))

        assertThat(dao.countUsage(used.id.toString())).isEqualTo(1)
        assertThat(dao.countUsage(unused.id.toString())).isEqualTo(0)
    }

    @Test
    fun countUsage_returnsZeroForAnUnknownLocation(): Unit = runBlocking {
        assertThat(dao.countUsage(UUID.randomUUID().toString())).isEqualTo(0)
    }

    @Test
    fun countUsage_dropsToZeroAfterTheReferencingEventsAreDeleted(): Unit = runBlocking {
        val location = savedLocation()
        dao.insert(location)
        val event = refuelEvent(savedLocationId = location.id)
        db.refuelEventDao().upsert(event)

        db.refuelEventDao().deleteById(event.id)

        assertThat(dao.countUsage(location.id.toString())).isEqualTo(0)
    }

    // endregion
}
