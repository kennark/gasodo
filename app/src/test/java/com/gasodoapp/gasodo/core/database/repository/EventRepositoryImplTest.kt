package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.EventDao
import com.gasodoapp.gasodo.core.database.projections.DateMileage
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [EventRepositoryImpl].
 * Tests verify that the repository correctly delegates to the [EventDao].
 */
class EventRepositoryImplTest {

    private lateinit var dao: EventDao
    private lateinit var repository: EventRepository

    @Before
    fun setup() {
        dao = mockk()
        repository = EventRepositoryImpl(dao)
    }

    @Test
    fun `getDateWithHigherMileage delegates to DAO and returns result`() = runTest {
        // Arrange
        val expectedMileage = 50000L
        val expectedDate = LocalDate.of(2025, 6, 15).toEpochDay()
        val expected = DateMileage(date = expectedDate, mileage = expectedMileage)
        val inputMileage = expectedMileage - 100

        coEvery { dao.getDateWithHigherMileage(inputMileage) } returns expected

        // Act
        val result = repository.getDateWithHigherMileage(inputMileage)

        // Assert
        assertThat(result).isEqualTo(expected)
        assertThat(result?.mileage).isEqualTo(expectedMileage)
        assertThat(result?.date).isEqualTo(expectedDate)

        coVerify { dao.getDateWithHigherMileage(inputMileage) }
    }

    @Test
    fun `getDateWithHigherMileage returns null when DAO returns null`() = runTest {
        // Arrange
        val inputMileage = 50000L
        coEvery { dao.getDateWithHigherMileage(inputMileage) } returns null

        // Act
        val result = repository.getDateWithHigherMileage(inputMileage)

        // Assert
        assertThat(result).isNull()

        coVerify { dao.getDateWithHigherMileage(inputMileage) }
    }

    @Test
    fun `getDateWithLowerMileage delegates to DAO and returns result`() = runTest {
        // Arrange
        val expectedMileage = 45000L
        val expectedDate = LocalDate.of(2025, 3, 20).toEpochDay()
        val expected = DateMileage(date = expectedDate, mileage = expectedMileage)
        val inputMileage = expectedMileage + 100

        coEvery { dao.getDateWithLowerMileage(inputMileage) } returns expected

        // Act
        val result = repository.getDateWithLowerMileage(inputMileage)

        // Assert
        assertThat(result).isEqualTo(expected)
        assertThat(result?.mileage).isEqualTo(expectedMileage)
        assertThat(result?.date).isEqualTo(expectedDate)

        coVerify { dao.getDateWithLowerMileage(inputMileage) }
    }

    @Test
    fun `getDateWithLowerMileage returns null when DAO returns null`() = runTest {
        // Arrange
        val inputMileage = 50000L
        coEvery { dao.getDateWithLowerMileage(inputMileage) } returns null

        // Act
        val result = repository.getDateWithLowerMileage(inputMileage)

        // Assert
        assertThat(result).isNull()

        coVerify { dao.getDateWithLowerMileage(inputMileage) }
    }
}
