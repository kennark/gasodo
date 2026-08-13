package com.example.carrefueltracker.core.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FuelTypeTest {

    @Test
    fun `all fuel types have correct display names`() {
        assertThat(FuelType.PETROL.displayName).isEqualTo("Petrol")
        assertThat(FuelType.DIESEL.displayName).isEqualTo("Diesel")
        assertThat(FuelType.PREMIUM.displayName).isEqualTo("Premium")
        assertThat(FuelType.ELECTRIC.displayName).isEqualTo("Electric")
        assertThat(FuelType.HYBRID.displayName).isEqualTo("Hybrid")
        assertThat(FuelType.LPG.displayName).isEqualTo("LPG")
        assertThat(FuelType.OTHER.displayName).isEqualTo("Other")
    }

    @Test
    fun `toString returns displayName for each fuel type`() {
        assertThat(FuelType.PETROL.toString()).isEqualTo("Petrol")
        assertThat(FuelType.DIESEL.toString()).isEqualTo("Diesel")
        assertThat(FuelType.PREMIUM.toString()).isEqualTo("Premium")
        assertThat(FuelType.ELECTRIC.toString()).isEqualTo("Electric")
        assertThat(FuelType.HYBRID.toString()).isEqualTo("Hybrid")
        assertThat(FuelType.LPG.toString()).isEqualTo("LPG")
        assertThat(FuelType.OTHER.toString()).isEqualTo("Other")
    }

    @Test
    fun `there are exactly 7 fuel types`() {
        assertThat(FuelType.entries.size).isEqualTo(7)
    }
}