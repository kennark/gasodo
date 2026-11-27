package com.gasodoapp.gasodo.feature.inspection

import androidx.paging.PagingData
import com.gasodoapp.gasodo.core.database.entity.InspectionEvent
import com.gasodoapp.gasodo.core.database.junctions.InspectionEventWithParts
import com.gasodoapp.gasodo.core.database.repository.InspectionRepository
import com.gasodoapp.gasodo.core.database.repository.SavedLocationRepository
import com.gasodoapp.gasodo.core.settings.SettingsRepository
import com.gasodoapp.gasodo.core.settings.Units
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for [InspectionScreenViewModel].
 * Tests cover the multi-select state machine and the bulk delete it triggers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InspectionScreenViewModelTest {

    private lateinit var inspectionRepository: InspectionRepository
    private lateinit var locationRepository: SavedLocationRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: InspectionScreenViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        inspectionRepository = mockk(relaxed = true)
        locationRepository = mockk(relaxed = true)
        settingsRepository = mockk()
        every { inspectionRepository.getAllByDatePaged() } returns
                emptyFlow<PagingData<InspectionEventWithParts>>()
        every { settingsRepository.units } returns MutableStateFlow(Units())
        viewModel = InspectionScreenViewModel(
            inspectionRepository,
            locationRepository,
            settingsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selection starts inactive and empty`() = runTest {
        // Assert
        assertThat(viewModel.selection.value.active).isFalse()
        assertThat(viewModel.selection.value.ids).isEmpty()
        assertThat(viewModel.selection.value.count).isEqualTo(0)
    }

    @Test
    fun `onSelectClick activates selection mode without selecting anything`() = runTest {
        // Act
        viewModel.onSelectClick()

        // Assert
        assertThat(viewModel.selection.value.active).isTrue()
        assertThat(viewModel.selection.value.ids).isEmpty()
        assertThat(viewModel.selection.value.count).isEqualTo(0)
    }

    @Test
    fun `onEventLongPress activates selection mode and selects the event`() = runTest {
        // Arrange
        val id = UUID.randomUUID()

        // Act
        viewModel.onEventLongPress(id)

        // Assert
        assertThat(viewModel.selection.value.active).isTrue()
        assertThat(viewModel.selection.value.ids).containsExactly(id)
        assertThat(viewModel.selection.value.count).isEqualTo(1)
    }

    @Test
    fun `onEventLongPress on an already selected event keeps it selected once`() = runTest {
        // Arrange
        val id = UUID.randomUUID()

        // Act
        viewModel.onEventLongPress(id)
        viewModel.onEventLongPress(id)

        // Assert
        assertThat(viewModel.selection.value.ids).containsExactly(id)
        assertThat(viewModel.selection.value.count).isEqualTo(1)
    }

    @Test
    fun `onEventLongPress adds to an existing selection`() = runTest {
        // Arrange
        val first = UUID.randomUUID()
        val second = UUID.randomUUID()

        // Act
        viewModel.onEventLongPress(first)
        viewModel.onEventLongPress(second)

        // Assert
        assertThat(viewModel.selection.value.ids).containsExactly(first, second)
        assertThat(viewModel.selection.value.count).isEqualTo(2)
    }

    @Test
    fun `onEventTap does nothing while selection mode is off`() = runTest {
        // Act
        viewModel.onEventTap(UUID.randomUUID())

        // Assert
        assertThat(viewModel.selection.value.active).isFalse()
        assertThat(viewModel.selection.value.ids).isEmpty()
    }

    @Test
    fun `onEventTap deselects an already selected event`() = runTest {
        // Arrange
        val kept = UUID.randomUUID()
        val removed = UUID.randomUUID()
        viewModel.onEventLongPress(kept)
        viewModel.onEventLongPress(removed)

        // Act
        viewModel.onEventTap(removed)

        // Assert
        assertThat(viewModel.selection.value.active).isTrue()
        assertThat(viewModel.selection.value.ids).containsExactly(kept)
    }

    @Test
    fun `onEventTap selects an unselected event while in selection mode`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        viewModel.onSelectClick()

        // Act
        viewModel.onEventTap(id)

        // Assert
        assertThat(viewModel.selection.value.ids).containsExactly(id)
    }

    @Test
    fun `deselecting the last event leaves selection mode`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        viewModel.onEventLongPress(id)

        // Act
        viewModel.onEventTap(id)

        // Assert
        assertThat(viewModel.selection.value.active).isFalse()
        assertThat(viewModel.selection.value.ids).isEmpty()
    }

    @Test
    fun `onClearSelection returns to the initial state`() = runTest {
        // Arrange
        viewModel.onEventLongPress(UUID.randomUUID())
        viewModel.onEventLongPress(UUID.randomUUID())

        // Act
        viewModel.onClearSelection()

        // Assert
        assertThat(viewModel.selection.value.active).isFalse()
        assertThat(viewModel.selection.value.ids).isEmpty()
    }

    @Test
    fun `onDeleteSelected deletes exactly the selected events and clears the selection`() =
        runTest {
            // Arrange
            val first = UUID.randomUUID()
            val second = UUID.randomUUID()
            viewModel.onEventLongPress(first)
            viewModel.onEventLongPress(second)

            // Act
            viewModel.onDeleteSelected()
            advanceUntilIdle()

            // Assert
            coVerify(exactly = 1) {
                inspectionRepository.deleteByIds(match<Collection<UUID>> {
                    it.toSet() == setOf(first, second)
                })
            }
            assertThat(viewModel.selection.value.active).isFalse()
            assertThat(viewModel.selection.value.ids).isEmpty()
        }

    @Test
    fun `onDeleteSelected does nothing when nothing is selected`() = runTest {
        // Act
        viewModel.onDeleteSelected()
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { inspectionRepository.deleteByIds(any()) }
        assertThat(viewModel.selection.value.active).isFalse()
    }

    @Test
    fun `deleting a single event still delegates to the repository`() = runTest {
        // Arrange
        val eventWithParts = mockk<InspectionEventWithParts>(relaxed = true)

        // Act
        viewModel.onDeleteInspectionEvent(eventWithParts)
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { inspectionRepository.delete(eventWithParts.event) }
        coVerify(exactly = 0) { inspectionRepository.deleteByIds(any()) }
    }

    // region Expansion

    @Test
    fun `expanded ids start empty`() = runTest {
        // Assert
        assertThat(viewModel.expandedIds.value).isEmpty()
    }

    @Test
    fun `onToggleExpanded opens a closed event`() = runTest {
        // Arrange
        val id = UUID.randomUUID()

        // Act
        viewModel.onToggleExpanded(id)

        // Assert
        assertThat(viewModel.expandedIds.value).containsExactly(id)
    }

    @Test
    fun `onToggleExpanded closes an already open event`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        viewModel.onToggleExpanded(id)

        // Act
        viewModel.onToggleExpanded(id)

        // Assert
        assertThat(viewModel.expandedIds.value).isEmpty()
    }

    @Test
    fun `onToggleExpanded keeps several events open at once`() = runTest {
        // Arrange
        val first = UUID.randomUUID()
        val second = UUID.randomUUID()

        // Act
        viewModel.onToggleExpanded(first)
        viewModel.onToggleExpanded(second)

        // Assert
        assertThat(viewModel.expandedIds.value).containsExactly(first, second)
    }

    @Test
    fun `onCollapseAll closes every open event`() = runTest {
        // Arrange
        viewModel.onToggleExpanded(UUID.randomUUID())
        viewModel.onToggleExpanded(UUID.randomUUID())

        // Act
        viewModel.onCollapseAll()

        // Assert
        assertThat(viewModel.expandedIds.value).isEmpty()
    }

    @Test
    fun `deleting a single event closes it if it was open`() = runTest {
        // Arrange
        val id = UUID.randomUUID()
        val event = mockk<InspectionEvent>(relaxed = true)
        every { event.id } returns id
        val eventWithParts = mockk<InspectionEventWithParts>(relaxed = true)
        every { eventWithParts.event } returns event
        viewModel.onToggleExpanded(id)

        // Act
        viewModel.onDeleteInspectionEvent(eventWithParts)
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.expandedIds.value).isEmpty()
    }

    @Test
    fun `onDeleteSelected closes every deleted event`() = runTest {
        // Arrange
        val first = UUID.randomUUID()
        val second = UUID.randomUUID()
        val kept = UUID.randomUUID()
        viewModel.onToggleExpanded(first)
        viewModel.onToggleExpanded(second)
        viewModel.onToggleExpanded(kept)
        viewModel.onEventLongPress(first)
        viewModel.onEventLongPress(second)

        // Act
        viewModel.onDeleteSelected()
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.expandedIds.value).containsExactly(kept)
    }

    // endregion
}
