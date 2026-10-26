package com.miagenda.app.data.local

import com.miagenda.app.data.local.dao.ActivityDao
import com.miagenda.app.data.local.dao.CategoryDao
import com.miagenda.app.data.local.dao.HabitDao
import com.miagenda.app.data.local.dao.RecurrenceDao
import com.miagenda.app.data.local.dao.ReflectionDao
import com.miagenda.app.data.local.dao.TaskDao
import com.miagenda.app.domain.activity.ActivityTimeCheck
import com.miagenda.app.domain.activity.checkActivityTimes
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.HabitRepository
import com.miagenda.app.domain.repository.ReflectionRepository
import com.miagenda.app.domain.repository.TaskRepository
import com.miagenda.app.domain.time.TimeProvider
import com.miagenda.app.domain.time.parseDaysOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(
    private val categoryDao: CategoryDao,
    private val timeProvider: TimeProvider
) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> {
        return categoryDao.observeAll().map { categories -> categories.map { it.toDomain() } }
    }

    override suspend fun getAll(): List<Category> = categoryDao.getAll().map { it.toDomain() }

    override suspend fun get(id: Long): Category? = categoryDao.getById(id)?.toDomain()

    override suspend fun count(): Int = categoryDao.count()

    override suspend fun insert(category: Category): Category {
        val name = requireName(category.name)
        val now = timeProvider.now()
        val id = categoryDao.insert(
            category.copy(name = name, createdAt = now, updatedAt = now).toEntity().copy(id = 0)
        )
        return category.copy(id = id, name = name, createdAt = now, updatedAt = now)
    }

    override suspend fun update(category: Category): Category {
        val current = categoryDao.getById(category.id)
            ?: throw IllegalArgumentException("Category ${category.id} does not exist")
        val name = requireName(category.name)
        val updated = category.copy(
            name = name,
            createdAt = current.createdAt,
            updatedAt = timeProvider.now()
        )
        categoryDao.update(updated.toEntity())
        return updated
    }

    override suspend fun delete(id: Long) {
        categoryDao.deleteById(id)
    }
}

