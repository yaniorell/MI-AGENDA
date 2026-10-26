package com.miagenda.app.domain

import com.miagenda.app.domain.habit.habitOccursOn
import com.miagenda.app.domain.habit.habitProgress
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitScheduleTest {
    private val created = LocalDateTime.of(2026, 10, 19, 8, 0)
    private val today = LocalDate.of(2026, 10, 25)

    @Test
    fun unHabitoDiarioCuentaDesdeElDiaEnQueSeCrea() {
        val habit = habit(HabitFrequency.DAILY, days = null)
        assertFalse(habitOccursOn(habit, LocalDate.of(2026, 10, 18)))
        assertTrue(habitOccursOn(habit, created.toLocalDate()))
        val progress = habitProgress(
            habit = habit,
            logs = listOf(
                log(habit, LocalDate.of(2026, 10, 19), HabitLogStatus.COMPLETED),
                log(habit, LocalDate.of(2026, 10, 20), HabitLogStatus.PARTIAL),
                log(habit, LocalDate.of(2026, 10, 21), HabitLogStatus.NOT_DONE)
            ),
            today = today
        )
        assertEquals(7, progress.scheduled)
        assertEquals(1, progress.completed)
        assertEquals(1, progress.partial)
        assertEquals(1, progress.notDone)
        assertEquals(4, progress.unmarked)
        assertEquals(21, progress.percent)
        assertEquals(0, progress.streak)
    }

    @Test
    fun sinDiasProgramadosElPorcentajeNoEsCero() {
        val habit = habit(HabitFrequency.DAILY, days = null).copy(
            createdAt = LocalDateTime.of(2026, 10, 26, 8, 0)
        )
        val progress = habitProgress(habit, emptyList(), today)
        assertEquals(0, progress.scheduled)
        assertNull(progress.percent)
    }

    @Test
    fun unDiaSinRegistroEntraEnElPorcentajePeroNoEsNoHecho() {
        val habit = habit(HabitFrequency.DAILY, days = null).copy(
            createdAt = LocalDateTime.of(2026, 10, 24, 8, 0)
        )
        val progress = habitProgress(
            habit = habit,
            logs = listOf(log(habit, LocalDate.of(2026, 10, 24), HabitLogStatus.COMPLETED)),
            today = today
        )
        assertEquals(2, progress.scheduled)
        assertEquals(1, progress.unmarked)
        assertEquals(0, progress.notDone)
        assertEquals(50, progress.percent)
    }

    @Test
    fun laRachaSaltaElDiaDeHoySiTodaviaNoTieneRegistro() {
        val habit = habit(HabitFrequency.DAILY, days = null)
        val progress = habitProgress(
            habit = habit,
            logs = listOf(
                log(habit, LocalDate.of(2026, 10, 23), HabitLogStatus.COMPLETED),
                log(habit, LocalDate.of(2026, 10, 24), HabitLogStatus.COMPLETED)
            ),
            today = today
        )
        assertEquals(2, progress.streak)
    }

    @Test
    fun unParcialCortaLaRacha() {
        val day = LocalDate.of(2026, 10, 21)
        val habit = habit(HabitFrequency.DAILY, days = null)
        val progress = habitProgress(
            habit = habit,
            logs = listOf(
                log(habit, LocalDate.of(2026, 10, 19), HabitLogStatus.COMPLETED),
                log(habit, LocalDate.of(2026, 10, 20), HabitLogStatus.PARTIAL),
                log(habit, day, HabitLogStatus.COMPLETED)
            ),
            today = day
        )
        assertEquals(1, progress.streak)
    }

    @Test
    fun losDiasConcretosSoloCuentanEsosDias() {
        val habit = habit(HabitFrequency.SPECIFIC_DAYS, days = "1,3")
        val progress = habitProgress(habit, emptyList(), today)
        assertEquals(2, progress.scheduled)
        assertEquals(0, progress.percent)
        assertFalse(habitOccursOn(habit, LocalDate.of(2026, 10, 20)))
        assertTrue(habitOccursOn(habit, LocalDate.of(2026, 10, 21)))
    }

    private fun habit(frequency: HabitFrequency, days: String?): Habit {
        return Habit(
            id = 4,
            title = "Leer",
            notes = "",
            categoryId = null,
            frequency = frequency,
            daysOfWeek = days,
            isArchived = false,
            createdAt = created,
            updatedAt = created
        )
    }

    private fun log(habit: Habit, date: LocalDate, status: HabitLogStatus): HabitLog {
        return HabitLog(
            id = date.dayOfMonth.toLong(),
            habitId = habit.id,
            date = date,
            status = status,
            createdAt = created,
            updatedAt = created
        )
    }
}
