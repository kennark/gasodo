package com.gasodoapp.gasodo.core.enums

/**
 * Possible outcomes of a vehicle inspection.
 */
enum class InspectionStatus(val displayName: String) {
    PASS("Pass"),
    FAIL("Fail"),
    CONDITIONAL_PASS("Conditional Pass");

    override fun toString(): String = displayName
}
