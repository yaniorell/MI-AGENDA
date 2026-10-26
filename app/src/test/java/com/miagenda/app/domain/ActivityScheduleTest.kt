package com.miagenda.app.domain

import com.miagenda.app.domain.activity.dayProgress
import com.miagenda.app.domain.activity.expandActivities
import com.miagenda.app.domain.activity.mondayOf
import com.miagenda.app.domain.activity.nextOpenOccurrence
import com.miagenda.app.domain.activity.parseAgendaDate
import com.miagenda.app.domain.activity.parseClock
import com.miagenda.app.domain.activity.visibleOccurrences
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.RecurrenceRule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ActivityScheduleTest {
    private val now = LocalDateTime.of(2026, 10, 19, 8, 0)

    @Test
    fun laSemanaEmpiezaEnLunes() {
        assertEquals(LocalDate.of(2026, 10, 19), mondayOf(LocalDate.of(2026, 10, 25)))
    }

    @Test
    fun unaActividadSinRepeticionSoloSaleSuDia() {
        val activity = activity(date = LocalDate.of(2026, 10, 21))
        val week = expandActivities(
            activities = listOf(activity),
            rules = emptyList(),
            overrides = emptyList(),
            rangeStart = LocalDate.of(2026, 10, 19),
            rangeEnd = LocalDate.of(2026, 10, 25)
        )
        assertEquals(listOf(LocalDate.of(2026, 10, 21)), week.map { it.date })
    }

    @Test
    fun cadaDosDiasRespetaElIntervaloYLaFechaFinal() {
        val activity = activity(date = LocalDate.of(2026, 10, 19), ruleId = 4)
        val rule = rule(
            id = 4,
            frequency = RecurrenceFrequency.DAILY,
            interval = 2,
            start = LocalDate.of(2026, 10, 19),
            end = LocalDate.of(2026, 10, 23)
        )
        val dates = expandActivities(
            activities = listOf(activity),
            rules = listOf(rule),
            overrides = emptyList(),
            rangeStart = LocalDate.of(2026, 10, 19),
            rangeEnd = LocalDate.of(2026, 10, 25)
        ).map { it.date }
        assertEquals(
            listOf(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 21), LocalDate.of(2026, 10, 23)),
            dates
        )
    }

    @Test
    fun losDiasConcretosSoloCaenEnEsosDias() {
        val activity = activity(date = LocalDate.of(2026, 10, 19), ruleId = 2)
        val rule = rule(
            id = 2,
            frequency = RecurrenceFrequency.SPECIFIC_DAYS,
            interval = 1,
            days = "1,3",
            start = LocalDate.of(2026, 10, 19)
        )
        val dates = expandActivities(
            activities = listOf(activity),
            rules = listOf(rule),
            overrides = emptyList(),
            rangeStart = LocalDate.of(2026, 10, 19),
            rangeEnd = LocalDate.of(2026, 10, 25)
        ).map { it.date }
        assertEquals(listOf(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 21)), dates)
    }

    @Test
    fun cadaDosSemanasMantieneElMismoDia() {
        val activity = activity(date = LocalDate.of(2026, 10, 21), ruleId = 8)
        val rule = rule(
            id = 8,
            frequency = RecurrenceFrequency.EVERY_N_WEEKS,
            interval = 2,
            start = LocalDate.of(2026, 10, 21)
        )
        val dates = expandActivities(
            activities = listOf(activity),
            rules = listOf(rule),
            overrides = emptyList(),
            rangeStart = LocalDate.of(2026, 10, 21),
            rangeEnd = LocalDate.of(2026, 11, 4)
        ).map { it.date }
        assertEquals(listOf(LocalDate.of(2026, 10, 21), LocalDate.of(2026, 11, 4)), dates)
    }

    @Test
    fun unDiaSaltadoNoSeMuestraYOtroDiaPuedeCambiarEstado() {
        val activity = activity(date = LocalDate.of(2026, 10, 19), ruleId = 3)
        val rule = rule(
            id = 3,
            frequency = RecurrenceFrequency.DAILY,
            interval = 1,
            start = LocalDate.of(2026, 10, 19),
            end = LocalDate.of(2026, 10, 20)
        )
        val occurrences = expandActivities(
            activities = listOf(activity),
            rules = listOf(rule),
            overrides = listOf(
                override(date = LocalDate.of(2026, 10, 19), skipped = true),
                override(date = LocalDate.of(2026, 10, 20), status = ActivityStatus.COMPLETED)
            ),
            rangeStart = LocalDate.of(2026, 10, 19),
            rangeEnd = LocalDate.of(2026, 10, 20)
        )
        val visible = visibleOccurrences(occurrences)
        assertEquals(listOf(LocalDate.of(2026, 10, 20)), visible.map { it.date })
        assertEquals(ActivityStatus.COMPLETED, visible.single().status)
        assertEquals(ActivityStatus.PENDING, activity.status)
    }

    @Test
    fun elProgresoCuentaLaParcialComoMedia() {
        val items = listOf(
            occurrence(ActivityStatus.COMPLETED),
            occurrence(ActivityStatus.PARTIAL),
            occurrence(ActivityStatus.CANCELLED)
        )
        val progress = dayProgress(items)
        assertEquals(75, progress?.percent)
        assertEquals(2, progress?.scheduled)
    }

    @Test
    fun sinActividadesContablesNoHayPorcentaje() {
        assertNull(dayProgress(listOf(occurrence(ActivityStatus.CANCELLED))))
    }

    @Test
    fun laSiguienteEsLaPrimeraQueTodaviaNoTermina() {
        val early = occurrence(ActivityStatus.PENDING, start = LocalTime.of(8, 0), end = LocalTime.of(9, 0))
        val next = occurrence(ActivityStatus.PENDING, start = LocalTime.of(11, 0), end = LocalTime.of(12, 0))
        val chosen = nextOpenOccurrence(listOf(early, next), LocalTime.of(10, 0))
        assertEquals(LocalTime.of(11, 0), chosen?.startTime)
    }

    @Test
    fun laHoraYLaFechaAceptanElFormatoDeLaPantalla() {
        assertEquals(LocalTime.of(9, 5), parseClock("9:05"))
        assertNull(parseClock("24:00"))
        assertEquals(LocalDate.of(2026, 10, 25), parseAgendaDate(" 2026-10-25 "))
        assertNull(parseAgendaDate("25/10/2026"))
    }

    private fun activity(date: LocalDate, ruleId: Long? = null) = AgendaActivity(
        id = 1,
        title = "Clase",
        notes = "",
        date = date,
        startTime = LocalTime.of(9, 0),
        endTime = LocalTime.of(10, 0),
        categoryId = null,
        status = ActivityStatus.PENDING,
        recurrenceRuleId = ruleId,
        createdAt = now,
        updatedAt = now
    )

    private fun rule(
        id: Long,
        frequency: RecurrenceFrequency,
        interval: Int,
        start: LocalDate,
        end: LocalDate? = null,
        days: String? = null
    ) = RecurrenceRule(
        id = id,
        frequency = frequency,
        intervalCount = interval,
        daysOfWeek = days,
        startDate = start,
        endDate = end,
        createdAt = now,
        updatedAt = now
    )

    private fun override(
        date: LocalDate,
        skipped: Boolean = false,
        status: ActivityStatus? = null
    ) = ActivityOverride(
        id = date.dayOfMonth.toLong(),
        activityId = 1,
        occurrenceDate = date,
        status = status,
        startTime = null,
        endTime = null,
        isSkipped = skipped,
        createdAt = now,
        updatedAt = now
    )

    private fun occurrence(
        status: ActivityStatus,
        start: LocalTime = LocalTime.of(9, 0),
        end: LocalTime = LocalTime.of(10, 0)
    ) = com.miagenda.app.domain.activity.ActivityOccurrence(
        activity = activity(LocalDate.of(2026, 10, 25)),
        date = LocalDate.of(2026, 10, 25),
        startTime = start,
        endTime = end,
        status = status,
        isSkipped = false
    )
}
