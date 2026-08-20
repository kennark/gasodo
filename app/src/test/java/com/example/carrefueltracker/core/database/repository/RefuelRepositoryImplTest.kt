package com.example.carrefueltracker.core.database.repository

import com.example.carrefueltracker.core.database.BaseColumns
import com.example.carrefueltracker.core.database.dao.RefuelEventDao
import com.example.carrefueltracker.core.database.entity.RefuelEvent
import com.example.carrefueltracker.core.enums.PaymentMethod
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Unit tests for [RefuelRepositoryImpl].
 * Tests verify CRUD operations and date range queries correctly delegate to [RefuelEventDao].
 */
class RefuelRepositoryImplTest {

    private lateinit var dao: RefuelEventDao
    private lateinit var repository: RefuelRepository

    @Before
    fun setup() {
        dao = mockk()
        repository = RefuelRepositoryImpl(dao)
    }

    @Test
    fun `getAllByYearMonth delegates to DAO with correct date range`() = runTest {
        // Arrange
        val year = 2025
        val month = 6
        val startDate = LocalDate.of(year, month, 1).toEpochDay().times(86400000L)
        val endDate = LocalDate.of(year, month + 1, 1).toEpochDay().times(86400000L)
        val expectedEvents = listOf(
            makeRefuelEvent(date = LocalDate.of(2025, 6, 10).toEpochDay().times(86400000L)),
            makeRefuelEvent(date = LocalDate.of(2025, 6, 20).toEpochDay().times(86400000L))
        )
        every { dao.getAllInDateRange(startDate, endDate) } returns flowOf(expectedEvents)

        // Act
        var result: List<RefuelEvent> = emptyList()
        repository.getAllByYearMonth(year, month).collect { value -> result = value }

        // Assert
        assertThat(result).isEqualTo(expectedEvents)
        assertThat(result).hasSize(2)
    }

    @Test
    fun `getAllWithinTime delegates to DAO with correct date range`() = runTest {
        // Arrange
        val start = LocalDate.of(2025, 1, 1).toEpochDay()
        val end = LocalDate.of(2025, 6, 30).toEpochDay()
        val expectedEvents = listOf(
            makeRefuelEvent(date = LocalDate.of(2025, 3, 15).toEpochDay())
        )
        every { dao.getAllInDateRange(start, end) } returns flowOf(expectedEvents)

        // Act
        var result: List<RefuelEvent> = emptyList()
        repository.getAllWithinTime(start, end).collect { value -> result = value }

        // Assert
        assertThat(result).isEqualTo(expectedEvents)
        assertThat(result).hasSize(1)
    }

    @Test
    fun `getById delegates to DAO and returns result`() = runTest {
        // Arrange
        val expectedId = UUID.randomUUID()
        val expected = makeRefuelEvent(id = expectedId)
        coEvery { dao.getById(expectedId) } returns expected

        // Act
        val result = repository.getById(expectedId)

        // Assert
        assertThat(result).isEqualTo(expected)
        assertThat(result?.id).isEqualTo(expectedId)

        coVerify { dao.getById(expectedId) }
    }

    @Test
    fun `getById returns null when DAO returns null`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        coEvery { dao.getById(id) } returns null

        // Act
        val result = repository.getById(id)

        // Assert
        assertThat(result).isNull()

        coVerify { dao.getById(id) }
    }

    @Test
    fun `insert delegates to DAO`() = runTest {
        // Arrange
        val event = makeRefuelEvent()
        coEvery { dao.upsert(event) } returns 1L

        // Act
        repository.upsert(event)

        // Assert
        coVerify { dao.upsert(event) }
    }

    @Test
    fun `update delegates to DAO`() = runTest {
        // Arrange
        val event = makeRefuelEvent()
        coEvery { dao.update(event) } returns Unit

        // Act
        repository.update(event)

        // Assert
        coVerify { dao.update(event) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        // Arrange
        val event = makeRefuelEvent()
        coEvery { dao.delete(event) } returns Unit

        // Act
        repository.delete(event)

        // Assert
        coVerify { dao.delete(event) }
    }

    private fun makeRefuelEvent(
        id: UUID = UUID.randomUUID(),
        date: Long = LocalDate.of(2025, 6, 15).toEpochDay(),
        mileage: Long = 50000,
        amountLiters: BigDecimal? = BigDecimal("45.5"),
        pricePerLiter: BigDecimal? = BigDecimal("6.50"),
        totalCost: BigDecimal? = BigDecimal("295.75"),
        paymentMethod: PaymentMethod? = PaymentMethod.CARD,
        fullFillUp: Boolean = true
    ): RefuelEvent {
        return RefuelEvent(
            id = id,
            base = BaseColumns(
                date = date,
                mileage = mileage,
                savedLocationId = null,
                photoUris = emptyList(),
                notes = ""
            ),
            amountLiters = amountLiters,
            pricePerLiter = pricePerLiter,
            totalCost = totalCost,
            paymentMethod = paymentMethod,
            fullFillUp = fullFillUp
        )
    }
}
