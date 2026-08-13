package com.example.carrefueltracker.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ServiceTypeTest {

    @Test
    fun `all service types have correct display names`() {
        assertThat(ServiceType.BRAKE_DISKS.displayName).isEqualTo("Brake Disks")
        assertThat(ServiceType.BRAKE_PADS.displayName).isEqualTo("Brake Pads")
        assertThat(ServiceType.OIL_CHANGE.displayName).isEqualTo("Oil Change")
    }

    @Test
    fun `toString returns displayName for each service type`() {
        assertThat(ServiceType.BRAKE_DISKS.toString()).isEqualTo("Brake Disks")
        assertThat(ServiceType.BRAKE_PADS.toString()).isEqualTo("Brake Pads")
        assertThat(ServiceType.OIL_CHANGE.toString()).isEqualTo("Oil Change")
    }

    @Test
    fun `there are exactly 3 service types`() {
        assertThat(ServiceType.entries.size).isEqualTo(3)
    }
}