package com.miagenda.app.domain.habit

import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.time.parseDaysOfWeek
import java.time.LocalDate
import kotlin.math.roundToInt

data class HabitProgress(
    val percent: Int?,
    val scheduled: Int,
    val completed: Int,
    val partial: Int,
    val notDone: Int,
    val unmarked: Int,
    val streak: Int
)

data class HabitDayRecord(
    val date: LocalDate,
    val status: HabitLogStatus?
)

fun habitOccursOn(habit: Habit, date: LocalDate): Boolean {
    if (date.isBefore(habit.createdAt.toLocalDate())) {
        return false
    }
    return when (habit.frequency) {
        HabitFrequency.DAILY -> true
        HabitFrequency.SPECIFIC_DAYS -> date.dayOfWeek.value in parseDaysOfWeek(habit.daysOfWeek)
    }
}

fun habitProgress(habit: Habit, logs: List<HabitLog>, today: LocalDate): HabitProgress {
    val days = scheduledDates(habit, habit.createdAt.toLocalDate(), today)
    val byDate = logs.filter { it.habitId == habit.id }.associateBy { it.date }
    var completed = 0
    var partial = 0
    var notDone = 0
    var unmarked = 0
    for (day in days) {
        when (byDate[day]?.status) {
            HabitLogStatus.COMPLETED -> completed += 1
            HabitLogStatus.PARTIAL -> partial += 1
            HabitLogStatus.NOT_DONE -> notDone += 1
            null -> unmarked += 1
        }
    }
    val percent = if (days.isEmpty()) {
        null
    } else {
        ((completed + (0.5 * partial)) / days.size * 100.0).roundToInt()
    }
    return HabitProgress(
        percent = percent,
        scheduled = days.size,
        completed = completed,
        partial = partial,
        notDone = notDone,
        unmarked = unmarked,
        streak = completedStreak(days, byDate, today)
    )
}

fun recentScheduledDays(
    habit: Habit,
    logs: List<HabitLog>,
    today: LocalDate,
    limit: Int = 14
): List<HabitDayRecord> {
    val byDate = logs.filter { it.habitId == habit.id }.associateBy { it.date }
    return scheduledDates(habit, habit.createdAt.toLocalDate(), today)
        .asReversed()
        .take(limit)
        .map { date -> HabitDayRecord(date = date, status = byDate[date]?.status) }
}

private fun scheduledDates(habit: Habit, start: LocalDate, end: LocalDate): List<LocalDate> {
    if (end.isBefore(start)) {
        return emptyList()
    }
    return generateSequence(start) { previous ->
        val next = previous.plusDays(1)
        if (next.isAfter(end)) null else next
    }.filter { date -> habitOccursOn(habit, date) }.toList()
}

private fun completedStreak(
    days: List<LocalDate>,
    byDate: Map<LocalDate, HabitLog>,
    today: LocalDate
): Int {
    val past = days.filter { !it.isAfter(today) }.asReversed()
    var index = 0
    if (past.firstOrNull() == today && byDate[today] == null) {
        index = 1
    }
    var streak = 0
    while (index < past.size && byDate[past[index]]?.status == HabitLogStatus.COMPLETED) {
        streak += 1
        index += 1
    }
    return streak
}
