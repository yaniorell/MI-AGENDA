package com.miagenda.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miagenda.app.data.local.entity.ActivityOverrideEntity
import com.miagenda.app.data.local.entity.AgendaActivityEntity
import com.miagenda.app.data.local.entity.AgendaTaskEntity
import com.miagenda.app.data.local.entity.CategoryEntity
import com.miagenda.app.data.local.entity.DailyReflectionEntity
import com.miagenda.app.data.local.entity.HabitEntity
import com.miagenda.app.data.local.entity.HabitLogEntity
import com.miagenda.app.data.local.entity.RecurrenceRuleEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort_order ASC, id ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sort_order ASC, id ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert
    suspend fun insert(entity: CategoryEntity): Long

    @Update
    suspend fun update(entity: CategoryEntity): Int

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface RecurrenceDao {
    @Query("SELECT * FROM recurrence_rules ORDER BY id ASC")
    fun observeAll(): Flow<List<RecurrenceRuleEntity>>

    @Query("SELECT * FROM recurrence_rules ORDER BY id ASC")
    suspend fun getAll(): List<RecurrenceRuleEntity>

    @Query("SELECT * FROM recurrence_rules WHERE id = :id")
    suspend fun getById(id: Long): RecurrenceRuleEntity?

    @Insert
    suspend fun insert(entity: RecurrenceRuleEntity): Long

    @Update
    suspend fun update(entity: RecurrenceRuleEntity): Int

    @Query("DELETE FROM recurrence_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recurrence_rules")
    suspend fun deleteAll()
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM agenda_activities ORDER BY date ASC, start_time ASC, id ASC")
    fun observeAll(): Flow<List<AgendaActivityEntity>>

    @Query(
        "SELECT * FROM agenda_activities WHERE date = :date ORDER BY start_time ASC, id ASC"
    )
    fun observeOn(date: LocalDate): Flow<List<AgendaActivityEntity>>

    @Query("SELECT * FROM agenda_activities ORDER BY date ASC, start_time ASC, id ASC")
    suspend fun getAll(): List<AgendaActivityEntity>

    @Query("SELECT * FROM agenda_activities WHERE id = :id")
    suspend fun getById(id: Long): AgendaActivityEntity?

    @Insert
    suspend fun insert(entity: AgendaActivityEntity): Long

    @Update
    suspend fun update(entity: AgendaActivityEntity): Int

    @Query("DELETE FROM agenda_activities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM agenda_activities")
    suspend fun deleteAll()

    @Query("SELECT * FROM activity_overrides ORDER BY occurrence_date ASC, id ASC")
    suspend fun getAllOverrides(): List<ActivityOverrideEntity>

    @Query("SELECT * FROM activity_overrides ORDER BY occurrence_date ASC, id ASC")
    fun observeAllOverrides(): Flow<List<ActivityOverrideEntity>>

    @Query("DELETE FROM activity_overrides")
    suspend fun deleteAllOverrides()

    @Query(
        "SELECT * FROM activity_overrides WHERE occurrence_date >= :start " +
            "AND occurrence_date <= :end ORDER BY occurrence_date ASC, id ASC"
    )
    fun observeOverridesBetween(start: LocalDate, end: LocalDate): Flow<List<ActivityOverrideEntity>>

    @Query(
        "SELECT * FROM activity_overrides WHERE activity_id = :activityId " +
            "ORDER BY occurrence_date ASC, id ASC"
    )
    fun observeOverrides(activityId: Long): Flow<List<ActivityOverrideEntity>>

    @Query(
        "SELECT * FROM activity_overrides WHERE activity_id = :activityId " +
            "AND occurrence_date = :occurrenceDate"
    )
    suspend fun getOverride(activityId: Long, occurrenceDate: LocalDate): ActivityOverrideEntity?

    @Insert
    suspend fun insertOverride(entity: ActivityOverrideEntity): Long

    @Update
    suspend fun updateOverride(entity: ActivityOverrideEntity): Int

    @Query("DELETE FROM activity_overrides WHERE id = :id")
    suspend fun deleteOverrideById(id: Long)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY due_date IS NULL, due_date ASC, id ASC")
    fun observeAll(): Flow<List<AgendaTaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    suspend fun getAll(): List<AgendaTaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): AgendaTaskEntity?

    @Insert
    suspend fun insert(entity: AgendaTaskEntity): Long

    @Update
    suspend fun update(entity: AgendaTaskEntity): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY is_archived ASC, id ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    suspend fun getAll(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Insert
    suspend fun insert(entity: HabitEntity): Long

    @Update
    suspend fun update(entity: HabitEntity): Int

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM habits")
    suspend fun deleteAll()

    @Query("SELECT * FROM habit_logs ORDER BY date ASC, id ASC")
    suspend fun getAllLogs(): List<HabitLogEntity>

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllLogs()

    @Query("SELECT * FROM habit_logs ORDER BY date ASC, id ASC")
    fun observeAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY date ASC, id ASC")
    fun observeLogs(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND date = :date")
    suspend fun getLog(habitId: Long, date: LocalDate): HabitLogEntity?

    @Insert
    suspend fun insertLog(entity: HabitLogEntity): Long

    @Update
    suspend fun updateLog(entity: HabitLogEntity): Int

    @Query("DELETE FROM habit_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM daily_reflections WHERE date = :date")
    fun observe(date: LocalDate): Flow<DailyReflectionEntity?>

    @Query("SELECT * FROM daily_reflections WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<DailyReflectionEntity>>

    @Query("SELECT * FROM daily_reflections WHERE date = :date")
    suspend fun get(date: LocalDate): DailyReflectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyReflectionEntity)

    @Query("DELETE FROM daily_reflections WHERE date = :date")
    suspend fun delete(date: LocalDate)

    @Query("SELECT * FROM daily_reflections ORDER BY date ASC")
    suspend fun getAll(): List<DailyReflectionEntity>

    @Query("DELETE FROM daily_reflections")
    suspend fun deleteAll()
}
