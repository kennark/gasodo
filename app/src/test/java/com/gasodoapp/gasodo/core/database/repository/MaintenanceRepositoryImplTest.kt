package com.gasodoapp.gasodo.core.database.repository

import androidx.paging.PagingSource
import com.gasodoapp.gasodo.core.database.AppDatabase
import com.gasodoapp.gasodo.core.database.BaseColumns
import com.gasodoapp.gasodo.core.database.dao.MaintenanceEventDao
import com.gasodoapp.gasodo.core.database.dao.UsedMaintenanceServiceDao
import com.gasodoapp.gasodo.core.database.entity.MaintenanceEvent
import com.gasodoapp.gasodo.core.database.junctions.MaintenanceEventWithServices
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Unit tests for [MaintenanceRepositoryImpl].
 * Tests verify CRUD operations correctly delegate to [MaintenanceEventDao].
 */
class MaintenanceRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var eventDao: MaintenanceEventDao
    private lateinit var usedServiceDao: UsedMaintenanceServiceDao
    private lateinit var repository: MaintenanceRepository

    @Before
    fun setup() {
        db = mockk()
        eventDao = mockk()
        usedServiceDao = mockk()
        repository = MaintenanceRepositoryImpl(db, eventDao, usedServiceDao)
    }

    @Test
    fun `getAll delegates to DAO`() {
        // Arrange
        val pagingSource = mockk<PagingSource<Int, MaintenanceEventWithServices>>()
        every { eventDao.getAllWithServiceTypesOrderByDate() } returns pagingSource

        // Act
        val result = repository.getAll()

        // Assert
        assertThat(result).isEqualTo(pagingSource)
    }

    @Test
    fun `getById delegates to DAO and returns result`() = runTest {
        // Arrange
        val expectedId = UUID.randomUUID()
        val expected = makeMaintenanceEvent(id = expectedId)
        coEvery { eventDao.getById(expectedId) } returns expected

        // Act
        val result = repository.getById(expectedId)

        // Assert
        assertThat(result).isEqualTo(expected)
        assertThat(result?.id).isEqualTo(expectedId)

        coVerify { eventDao.getById(expectedId) }
    }

    @Test
    fun `getById returns null when DAO returns null`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        coEvery { eventDao.getById(id) } returns null

        // Act
        val result = repository.getById(id)

        // Assert
        assertThat(result).isNull()

        coVerify { eventDao.getById(id) }
    }

    @Test
    fun `insert delegates to DAO`() = runTest {
        // Arrange
        val event = makeMaintenanceEvent()
        coEvery { eventDao.insert(event) } returns 1L

        // Act
        repository.insert(event)

        // Assert
        coVerify { eventDao.insert(event) }
    }

    @Test
    fun `update delegates to DAO`() = runTest {
        // Arrange
        val event = makeMaintenanceEvent()
        coEvery { eventDao.update(event) } returns Unit

        // Act
        repository.update(event)

        // Assert
        coVerify { eventDao.update(event) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        // Arrange
        val event = makeMaintenanceEvent()
        coEvery { eventDao.delete(event) } returns Unit

        // Act
        repository.delete(event)

        // Assert
        coVerify { eventDao.delete(event) }
    }

    private fun makeMaintenanceEvent(
        id: UUID = UUID.randomUUID(),
        date: Long = LocalDate.of(2025, 6, 15).toEpochDay(),
        mileage: Long = 50000,
        totalCost: BigDecimal? = BigDecimal("89.99")
    ): MaintenanceEvent {
        return MaintenanceEvent(
            id = id,
            base = BaseColumns(
                date = date,
                mileage = mileage,
                savedLocationId = null,
                photoUris = emptyList(),
                notes = ""
            ),
            totalCost = totalCost
        )
    }
}
