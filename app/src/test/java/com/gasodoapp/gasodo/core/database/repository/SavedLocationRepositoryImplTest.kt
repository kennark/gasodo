package com.gasodoapp.gasodo.core.database.repository

import com.gasodoapp.gasodo.core.database.dao.SavedLocationDao
import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for [SavedLocationRepositoryImpl].
 * Tests verify CRUD operations correctly delegate to [SavedLocationDao].
 */
class SavedLocationRepositoryImplTest {

    private lateinit var dao: SavedLocationDao
    private lateinit var repository: SavedLocationRepository

    @Before
    fun setup() {
        dao = mockk()
        repository = SavedLocationRepositoryImpl(dao)
    }

    @Test
    fun `getAll returns Flow from DAO`() = runTest {
        // Arrange
        val expectedLocations = listOf(
            makeSavedLocation(name = "Test location 1"),
            makeSavedLocation(name = "Test location 2")
        )
        every { dao.getAll() } returns flowOf(expectedLocations)

        // Act
        var result: List<SavedLocation> = emptyList()
        repository.getAll().collect { value -> result = value }

        // Assert
        assertThat(result).isEqualTo(expectedLocations)
        assertThat(result).hasSize(2)
    }

    @Test
    fun `getAll returns empty list when DAO returns empty`() = runTest {
        // Arrange
        every { dao.getAll() } returns flowOf(emptyList())

        // Act
        var result: List<SavedLocation> = emptyList()
        repository.getAll().collect { value -> result = value }

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `getById delegates to DAO and returns result`() = runTest {
        // Arrange
        val expectedId = UUID.randomUUID()
        val expected = makeSavedLocation(id = expectedId, name = "Test Station")
        coEvery { dao.getById(expectedId.toString()) } returns expected

        // Act
        val result = repository.getById(expectedId)

        // Assert
        assertThat(result).isEqualTo(expected)
        assertThat(result?.id).isEqualTo(expectedId)

        coVerify { dao.getById(expectedId.toString()) }
    }

    @Test
    fun `getById returns null when DAO returns null`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        coEvery { dao.getById(id.toString()) } returns null

        // Act
        val result = repository.getById(id)

        // Assert
        assertThat(result).isNull()

        coVerify { dao.getById(id.toString()) }
    }

    @Test
    fun `insert delegates to DAO`() = runTest {
        // Arrange
        val location = makeSavedLocation()
        coEvery { dao.insert(location) } returns 1L

        // Act
        repository.insert(location)

        // Assert
        coVerify { dao.insert(location) }
    }

    @Test
    fun `update delegates to DAO`() = runTest {
        // Arrange
        val location = makeSavedLocation()
        coEvery { dao.update(location) } returns Unit

        // Act
        repository.update(location)

        // Assert
        coVerify { dao.update(location) }
    }

    @Test
    fun `delete delegates to DAO`() = runTest {
        // Arrange
        val location = makeSavedLocation()
        coEvery { dao.delete(location) } returns Unit

        // Act
        repository.delete(location)

        // Assert
        coVerify { dao.delete(location) }
    }

    private fun makeSavedLocation(
        id: UUID = UUID.randomUUID(),
        name: String = "Test Location",
        latitude: Double? = 40.7128,
        longitude: Double? = -74.0060,
        address: String? = "123 Main St"
    ): SavedLocation {
        return SavedLocation(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            address = address
        )
    }
}
