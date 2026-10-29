package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.InspectablePartDao
import com.gasodoapp.gasodo.core.database.dao.UsedInspectablePartDao
import com.gasodoapp.gasodo.core.database.entity.InspectablePart
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
 * Unit tests for [InspectablePartRepositoryImpl].
 * Tests verify CRUD operations correctly delegate to [InspectablePartDao] and that the
 * usage count is resolved through [UsedInspectablePartDao].
 */
class InspectablePartRepositoryImplTest {

    private lateinit var dao: InspectablePartDao
    private lateinit var usedPartDao: UsedInspectablePartDao
    private lateinit var repository: InspectablePartRepository

    @Before
    fun setup() {
        dao = mockk()
        usedPartDao = mockk()
        repository = InspectablePartRepositoryImpl(dao, usedPartDao)
    }

    @Test
    fun `getAll returns Flow from DAO`() = runTest {
        // Arrange
        val expected = listOf(
            makeInspectablePart(id = 1L, name = "Oil Filter"),
            makeInspectablePart(id = 2L, name = "Brake Pads"),
        )
        every { dao.getAll() } returns flowOf(expected)

        // Act
        var result: List<InspectablePart> = emptyList()
        repository.getAll().collect { value -> result = value }

        // Assert
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `getAll returns empty list when DAO returns empty`() = runTest {
        // Arrange
        every { dao.getAll() } returns flowOf(emptyList())

        // Act
        var result: List<InspectablePart> = emptyList()
        repository.getAll().collect { value -> result = value }

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `getById delegates to DAO and returns result`() = runTest {
        // Arrange
        val expected = makeInspectablePart(id = 7L, name = "Air Filter")
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
        val entity = makeInspectablePart(id = 0L, name = "New Part")
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
        val entity = makeInspectablePart(id = 3L, name = "Renamed Part")
        coEvery { dao.update(entity) } just runs

        // Act
        repository.update(entity)

        // Assert
        coVerify { dao.update(entity) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        // Arrange
        val entity = makeInspectablePart(id = 4L, name = "Obsolete Part")
        coEvery { dao.delete(entity) } just runs

        // Act
        repository.delete(entity)

        // Assert
        coVerify { dao.delete(entity) }
    }

    @Test
    fun `countUsage delegates to used part DAO`() = runTest {
        // Arrange
        coEvery { usedPartDao.countByPartId(4L) } returns 3

        // Act
        val result = repository.countUsage(4L)

        // Assert
        assertThat(result).isEqualTo(3)
        coVerify { usedPartDao.countByPartId(4L) }
    }

    @Test
    fun `countUsage forwards the given id to the used part DAO`() = runTest {
        // Arrange
        coEvery { usedPartDao.countByPartId(any()) } returns 1

        // Act
        repository.countUsage(12L)

        // Assert
        coVerify(exactly = 1) { usedPartDao.countByPartId(12L) }
        coVerify(exactly = 0) { usedPartDao.countByPartId(4L) }
    }

    @Test
    fun `countUsage returns zero when part is unused`() = runTest {
        // Arrange
        coEvery { usedPartDao.countByPartId(9L) } returns 0

        // Act
        val result = repository.countUsage(9L)

        // Assert
        assertThat(result).isEqualTo(0)
    }

    private fun makeInspectablePart(id: Long, name: String, notes: String? = null) =
        InspectablePart(id = id, partName = name, notes = notes)
}
