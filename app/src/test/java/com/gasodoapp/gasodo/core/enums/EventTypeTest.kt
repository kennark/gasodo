package com.gasodoapp.gasodo.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [EventType].
 *
 * The user facing wording lives in string resources, which cannot be read from a plain JVM test.
 * These tests instead guard the mapping itself: every entry must point at a real resource and no
 * two entries may share one, which is what a copy-paste slip in the enum would produce.
 */
class EventTypeTest {

    @Test
    fun `every event type maps to a string resource`() {
        EventType.entries.forEach { type ->
            assertThat(type.labelRes).isNotEqualTo(0)
        }
    }

    @Test
    fun `label resources are unique`() {
        val labelRes = EventType.entries.map { it.labelRes }

        assertThat(labelRes).containsNoDuplicates()
    }

    @Test
    fun `there are exactly 3 event types`() {
        assertThat(EventType.entries.size).isEqualTo(3)
    }
}
