package com.gasodoapp.gasodo.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

class DbConverterUtilsTest {

    private val utils = DbConverterUtils()
    private val uuidString = "30c2d08a-a9de-4c30-94c6-32506663d336"

    // List<String> ↔ String (comma-separated)
    private val testStringList = listOf("oil", "filter", "wipers")
    private val commaSeparatedString = "oil,filter,wipers"

    // BigDecimal ↔ String (3 decimal places)
    private val bigDecimalWith3Places = BigDecimal("12.345")
    private val bigDecimalWith5Places = BigDecimal("12.34567")
    private val bigDecimalWith1Place = BigDecimal("12.3")
    private val bigDecimalWithNoPlaces = BigDecimal.TEN

    @Test
    fun `fromUuid with UUID returns String`() {
        val uuid = UUID.fromString(uuidString)

        assertThat(utils.fromUuid(uuid)).isEqualTo(uuidString)
    }

    @Test
    fun `fromUuid with null returns null`() {
        assertThat(utils.fromUuid(null)).isEqualTo(null)
    }

    @Test
    fun `toUuid with string returns UUID`() {
        val uuid = UUID.fromString(uuidString)
        val otherUuid = utils.toUuid(uuidString)
        assertThat(otherUuid!!.leastSignificantBits).isEqualTo(uuid.leastSignificantBits)
        assertThat(otherUuid.mostSignificantBits).isEqualTo(uuid.mostSignificantBits)
    }

    @Test
    fun `toUuid with null returns null`() {
        val uuid = utils.toUuid(null)
        assertThat(uuid).isNull()
    }

    @Test
    fun `fromStringList with list returns comma-separated string`() {
        assertThat(utils.fromStringList(testStringList)).isEqualTo(commaSeparatedString)
    }

    @Test
    fun `fromStringList with empty list returns empty string`() {
        assertThat(utils.fromStringList(emptyList())).isEqualTo("")
    }

    @Test
    fun `toStringList with comma-separated string returns list`() {
        val result = utils.toStringList(commaSeparatedString)
        assertThat(result).containsExactly(*testStringList.toTypedArray())
    }

    @Test
    fun `toStringList with empty string returns empty list`() {
        val result = utils.toStringList("")
        assertThat(result).isEmpty()
    }

    @Test
    fun `fromBigDecimal with BigDecimal no places returns string with 3 decimals`() {
        assertThat(utils.fromBigDecimal(bigDecimalWithNoPlaces)).isEqualTo("10.000")
    }

    @Test
    fun `fromBigDecimal with BigDecimal 1 place returns string with 3 decimals`() {
        assertThat(utils.fromBigDecimal(bigDecimalWith1Place)).isEqualTo("12.300")
    }

    @Test
    fun `fromBigDecimal with BigDecimal 3 places returns string with 3 decimals`() {
        assertThat(utils.fromBigDecimal(bigDecimalWith3Places)).isEqualTo("12.345")
    }

    @Test
    fun `fromBigDecimal with BigDecimal 5 places returns string with 3 decimals and rounded up`() {
        assertThat(utils.fromBigDecimal(bigDecimalWith5Places)).isEqualTo("12.346")
    }

    @Test
    fun `fromBigDecimal with null returns null`() {
        assertThat(utils.fromBigDecimal(null)).isNull()
    }

    @Test
    fun `toBigDecimal with string returns BigDecimal`() {
        val result = utils.toBigDecimal("12.345")
        assertThat(result).isEqualTo(bigDecimalWith3Places)
    }

    @Test
    fun `toBigDecimal with null returns null`() {
        assertThat(utils.toBigDecimal(null)).isNull()
    }

    @Test
    fun `toBigDecimal with empty string returns null`() {
        assertThat(utils.toBigDecimal("")).isNull()
    }

    @Test
    fun `toBigDecimal with invalid string returns null`() {
        assertThat(utils.toBigDecimal("asdf")).isNull()
    }
} 