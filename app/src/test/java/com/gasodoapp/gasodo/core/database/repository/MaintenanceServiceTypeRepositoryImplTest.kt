package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.MaintenanceServiceTypeDao
import com.gasodoapp.gasodo.core.database.dao.UsedMaintenanceServiceDao
import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [MaintenanceServiceTypeRepositoryImpl].
 * Tests verify CRUD operations correctly delegate to [MaintenanceServiceTypeDao] and that the
 * usage count is resolved through [UsedMaintenanceServiceDao].
 */
class MaintenanceServiceTypeRepositoryImplTest {

    private lateinit var dao: MaintenanceServiceTypeDao
    private lateinit var usedServiceDao: UsedMaintenanceServiceDao
    private lateinit var repository: MaintenanceServiceTypeRepository

    @Before
    fun setup() {
        dao = mockk()
        usedServiceDao = mockk()
        repository = MaintenanceServiceTypeRepositoryImpl(dao, usedServiceDao)
    }

    @Test
    fun `getAll returns Flow from DAO`() = runTest {
        // Arrange
        val expected = listOf(
            makeServiceType(id = 1L, name = "Oil Change"),
            makeServiceType(id = 2L, name = "Tire Rotation")
        )
        every { dao.getAll() } returns flowOf(expected)

        // Act
        var result: List<MaintenanceServiceType> = emptyList()
        repository.getAll().collect { value -> result = value }

        // Assert
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `getAll returns empty list when DAO returns empty`() = runTest {
        // Arrange
        every { dao.getAll() } returns flowOf(emptyList())

        // Act
        var result: List<MaintenanceServiceType> = emptyList()
        repository.getAll().collect { value -> result = value }

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `getById delegates to DAO and returns result`() = runTest {
        // Arrange
        val expected = makeServiceType(id = 7L, name = "Brake Change")
        coEvery { dao.getById(7L) } returns expected

        // Act
        val result = repository.getById(7L)

        // Assert
        assertThat(result).isEqualTo(expected)
        coVerify { dao.getById(7L) }
    }

    @Test
    fun `getById returns null when DAO returns null`() = runTest {
        // Arrange
        coEvery { dao.getById(1L) } returns null

        // Act
        val result = repository.getById(1L)

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `insert delegates to DAO and returns generated id`() = runTest {
        // Arrange
        val entity = makeServiceType(id = 0L, name = "New Service")
        coEvery { dao.insert(entity) } returns 42L

        // Act
        val id = repository.insert(entity)

        // Assert
        assertThat(id).isEqualTo(42L)
        coVerify { dao.insert(entity) }
    }

    @Test
    fun `update delegates to DAO`() = runTest {
        // Arrange
        val entity = makeServiceType(id = 3L, name = "Renamed")
        coEvery { dao.update(entity) } just runs

        // Act
        repository.update(entity)

        // Assert
        coVerify { dao.update(entity) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        // Arrange
        val entity = makeServiceType(id = 4L, name = "Obsolete")
        coEvery { dao.delete(entity) } just runs

        // Act
        repository.delete(entity)

        // Assert
        coVerify { dao.delete(entity) }
    }

    @Test
    fun `countUsage delegates to used service DAO`() = runTest {
        // Arrange
        coEvery { usedServiceDao.countByServiceId(4L) } returns 3

        // Act
        val result = repository.countUsage(4L)

        // Assert
        assertThat(result).isEqualTo(3)
        coVerify { usedServiceDao.countByServiceId(4L) }
    }

    @Test
    fun `countUsage returns zero when service is unused`() = runTest {
        // Arrange
        coEvery { usedServiceDao.countByServiceId(9L) } returns 0

        // Act
        val result = repository.countUsage(9L)

        // Assert
        assertThat(result).isEqualTo(0)
    }

    private fun makeServiceType(id: Long, name: String, notes: String? = null) =
        MaintenanceServiceType(id = id, serviceName = name, notes = notes)
}
