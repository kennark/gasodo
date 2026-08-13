package com.example.carrefueltracker.core.database.repository

import com.example.carrefueltracker.core.database.BaseColumns
import com.example.carrefueltracker.core.database.dao.InspectionEventDao
import com.example.carrefueltracker.core.database.entity.InspectionEvent
import com.example.carrefueltracker.core.enums.InspectionStatus
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

/**
 * Unit tests for [InspectionRepositoryImpl].
 * Tests verify CRUD operations correctly delegate to [InspectionEventDao].
 */
class InspectionRepositoryImplTest {

    private lateinit var dao: InspectionEventDao
    private lateinit var repository: InspectionRepository

    @Before
    fun setup() {
        dao = mockk()
        repository = InspectionRepositoryImpl(dao)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getAll returns Flow from DAO`() = runTest {
        // Arrange
        val expectedEvents = listOf(
            makeInspectionEvent(date = LocalDate.of(2025, 6, 1).toEpochDay()),
            makeInspectionEvent(date = LocalDate.of(2025, 5, 15).toEpochDay())
        )

        every { dao.getAll() } returns flowOf(expectedEvents)

        // Act
        var result: List<InspectionEvent> = emptyList()
        repository.getAll().collect { value -> result = value }

        // Assert
        assertThat(result).isEqualTo(expectedEvents)
        assertThat(result).hasSize(2)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `getAll returns empty list when DAO returns empty`() = runTest {
        // Arrange
        every { dao.getAll() } returns flowOf(emptyList())

        // Act
        var result: List<InspectionEvent> = emptyList()
        repository.getAll().collect { value -> result = value }

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `getById delegates to DAO and returns result`() = runTest {
        // Arrange
        val expectedId = UUID.randomUUID()
        val expected = makeInspectionEvent(id = expectedId)
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
        val event = makeInspectionEvent()
        coEvery { dao.insert(event) } returns 1L

        // Act
        repository.insert(event)

        // Assert
        coVerify { dao.insert(event) }
    }

    @Test
    fun `update delegates to DAO`() = runTest {
        // Arrange
        val event = makeInspectionEvent()
        coEvery { dao.update(event) } returns Unit

        // Act
        repository.update(event)

        // Assert
        coVerify { dao.update(event) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        // Arrange
        val event = makeInspectionEvent()
        coEvery { dao.delete(event) } returns Unit

        // Act
        repository.delete(event)

        // Assert
        coVerify { dao.delete(event) }
    }

    private fun makeInspectionEvent(
        id: UUID = UUID.randomUUID(),
        date: Long = LocalDate.of(2025, 6, 15).toEpochDay(),
        mileage: Long = 50000,
        status: InspectionStatus = InspectionStatus.PASS,
        findings: List<String> = listOf("All systems nominal")
    ): InspectionEvent {
        return InspectionEvent(
            id = id,
            base = BaseColumns(
                date = date,
                mileage = mileage,
                savedLocationId = null,
                photoUris = emptyList(),
                notes = ""
            ),
            status = status,
            findings = findings
        )
    }
}
