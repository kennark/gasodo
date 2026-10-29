package com.gasodoapp.gasodo.ui.components.catalogue

/**
 * Reason why the name typed into a catalogue editor cannot be saved.
 */
enum class NameError {
    /** The name is blank */
    EMPTY,

    /** Another entry already uses this name */
    DUPLICATE,
}

/**
 * User facing message for a [NameError].
 *
 * @param type name of the thing being edited
 */
fun NameError.message(type: String): String = when (this) {
    NameError.EMPTY -> "Enter a name"
    NameError.DUPLICATE -> "A $type with this name already exists"
}
