package com.miagenda.app.data.backup

import com.miagenda.app.domain.backup.AgendaSnapshot
import com.miagenda.app.domain.backup.BackupContents
import com.miagenda.app.domain.backup.BackupFormatException
import com.miagenda.app.domain.backup.BackupProblem
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.model.UserPreferences
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.serialization.Serializable

@Serializable
internal data class BackupDocument(
    val format: String,
    val version: Int,
    val exportedAt: String,
    val preferences: BackupPreferencesDto,
    val categories: List<BackupCategoryDto>,
    val recurrenceRules: List<BackupRuleDto>,
    val activities: List<BackupActivityDto>,
    val activityOverrides: List<BackupOverrideDto>,
    val tasks: List<BackupTaskDto>,
    val habits: List<BackupHabitDto>,
    val habitLogs: List<BackupHabitLogDto>,
    val reflections: List<BackupReflectionDto>
)

@Serializable
internal data class BackupPreferencesDto(
    val displayName: String,
    val themeMode: String,
    val accentColor: String,
    val homeSections: List<String>
)

@Serializable
internal data class BackupCategoryDto(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val iconKey: String,
    val sortOrder: Int,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class BackupRuleDto(
    val id: Long,
    val frequency: String,
    val intervalCount: Int,
    val daysOfWeek: String?,
    val startDate: String,
    val endDate: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class BackupActivityDto(
    val id: Long,
    val title: String,
    val notes: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val categoryId: Long?,
    val status: String,
    val recurrenceRuleId: Long?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class BackupOverrideDto(
    val id: Long,
    val activityId: Long,
    val occurrenceDate: String,
    val status: String?,
    val startTime: String?,
    val endTime: String?,
    val isSkipped: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class BackupTaskDto(
    val id: Long,
    val title: String,
    val notes: String,
    val dueDate: String?,
    val priority: String,
    val status: String,
    val categoryId: Long?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class BackupHabitDto(
    val id: Long,
    val title: String,
    val notes: String,
    val categoryId: Long?,
    val frequency: String,
    val daysOfWeek: String?,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class BackupHabitLogDto(
    val id: Long,
    val habitId: Long,
    val date: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class BackupReflectionDto(
    val date: String,
    val note: String,
    val createdAt: String,
    val updatedAt: String
)

internal fun BackupContents.toDocument(): BackupDocument {
    return BackupDocument(
        format = BACKUP_FORMAT,
        version = BACKUP_VERSION,
        exportedAt = exportedAt.toString(),
        preferences = BackupPreferencesDto(
            displayName = preferences.displayName,
            themeMode = preferences.themeMode.name,
            accentColor = preferences.accentColor.name,
            homeSections = HomeSection.entries
                .filter { section -> section in preferences.visibleSections }
                .map { section -> section.name }
        ),
        categories = snapshot.categories.map { category ->
            BackupCategoryDto(
                id = category.id,
                name = category.name,
                colorArgb = category.colorArgb,
                iconKey = category.iconKey,
                sortOrder = category.sortOrder,
                isArchived = category.isArchived,
                createdAt = category.createdAt.toString(),
                updatedAt = category.updatedAt.toString()
            )
        },
        recurrenceRules = snapshot.rules.map { rule ->
            BackupRuleDto(
                id = rule.id,
                frequency = rule.frequency.name,
                intervalCount = rule.intervalCount,
                daysOfWeek = rule.daysOfWeek,
                startDate = rule.startDate.toString(),
                endDate = rule.endDate?.toString(),
                createdAt = rule.createdAt.toString(),
                updatedAt = rule.updatedAt.toString()
            )
        },
        activities = snapshot.activities.map { activity ->
            BackupActivityDto(
                id = activity.id,
                title = activity.title,
                notes = activity.notes,
                date = activity.date.toString(),
                startTime = activity.startTime.toString(),
                endTime = activity.endTime.toString(),
                categoryId = activity.categoryId,
                status = activity.status.name,
                recurrenceRuleId = activity.recurrenceRuleId,
                createdAt = activity.createdAt.toString(),
                updatedAt = activity.updatedAt.toString()
            )
        },
        activityOverrides = snapshot.overrides.map { override ->
            BackupOverrideDto(
                id = override.id,
                activityId = override.activityId,
                occurrenceDate = override.occurrenceDate.toString(),
                status = override.status?.name,
                startTime = override.startTime?.toString(),
                endTime = override.endTime?.toString(),
                isSkipped = override.isSkipped,
                createdAt = override.createdAt.toString(),
                updatedAt = override.updatedAt.toString()
            )
        },
        tasks = snapshot.tasks.map { task ->
            BackupTaskDto(
                id = task.id,
                title = task.title,
                notes = task.notes,
                dueDate = task.dueDate?.toString(),
                priority = task.priority.name,
                status = task.status.name,
                categoryId = task.categoryId,
                createdAt = task.createdAt.toString(),
                updatedAt = task.updatedAt.toString()
            )
        },
        habits = snapshot.habits.map { habit ->
            BackupHabitDto(
                id = habit.id,
                title = habit.title,
                notes = habit.notes,
                categoryId = habit.categoryId,
                frequency = habit.frequency.name,
                daysOfWeek = habit.daysOfWeek,
                isArchived = habit.isArchived,
                createdAt = habit.createdAt.toString(),
                updatedAt = habit.updatedAt.toString()
            )
        },
        habitLogs = snapshot.logs.map { log ->
            BackupHabitLogDto(
                id = log.id,
                habitId = log.habitId,
                date = log.date.toString(),
                status = log.status.name,
                createdAt = log.createdAt.toString(),
                updatedAt = log.updatedAt.toString()
            )
        },
        reflections = snapshot.reflections.map { reflection ->
            BackupReflectionDto(
                date = reflection.date.toString(),
                note = reflection.note,
                createdAt = reflection.createdAt.toString(),
                updatedAt = reflection.updatedAt.toString()
            )
        }
    )
}

internal fun BackupDocument.toContents(): BackupContents {
    if (preferences.homeSections.size != preferences.homeSections.toSet().size) {
        throw BackupFormatException(BackupProblem.INVALID)
    }
    val sections = preferences.homeSections.map { name -> name.toEnum<HomeSection>() }
    return BackupContents(
        exportedAt = exportedAt.toLocalDateTime(),
        preferences = UserPreferences(
            displayName = preferences.displayName,
            themeMode = preferences.themeMode.toEnum(),
            accentColor = preferences.accentColor.toEnum(),
            visibleSections = sections.toSet(),
            recoveredFromError = false
        ),
        snapshot = AgendaSnapshot(
            categories = categories.map { category ->
                Category(
                    id = category.id,
                    name = category.name,
                    colorArgb = category.colorArgb,
                    iconKey = category.iconKey,
                    sortOrder = category.sortOrder,
                    isArchived = category.isArchived,
                    createdAt = category.createdAt.toLocalDateTime(),
                    updatedAt = category.updatedAt.toLocalDateTime()
                )
            },
            rules = recurrenceRules.map { rule ->
                RecurrenceRule(
                    id = rule.id,
                    frequency = rule.frequency.toEnum(),
                    intervalCount = rule.intervalCount,
                    daysOfWeek = rule.daysOfWeek,
                    startDate = rule.startDate.toLocalDate(),
                    endDate = rule.endDate?.toLocalDate(),
                    createdAt = rule.createdAt.toLocalDateTime(),
                    updatedAt = rule.updatedAt.toLocalDateTime()
                )
            },
            activities = activities.map { activity ->
                AgendaActivity(
                    id = activity.id,
                    title = activity.title,
                    notes = activity.notes,
                    date = activity.date.toLocalDate(),
                    startTime = activity.startTime.toLocalTime(),
                    endTime = activity.endTime.toLocalTime(),
                    categoryId = activity.categoryId,
                    status = activity.status.toEnum(),
                    recurrenceRuleId = activity.recurrenceRuleId,
                    createdAt = activity.createdAt.toLocalDateTime(),
                    updatedAt = activity.updatedAt.toLocalDateTime()
                )
            },
            overrides = activityOverrides.map { override ->
                ActivityOverride(
                    id = override.id,
                    activityId = override.activityId,
                    occurrenceDate = override.occurrenceDate.toLocalDate(),
                    status = override.status?.toEnum<ActivityStatus>(),
                    startTime = override.startTime?.toLocalTime(),
                    endTime = override.endTime?.toLocalTime(),
                    isSkipped = override.isSkipped,
                    createdAt = override.createdAt.toLocalDateTime(),
                    updatedAt = override.updatedAt.toLocalDateTime()
                )
            },
            tasks = tasks.map { task ->
                AgendaTask(
                    id = task.id,
                    title = task.title,
                    notes = task.notes,
                    dueDate = task.dueDate?.toLocalDate(),
                    priority = task.priority.toEnum(),
                    status = task.status.toEnum(),
                    categoryId = task.categoryId,
                    createdAt = task.createdAt.toLocalDateTime(),
                    updatedAt = task.updatedAt.toLocalDateTime()
                )
            },
            habits = habits.map { habit ->
                Habit(
                    id = habit.id,
                    title = habit.title,
                    notes = habit.notes,
                    categoryId = habit.categoryId,
                    frequency = habit.frequency.toEnum(),
                    daysOfWeek = habit.daysOfWeek,
                    isArchived = habit.isArchived,
                    createdAt = habit.createdAt.toLocalDateTime(),
                    updatedAt = habit.updatedAt.toLocalDateTime()
                )
            },
            logs = habitLogs.map { log ->
                HabitLog(
                    id = log.id,
                    habitId = log.habitId,
                    date = log.date.toLocalDate(),
                    status = log.status.toEnum(),
                    createdAt = log.createdAt.toLocalDateTime(),
                    updatedAt = log.updatedAt.toLocalDateTime()
                )
            },
            reflections = reflections.map { reflection ->
                DailyReflection(
                    date = reflection.date.toLocalDate(),
                    note = reflection.note,
                    createdAt = reflection.createdAt.toLocalDateTime(),
                    updatedAt = reflection.updatedAt.toLocalDateTime()
                )
            }
        )
    )
}

private inline fun <reified T : Enum<T>> String.toEnum(): T {
    return enumValues<T>().firstOrNull { value -> value.name == this }
        ?: throw BackupFormatException(BackupProblem.INVALID)
}

private fun String.toLocalDate(): LocalDate {
    return try {
        LocalDate.parse(this)
    } catch (error: DateTimeException) {
        throw BackupFormatException(BackupProblem.INVALID)
    }
}

private fun String.toLocalTime(): LocalTime {
    return try {
        LocalTime.parse(this)
    } catch (error: DateTimeException) {
        throw BackupFormatException(BackupProblem.INVALID)
    }
}

private fun String.toLocalDateTime(): LocalDateTime {
    return try {
        LocalDateTime.parse(this)
    } catch (error: DateTimeException) {
        throw BackupFormatException(BackupProblem.INVALID)
    }
}
