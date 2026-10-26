package com.miagenda.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "color_argb") val colorArgb: Int,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)

@Entity(tableName = "recurrence_rules")
data class RecurrenceRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val frequency: RecurrenceFrequency,
    @ColumnInfo(name = "interval_count") val intervalCount: Int,
    @ColumnInfo(name = "days_of_week") val daysOfWeek: String?,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "end_date") val endDate: LocalDate?,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)

@Entity(
    tableName = "agenda_activities",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RecurrenceRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["recurrence_rule_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["category_id"]),
        Index(value = ["recurrence_rule_id"])
    ]
)
data class AgendaActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String,
    val date: LocalDate,
    @ColumnInfo(name = "start_time") val startTime: LocalTime,
    @ColumnInfo(name = "end_time") val endTime: LocalTime,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    val status: ActivityStatus,
    @ColumnInfo(name = "recurrence_rule_id") val recurrenceRuleId: Long?,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)

@Entity(
    tableName = "activity_overrides",
    foreignKeys = [
        ForeignKey(
            entity = AgendaActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["activity_id", "occurrence_date"], unique = true)
    ]
)
data class ActivityOverrideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "activity_id") val activityId: Long,
    @ColumnInfo(name = "occurrence_date") val occurrenceDate: LocalDate,
    val status: ActivityStatus?,
    @ColumnInfo(name = "start_time") val startTime: LocalTime?,
    @ColumnInfo(name = "end_time") val endTime: LocalTime?,
    @ColumnInfo(name = "is_skipped") val isSkipped: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["due_date"]),
        Index(value = ["category_id"])
    ]
)
data class AgendaTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String,
    @ColumnInfo(name = "due_date") val dueDate: LocalDate?,
    val priority: TaskPriority,
    val status: TaskStatus,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)

@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["category_id"])
    ]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    val frequency: HabitFrequency,
    @ColumnInfo(name = "days_of_week") val daysOfWeek: String?,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habit_id", "date"], unique = true)
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    val date: LocalDate,
    val status: HabitLogStatus,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)

@Entity(tableName = "daily_reflections")
data class DailyReflectionEntity(
    @PrimaryKey val date: LocalDate,
    val note: String,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime
)
