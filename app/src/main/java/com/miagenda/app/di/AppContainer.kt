package com.miagenda.app.di

import android.content.Context
import com.miagenda.app.data.backup.BackupFiles
import com.miagenda.app.data.backup.JsonBackupCodec
import com.miagenda.app.data.backup.RoomAgendaBackupStore
import com.miagenda.app.data.local.AgendaDatabase
import com.miagenda.app.data.local.RoomActivityRepository
import com.miagenda.app.data.local.RoomCategoryRepository
import com.miagenda.app.data.local.RoomHabitRepository
import com.miagenda.app.data.local.RoomReflectionRepository
import com.miagenda.app.data.local.RoomTaskRepository
import com.miagenda.app.data.preferences.DataStoreUserPreferencesRepository
import com.miagenda.app.domain.time.TimeProvider
import com.miagenda.app.domain.usecase.ExportBackup
import com.miagenda.app.domain.usecase.ImportBackup
import com.miagenda.app.domain.usecase.ObserveUserPreferences
import com.miagenda.app.domain.usecase.UpdateAccentColor
import com.miagenda.app.domain.usecase.UpdateDisplayName
import com.miagenda.app.domain.usecase.UpdateHomeSections
import com.miagenda.app.domain.usecase.UpdateThemeMode
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AppContainer(
    context: Context,
    appScope: CoroutineScope
) {
    private val applicationContext = context.applicationContext
    private val preferencesRepository = DataStoreUserPreferencesRepository(applicationContext)
    private val timeProvider = TimeProvider { LocalDateTime.now() }
    val database: AgendaDatabase = AgendaDatabase.create(applicationContext)
    val categoryRepository = RoomCategoryRepository(database.categoryDao(), timeProvider)
    val activityRepository = RoomActivityRepository(
        activityDao = database.activityDao(),
        recurrenceDao = database.recurrenceDao(),
        timeProvider = timeProvider
    )
    val taskRepository = RoomTaskRepository(database.taskDao(), timeProvider)
    val habitRepository = RoomHabitRepository(database.habitDao(), timeProvider)
    val reflectionRepository = RoomReflectionRepository(database.reflectionDao(), timeProvider)
    private val backupStore = RoomAgendaBackupStore(database)
    private val exportBackup = ExportBackup(
        store = backupStore,
        preferences = preferencesRepository,
        codec = JsonBackupCodec,
        timeProvider = timeProvider
    )
    private val importBackup = ImportBackup(
        store = backupStore,
        preferences = preferencesRepository,
        codec = JsonBackupCodec
    )
    private val backupFiles = BackupFiles(applicationContext)

    val observeUserPreferences = ObserveUserPreferences(preferencesRepository)
    val updateDisplayName = UpdateDisplayName(preferencesRepository)
    val updateThemeMode = UpdateThemeMode(preferencesRepository)
    val updateAccentColor = UpdateAccentColor(preferencesRepository)
    val updateHomeSections = UpdateHomeSections(preferencesRepository)

    init {
        appScope.launch {
            database.categoryDao().count()
        }
    }

    fun viewModelFactory(): MiAgendaViewModelFactory {
        return MiAgendaViewModelFactory(
            observeUserPreferences = observeUserPreferences,
            updateDisplayName = updateDisplayName,
            updateThemeMode = updateThemeMode,
            updateAccentColor = updateAccentColor,
            updateHomeSections = updateHomeSections,
            activityRepository = activityRepository,
            categoryRepository = categoryRepository,
            taskRepository = taskRepository,
            habitRepository = habitRepository,
            reflectionRepository = reflectionRepository,
            timeProvider = timeProvider,
            exportBackup = exportBackup,
            importBackup = importBackup,
            backupFiles = backupFiles
        )
    }
}
