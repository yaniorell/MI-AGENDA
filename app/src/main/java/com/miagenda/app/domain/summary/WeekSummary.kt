package com.miagenda.app.domain.summary

import com.miagenda.app.domain.activity.dayProgress
import com.miagenda.app.domain.activity.expandActivities
import com.miagenda.app.domain.activity.mondayOf
import com.miagenda.app.domain.activity.visibleOccurrences
import com.miagenda.app.domain.habit.habitOccursOn
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.model.TaskStatus
import java.time.LocalDate
import kotlin.math.roundToInt

data class WeekBounds(
    val start: LocalDate,
    val end: LocalDate
)

data class WeekActivityStats(
    val scheduled: Int,
    val completed: Int,
    val partial: Int,
    val open: Int,
    val cancelled: Int,
    val percent: Int?
)

data class WeekTaskStats(
    val due: Int,
    val pending: Int,
    val completed: Int,
    val cancelled: Int
)

data class WeekHabitStats(
    val scheduled: Int,
    val completed: Int,
    val partial: Int,
    val notDone: Int,
    val unmarked: Int,
    val percent: Int?
)

data class WeekSummary(
    val bounds: WeekBounds,
    val activities: WeekActivityStats,
    val tasks: WeekTaskStats,
    val habits: WeekHabitStats
)

fun weekBounds(date: LocalDate): WeekBounds {
    val start = mondayOf(date)
    return WeekBounds(start = start, end = start.plusDays(6))
}

fun summarizeWeek(
    activities: List<AgendaActivity>,
    rules: List<RecurrenceRule>,
    overrides: List<ActivityOverride>,
    tasks: List<AgendaTask>,
    habits: List<Habit>,
    logs: List<HabitLog>,
    dateInWeek: LocalDate
): WeekSummary {
    val bounds = weekBounds(dateInWeek)
    val visible = visibleOccurrences(
        expandActivities(activities, rules, overrides, bounds.start, bounds.end)
    )
    val progress = dayProgress(visible)
    val activityStats = WeekActivityStats(
        scheduled = progress?.scheduled ?: 0,
        completed = visible.count { it.status == ActivityStatus.COMPLETED },
        partial = visible.count { it.status == ActivityStatus.PARTIAL },
        open = visible.count { occurrence ->
            occurrence.status == ActivityStatus.PENDING ||
                occurrence.status == ActivityStatus.RESCHEDULED
        },
        cancelled = visible.count { it.status == ActivityStatus.CANCELLED },
        percent = progress?.percent
    )
    val dueTasks = tasks.filter { task ->
        val due = task.dueDate
        due != null && !due.isBefore(bounds.start) && !due.isAfter(bounds.end)
    }
    val taskStats = WeekTaskStats(
        due = dueTasks.size,
        pending = dueTasks.count { it.status == TaskStatus.PENDING },
        completed = dueTasks.count { it.status == TaskStatus.COMPLETED },
        cancelled = dueTasks.count { it.status == TaskStatus.CANCELLED }
    )
    val logsByKey = logs.associateBy { log -> log.habitId to log.date }
    var scheduled = 0
    var completed = 0
    var partial = 0
    var notDone = 0
    var unmarked = 0
    val days = generateSequence(bounds.start) { previous ->
        val next = previous.plusDays(1)
        if (next.isAfter(bounds.end)) null else next
    }
    for (day in days) {
        for (habit in habits) {
            if (!habitOccursOn(habit, day)) {
                continue
            }
            scheduled += 1
            when (logsByKey[habit.id to day]?.status) {
                HabitLogStatus.COMPLETED -> completed += 1
                HabitLogStatus.PARTIAL -> partial += 1
                HabitLogStatus.NOT_DONE -> notDone += 1
                null -> unmarked += 1
            }
        }
    }
    val habitPercent = if (scheduled == 0) {
        null
    } else {
        ((completed + (0.5 * partial)) / scheduled * 100.0).roundToInt()
    }
    return WeekSummary(
        bounds = bounds,
        activities = activityStats,
        tasks = taskStats,
        habits = WeekHabitStats(
            scheduled = scheduled,
            completed = completed,
            partial = partial,
            notDone = notDone,
            unmarked = unmarked,
            percent = habitPercent
        )
    )
}
