package com.gasodoapp.gasodo.core.database.dao

import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.InMemoryDatabaseRule
import com.gasodoapp.gasodo.core.database.entity.UsedInspectablePart
import com.gasodoapp.gasodo.core.database.inspectablePart
import com.gasodoapp.gasodo.core.database.inspectionEvent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

/**
 * Tests [InspectablePartDao] and the [UsedInspectablePartDao] counts that back the "in use"
 * guard in the Manage screens.
 */
class InspectablePartDaoTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule()

    private val db: AppDatabase get() = dbRule.database
    private val dao: InspectablePartDao get() = dbRule.database.inspectablePartDao()
    private val usedPartDao: UsedInspectablePartDao get() = dbRule.database.usedInspectablePartDao()

    @Test
    fun insert_returnsTheGeneratedIdAndGetByIdReadsItBack(): Unit = runBlocking {
        val id = dao.insert(inspectablePart(partName = "Engine Oil", notes = "check level"))

        val stored = dao.getById(id)

        assertThat(id).isGreaterThan(0L)
        assertThat(stored).isNotNull()
        assertThat(stored!!.partName).isEqualTo("Engine Oil")
        assertThat(stored.notes).isEqualTo("check level")
    }

    @Test
    fun getAll_returnsEveryPart(): Unit = runBlocking {
        dao.insertAll(
            listOf(
                inspectablePart(partName = "Brakes"),
                inspectablePart(partName = "Tires")
            )
        )

        assertThat(dao.getAll().first().map { it.partName })
            .containsExactly("Brakes", "Tires")
    }

    @Test
    fun update_replacesTheStoredValues(): Unit = runBlocking {
        val id = dao.insert(inspectablePart(partName = "Old"))
        val stored = dao.getById(id)!!

        dao.update(stored.copy(partName = "New"))

        assertThat(dao.getById(id)!!.partName).isEqualTo("New")
    }

    @Test
    fun deleteById_removesTheRow(): Unit = runBlocking {
        val id = dao.insert(inspectablePart())

        dao.deleteById(id)

        assertThat(dao.getById(id)).isNull()
    }

    @Test
    fun getById_returnsNullForAnUnknownId(): Unit = runBlocking {
        assertThat(dao.getById(-1L)).isNull()
    }

    // region junction counts

    @Test
    fun countByPartId_countsTheJunctionRows(): Unit = runBlocking {
        val id = dao.insert(inspectablePart())
        val first = inspectionEvent()
        val second = inspectionEvent()
        db.inspectionEventDao().insertAll(listOf(first, second))
        usedPartDao.insertAll(
            listOf(
                UsedInspectablePart(inspectionEventId = first.id, partId = id),
                UsedInspectablePart(inspectionEventId = second.id, partId = id),
            )
        )

        assertThat(usedPartDao.countByPartId(id)).isEqualTo(2)
    }

    @Test
    fun countByPartId_returnsZeroWhenThePartIsUnused(): Unit = runBlocking {
        val id = dao.insert(inspectablePart())

        assertThat(usedPartDao.countByPartId(id)).isEqualTo(0)
    }

    @Test
    fun deleteByInspectionEventId_clearsOnlyThatEventsJunctionRows(): Unit = runBlocking {
        val keptPart = dao.insert(inspectablePart(partName = "Kept"))
        val droppedPart = dao.insert(inspectablePart(partName = "Dropped"))
        val first = inspectionEvent()
        val second = inspectionEvent()
        db.inspectionEventDao().insertAll(listOf(first, second))
        usedPartDao.insertAll(
            listOf(
                UsedInspectablePart(inspectionEventId = first.id, partId = droppedPart),
                UsedInspectablePart(inspectionEventId = second.id, partId = keptPart),
            )
        )

        usedPartDao.deleteByInspectionEventId(first.id)

        assertThat(usedPartDao.countByPartId(droppedPart)).isEqualTo(0)
        assertThat(usedPartDao.countByPartId(keptPart)).isEqualTo(1)
    }

    @Test
    fun deletingThePart_cascadesIntoTheJunctionTable(): Unit = runBlocking {
        val id = dao.insert(inspectablePart())
        val event = inspectionEvent()
        db.inspectionEventDao().insert(event)
        usedPartDao.insert(UsedInspectablePart(inspectionEventId = event.id, partId = id))

        dao.deleteById(id)

        assertThat(usedPartDao.countByPartId(id)).isEqualTo(0)
    }

    // endregion
}
