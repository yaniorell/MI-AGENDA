package com.miagenda.app.domain.repository

import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.RecurrenceRule
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun getAll(): List<Category>
    suspend fun get(id: Long): Category?
    suspend fun count(): Int
    suspend fun insert(category: Category): Category
    suspend fun update(category: Category): Category
    suspend fun delete(id: Long)
}

interface ActivityRepository {
    fun observeAll(): Flow<List<AgendaActivity>>
    fun observeOn(date: LocalDate): Flow<List<AgendaActivity>>
    suspend fun get(id: Long): AgendaActivity?
    suspend fun insert(activity: AgendaActivity): AgendaActivity
    suspend fun update(activity: AgendaActivity): AgendaActivity
    suspend fun delete(id: Long)
    fun observeRules(): Flow<List<RecurrenceRule>>
    fun observeOverridesBetween(start: LocalDate, end: LocalDate): Flow<List<ActivityOverride>>
    suspend fun insertRule(rule: RecurrenceRule): RecurrenceRule
    suspend fun getRule(id: Long): RecurrenceRule?
    suspend fun updateRule(rule: RecurrenceRule): RecurrenceRule
    suspend fun deleteRule(id: Long)
    fun observeOverrides(activityId: Long): Flow<List<ActivityOverride>>
    suspend fun getOverride(activityId: Long, occurrenceDate: LocalDate): ActivityOverride?
    suspend fun insertOverride(override: ActivityOverride): ActivityOverride
    suspend fun updateOverride(override: ActivityOverride): ActivityOverride
    suspend fun deleteOverride(id: Long)
}

interface TaskRepository {
    fun observeAll(): Flow<List<AgendaTask>>
    suspend fun get(id: Long): AgendaTask?
    suspend fun insert(task: AgendaTask): AgendaTask
    suspend fun update(task: AgendaTask): AgendaTask
    suspend fun delete(id: Long)
}

interface HabitRepository {
    fun observeAll(): Flow<List<Habit>>
    suspend fun get(id: Long): Habit?
    suspend fun insert(habit: Habit): Habit
    suspend fun update(habit: Habit): Habit
    suspend fun delete(id: Long)
    fun observeAllLogs(): Flow<List<HabitLog>>
    fun observeLogs(habitId: Long): Flow<List<HabitLog>>
    suspend fun saveLog(habitId: Long, date: LocalDate, status: HabitLogStatus): HabitLog
    suspend fun getLog(habitId: Long, date: LocalDate): HabitLog?
    suspend fun insertLog(log: HabitLog): HabitLog
    suspend fun updateLog(log: HabitLog): HabitLog
    suspend fun deleteLog(id: Long)
}

interface ReflectionRepository {
    fun observe(date: LocalDate): Flow<DailyReflection?>
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<DailyReflection>>
    suspend fun get(date: LocalDate): DailyReflection?
    suspend fun save(reflection: DailyReflection): DailyReflection
    suspend fun delete(date: LocalDate)
}
