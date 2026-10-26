package com.miagenda.app.domain

import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import com.miagenda.app.domain.task.TaskGroup
import com.miagenda.app.domain.task.groupedTasks
import com.miagenda.app.domain.task.taskGroup
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskListsTest {
    private val today = LocalDate.of(2026, 10, 25)
    private val stamp = LocalDateTime.of(2026, 10, 25, 9, 0)

    @Test
    fun unaTareaPendienteSeAgrupaPorSuFecha() {
        assertEquals(TaskGroup.OVERDUE, taskGroup(task(1, LocalDate.of(2026, 10, 24)), today))
        assertEquals(TaskGroup.TODAY, taskGroup(task(2, today), today))
        assertEquals(TaskGroup.LATER, taskGroup(task(3, LocalDate.of(2026, 10, 26)), today))
        assertEquals(TaskGroup.UNDATED, taskGroup(task(4, null), today))
    }

    @Test
    fun cambiarLaFechaNoSacaUnaTareaHechaDeHechas() {
        val done = task(7, LocalDate.of(2026, 10, 20)).copy(status = TaskStatus.COMPLETED)
        assertEquals(TaskGroup.DONE, taskGroup(done, today))
        val cancelled = task(8, today).copy(status = TaskStatus.CANCELLED)
        assertEquals(TaskGroup.CANCELLED, taskGroup(cancelled, today))
    }

    @Test
    fun lasAtrasadasSalenPorFechaYLuegoPorPrioridad() {
        val earlyLow = task(1, LocalDate.of(2026, 10, 20), TaskPriority.LOW)
        val earlyHigh = task(2, LocalDate.of(2026, 10, 20), TaskPriority.HIGH)
        val later = task(3, LocalDate.of(2026, 10, 23), TaskPriority.HIGH)
        val sections = groupedTasks(listOf(later, earlyLow, earlyHigh), today)
        assertEquals(TaskGroup.OVERDUE, sections.single().group)
        assertEquals(listOf(2L, 1L, 3L), sections.single().tasks.map { it.id })
    }

    private fun task(
        id: Long,
        dueDate: LocalDate?,
        priority: TaskPriority = TaskPriority.MEDIUM
    ): AgendaTask {
        return AgendaTask(
            id = id,
            title = "Entrega",
            notes = "",
            dueDate = dueDate,
            priority = priority,
            status = TaskStatus.PENDING,
            categoryId = null,
            createdAt = stamp,
            updatedAt = stamp
        )
    }
}
