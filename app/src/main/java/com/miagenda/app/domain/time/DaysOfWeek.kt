package com.miagenda.app.domain.time

/**
 * Días guardados como números ISO-8601 separados por comas.
 * El lunes es 1 y el domingo es 7. Ejemplo: "1,3,5".
 */
fun parseDaysOfWeek(value: String?): Set<Int> {
    if (value.isNullOrBlank()) {
        return emptySet()
    }
    val days = value.split(",").map { token ->
        val day = token.trim().toIntOrNull()
            ?: throw IllegalArgumentException("Invalid day of week")
        require(day in 1..7) { "Invalid day of week" }
        day
    }
    require(days.size == days.toSet().size) { "Repeated day of week" }
    return days.toSet()
}
