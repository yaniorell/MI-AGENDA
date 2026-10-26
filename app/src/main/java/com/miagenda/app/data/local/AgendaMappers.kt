package com.miagenda.app.data.local

import com.miagenda.app.data.local.entity.ActivityOverrideEntity
import com.miagenda.app.data.local.entity.AgendaActivityEntity
import com.miagenda.app.data.local.entity.AgendaTaskEntity
import com.miagenda.app.data.local.entity.CategoryEntity
import com.miagenda.app.data.local.entity.DailyReflectionEntity
import com.miagenda.app.data.local.entity.HabitEntity
import com.miagenda.app.data.local.entity.HabitLogEntity
import com.miagenda.app.data.local.entity.RecurrenceRuleEntity
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.RecurrenceRule

internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    colorArgb = colorArgb,
    iconKey = iconKey,
    sortOrder = sortOrder,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    colorArgb = colorArgb,
    iconKey = iconKey,
    sortOrder = sortOrder,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun RecurrenceRuleEntity.toDomain(): RecurrenceRule = RecurrenceRule(
    id = id,
    frequency = frequency,
    intervalCount = intervalCount,
    daysOfWeek = daysOfWeek,
    startDate = startDate,
    endDate = endDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun RecurrenceRule.toEntity(): RecurrenceRuleEntity = RecurrenceRuleEntity(
    id = id,
    frequency = frequency,
    intervalCount = intervalCount,
    daysOfWeek = daysOfWeek,
    startDate = startDate,
    endDate = endDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun AgendaActivityEntity.toDomain(): AgendaActivity = AgendaActivity(
    id = id,
    title = title,
    notes = notes,
    date = date,
    startTime = startTime,
    endTime = endTime,
    categoryId = categoryId,
    status = status,
    recurrenceRuleId = recurrenceRuleId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun AgendaActivity.toEntity(): AgendaActivityEntity = AgendaActivityEntity(
    id = id,
    title = title,
    notes = notes,
    date = date,
    startTime = startTime,
    endTime = endTime,
    categoryId = categoryId,
    status = status,
    recurrenceRuleId = recurrenceRuleId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun ActivityOverrideEntity.toDomain(): ActivityOverride = ActivityOverride(
    id = id,
    activityId = activityId,
    occurrenceDate = occurrenceDate,
    status = status,
    startTime = startTime,
    endTime = endTime,
    isSkipped = isSkipped,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun ActivityOverride.toEntity(): ActivityOverrideEntity = ActivityOverrideEntity(
    id = id,
    activityId = activityId,
    occurrenceDate = occurrenceDate,
    status = status,
    startTime = startTime,
    endTime = endTime,
    isSkipped = isSkipped,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun AgendaTaskEntity.toDomain(): AgendaTask = AgendaTask(
    id = id,
    title = title,
    notes = notes,
    dueDate = dueDate,
    priority = priority,
    status = status,
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun AgendaTask.toEntity(): AgendaTaskEntity = AgendaTaskEntity(
    id = id,
    title = title,
    notes = notes,
    dueDate = dueDate,
    priority = priority,
    status = status,
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    title = title,
    notes = notes,
    categoryId = categoryId,
    frequency = frequency,
    daysOfWeek = daysOfWeek,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    title = title,
    notes = notes,
    categoryId = categoryId,
    frequency = frequency,
    daysOfWeek = daysOfWeek,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun HabitLogEntity.toDomain(): HabitLog = HabitLog(
    id = id,
    habitId = habitId,
    date = date,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    id = id,
    habitId = habitId,
    date = date,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun DailyReflectionEntity.toDomain(): DailyReflection = DailyReflection(
    date = date,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

internal fun DailyReflection.toEntity(): DailyReflectionEntity = DailyReflectionEntity(
    date = date,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)
