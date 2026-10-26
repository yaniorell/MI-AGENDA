package com.miagenda.app.data.local

import androidx.room.TypeConverter
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AgendaConverters {
    @TypeConverter
    fun dateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun timeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun dateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun activityStatusToString(value: ActivityStatus?): String? = value?.name

    @TypeConverter
    fun stringToActivityStatus(value: String?): ActivityStatus? = value?.let(ActivityStatus::valueOf)

    @TypeConverter
    fun taskStatusToString(value: TaskStatus?): String? = value?.name

    @TypeConverter
    fun stringToTaskStatus(value: String?): TaskStatus? = value?.let(TaskStatus::valueOf)

    @TypeConverter
    fun taskPriorityToString(value: TaskPriority?): String? = value?.name

    @TypeConverter
    fun stringToTaskPriority(value: String?): TaskPriority? = value?.let(TaskPriority::valueOf)

    @TypeConverter
    fun recurrenceFrequencyToString(value: RecurrenceFrequency?): String? = value?.name

    @TypeConverter
    fun stringToRecurrenceFrequency(value: String?): RecurrenceFrequency? =
        value?.let(RecurrenceFrequency::valueOf)

    @TypeConverter
    fun habitFrequencyToString(value: HabitFrequency?): String? = value?.name

    @TypeConverter
    fun stringToHabitFrequency(value: String?): HabitFrequency? = value?.let(HabitFrequency::valueOf)

    @TypeConverter
    fun habitLogStatusToString(value: HabitLogStatus?): String? = value?.name

    @TypeConverter
    fun stringToHabitLogStatus(value: String?): HabitLogStatus? = value?.let(HabitLogStatus::valueOf)
}
