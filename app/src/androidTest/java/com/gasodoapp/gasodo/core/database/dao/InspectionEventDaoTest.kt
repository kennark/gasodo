package com.gasodoapp.gasodo.core.database.dao

import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.InMemoryDatabaseRule
import com.gasodoapp.gasodo.core.database.entity.UsedInspectablePart
import com.gasodoapp.gasodo.core.database.insertReturning
import com.gasodoapp.gasodo.core.database.inspectablePart
import com.gasodoapp.gasodo.core.database.inspectionEvent
import com.gasodoapp.gasodo.core.enums.InspectionStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

/**
 * Tests [InspectionEventDao], including the `@Relation` through `used_inspectable_parts`, the
 * `findings` list converter and the `ON DELETE CASCADE` behaviour of the junction table.
 */
class InspectionEventDaoTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule()

    private val db: AppDatabase get() = dbRule.database
    private val dao: InspectionEventDao get() = dbRule.database.inspectionEventDao()
    private val usedPartDao: UsedInspectablePartDao get() = dbRule.database.usedInspectablePartDao()

    @Test
    fun insert_andGetById_roundTripsTheFindingsList(): Unit = runBlocking {
        val event = inspectionEvent(
            date = LocalDate.of(2025, 8, 20),
            mileage = 54_321L,
            status = InspectionStatus.CONDITIONAL_PASS,
            findings = listOf("worn brakes", "low tread"),
            notes = "Annual inspection",
        )

        dao.insert(event)
        val stored = dao.getById(event.id)

        assertThat(stored).isNotNull()
        assertThat(stored!!.base.date).isEqualTo(LocalDate.of(2025, 8, 20))
        assertThat(stored.status).isEqualTo(InspectionStatus.CONDITIONAL_PASS)
        assertThat(stored.findings).containsExactly("worn brakes", "low tread").inOrder()
        assertThat(stored.base.notes).isEqualTo("Annual inspection")
    }

    @Test
    fun insert_andGetById_roundTripsAnEmptyFindingsList(): Unit = runBlocking {
        val event = inspectionEvent(findings = emptyList())
        dao.insert(event)

        assertThat(dao.getById(event.id)!!.findings).isEmpty()
    }

    @Test
    fun getByIdWithParts_resolvesTheManyToManyRelation(): Unit = runBlocking {
        val brakes = db.inspectablePartDao().insertReturning(inspectablePart(partName = "Brakes"))
        val tires = db.inspectablePartDao().insertReturning(inspectablePart(partName = "Tires"))
        val event = inspectionEvent()
        dao.insert(event)
        usedPartDao.insertAll(
            listOf(
                UsedInspectablePart(inspectionEventId = event.id, partId = brakes.id),
                UsedInspectablePart(inspectionEventId = event.id, partId = tires.id),
            )
        )

        val stored = dao.getByIdWithParts(event.id)

        assertThat(stored).isNotNull()
        assertThat(stored!!.event.id).isEqualTo(event.id)
        assertThat(stored.parts.map { it.partName }).containsExactly("Brakes", "Tires")
    }

    @Test
    fun getByIdWithParts_returnsAnEmptyListWhenThereAreNoJunctionRows(): Unit = runBlocking {
        val event = inspectionEvent()
        dao.insert(event)

        assertThat(dao.getByIdWithParts(event.id)!!.parts).isEmpty()
    }

    @Test
    fun getByIdWithParts_returnsNullForAnUnknownId(): Unit = runBlocking {
        assertThat(dao.getByIdWithParts(UUID.randomUUID())).isNull()
    }

    @Test
    fun getAll_emitsEveryInspectionEvent(): Unit = runBlocking {
        dao.insertAll(listOf(inspectionEvent(), inspectionEvent()))

        assertThat(dao.getAll().first()).hasSize(2)
    }

    @Test
    fun deleteByIds_cascadesIntoTheJunctionTable(): Unit = runBlocking {
        val part = db.inspectablePartDao().insertReturning(inspectablePart())
        val event = inspectionEvent()
        dao.insert(event)
        usedPartDao.insert(UsedInspectablePart(inspectionEventId = event.id, partId = part.id))

        dao.deleteByIds(listOf(event.id))

        assertThat(dao.getById(event.id)).isNull()
        assertThat(usedPartDao.countByPartId(part.id)).isEqualTo(0)
    }

    @Test
    fun update_replacesTheStoredValues(): Unit = runBlocking {
        val event = inspectionEvent(status = InspectionStatus.PASS)
        dao.insert(event)

        dao.update(event.copy(status = InspectionStatus.FAIL, findings = listOf("rust")))

        val stored = dao.getById(event.id)
        assertThat(stored!!.status).isEqualTo(InspectionStatus.FAIL)
        assertThat(stored.findings).containsExactly("rust")
    }

    @Test
    fun getAllInDateRange_returnsEventsWithPartsOrderedByMileageDesc(): Unit = runBlocking {
        val part =
            db.inspectablePartDao().insertReturning(inspectablePart(partName = "Undercarriage"))
        val start = LocalDate.of(2025, 5, 1)
        val end = LocalDate.of(2025, 5, 31)
        val lower = inspectionEvent(date = start, mileage = 1_000L)
        val higher = inspectionEvent(date = end, mileage = 3_000L)
        dao.insertAll(listOf(lower, higher))
        usedPartDao.insert(UsedInspectablePart(inspectionEventId = higher.id, partId = part.id))

        val result = dao.getAllInDateRange(start, end).first()

        assertThat(result.map { it.event.base.mileage }).containsExactly(3_000L, 1_000L).inOrder()
        assertThat(result.first().parts.map { it.partName }).containsExactly("Undercarriage")
    }

    @Test
    fun deleteByIds_withAnEmptyCollectionDeletesNothing(): Unit = runBlocking {
        val event = inspectionEvent()
        dao.insert(event)

        dao.deleteByIds(emptyList())

        assertThat(dao.getById(event.id)).isNotNull()
    }
}
