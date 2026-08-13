package com.example.carrefueltracker.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EventTypeTest {

    @Test
    fun `all event types have correct display names`() {
        assertThat(EventType.REFUEL.displayName).isEqualTo("Refueling")
        assertThat(EventType.MAINTENANCE.displayName).isEqualTo("Maintenance")
        assertThat(EventType.INSPECTION.displayName).isEqualTo("Inspection")
    }

    @Test
    fun `toString returns displayName for each event type`() {
        assertThat(EventType.REFUEL.toString()).isEqualTo("Refueling")
        assertThat(EventType.MAINTENANCE.toString()).isEqualTo("Maintenance")
        assertThat(EventType.INSPECTION.toString()).isEqualTo("Inspection")
    }

    @Test
    fun `there are exactly 3 event types`() {
        assertThat(EventType.entries.size).isEqualTo(3)
    }
}