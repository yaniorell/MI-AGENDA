package com.miagenda.app.domain

import com.miagenda.app.domain.home.agendaDateLabel
import com.miagenda.app.domain.home.greetingText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HomeTextTest {
    @Test
    fun greetingWithoutNameIsJustHello() {
        assertEquals("Hola", greetingText(""))
        assertEquals("Hola", greetingText("   "))
    }

    @Test
    fun greetingUsesTheTrimmedName() {
        assertEquals("Hola, Ana", greetingText("  Ana  "))
    }

    @Test
    fun dateUsesSpanishWords() {
        assertEquals("domingo 25 de octubre", agendaDateLabel(LocalDate.of(2026, 10, 25)))
    }

    @Test
    fun leapDayFormatsWithoutFailing() {
        val label = agendaDateLabel(LocalDate.of(2028, 2, 29))
        assertTrue(label.contains("29"))
        assertTrue(label.contains("febrero"))
    }
}
