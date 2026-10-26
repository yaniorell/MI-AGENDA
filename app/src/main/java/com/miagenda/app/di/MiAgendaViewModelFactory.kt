package com.miagenda.app.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.miagenda.app.data.backup.BackupFiles
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.HabitRepository
import com.miagenda.app.domain.repository.ReflectionRepository
import com.miagenda.app.domain.repository.TaskRepository
import com.miagenda.app.domain.time.TimeProvider
import com.miagenda.app.domain.usecase.ExportBackup
import com.miagenda.app.domain.usecase.ImportBackup
import com.miagenda.app.domain.usecase.ObserveUserPreferences
import com.miagenda.app.domain.usecase.UpdateAccentColor
import com.miagenda.app.domain.usecase.UpdateDisplayName
import com.miagenda.app.domain.usecase.UpdateHomeSections
import com.miagenda.app.domain.usecase.UpdateThemeMode
import com.miagenda.app.ui.backup.BackupViewModel
import com.miagenda.app.ui.activity.ActivityDetailViewModel
import com.miagenda.app.ui.activity.ActivityEditorViewModel
import com.miagenda.app.ui.calendar.CalendarViewModel
import com.miagenda.app.ui.category.CategoryManagerViewModel
import com.miagenda.app.ui.habit.HabitDetailViewModel
import com.miagenda.app.ui.habit.HabitEditorViewModel
import com.miagenda.app.ui.habit.HabitsViewModel
import com.miagenda.app.ui.home.HomeViewModel
import com.miagenda.app.ui.reflection.ReflectionViewModel
import com.miagenda.app.ui.reflection.WeekSummaryViewModel
import com.miagenda.app.ui.task.TaskDetailViewModel
import com.miagenda.app.ui.task.TaskEditorViewModel
import com.miagenda.app.ui.task.TasksViewModel
import com.miagenda.app.ui.settings.SettingsViewModel
import com.miagenda.app.ui.theme.AppViewModel
import java.time.LocalDate

class MiAgendaViewModelFactory(
    private val observeUserPreferences: ObserveUserPreferences,
    private val updateDisplayName: UpdateDisplayName,
    private val updateThemeMode: UpdateThemeMode,
    private val updateAccentColor: UpdateAccentColor,
    private val updateHomeSections: UpdateHomeSections,
    private val activityRepository: ActivityRepository,
    private val categoryRepository: CategoryRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val reflectionRepository: ReflectionRepository,
    private val timeProvider: TimeProvider,
    private val exportBackup: ExportBackup,
    private val importBackup: ImportBackup,
    private val backupFiles: BackupFiles,
    private val dateProvider: DateProvider = DateProvider { LocalDate.now() }
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val viewModel: ViewModel = when {
            modelClass.isAssignableFrom(AppViewModel::class.java) ->
                AppViewModel(observeUserPreferences)
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(
                    observeUserPreferences = observeUserPreferences,
                    activityRepository = activityRepository,
                    categoryRepository = categoryRepository,
                    habitRepository = habitRepository,
                    reflectionRepository = reflectionRepository,
                    dateProvider = dateProvider,
                    timeProvider = timeProvider
                )
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(
                    observeUserPreferences = observeUserPreferences,
                    updateDisplayName = updateDisplayName,
                    updateThemeMode = updateThemeMode,
                    updateAccentColor = updateAccentColor,
                    updateHomeSections = updateHomeSections
                )
            modelClass.isAssignableFrom(BackupViewModel::class.java) ->
                BackupViewModel(
                    exportBackup = exportBackup,
                    importBackup = importBackup,
                    backupFiles = backupFiles,
                    dateProvider = dateProvider
                )
            modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
                CalendarViewModel(
                    activityRepository = activityRepository,
                    categoryRepository = categoryRepository,
                    dateProvider = dateProvider
                )
            modelClass.isAssignableFrom(ActivityEditorViewModel::class.java) ->
                ActivityEditorViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    activityRepository = activityRepository,
                    categoryRepository = categoryRepository
                )
            modelClass.isAssignableFrom(ActivityDetailViewModel::class.java) ->
                ActivityDetailViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    activityRepository = activityRepository,
                    categoryRepository = categoryRepository
                )
            modelClass.isAssignableFrom(CategoryManagerViewModel::class.java) ->
                CategoryManagerViewModel(categoryRepository)
            modelClass.isAssignableFrom(TasksViewModel::class.java) ->
                TasksViewModel(taskRepository, categoryRepository, dateProvider)
            modelClass.isAssignableFrom(TaskEditorViewModel::class.java) ->
                TaskEditorViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    taskRepository = taskRepository,
                    categoryRepository = categoryRepository
                )
            modelClass.isAssignableFrom(TaskDetailViewModel::class.java) ->
                TaskDetailViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    taskRepository = taskRepository,
                    categoryRepository = categoryRepository
                )
            modelClass.isAssignableFrom(HabitsViewModel::class.java) ->
                HabitsViewModel(habitRepository, categoryRepository, dateProvider)
            modelClass.isAssignableFrom(HabitEditorViewModel::class.java) ->
                HabitEditorViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    habitRepository = habitRepository,
                    categoryRepository = categoryRepository
                )
            modelClass.isAssignableFrom(ReflectionViewModel::class.java) ->
                ReflectionViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    reflectionRepository = reflectionRepository,
                    dateProvider = dateProvider
                )
            modelClass.isAssignableFrom(WeekSummaryViewModel::class.java) ->
                WeekSummaryViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    activityRepository = activityRepository,
                    taskRepository = taskRepository,
                    habitRepository = habitRepository,
                    reflectionRepository = reflectionRepository,
                    dateProvider = dateProvider
                )
            modelClass.isAssignableFrom(HabitDetailViewModel::class.java) ->
                HabitDetailViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    habitRepository = habitRepository,
                    categoryRepository = categoryRepository,
                    dateProvider = dateProvider
                )
            else -> throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
        }
        return viewModel as T
    }
}
