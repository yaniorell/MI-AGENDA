package com.miagenda.app.domain.activity

import java.time.LocalDate
import java.time.LocalTime

fun parseAgendaDate(value: String): LocalDate? {
    return runCatching { LocalDate.parse(value.trim()) }.getOrNull()
}

fun parseClock(value: String): LocalTime? {
    val match = Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(value.trim()) ?: return null
    val hour = match.groupValues[1].toInt()
    val minute = match.groupValues[2].toInt()
    if (hour !in 0..23 || minute !in 0..59) {
        return null
    }
    return LocalTime.of(hour, minute)
}
