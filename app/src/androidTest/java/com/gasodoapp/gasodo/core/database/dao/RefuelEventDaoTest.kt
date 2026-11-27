package com.gasodoapp.gasodo.core.database.dao

import android.database.sqlite.SQLiteConstraintException
import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.InMemoryDatabaseRule
import com.gasodoapp.gasodo.core.database.refuelEvent
import com.gasodoapp.gasodo.core.database.savedLocation
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
 * Tests [RefuelEventDao] CRUD, the type converters and the foreign key to `saved_locations`.
 */
class RefuelEventDaoTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule()

    private val db: AppDatabase get() = dbRule.database
    private val dao: RefuelEventDao get() = dbRule.database.refuelEventDao()

    @Test
    fun upsert_andGetById_roundTripsEveryColumnThroughItsConverter(): Unit = runBlocking {
        val location = savedLocation(name = "Shell")
        db.savedLocationDao().insert(location)

        val event = refuelEvent(
            date = LocalDate.of(2025, 7, 14),
            mileage = 123_456L,
            savedLocationId = location.id,
            amountLiters = BigDecimal("45.5"),
            pricePerLiter = BigDecimal("1.799"),
            totalCost = BigDecimal("81.85"),
            paymentMethod = PaymentMethod.CARD,
            fullFillUp = true,
            photoUris = listOf("content://photo/1", "content://photo/2"),
            notes = "Full tank before the trip",
        )

        dao.upsert(event)
        val stored = dao.getById(event.id)

        assertThat(stored).isNotNull()
        assertThat(stored!!.id).isEqualTo(event.id)
        assertThat(stored.base.date).isEqualTo(LocalDate.of(2025, 7, 14))
        assertThat(stored.base.mileage).isEqualTo(123_456L)
        // BigDecimal is stored at scale 3, so a two-decimal input comes back padded.
        assertThat(stored.amountLiters).isEqualTo(BigDecimal("45.500"))
        assertThat(stored.totalCost).isEqualTo(BigDecimal("81.850"))
        assertThat(stored.base.photoUris).containsExactly("content://photo/1", "content://photo/2")
            .inOrder()
        assertThat(stored.paymentMethod).isEqualTo(PaymentMethod.CARD)
        assertThat(stored.fullFillUp).isTrue()
        assertThat(stored.base.notes).isEqualTo("Full tank before the trip")
    }

    @Test
    fun upsert_replacesTheRowWhenTheIdAlreadyExists(): Unit = runBlocking {
        val event = refuelEvent(mileage = 1_000L, notes = "original")
        dao.upsert(event)

        dao.upsert(event.copy(base = event.base.copy(mileage = 2_000L, notes = "updated")))

        val stored =
            dao.getAllInDateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2030, 1, 1)).first()
        assertThat(stored).hasSize(1)
        assertThat(stored.single().base.mileage).isEqualTo(2_000L)
        assertThat(stored.single().base.notes).isEqualTo("updated")
    }

    @Test
    fun insertAll_throwsWhenAnIdAlreadyExists(): Unit = runBlocking {
        val event = refuelEvent()
        dao.insertAll(listOf(event))

        val error = runCatching { dao.insertAll(listOf(event)) }.exceptionOrNull()

        assertThat(error).isInstanceOf(SQLiteConstraintException::class.java)
    }

    @Test
    fun getAllInDateRange_isInclusiveOnBothBoundsAndOrderedByMileageDesc(): Unit = runBlocking {
        val start = LocalDate.of(2025, 3, 1)
        val end = LocalDate.of(2025, 3, 31)
        dao.upsert(refuelEvent(date = start, mileage = 1_000L))
        dao.upsert(refuelEvent(date = end, mileage = 2_000L))
        dao.upsert(refuelEvent(date = LocalDate.of(2025, 3, 15), mileage = 3_000L))
        dao.upsert(refuelEvent(date = LocalDate.of(2025, 2, 28), mileage = 9_000L))
        dao.upsert(refuelEvent(date = LocalDate.of(2025, 4, 1), mileage = 9_500L))

        val result = dao.getAllInDateRange(start, end).first()

        assertThat(result.map { it.base.mileage }).containsExactly(3_000L, 2_000L, 1_000L).inOrder()
    }

    @Test
    fun getDateWithHigherMileage_isInclusiveUnlikeTheUnionViewQuery(): Unit = runBlocking {
        val date = LocalDate.of(2025, 5, 5)
        dao.upsert(refuelEvent(date = date, mileage = 5_000L))

        val result = dao.getDateWithHigherMileage(5_000L)

        assertThat(result).isNotNull()
        assertThat(result!!.mileage).isEqualTo(5_000L)
        assertThat(result.date).isEqualTo(date)
        // Same lookup, but EventDao uses a strict `>` so it does not match this row.
        assertThat(db.eventDao().getDateWithHigherMileage(5_000L)).isNull()
    }

    @Test
    fun getDateWithLowerMileage_isInclusiveUnlikeTheUnionViewQuery(): Unit = runBlocking {
        val date = LocalDate.of(2025, 5, 5)
        dao.upsert(refuelEvent(date = date, mileage = 5_000L))

        val result = dao.getDateWithLowerMileage(5_000L)

        assertThat(result).isNotNull()
        assertThat(result!!.mileage).isEqualTo(5_000L)
        assertThat(db.eventDao().getDateWithLowerMileage(5_000L)).isNull()
    }

    @Test
    fun deleteByIds_removesOnlyTheListedIds(): Unit = runBlocking {
        val keep = refuelEvent(mileage = 1_000L)
        val remove1 = refuelEvent(mileage = 2_000L)
        val remove2 = refuelEvent(mileage = 3_000L)
        dao.insertAll(listOf(keep, remove1, remove2))

        dao.deleteByIds(listOf(remove1.id, remove2.id))

        assertThat(dao.getById(remove1.id)).isNull()
        assertThat(dao.getById(remove2.id)).isNull()
        assertThat(dao.getById(keep.id)).isNotNull()
    }

    @Test
    fun deleteByIds_withAnEmptyCollectionDeletesNothing(): Unit = runBlocking {
        val event = refuelEvent()
        dao.upsert(event)

        dao.deleteByIds(emptyList())

        assertThat(dao.getById(event.id)).isNotNull()
    }

    @Test
    fun deleteById_removesTheSingleRow(): Unit = runBlocking {
        val event = refuelEvent()
        dao.upsert(event)

        dao.deleteById(event.id)

        assertThat(dao.getById(event.id)).isNull()
    }

    @Test
    fun getById_returnsNullForAnUnknownId(): Unit = runBlocking {
        assertThat(dao.getById(UUID.randomUUID())).isNull()
    }

    @Test
    fun deletingTheSavedLocation_setsTheEventForeignKeyToNull(): Unit = runBlocking {
        val location = savedLocation(name = "Shell")
        db.savedLocationDao().insert(location)
        val event = refuelEvent(savedLocationId = location.id)
        dao.upsert(event)

        db.savedLocationDao().delete(location)

        val stored = dao.getById(event.id)
        assertThat(stored).isNotNull()
        assertThat(stored!!.base.savedLocationId).isNull()
    }

    @Test
    fun insertingAnEventWithAnUnknownLocation_throws(): Unit = runBlocking {
        val error = runCatching {
            dao.upsert(refuelEvent(savedLocationId = UUID.randomUUID()))
        }.exceptionOrNull()

        assertThat(error).isInstanceOf(SQLiteConstraintException::class.java)
    }
}
