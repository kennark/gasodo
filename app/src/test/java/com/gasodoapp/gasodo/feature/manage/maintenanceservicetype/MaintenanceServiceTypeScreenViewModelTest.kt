package com.gasodoapp.gasodo.feature.manage.maintenanceservicetype

import com.gasodoapp.gasodo.core.database.entity.MaintenanceServiceType
import com.gasodoapp.gasodo.core.database.repository.MaintenanceServiceTypeRepository
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

/**
 * Unit tests for [MaintenanceServiceTypeScreenViewModel].
 * Tests cover listing/filtering, the create/edit dialog and the delete confirmation flow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MaintenanceServiceTypeScreenViewModelTest {

    private lateinit var repository: MaintenanceServiceTypeRepository
    private lateinit var viewModel: MaintenanceServiceTypeScreenViewModel
    private lateinit var stored: MutableStateFlow<List<MaintenanceServiceType>>

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = mockk()
        stored = MutableStateFlow(emptyList())
        every { repository.getAll() } returns stored
        coEvery { repository.countUsage(any()) } returns 0
        viewModel = MaintenanceServiceTypeScreenViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `visible service types are empty by default`() = runTest {
        // Arrange
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.visibleServiceTypes.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.visibleServiceTypes.value).isEmpty()
        assertThat(viewModel.searchQuery.value).isEmpty()
        assertThat(viewModel.editor.value).isNull()
        assertThat(viewModel.pendingDelete.value).isNull()

        job.cancel()
    }

    @Test
    fun `visible service types are sorted alphabetically ignoring case`() = runTest {
        // Arrange
        stored.value = listOf(
            makeType(1L, "tire rotation"),
            makeType(2L, "Air Filter"),
            makeType(3L, "brake change")
        )
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.visibleServiceTypes.collect {}
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.visibleServiceTypes.value.map { it.serviceName })
            .containsExactly("Air Filter", "brake change", "tire rotation")
            .inOrder()

        job.cancel()
    }

    @Test
    fun `search query filters service types case insensitively`() = runTest {
        // Arrange
        stored.value = listOf(
            makeType(1L, "Oil Change"),
            makeType(2L, "Air Filter"),
            makeType(3L, "Cabin Air Filter")
        )
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.visibleServiceTypes.collect {}
        }

        // Act
        viewModel.onSearchQueryChange("air")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.visibleServiceTypes.value.map { it.serviceName })
            .containsExactly("Air Filter", "Cabin Air Filter")
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
        assertThat(editor.name).isEmpty()
        assertThat(editor.notes).isEmpty()
    }

    @Test
    fun `onEditClick opens an editor prefilled with the item`() {
        // Arrange
        val item = makeType(5L, "Oil Change", notes = "Every 10k km")

        // Act
        viewModel.onEditClick(item)

        // Assert
        val editor = viewModel.editor.value
        assertThat(editor?.id).isEqualTo(5L)
        assertThat(editor?.isNew).isFalse()
        assertThat(editor?.name).isEqualTo("Oil Change")
        assertThat(editor?.notes).isEqualTo("Every 10k km")
    }

    @Test
    fun `onEditorConfirm inserts a trimmed type and closes the dialog`() = runTest {
        // Arrange
        val captured = slot<MaintenanceServiceType>()
        coEvery { repository.insert(capture(captured)) } returns 1L
        viewModel.onCreateClick()
        viewModel.onEditorNameChange("  New Service  ")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(captured.captured.serviceName).isEqualTo("New Service")
        assertThat(captured.captured.notes).isNull()
        assertThat(viewModel.editor.value).isNull()
    }

    @Test
    fun `onEditorConfirm stores blank notes as null`() = runTest {
        // Arrange
        val captured = slot<MaintenanceServiceType>()
        coEvery { repository.insert(capture(captured)) } returns 1L
        viewModel.onCreateClick()
        viewModel.onEditorNameChange("Oil Change")
        viewModel.onEditorNotesChange("   ")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(captured.captured.notes).isNull()
    }

    @Test
    fun `onEditorConfirm updates an existing type keeping its id`() = runTest {
        // Arrange
        val existing = makeType(5L, "Oil Change", notes = null)
        val captured = slot<MaintenanceServiceType>()
        stored.value = listOf(existing)
        coEvery { repository.getById(5L) } returns existing
        coEvery { repository.update(capture(captured)) } just runs

        viewModel.onEditClick(existing)
        viewModel.onEditorNameChange("Oil & Filter")
        viewModel.onEditorNotesChange("Every 10k km")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(captured.captured.id).isEqualTo(5L)
        assertThat(captured.captured.serviceName).isEqualTo("Oil & Filter")
        assertThat(captured.captured.notes).isEqualTo("Every 10k km")
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
        assertThat(viewModel.editor.value?.nameError).isEqualTo(ServiceTypeNameError.EMPTY)
        assertThat(viewModel.editor.value).isNotNull()
        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `onEditorConfirm rejects a duplicate name from another type`() = runTest {
        // Arrange
        stored.value = listOf(makeType(1L, "Oil Change"))
        every { repository.getAll() } returns flowOf(stored.value)
        viewModel.onCreateClick()
        viewModel.onEditorNameChange("oil change")

        // Act
        viewModel.onEditorConfirm()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.editor.value?.nameError).isEqualTo(ServiceTypeNameError.DUPLICATE)
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
    fun `editing a type does not treat its own name as duplicate`() = runTest {
        // Arrange
        val existing = makeType(1L, "Oil Change")
        val updated = slot<MaintenanceServiceType>()
        stored.value = listOf(existing)
        coEvery { repository.getById(1L) } returns existing
        coEvery { repository.update(capture(updated)) } just runs

        viewModel.onEditClick(existing)
        viewModel.onEditorNameChange("Oil Change")

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
        val item = makeType(3L, "Tire Rotation")
        coEvery { repository.countUsage(3L) } returns 4

        // Act
        viewModel.onDeleteClick(item)
        advanceUntilIdle()

        // Assert
        val pending = viewModel.pendingDelete.value
        assertThat(pending?.target).isEqualTo(item)
        assertThat(pending?.usageCount).isEqualTo(4)
    }

    @Test
    fun `onDeleteConfirm deletes the pending type`() = runTest {
        // Arrange
        val item = makeType(3L, "Tire Rotation")
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
        val item = makeType(3L, "Tire Rotation")
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
        val item = makeType(3L, "Tire Rotation")
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

    private fun makeType(id: Long, name: String, notes: String? = null) =
        MaintenanceServiceType(id = id, serviceName = name, notes = notes)
}
