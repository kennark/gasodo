package com.gasodoapp.gasodo.core.enums

import com.gasodoapp.gasodo.core.enums.units.Currency
import com.gasodoapp.gasodo.core.enums.units.Distance
import com.gasodoapp.gasodo.core.enums.units.Liquid
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Cross cutting guard for the enum to string resource mapping.
 *
 * Every enum that is rendered in the UI points at a string resource instead of carrying English
 * text. A zero id means the entry forgot its resource; a duplicate across two different enums means
 * a copy-paste slip. Both would otherwise only surface as wrong text on a device.
 *
 * The wording itself cannot be asserted here because plain JVM tests have no resources, which is
 * why the per enum tests assert their own counts and fallbacks while this test asserts that the
 * whole set of user facing labels is populated and distinct.
 */
class EnumsResourceMappingTest {

    private val allLabelRes: List<Int> =
        EventType.entries.map { it.labelRes } +
                FuelType.entries.map { it.labelRes } +
                PaymentMethod.entries.map { it.labelRes } +
                InspectionStatus.entries.map { it.labelRes } +
                ThemeMode.entries.map { it.labelRes } +
                Distance.entries.map { it.nameRes } +
                Liquid.entries.map { it.nameRes } +
                Currency.entries.map { it.nameRes }

    @Test
    fun `no enum entry is missing its label resource`() {
        assertThat(allLabelRes).doesNotContain(0)
    }

    /**
     * Two enums sharing one resource would render the same word for two different concepts.
     */
    @Test
    fun `no two enum entries share a label resource`() {
        assertThat(allLabelRes).containsNoDuplicates()
    }

    /**
     * The singular unit names are rendered into sentences such as `Price per Liter`, and must not
     * collide with each other or with the plural names.
     */
    @Test
    fun `singular unit resources are populated and distinct`() {
        val singularRes = Distance.entries.map { it.singularNameRes } +
                Liquid.entries.map { it.singularNameRes }

        assertThat(singularRes).doesNotContain(0)
        assertThat(singularRes).containsNoDuplicates()
        assertThat(singularRes).containsNoneIn(allLabelRes)
    }

    @Test
    fun `theme descriptions are populated and distinct`() {
        val descriptionRes = ThemeMode.entries.map { it.descriptionRes }

        assertThat(descriptionRes).doesNotContain(0)
        assertThat(descriptionRes).containsNoDuplicates()
    }

    /**
     * Shorthands and symbols are deliberately not translated, so they stay non blank literals.
     */
    @Test
    fun `unit shorthands and currency symbols stay literal`() {
        Distance.entries.forEach { assertThat(it.shorthand).isNotEmpty() }
        Liquid.entries.forEach { assertThat(it.shorthand).isNotEmpty() }
        Currency.entries.forEach { assertThat(it.symbol).isNotEmpty() }
    }
}
