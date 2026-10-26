package com.miagenda.app.domain.home

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun interface DateProvider {
    fun today(): LocalDate
}

private val spanishLocale: Locale = Locale.forLanguageTag("es-ES")

private val agendaDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", spanishLocale)

fun greetingText(displayName: String): String {
    val trimmed = displayName.trim()
    return if (trimmed.isEmpty()) {
        "Hola"
    } else {
        "Hola, $trimmed"
    }
}

fun agendaDateLabel(date: LocalDate): String {
    return agendaDateFormatter.format(date)
}
