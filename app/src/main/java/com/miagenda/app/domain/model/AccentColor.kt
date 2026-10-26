package com.miagenda.app.domain.model

enum class AccentColor {
    CORAL,
    TEAL,
    VIOLET,
    YELLOW;

    companion object {
        fun fromStorage(value: String?): AccentColor {
            return entries.firstOrNull { color -> color.name == value } ?: CORAL
        }
    }
}
