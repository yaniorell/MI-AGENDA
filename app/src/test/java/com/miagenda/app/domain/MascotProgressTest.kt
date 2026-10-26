package com.miagenda.app.domain

import com.miagenda.app.domain.mascot.MascotStage
import com.miagenda.app.domain.mascot.completedProgress
import com.miagenda.app.domain.mascot.mascotStage
import com.miagenda.app.domain.mascot.rememberedProgress
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class MascotProgressTest {
    @Test
    fun soloCuentaLoCompletadoYNoRetrocede() {
        val now = LocalDateTime.of(2026, 10, 25, 12, 0)
        val progress = completedProgress(
            activities = listOf(
                activity(id = 1, status = ActivityStatus.COMPLETED, ruleId = null),
                activity(id = 2, status = ActivityStatus.COMPLETED, ruleId = 9),
                activity(id = 3, status = ActivityStatus.PARTIAL, ruleId = null)
            ),
            overrides = listOf(
                override(id = 4, status = ActivityStatus.COMPLETED, skipped = false),
                override(id = 5, status = ActivityStatus.COMPLETED, skipped = true),
                override(id = 6, status = ActivityStatus.PENDING, skipped = false)
            ),
            tasks = listOf(
                task(id = 7, status = TaskStatus.COMPLETED),
                task(id = 8, status = TaskStatus.CANCELLED)
            ),
            logs = listOf(
                log(id = 9, status = HabitLogStatus.COMPLETED),
                log(id = 10, status = HabitLogStatus.PARTIAL),
                log(id = 11, status = HabitLogStatus.NOT_DONE)
            )
        )
        assertEquals(4, progress)
        assertEquals(12, rememberedProgress(stored = 12, live = progress))
        assertEquals(progress, rememberedProgress(stored = 0, live = progress))
        assertEquals(MascotStage.SLEEPING, mascotStage(0))
        assertEquals(MascotStage.AWAKE, mascotStage(1))
        assertEquals(MascotStage.AWAKE, mascotStage(7))
        assertEquals(MascotStage.STUDYING, mascotStage(8))
        assertEquals(MascotStage.STUDYING, mascotStage(23))
        assertEquals(MascotStage.SPECIAL, mascotStage(24))
    }

    private fun activity(id: Long, status: ActivityStatus, ruleId: Long?): AgendaActivity {
        val now = LocalDateTime.of(2026, 10, 25, 12, 0)
        return AgendaActivity(
            id = id,
            title = "Clase",
            notes = "",
            date = LocalDate.of(2026, 10, 25),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
            categoryId = null,
            status = status,
            recurrenceRuleId = ruleId,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun override(id: Long, status: ActivityStatus, skipped: Boolean): ActivityOverride {
        val now = LocalDateTime.of(2026, 10, 25, 12, 0)
        return ActivityOverride(
            id = id,
            activityId = 2,
            occurrenceDate = LocalDate.of(2026, 10, 25),
            status = status,
            startTime = null,
            endTime = null,
            isSkipped = skipped,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun task(id: Long, status: TaskStatus): AgendaTask {
        val now = LocalDateTime.of(2026, 10, 25, 12, 0)
        return AgendaTask(
            id = id,
            title = "Informe",
            notes = "",
            dueDate = null,
            priority = TaskPriority.MEDIUM,
            status = status,
            categoryId = null,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun log(id: Long, status: HabitLogStatus): HabitLog {
        val now = LocalDateTime.of(2026, 10, 25, 12, 0)
        return HabitLog(
            id = id,
            habitId = 1,
            date = LocalDate.of(2026, 10, 25),
            status = status,
            createdAt = now,
            updatedAt = now
        )
    }
}
