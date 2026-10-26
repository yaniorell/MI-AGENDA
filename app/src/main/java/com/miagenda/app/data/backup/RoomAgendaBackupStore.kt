package com.miagenda.app.data.backup

import androidx.room.Transactor
import androidx.room.useWriterConnection
import com.miagenda.app.data.local.AgendaDatabase
import com.miagenda.app.data.local.toDomain
import com.miagenda.app.data.local.toEntity
import com.miagenda.app.domain.backup.AgendaSnapshot
import com.miagenda.app.domain.repository.AgendaBackupStore

internal class RoomAgendaBackupStore(
    private val database: AgendaDatabase
) : AgendaBackupStore {
    override suspend fun read(): AgendaSnapshot {
        return database.inTransaction {
            AgendaSnapshot(
                categories = database.categoryDao().getAll().map { category -> category.toDomain() },
                rules = database.recurrenceDao().getAll().map { rule -> rule.toDomain() },
                activities = database.activityDao().getAll().map { activity -> activity.toDomain() },
                overrides = database.activityDao().getAllOverrides().map { override -> override.toDomain() },
                tasks = database.taskDao().getAll().map { task -> task.toDomain() },
                habits = database.habitDao().getAll().map { habit -> habit.toDomain() },
                logs = database.habitDao().getAllLogs().map { log -> log.toDomain() },
                reflections = database.reflectionDao().getAll().map { reflection -> reflection.toDomain() }
            )
        }
    }

    override suspend fun replace(snapshot: AgendaSnapshot) {
        database.inTransaction {
            database.habitDao().deleteAllLogs()
            database.habitDao().deleteAll()
            database.taskDao().deleteAll()
            database.activityDao().deleteAllOverrides()
            database.activityDao().deleteAll()
            database.recurrenceDao().deleteAll()
            database.reflectionDao().deleteAll()
            database.categoryDao().deleteAll()

            snapshot.categories.forEach { category ->
                database.categoryDao().insert(category.toEntity())
            }
            snapshot.rules.forEach { rule ->
                database.recurrenceDao().insert(rule.toEntity())
            }
            snapshot.activities.forEach { activity ->
                database.activityDao().insert(activity.toEntity())
            }
            snapshot.overrides.forEach { override ->
                database.activityDao().insertOverride(override.toEntity())
            }
            snapshot.tasks.forEach { task ->
                database.taskDao().insert(task.toEntity())
            }
            snapshot.habits.forEach { habit ->
                database.habitDao().insert(habit.toEntity())
            }
            snapshot.logs.forEach { log ->
                database.habitDao().insertLog(log.toEntity())
            }
            snapshot.reflections.forEach { reflection ->
                database.reflectionDao().upsert(reflection.toEntity())
            }
        }
    }
}

private suspend fun <T> AgendaDatabase.inTransaction(block: suspend () -> T): T {
    return useWriterConnection { transactor ->
        transactor.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
            block()
        }
    }
}
