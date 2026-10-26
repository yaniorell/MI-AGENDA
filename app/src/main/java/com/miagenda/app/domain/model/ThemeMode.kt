package com.miagenda.app.domain.model

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM;

    companion object {
        fun fromStorage(value: String?): ThemeMode {
            return when (value) {
                LIGHT.name -> LIGHT
                SYSTEM.name -> SYSTEM
                else -> DARK
            }
        }
    }
}
