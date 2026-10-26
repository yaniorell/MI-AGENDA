package com.miagenda.app.domain.backup

import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.model.UserPreferences
import java.time.LocalDateTime

data class AgendaSnapshot(
    val categories: List<Category> = emptyList(),
    val rules: List<RecurrenceRule> = emptyList(),
    val activities: List<AgendaActivity> = emptyList(),
    val overrides: List<ActivityOverride> = emptyList(),
    val tasks: List<AgendaTask> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val logs: List<HabitLog> = emptyList(),
    val reflections: List<DailyReflection> = emptyList()
)

data class BackupContents(
    val exportedAt: LocalDateTime,
    val preferences: UserPreferences,
    val snapshot: AgendaSnapshot
)
