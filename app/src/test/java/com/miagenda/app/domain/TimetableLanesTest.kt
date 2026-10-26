package com.miagenda.app.domain

import com.miagenda.app.domain.activity.TimedSpan
import com.miagenda.app.domain.activity.assignLanes
import com.miagenda.app.domain.activity.timetableHourRange
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TimetableLanesTest {
    @Test
    fun lasClasesQueSeCruzanCompartenElAncho() {
        val placed = assignLanes(
            listOf(
                span(1, 9, 0, 10, 30),
                span(2, 10, 0, 11, 0),
                span(3, 10, 15, 11, 0)
            )
        ).associateBy { it.id }
        assertEquals(0, placed.getValue(1).lane)
        assertEquals(3, placed.getValue(1).laneCount)
        assertEquals(1, placed.getValue(2).lane)
        assertEquals(3, placed.getValue(2).laneCount)
        assertEquals(2, placed.getValue(3).lane)
        assertEquals(3, placed.getValue(3).laneCount)
        val touching = assignLanes(listOf(span(4, 12, 0, 13, 0), span(5, 13, 0, 14, 0)))
        assertEquals(listOf(0, 0), touching.map { it.lane })
        assertEquals(listOf(1, 1), touching.map { it.laneCount })
    }

    @Test
    fun elHorarioCubreDesdeLasSieteHastaLasDiezSalvoQueHayaClasesFuera() {
        assertEquals(7 until 22, timetableHourRange(emptyList()))
        assertEquals(
            6 until 23,
            timetableHourRange(listOf(span(1, 6, 30, 22, 15)))
        )
    }

    private fun span(id: Long, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimedSpan {
        return TimedSpan(
            id = id,
            start = LocalTime.of(startHour, startMinute),
            end = LocalTime.of(endHour, endMinute)
        )
    }
}
