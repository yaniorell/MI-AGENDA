package com.miagenda.app.domain

import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import com.miagenda.app.domain.summary.summarizeWeek
import com.miagenda.app.domain.summary.weekBounds
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeekSummaryTest {
    private val stamp = LocalDateTime.of(2026, 10, 19, 8, 0)
    private val sunday = LocalDate.of(2026, 10, 25)

    @Test
    fun laSemanaVaDeLunesADomingo() {
        val bounds = weekBounds(sunday)
        assertEquals(LocalDate.of(2026, 10, 19), bounds.start)
        assertEquals(sunday, bounds.end)
    }

    @Test
    fun elResumenCuentaLaSemanaYDejaFueraLoDemas() {
        val habit = Habit(
            id = 3,
            title = "Leer",
            notes = "",
            categoryId = null,
            frequency = HabitFrequency.DAILY,
            daysOfWeek = null,
            isArchived = false,
            createdAt = stamp,
            updatedAt = stamp
        )
        val summary = summarizeWeek(
            activities = listOf(
                activity(1, LocalDate.of(2026, 10, 21), ActivityStatus.COMPLETED),
                activity(2, LocalDate.of(2026, 10, 22), ActivityStatus.PENDING),
                activity(3, LocalDate.of(2026, 10, 23), ActivityStatus.CANCELLED),
                activity(4, LocalDate.of(2026, 10, 26), ActivityStatus.COMPLETED)
            ),
            rules = emptyList(),
            overrides = emptyList(),
            tasks = listOf(
                task(1, LocalDate.of(2026, 10, 21), TaskStatus.PENDING),
                task(2, LocalDate.of(2026, 10, 24), TaskStatus.COMPLETED),
                task(3, null, TaskStatus.PENDING),
                task(4, LocalDate.of(2026, 11, 2), TaskStatus.PENDING)
            ),
            habits = listOf(habit),
            logs = listOf(
                HabitLog(1, habit.id, LocalDate.of(2026, 10, 19), HabitLogStatus.COMPLETED, stamp, stamp),
                HabitLog(2, habit.id, LocalDate.of(2026, 10, 20), HabitLogStatus.PARTIAL, stamp, stamp)
            ),
            dateInWeek = sunday
        )
        assertEquals(2, summary.activities.scheduled)
        assertEquals(1, summary.activities.completed)
        assertEquals(1, summary.activities.open)
        assertEquals(1, summary.activities.cancelled)
        assertEquals(50, summary.activities.percent)
        assertEquals(2, summary.tasks.due)
        assertEquals(1, summary.tasks.pending)
        assertEquals(1, summary.tasks.completed)
        assertEquals(7, summary.habits.scheduled)
        assertEquals(1, summary.habits.completed)
        assertEquals(1, summary.habits.partial)
        assertEquals(5, summary.habits.unmarked)
        assertEquals(21, summary.habits.percent)
    }

    @Test
    fun unaSemanaSinNadaNoMuestraCeroPorCiento() {
        val summary = summarizeWeek(
            activities = emptyList(),
            rules = emptyList(),
            overrides = emptyList(),
            tasks = emptyList(),
            habits = emptyList(),
            logs = emptyList(),
            dateInWeek = sunday
        )
        assertNull(summary.activities.percent)
        assertNull(summary.habits.percent)
        assertEquals(0, summary.tasks.due)
    }

    private fun activity(id: Long, date: LocalDate, status: ActivityStatus): AgendaActivity {
        return AgendaActivity(
            id = id,
            title = "Clase",
            notes = "",
            date = date,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            categoryId = null,
            status = status,
            recurrenceRuleId = null,
            createdAt = stamp,
            updatedAt = stamp
        )
    }

    private fun task(id: Long, dueDate: LocalDate?, status: TaskStatus): AgendaTask {
        return AgendaTask(
            id = id,
            title = "Entrega",
            notes = "",
            dueDate = dueDate,
            priority = TaskPriority.MEDIUM,
            status = status,
            categoryId = null,
            createdAt = stamp,
            updatedAt = stamp
        )
    }
}
