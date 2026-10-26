package com.miagenda.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class ActivityStatus {
    PENDING,
    COMPLETED,
    PARTIAL,
    RESCHEDULED,
    CANCELLED
}

enum class TaskStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    SPECIFIC_DAYS,
    EVERY_N_WEEKS
}

enum class HabitFrequency {
    DAILY,
    SPECIFIC_DAYS
}

enum class HabitLogStatus {
    COMPLETED,
    PARTIAL,
    NOT_DONE
}

data class Category(
    val id: Long,
    val name: String,
    val colorArgb: Int,
    val iconKey: String,
    val sortOrder: Int,
    val isArchived: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class RecurrenceRule(
    val id: Long,
    val frequency: RecurrenceFrequency,
    val intervalCount: Int,
    val daysOfWeek: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AgendaActivity(
    val id: Long,
    val title: String,
    val notes: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val categoryId: Long?,
    val status: ActivityStatus,
    val recurrenceRuleId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ActivityOverride(
    val id: Long,
    val activityId: Long,
    val occurrenceDate: LocalDate,
    val status: ActivityStatus?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val isSkipped: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AgendaTask(
    val id: Long,
    val title: String,
    val notes: String,
    val dueDate: LocalDate?,
    val priority: TaskPriority,
    val status: TaskStatus,
    val categoryId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class Habit(
    val id: Long,
    val title: String,
    val notes: String,
    val categoryId: Long?,
    val frequency: HabitFrequency,
    val daysOfWeek: String?,
    val isArchived: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class HabitLog(
    val id: Long,
    val habitId: Long,
    val date: LocalDate,
    val status: HabitLogStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class DailyReflection(
    val date: LocalDate,
    val note: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