class RoomActivityRepository(
    private val activityDao: ActivityDao,
    private val recurrenceDao: RecurrenceDao,
    private val timeProvider: TimeProvider
) : ActivityRepository {
    override fun observeAll(): Flow<List<AgendaActivity>> {
        return activityDao.observeAll().map { activities -> activities.map { it.toDomain() } }
    }

    override fun observeOn(date: LocalDate): Flow<List<AgendaActivity>> {
        return activityDao.observeOn(date).map { activities -> activities.map { it.toDomain() } }
    }

    override suspend fun get(id: Long): AgendaActivity? = activityDao.getById(id)?.toDomain()

    override suspend fun insert(activity: AgendaActivity): AgendaActivity {
        val prepared = prepareActivity(activity)
        val now = timeProvider.now()
        val id = activityDao.insert(
            prepared.copy(createdAt = now, updatedAt = now).toEntity().copy(id = 0)
        )
        return prepared.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun update(activity: AgendaActivity): AgendaActivity {
        val current = activityDao.getById(activity.id)
            ?: throw IllegalArgumentException("Activity ${activity.id} does not exist")
        val prepared = prepareActivity(activity)
        val updated = prepared.copy(createdAt = current.createdAt, updatedAt = timeProvider.now())
        activityDao.update(updated.toEntity())
        return updated
    }

    override suspend fun delete(id: Long) {
        activityDao.deleteById(id)
    }

    override fun observeRules(): Flow<List<RecurrenceRule>> {
        return recurrenceDao.observeAll().map { rules -> rules.map { it.toDomain() } }
    }

    override fun observeOverridesBetween(
        start: LocalDate,
        end: LocalDate
    ): Flow<List<ActivityOverride>> {
        return activityDao.observeOverridesBetween(start, end).map { overrides ->
            overrides.map { it.toDomain() }
        }
    }

    override fun observeAllOverrides(): Flow<List<ActivityOverride>> {
        return activityDao.observeAllOverrides().map { overrides ->
            overrides.map { it.toDomain() }
        }
    }

    override suspend fun insertRule(rule: RecurrenceRule): RecurrenceRule {
        val prepared = prepareRule(rule)
        val now = timeProvider.now()
        val id = recurrenceDao.insert(
            prepared.copy(createdAt = now, updatedAt = now).toEntity().copy(id = 0)
        )
        return prepared.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun getRule(id: Long): RecurrenceRule? = recurrenceDao.getById(id)?.toDomain()

    override suspend fun updateRule(rule: RecurrenceRule): RecurrenceRule {
        val current = recurrenceDao.getById(rule.id)
            ?: throw IllegalArgumentException("Recurrence rule ${rule.id} does not exist")
        val prepared = prepareRule(rule)
        val updated = prepared.copy(createdAt = current.createdAt, updatedAt = timeProvider.now())
        recurrenceDao.update(updated.toEntity())
        return updated
    }

    override suspend fun deleteRule(id: Long) {
        recurrenceDao.deleteById(id)
    }

    override fun observeOverrides(activityId: Long): Flow<List<ActivityOverride>> {
        return activityDao.observeOverrides(activityId).map { overrides ->
            overrides.map { it.toDomain() }
        }
    }

    override suspend fun getOverride(activityId: Long, occurrenceDate: LocalDate): ActivityOverride? {
        return activityDao.getOverride(activityId, occurrenceDate)?.toDomain()
    }

    override suspend fun insertOverride(override: ActivityOverride): ActivityOverride {
        val prepared = prepareOverride(override)
        val now = timeProvider.now()
        val id = activityDao.insertOverride(
            prepared.copy(createdAt = now, updatedAt = now).toEntity().copy(id = 0)
        )
        return prepared.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun updateOverride(override: ActivityOverride): ActivityOverride {
        val current = activityDao.getOverride(override.activityId, override.occurrenceDate)
            ?: throw IllegalArgumentException("Activity override ${override.id} does not exist")
        val prepared = prepareOverride(override)
        val updated = prepared.copy(
            id = current.id,
            createdAt = current.createdAt,
            updatedAt = timeProvider.now()
        )
        activityDao.updateOverride(updated.toEntity())
        return updated
    }

    override suspend fun deleteOverride(id: Long) {
        activityDao.deleteOverrideById(id)
    }
}

class RoomTaskRepository(
    private val taskDao: TaskDao,
    private val timeProvider: TimeProvider
) : TaskRepository {
    override fun observeAll(): Flow<List<AgendaTask>> {
        return taskDao.observeAll().map { tasks -> tasks.map { it.toDomain() } }
    }

    override suspend fun get(id: Long): AgendaTask? = taskDao.getById(id)?.toDomain()

    override suspend fun insert(task: AgendaTask): AgendaTask {
        val prepared = prepareTask(task)
        val now = timeProvider.now()
        val id = taskDao.insert(prepared.copy(createdAt = now, updatedAt = now).toEntity().copy(id = 0))
        return prepared.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun update(task: AgendaTask): AgendaTask {
        val current = taskDao.getById(task.id)
            ?: throw IllegalArgumentException("Task ${task.id} does not exist")
        val prepared = prepareTask(task)
        val updated = prepared.copy(createdAt = current.createdAt, updatedAt = timeProvider.now())
        taskDao.update(updated.toEntity())
        return updated
    }

    override suspend fun delete(id: Long) {
        taskDao.deleteById(id)
    }
}

class RoomHabitRepository(
    private val habitDao: HabitDao,
    private val timeProvider: TimeProvider
) : HabitRepository {
    override fun observeAll(): Flow<List<Habit>> {
        return habitDao.observeAll().map { habits -> habits.map { it.toDomain() } }
    }

    override suspend fun get(id: Long): Habit? = habitDao.getById(id)?.toDomain()

    override suspend fun insert(habit: Habit): Habit {
        val prepared = prepareHabit(habit)
        val now = timeProvider.now()
        val id = habitDao.insert(prepared.copy(createdAt = now, updatedAt = now).toEntity().copy(id = 0))
        return prepared.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun update(habit: Habit): Habit {
        val current = habitDao.getById(habit.id)
            ?: throw IllegalArgumentException("Habit ${habit.id} does not exist")
        val prepared = prepareHabit(habit)
        val updated = prepared.copy(createdAt = current.createdAt, updatedAt = timeProvider.now())
        habitDao.update(updated.toEntity())
        return updated
    }

    override suspend fun delete(id: Long) {
        habitDao.deleteById(id)
    }

    override fun observeAllLogs(): Flow<List<HabitLog>> {
        return habitDao.observeAllLogs().map { logs -> logs.map { it.toDomain() } }
    }

    override fun observeLogs(habitId: Long): Flow<List<HabitLog>> {
        return habitDao.observeLogs(habitId).map { logs -> logs.map { it.toDomain() } }
    }

    override suspend fun saveLog(habitId: Long, date: LocalDate, status: HabitLogStatus): HabitLog {
        val now = timeProvider.now()
        val existing = habitDao.getLog(habitId, date)?.toDomain()
        return if (existing == null) {
            val created = HabitLog(
                id = 0,
                habitId = habitId,
                date = date,
                status = status,
                createdAt = now,
                updatedAt = now
            )
            val id = habitDao.insertLog(created.toEntity())
            created.copy(id = id)
        } else {
            val updated = existing.copy(status = status, updatedAt = now)
            habitDao.updateLog(updated.toEntity())
            updated
        }
    }

    override suspend fun getLog(habitId: Long, date: LocalDate): HabitLog? {
        return habitDao.getLog(habitId, date)?.toDomain()
    }

    override suspend fun insertLog(log: HabitLog): HabitLog {
        val now = timeProvider.now()
        val id = habitDao.insertLog(log.copy(createdAt = now, updatedAt = now).toEntity().copy(id = 0))
        return log.copy(id = id, createdAt = now, updatedAt = now)
    }

    override suspend fun updateLog(log: HabitLog): HabitLog {
        val current = habitDao.getLog(log.habitId, log.date)
            ?: throw IllegalArgumentException("Habit log ${log.id} does not exist")
        val updated = log.copy(
            id = current.id,
            createdAt = current.createdAt,
            updatedAt = timeProvider.now()
        )
        habitDao.updateLog(updated.toEntity())
        return updated
    }

    override suspend fun deleteLog(id: Long) {
        habitDao.deleteLogById(id)
    }
}

class RoomReflectionRepository(
    private val reflectionDao: ReflectionDao,
    private val timeProvider: TimeProvider
) : ReflectionRepository {
    override fun observe(date: LocalDate): Flow<DailyReflection?> {
        return reflectionDao.observe(date).map { reflection -> reflection?.toDomain() }
    }

    override fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<DailyReflection>> {
        return reflectionDao.observeBetween(start, end).map { notes -> notes.map { it.toDomain() } }
    }

    override suspend fun get(date: LocalDate): DailyReflection? = reflectionDao.get(date)?.toDomain()

    override suspend fun save(reflection: DailyReflection): DailyReflection {
        val existing = reflectionDao.get(reflection.date)
        val now = timeProvider.now()
        val saved = reflection.copy(
            note = reflection.note.trim(),
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        reflectionDao.upsert(saved.toEntity())
        return saved
    }

    override suspend fun delete(date: LocalDate) {
        reflectionDao.delete(date)
    }
}

private fun requireName(value: String): String {
    val name = value.trim()
    require(name.isNotEmpty()) { "Name cannot be blank" }
    return name
}

private fun requireTitle(value: String): String {
    val title = value.trim()
    require(title.isNotEmpty()) { "Title cannot be blank" }
    return title
}

private fun prepareActivity(activity: AgendaActivity): AgendaActivity {
    require(checkActivityTimes(activity.startTime, activity.endTime) is ActivityTimeCheck.Valid) {
        "Activity end time is before start time"
    }
    return activity.copy(title = requireTitle(activity.title), notes = activity.notes.trim())
}

private fun prepareRule(rule: RecurrenceRule): RecurrenceRule {
    require(rule.intervalCount >= 1) { "Recurrence interval must be at least 1" }
    if (rule.endDate != null) {
        require(!rule.endDate.isBefore(rule.startDate)) { "Recurrence end date is before start date" }
    }
    val days = parseDaysOfWeek(rule.daysOfWeek)
    val storedDays = when (rule.frequency) {
        RecurrenceFrequency.DAILY -> {
            require(days.isEmpty()) { "A daily recurrence does not use specific days" }
            null
        }
        RecurrenceFrequency.SPECIFIC_DAYS -> {
            require(days.isNotEmpty()) { "Specific days are required" }
            days.sorted().joinToString(",")
        }
        RecurrenceFrequency.WEEKLY,
        RecurrenceFrequency.EVERY_N_WEEKS -> days.takeIf { it.isNotEmpty() }?.sorted()?.joinToString(",")
    }
    return rule.copy(daysOfWeek = storedDays)
}

private fun prepareOverride(override: ActivityOverride): ActivityOverride {
    if (override.startTime != null && override.endTime != null) {
        require(checkActivityTimes(override.startTime, override.endTime) is ActivityTimeCheck.Valid) {
            "Activity end time is before start time"
        }
    }
    return override
}

private fun prepareTask(task: AgendaTask): AgendaTask {
    return task.copy(title = requireTitle(task.title), notes = task.notes.trim())
}

private fun prepareHabit(habit: Habit): Habit {
    val days = parseDaysOfWeek(habit.daysOfWeek)
    val storedDays = when (habit.frequency) {
        HabitFrequency.DAILY -> {
            require(days.isEmpty()) { "A daily habit does not use specific days" }
            null
        }
        HabitFrequency.SPECIFIC_DAYS -> {
            require(days.isNotEmpty()) { "Specific days are required" }
            days.sorted().joinToString(",")
        }
    }
    return habit.copy(
        title = requireTitle(habit.title),
        notes = habit.notes.trim(),
        daysOfWeek = storedDays
    )
}
