package com.example.carrefueltracker.core.database.entity

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SavedLocationTest {

    @Test
    fun `constructor with all parameters sets correct values`() {
        val id = java.util.UUID.randomUUID()
        val name = "Shell Gas Station"
        val latitude = 40.7128
        val longitude = -74.0060
        val address = "123 Main St, New York"

        val location = SavedLocation(id, name, latitude, longitude, address)

        assertThat(location.id).isEqualTo(id)
        assertThat(location.name).isEqualTo(name)
        assertThat(location.latitude).isEqualTo(latitude)
        assertThat(location.longitude).isEqualTo(longitude)
        assertThat(location.address).isEqualTo(address)
    }

    @Test
    fun `constructor with nullable coordinates handles null values`() {
        val location = SavedLocation(
            name = "Unknown Location",
            latitude = null,
            longitude = null,
            address = null
        )

        assertThat(location.latitude).isNull()
        assertThat(location.longitude).isNull()
        assertThat(location.address).isNull()
    }

    @Test
    fun `equality works for identical saved locations`() {
        val id = java.util.UUID.randomUUID()
        val location1 = SavedLocation(id, "Station", 40.0, -70.0, null)
        val location2 = SavedLocation(id, "Station", 40.0, -70.0, null)

        assertThat(location2).isEqualTo(location1)
    }

    @Test
    fun `different saved locations are not equal`() {
        val location1 = SavedLocation(
            name = "Station A",
            latitude = 40.0,
            longitude = -70.0,
            address = null
        )
        val location2 = SavedLocation(
            name = "Station B",
            latitude = 30.0,
            longitude = -60.0,
            address = null
        )

        assertThat(location2).isNotEqualTo(location1)
    }
}