package com.miagenda.app.domain.preferences

const val MAX_DISPLAY_NAME_LENGTH = 40

sealed interface NameValidation {
    data class Ok(val value: String) : NameValidation
    data object TooLong : NameValidation
}

fun validateDisplayName(raw: String): NameValidation {
    val trimmed = raw.trim()
    return if (trimmed.length > MAX_DISPLAY_NAME_LENGTH) {
        NameValidation.TooLong
    } else {
        NameValidation.Ok(trimmed)
    }
}
