package com.gasodoapp.gasodo.feature.manage.savedlocation

import com.gasodoapp.gasodo.core.database.entity.SavedLocation
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import com.gasodoapp.gasodo.ui.components.catalogue.NameError
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for [SavedLocationScreenViewModel].
 * Tests cover listing/filtering, the create/edit dialog and the delete confirmation flow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedLocationScreenViewModelTest {

    private lateinit var repository: SavedLocationRepository
    private lateinit var viewModel: SavedLocationScreenViewModel
    private lateinit var stored: MutableStateFlow<List<SavedLocation>>

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = mockk()
        stored = MutableStateFlow(emptyList())
        every { repository.getAll() } returns stored
        coEvery { repository.countUsage(any()) } returns 0
        viewModel = SavedLocationScreenViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `visible locations are empty by default`() = runTest {
        // Arrange
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.visibleLocations.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.visibleLocations.value).isEmpty()
        assertThat(viewModel.searchQuery.value).isEmpty()
        assertThat(viewModel.editor.value).isNull()
        assertThat(viewModel.pendingDelete.value).isNull()

        job.cancel()
    }

    @Test
    fun `visible locations are sorted alphabetically ignoring case`() = runTest {
        // Arrange
        stored.value = listOf(
            makeLocation(name = "shell"),
            makeLocation(name = "Aral"),
            makeLocation(name = "esso")
        )
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.visibleLocations.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.visibleLocations.value.map { it.name })
            .containsExactly("Aral", "esso", "shell")
            .inOrder()

        job.cancel()
    }

    @Test
    fun `search query filters locations case insensitively`() = runTest {
        // Arrange
        stored.value = listOf(
            makeLocation(name = "Shell Berlin"),
            makeLocation(name = "Aral Hamburg"),
            makeLocation(name = "Shell Munich")
        )
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.visibleLocations.collect {}
        }

        // Act
        viewModel.onSearchQueryChange("shell")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.visibleLocations.value.map { it.name })
            .containsExactly("Shell Berlin", "Shell Munich")
            .inOrder()

        job.cancel()
    }

    @Test
    fun `onCreateClick opens an empty new editor`() {
        // Act
        viewModel.onCreateClick()

        // Assert
        val editor = viewModel.editor.value
        assertThat(editor).isNotNull()
        assertThat(editor!!.isNew).isTrue()
        assertThat(editor.id).isNull()
        assertThat(editor.name).isEmpty()
    }

    @Test
    fun `onEditClick opens an editor prefilled with the location`() {
        // Arrange
        val item = makeLocation(
            name = "Shell Berlin",
            latitude = 52.52,
            longitude = 13.405,
            address = "Hauptstraße 1"
        )

        // Act
        viewModel.onEditClick(item)

        // Assert
        val editor = viewModel.editor.value
        assertThat(editor?.id).isEqualTo(item.id)
        assertThat(editor?.isNew).isFalse()
        assertThat(editor?.name).isEqualTo("Shell Berlin")
        assertThat(editor?.latitude).isEqualTo(52.52)
        assertThat(editor?.longitude).isEqualTo(13.405)
        assertThat(editor?.address).isEqualTo("Hauptstraße 1")
    }

    @Test
    fun `onEditorConfirm inserts a trimmed location and closes the dialog`() = runTest {
        // Arrange
        val captured = slot<SavedLocation>()
        coEvery { repository.insert(capture(captured)) } just runs
        viewModel.onCreateClick()
        viewModel.onEditorNameChange("  New Station  ")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(captured.captured.name).isEqualTo("New Station")
        assertThat(captured.captured.latitude).isNull()
        assertThat(captured.captured.longitude).isNull()
        assertThat(captured.captured.address).isNull()
        assertThat(viewModel.editor.value).isNull()
    }

    @Test
    fun `onEditorConfirm updates an existing location keeping its id and position`() = runTest {
        // Arrange
        val existing = makeLocation(
            name = "Old Name",
            latitude = 52.52,
            longitude = 13.405,
            address = "Hauptstraße 1"
        )
        val captured = slot<SavedLocation>()
        stored.value = listOf(existing)
        coEvery { repository.getById(existing.id) } returns existing
        coEvery { repository.update(capture(captured)) } just runs

        viewModel.onEditClick(existing)
        viewModel.onEditorNameChange("New Name")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(captured.captured.id).isEqualTo(existing.id)
        assertThat(captured.captured.name).isEqualTo("New Name")
        assertThat(captured.captured.latitude).isEqualTo(52.52)
        assertThat(captured.captured.longitude).isEqualTo(13.405)
        assertThat(captured.captured.address).isEqualTo("Hauptstraße 1")
        assertThat(viewModel.editor.value).isNull()
    }

    @Test
    fun `onEditorConfirm rejects a blank name`() = runTest {
        // Arrange
        viewModel.onCreateClick()
        viewModel.onEditorNameChange("   ")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.editor.value?.nameError).isEqualTo(NameError.EMPTY)
        assertThat(viewModel.editor.value).isNotNull()
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `onEditorConfirm rejects a duplicate name from another location`() = runTest {
        // Arrange
        stored.value = listOf(makeLocation(name = "Shell Berlin"))
        every { repository.getAll() } returns flowOf(stored.value)
        viewModel.onCreateClick()
        viewModel.onEditorNameChange("shell berlin")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.editor.value?.nameError).isEqualTo(NameError.DUPLICATE)
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `onEditorNameChange clears a previous error`() {
        // Arrange
        viewModel.onCreateClick()
        viewModel.onEditorNameChange(" ")
        viewModel.onEditorConfirm()

        // Act
        viewModel.onEditorNameChange("Fixed name")

        // Assert
        assertThat(viewModel.editor.value?.nameError).isNull()
    }

    @Test
    fun `editing a location does not treat its own name as duplicate`() = runTest {
        // Arrange
        val existing = makeLocation(name = "Shell Berlin")
        val updated = slot<SavedLocation>()
        stored.value = listOf(existing)
        coEvery { repository.getById(existing.id) } returns existing
        coEvery { repository.update(capture(updated)) } just runs

        viewModel.onEditClick(existing)
        viewModel.onEditorNameChange("Shell Berlin")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.editor.value).isNull()
        coVerify(exactly = 1) { repository.update(any()) }
    }

    @Test
    fun `onDeleteClick loads the usage count`() = runTest {
        // Arrange
        val item = makeLocation(name = "Shell Berlin")
        coEvery { repository.countUsage(item.id) } returns 4

        // Act
        viewModel.onDeleteClick(item)
        advanceUntilIdle()

        // Assert
        val pending = viewModel.pendingDelete.value
        assertThat(pending?.target).isEqualTo(item)
        assertThat(pending?.usageCount).isEqualTo(4)
    }

    @Test
    fun `onDeleteConfirm deletes the pending location`() = runTest {
        // Arrange
        val item = makeLocation(name = "Shell Berlin")
        coEvery { repository.delete(item) } just runs
        viewModel.onDeleteClick(item)
        advanceUntilIdle()

        // Act
        viewModel.onDeleteConfirm(item)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.delete(item) }
        assertThat(viewModel.pendingDelete.value).isNull()
    }

    @Test
    fun `onDeleteConfirm still deletes when the dialog already dismissed itself`() = runTest {
        // Arrange: DeleteDialog calls onDismissRequest before onConfirmRequest, so the pending
        // delete may already be cleared by the time the confirm runs.
        val item = makeLocation(name = "Shell Berlin")
        coEvery { repository.delete(item) } just runs
        viewModel.onDeleteClick(item)
        advanceUntilIdle()
        viewModel.onDeleteDismiss()

        // Act
        viewModel.onDeleteConfirm(item)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.delete(item) }
    }

    @Test
    fun `onDeleteDismiss closes the dialog without deleting`() = runTest {
        // Arrange
        val item = makeLocation(name = "Shell Berlin")
        viewModel.onDeleteClick(item)
        advanceUntilIdle()

        // Act
        viewModel.onDeleteDismiss()

        // Assert
        assertThat(viewModel.pendingDelete.value).isNull()
        coVerify(exactly = 0) { repository.delete(any()) }
    }

    @Test
    fun `onEditorDismiss closes the dialog without saving`() {
        // Arrange
        viewModel.onCreateClick()
        viewModel.onEditorNameChange("Discarded")

        // Act
        viewModel.onEditorDismiss()

        // Assert
        assertThat(viewModel.editor.value).isNull()
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    private fun makeLocation(
        id: UUID = UUID.randomUUID(),
        name: String,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
    ) = SavedLocation(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        address = address
    )
}
