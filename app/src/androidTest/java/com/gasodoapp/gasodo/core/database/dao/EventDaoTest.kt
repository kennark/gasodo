package com.gasodoapp.gasodo.core.database.dao

import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.InMemoryDatabaseRule
import com.gasodoapp.gasodo.core.database.inspectionEvent
import com.gasodoapp.gasodo.core.database.maintenanceEvent
import com.gasodoapp.gasodo.core.database.refuelEvent
import com.gasodoapp.gasodo.core.enums.EventType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Tests [EventDao], which is the only DAO reading the hand written
 * [com.gasodoapp.gasodo.core.database.views.AllEventsBaseColumnsView] `UNION ALL` view.
 *
 * The view unions the three event tables, so every expectation here spans multiple tables.
 */
class EventDaoTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule()

    private val db: AppDatabase get() = dbRule.database
    private val dao: EventDao get() = dbRule.database.eventDao()

    // region getHighestMileage

    @Test
    fun getHighestMileage_returnsMaximumAcrossAllThreeEventTables(): Unit = runBlocking {
        insertRefuel(mileage = 1_000L, date = LocalDate.of(2025, 1, 1))
        insertMaintenance(mileage = 5_000L, date = LocalDate.of(2025, 3, 1))
        insertInspection(mileage = 9_000L, date = LocalDate.of(2025, 6, 1))

        assertThat(dao.getHighestMileage()).isEqualTo(9_000L)
    }

    @Test
    fun getHighestMileage_returnsValueOfTheOnlyEvent(): Unit = runBlocking {
        insertMaintenance(mileage = 42_000L)

        assertThat(dao.getHighestMileage()).isEqualTo(42_000L)
    }

    @Test
    fun getHighestMileage_returnsNullWhenThereAreNoEvents(): Unit = runBlocking {
        assertThat(dao.getHighestMileage()).isNull()
    }

    @Test
    fun getHighestMileage_ignoresEventsWithNullMileage(): Unit = runBlocking {
        insertRefuel(mileage = null)
        insertMaintenance(mileage = 2_500L)

        assertThat(dao.getHighestMileage()).isEqualTo(2_500L)
    }

    @Test
    fun getHighestMileage_returnsNullWhenEveryMileageIsNull(): Unit = runBlocking {
        insertRefuel(mileage = null)
        insertInspection(mileage = null)

        assertThat(dao.getHighestMileage()).isNull()
    }

    // endregion

    // region getDateWithHigherMileage

    @Test
    fun getDateWithHigherMileage_returnsClosestHigherMileageAcrossTables(): Unit = runBlocking {
        insertRefuel(mileage = 1_000L, date = LocalDate.of(2025, 1, 1))
        insertMaintenance(mileage = 6_000L, date = LocalDate.of(2025, 4, 1))
        insertInspection(mileage = 9_000L, date = LocalDate.of(2025, 6, 1))

        val result = dao.getDateWithHigherMileage(5_000L)

        assertThat(result).isNotNull()
        assertThat(result!!.mileage).isEqualTo(6_000L)
        assertThat(result.date).isEqualTo(LocalDate.of(2025, 4, 1))
    }

    @Test
    fun getDateWithHigherMileage_isStrictlyGreaterAndSkipsAnEqualMileage(): Unit = runBlocking {
        insertRefuel(mileage = 5_000L, date = LocalDate.of(2025, 2, 1))
        insertMaintenance(mileage = 5_500L, date = LocalDate.of(2025, 3, 1))

        val result = dao.getDateWithHigherMileage(5_000L)

        assertThat(result!!.mileage).isEqualTo(5_500L)
    }

    @Test
    fun getDateWithHigherMileage_breaksMileageTieByEarliestDate(): Unit = runBlocking {
        insertRefuel(mileage = 6_000L, date = LocalDate.of(2025, 4, 1))
        insertInspection(mileage = 6_000L, date = LocalDate.of(2025, 2, 1))

        val result = dao.getDateWithHigherMileage(5_000L)

        assertThat(result!!.mileage).isEqualTo(6_000L)
        assertThat(result.date).isEqualTo(LocalDate.of(2025, 2, 1))
    }

    @Test
    fun getDateWithHigherMileage_returnsNullWhenNothingIsHigher(): Unit = runBlocking {
        insertRefuel(mileage = 5_000L)
        insertInspection(mileage = 4_000L)

        assertThat(dao.getDateWithHigherMileage(5_000L)).isNull()
    }

    // endregion

    // region getDateWithLowerMileage

    @Test
    fun getDateWithLowerMileage_returnsClosestLowerMileageAcrossTables(): Unit = runBlocking {
        insertRefuel(mileage = 1_000L, date = LocalDate.of(2025, 1, 1))
        insertMaintenance(mileage = 4_000L, date = LocalDate.of(2025, 3, 1))
        insertInspection(mileage = 9_000L, date = LocalDate.of(2025, 6, 1))

        val result = dao.getDateWithLowerMileage(5_000L)

        assertThat(result).isNotNull()
        assertThat(result!!.mileage).isEqualTo(4_000L)
        assertThat(result.date).isEqualTo(LocalDate.of(2025, 3, 1))
    }

    @Test
    fun getDateWithLowerMileage_isStrictlyLowerAndSkipsAnEqualMileage(): Unit = runBlocking {
        insertRefuel(mileage = 5_000L, date = LocalDate.of(2025, 2, 1))
        insertMaintenance(mileage = 4_500L, date = LocalDate.of(2025, 1, 15))

        val result = dao.getDateWithLowerMileage(5_000L)

        assertThat(result!!.mileage).isEqualTo(4_500L)
    }

    @Test
    fun getDateWithLowerMileage_breaksMileageTieByLatestDate(): Unit = runBlocking {
        insertRefuel(mileage = 2_000L, date = LocalDate.of(2025, 1, 1))
        insertInspection(mileage = 2_000L, date = LocalDate.of(2025, 1, 20))

        val result = dao.getDateWithLowerMileage(5_000L)

        assertThat(result!!.mileage).isEqualTo(2_000L)
        assertThat(result.date).isEqualTo(LocalDate.of(2025, 1, 20))
    }

    @Test
    fun getDateWithLowerMileage_returnsNullWhenNothingIsLower(): Unit = runBlocking {
        insertRefuel(mileage = 5_000L)
        insertInspection(mileage = 6_000L)

        assertThat(dao.getDateWithLowerMileage(5_000L)).isNull()
    }

    // endregion

    // region the union view itself

    @Test
    fun view_exposesOneRowPerEventAcrossTheThreeEventTables(): Unit = runBlocking {
        insertRefuel(mileage = 1_000L)
        insertRefuel(mileage = 1_100L)
        insertMaintenance(mileage = 2_000L)
        insertInspection(mileage = 3_000L)

        assertThat(viewRows()).hasSize(4)
    }

    @Test
    fun view_mapsEachTableToItsEventTypeAndIdColumn(): Unit = runBlocking {
        val refuel = refuelEvent(mileage = 1_000L)
        val maintenance = maintenanceEvent(mileage = 2_000L)
        val inspection = inspectionEvent(mileage = 3_000L)
        db.refuelEventDao().upsert(refuel)
        db.maintenanceEventDao().insert(maintenance)
        db.inspectionEventDao().insert(inspection)

        val rows = viewRows()

        assertThat(rows.map { it.eventType })
            .containsExactly("REFUEL", "MAINTENANCE", "INSPECTION")
            .inOrder()
        // `maintenance_events` keeps its primary key in `event_id`; the view normalises it to `id`.
        assertThat(rows.map { it.id })
            .containsExactly(
                refuel.id.toString(),
                maintenance.id.toString(),
                inspection.id.toString()
            )
            .inOrder()
    }

    @Test
    fun view_eventTypeLiteralsConvertBackToTheEnum(): Unit = runBlocking {
        insertRefuel(mileage = 1_000L)
        insertMaintenance(mileage = 2_000L)
        insertInspection(mileage = 3_000L)

        assertThat(viewRows().map { EventType.valueOf(it.eventType) })
            .containsExactly(EventType.REFUEL, EventType.MAINTENANCE, EventType.INSPECTION)
            .inOrder()
    }

    // endregion

    // region helpers

    private suspend fun insertRefuel(
        mileage: Long?,
        date: LocalDate? = LocalDate.of(2025, 1, 1),
    ) {
        db.refuelEventDao().upsert(refuelEvent(mileage = mileage, date = date))
    }

    private suspend fun insertMaintenance(
        mileage: Long?,
        date: LocalDate? = LocalDate.of(2025, 1, 1),
    ) {
        db.maintenanceEventDao().insert(maintenanceEvent(mileage = mileage, date = date))
    }

    private suspend fun insertInspection(
        mileage: Long?,
        date: LocalDate? = LocalDate.of(2025, 1, 1),
    ) {
        db.inspectionEventDao().insert(inspectionEvent(mileage = mileage, date = date))
    }

    /**
     * Reads the union view directly: no DAO exposes the view rows, only aggregates derived from it.
     */
    private fun viewRows(): List<ViewRow> =
        db.openHelper.readableDatabase
            .query("SELECT id, eventType FROM alleventsbasecolumnsview ORDER BY mileage ASC")
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(ViewRow(id = cursor.getString(0), eventType = cursor.getString(1)))
                    }
                }
            }

    private data class ViewRow(val id: String, val eventType: String)

    // endregion
}
