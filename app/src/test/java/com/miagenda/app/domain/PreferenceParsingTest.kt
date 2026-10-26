package com.miagenda.app.domain

import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferenceParsingTest {
    @Test
    fun missingSectionsMeanAllVisible() {
        assertEquals(HomeSection.entries.toSet(), HomeSection.fromStorage(null))
    }

    @Test
    fun blankSectionsMeanNoneVisible() {
        assertEquals(emptySet<HomeSection>(), HomeSection.fromStorage(""))
    }

    @Test
    fun unknownSectionTokenIsIgnored() {
        assertEquals(
            setOf(HomeSection.NEXT_ACTIVITY),
            HomeSection.fromStorage("NEXT_ACTIVITY,DESCONOCIDA")
        )
    }

    @Test
    fun sectionsRoundTrip() {
        val sections = setOf(HomeSection.TODAY_AGENDA, HomeSection.QUICK_LINKS)
        assertEquals(sections, HomeSection.fromStorage(HomeSection.toStorage(sections)))
    }

    @Test
    fun unknownAccentFallsBackToCoral() {
        assertEquals(AccentColor.CORAL, AccentColor.fromStorage(null))
        assertEquals(AccentColor.CORAL, AccentColor.fromStorage("ROJO"))
        assertEquals(AccentColor.TEAL, AccentColor.fromStorage("TEAL"))
        assertEquals(AccentColor.VIOLET, AccentColor.fromStorage("VIOLET"))
        assertEquals(AccentColor.YELLOW, AccentColor.fromStorage("YELLOW"))
    }

    @Test
    fun unknownThemeFallsBackToDark() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.DARK, ThemeMode.fromStorage("PURPLE"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromStorage("LIGHT"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorage("SYSTEM"))
    }
}
