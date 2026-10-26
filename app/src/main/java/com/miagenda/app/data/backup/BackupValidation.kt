package com.miagenda.app.data.backup

import com.miagenda.app.domain.activity.ActivityTimeCheck
import com.miagenda.app.domain.activity.checkActivityTimes
import com.miagenda.app.domain.backup.AgendaSnapshot
import com.miagenda.app.domain.backup.BackupContents
import com.miagenda.app.domain.backup.BackupFormatException
import com.miagenda.app.domain.backup.BackupProblem
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.model.UserPreferences
import com.miagenda.app.domain.preferences.NameValidation
import com.miagenda.app.domain.preferences.validateDisplayName
import com.miagenda.app.domain.time.parseDaysOfWeek

private const val MAX_REFLECTION_NOTE = 2_000

internal fun BackupContents.checked(): BackupContents {
    val categories = snapshot.categories.map { category -> category.checked() }
    requireIds(categories.map { category -> category.id })
    val categoryIds = categories.map { category -> category.id }.toSet()

    val rules = snapshot.rules.map { rule -> rule.checked() }
    requireIds(rules.map { rule -> rule.id })
    val ruleIds = rules.map { rule -> rule.id }.toSet()

    val activities = snapshot.activities.map { activity -> activity.checked(categoryIds, ruleIds) }
    requireIds(activities.map { activity -> activity.id })
    val activityIds = activities.map { activity -> activity.id }.toSet()

    val overrides = snapshot.overrides.map { override -> override.checked(activityIds) }
    requireIds(overrides.map { override -> override.id })
    val overrideKeys = overrides.map { override -> override.activityId to override.occurrenceDate }
    if (overrideKeys.size != overrideKeys.toSet().size) {
        invalid()
    }

    val tasks = snapshot.tasks.map { task -> task.checked(categoryIds) }
    requireIds(tasks.map { task -> task.id })

    val habits = snapshot.habits.map { habit -> habit.checked(categoryIds) }
    requireIds(habits.map { habit -> habit.id })
    val habitIds = habits.map { habit -> habit.id }.toSet()

    val logs = snapshot.logs.map { log -> log.checked(habitIds) }
    requireIds(logs.map { log -> log.id })
    val logKeys = logs.map { log -> log.habitId to log.date }
    if (logKeys.size != logKeys.toSet().size) {
        invalid()
    }

    val reflections = snapshot.reflections.map { reflection -> reflection.checked() }
    val reflectionDates = reflections.map { reflection -> reflection.date }
    if (reflectionDates.size != reflectionDates.toSet().size) {
        invalid()
    }

    return copy(
        preferences = preferences.checked(),
        snapshot = AgendaSnapshot(
            categories = categories,
            rules = rules,
            activities = activities,
            overrides = overrides,
            tasks = tasks,
            habits = habits,
            logs = logs,
            reflections = reflections
        )
    )
}

private fun UserPreferences.checked(): UserPreferences {
    val name = when (val result = validateDisplayName(displayName)) {
        is NameValidation.Ok -> result.value
        NameValidation.TooLong -> invalid()
    }
    if (mascotProgress < 0) {
        invalid()
    }
    return copy(displayName = name, recoveredFromError = false)
}

private fun Category.checked(): Category {
    val name = name.trim()
    val icon = iconKey.trim()
    if (name.isEmpty() || icon.isEmpty()) {
        invalid()
    }
    return copy(name = name, iconKey = icon)
}

private fun RecurrenceRule.checked(): RecurrenceRule {
    if (intervalCount < 1) {
        invalid()
    }
    if (endDate != null && endDate.isBefore(startDate)) {
        invalid()
    }
    val days = try {
        parseDaysOfWeek(daysOfWeek)
    } catch (error: IllegalArgumentException) {
        if (error is BackupFormatException) {
            throw error
        }
        invalid()
    }
    val storedDays = when (frequency) {
        RecurrenceFrequency.DAILY -> {
            if (days.isNotEmpty()) {
                invalid()
            }
            null
        }
        RecurrenceFrequency.SPECIFIC_DAYS -> {
            if (days.isEmpty()) {
                invalid()
            }
            days.sorted().joinToString(separator = ",")
        }
        RecurrenceFrequency.WEEKLY,
        RecurrenceFrequency.EVERY_N_WEEKS -> {
            days.takeIf { it.isNotEmpty() }?.sorted()?.joinToString(separator = ",")
        }
    }
    return copy(daysOfWeek = storedDays)
}

private fun AgendaActivity.checked(categories: Set<Long>, rules: Set<Long>): AgendaActivity {
    val title = title.trim()
    if (title.isEmpty()) {
        invalid()
    }
    if (checkActivityTimes(startTime, endTime) !is ActivityTimeCheck.Valid) {
        invalid()
    }
    if (categoryId != null && categoryId !in categories) {
        invalid()
    }
    if (recurrenceRuleId != null && recurrenceRuleId !in rules) {
        invalid()
    }
    return copy(title = title, notes = notes.trim())
}

private fun ActivityOverride.checked(activities: Set<Long>): ActivityOverride {
    if (activityId !in activities) {
        invalid()
    }
    val start = startTime
    val end = endTime
    if (start != null && end != null && checkActivityTimes(start, end) !is ActivityTimeCheck.Valid) {
        invalid()
    }
    return this
}

private fun AgendaTask.checked(categories: Set<Long>): AgendaTask {
    val title = title.trim()
    if (title.isEmpty()) {
        invalid()
    }
    if (categoryId != null && categoryId !in categories) {
        invalid()
    }
    return copy(title = title, notes = notes.trim())
}

private fun Habit.checked(categories: Set<Long>): Habit {
    val title = title.trim()
    if (title.isEmpty()) {
        invalid()
    }
    if (categoryId != null && categoryId !in categories) {
        invalid()
    }
    val days = try {
        parseDaysOfWeek(daysOfWeek)
    } catch (error: IllegalArgumentException) {
        if (error is BackupFormatException) {
            throw error
        }
        invalid()
    }
    val storedDays = when (frequency) {
        HabitFrequency.DAILY -> {
            if (days.isNotEmpty()) {
                invalid()
            }
            null
        }
        HabitFrequency.SPECIFIC_DAYS -> {
            if (days.isEmpty()) {
                invalid()
            }
            days.sorted().joinToString(separator = ",")
        }
    }
    return copy(title = title, notes = notes.trim(), daysOfWeek = storedDays)
}

private fun HabitLog.checked(habits: Set<Long>): HabitLog {
    if (habitId !in habits) {
        invalid()
    }
    return this
}

private fun DailyReflection.checked(): DailyReflection {
    val text = note.trim()
    if (text.isEmpty() || text.length > MAX_REFLECTION_NOTE) {
        invalid()
    }
    return copy(note = text)
}

private fun requireIds(ids: List<Long>) {
    if (ids.any { id -> id <= 0L } || ids.size != ids.toSet().size) {
        invalid()
    }
}

private fun invalid(): Nothing = throw BackupFormatException(BackupProblem.INVALID)
