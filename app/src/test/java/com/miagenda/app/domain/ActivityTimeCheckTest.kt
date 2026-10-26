package com.miagenda.app.domain

import com.miagenda.app.domain.activity.ActivityTimeCheck
import com.miagenda.app.domain.activity.checkActivityTimes
import com.miagenda.app.domain.time.parseDaysOfWeek
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityTimeCheckTest {
    @Test
    fun horaIgualEsValida() {
        val noon = LocalTime.of(12, 0)
        assertEquals(ActivityTimeCheck.Valid, checkActivityTimes(noon, noon))
    }

    @Test
    fun horaDeFinAnteriorSeRechaza() {
        val result = checkActivityTimes(LocalTime.of(18, 0), LocalTime.of(9, 0))
        assertEquals(ActivityTimeCheck.EndBeforeStart, result)
    }

    @Test
    fun diasDeLaSemanaAceptanLunesADomingo() {
        assertEquals(setOf(1, 3, 7), parseDaysOfWeek(" 7, 1, 3 "))
    }

    @Test
    fun diaFueraDeRangoSeRechaza() {
        val error = runCatching { parseDaysOfWeek("8") }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}