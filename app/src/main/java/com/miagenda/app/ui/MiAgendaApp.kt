package com.miagenda.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.miagenda.app.ui.backup.BackupSection
import com.miagenda.app.ui.activity.ActivityDetailScreen
import com.miagenda.app.ui.activity.ActivityDetailViewModel
import com.miagenda.app.ui.activity.ActivityEditorScreen
import com.miagenda.app.ui.activity.ActivityEditorViewModel
import com.miagenda.app.ui.calendar.CalendarScreen
import com.miagenda.app.ui.calendar.CalendarViewModel
import com.miagenda.app.ui.category.CategoryManagerScreen
import com.miagenda.app.ui.category.CategoryManagerViewModel
import com.miagenda.app.ui.components.AgendaScaffold
import com.miagenda.app.ui.components.MainDestination
import com.miagenda.app.ui.components.showsBottomBar
import com.miagenda.app.ui.editor.EditorNotReadyScreen
import com.miagenda.app.ui.habit.HabitDetailScreen
import com.miagenda.app.ui.habit.HabitDetailViewModel
import com.miagenda.app.ui.habit.HabitEditorScreen
import com.miagenda.app.ui.habit.HabitEditorViewModel
import com.miagenda.app.ui.habit.HabitsScreen
import com.miagenda.app.ui.habit.HabitsViewModel
import com.miagenda.app.ui.home.HomeScreen
import com.miagenda.app.ui.home.HomeViewModel
import com.miagenda.app.ui.reflection.ReflectionScreen
import com.miagenda.app.ui.reflection.ReflectionViewModel
import com.miagenda.app.ui.reflection.WeekSummaryScreen
import com.miagenda.app.ui.reflection.WeekSummaryViewModel
import com.miagenda.app.ui.task.TaskDetailScreen
import com.miagenda.app.ui.task.TaskDetailViewModel
import com.miagenda.app.ui.task.TaskEditorScreen
import com.miagenda.app.ui.task.TaskEditorViewModel
import com.miagenda.app.ui.task.TasksScreen
import com.miagenda.app.ui.task.TasksViewModel
import com.miagenda.app.ui.navigation.ActivityDetailRoute
import com.miagenda.app.ui.navigation.AddKindSheet
import com.miagenda.app.ui.navigation.CalendarRoute
import com.miagenda.app.ui.navigation.CategoriesRoute
import com.miagenda.app.ui.navigation.EditorKind
import com.miagenda.app.ui.navigation.EditorRoute
import com.miagenda.app.ui.navigation.HabitDetailRoute
import com.miagenda.app.ui.navigation.HabitsRoute
import com.miagenda.app.ui.navigation.HomeRoute
import com.miagenda.app.ui.navigation.ReflectionRoute
import com.miagenda.app.ui.navigation.WeekSummaryRoute
import com.miagenda.app.ui.navigation.SettingsRoute
import com.miagenda.app.ui.navigation.TaskDetailRoute
import com.miagenda.app.ui.navigation.TasksRoute
import com.miagenda.app.ui.settings.SettingsScreen
import com.miagenda.app.ui.settings.SettingsViewModel
import androidx.compose.foundation.layout.padding

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("La factory de ViewModels no está disponible.")
}

@Composable
fun MiAgendaApp(factory: ViewModelProvider.Factory) {
    val navController = rememberNavController()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    CompositionLocalProvider(LocalViewModelFactory provides factory) {
        AgendaScaffold(
            currentDestination = destination,
            showBottomBar = destination.showsBottomBar(),
            onNavigate = { target ->
                navController.navigate(target.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onAddClick = { showAddSheet = true }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = HomeRoute,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable<HomeRoute> {
                    val viewModel: HomeViewModel = viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner, viewModel) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                viewModel.onResume()
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }
                    HomeScreen(
                        state = state,
                        onOpenCalendar = {
                            navController.navigate(CalendarRoute) {
                                launchSingleTop = true
                            }
                        },
                        onOpenTasks = {
                            navController.navigate(TasksRoute) {
                                launchSingleTop = true
                            }
                        },
                        onOpenHabits = {
                            navController.navigate(HabitsRoute) {
                                launchSingleTop = true
                            }
                        },
                        onOpenActivity = { item ->
                            navController.navigate(
                                ActivityDetailRoute(item.activityId, item.date.toString())
                            )
                        },
                        onOpenHabit = { habitId ->
                            navController.navigate(HabitDetailRoute(habitId))
                        },
                        onOpenReflection = { navController.navigate(ReflectionRoute()) },
                        onOpenWeek = { navController.navigate(WeekSummaryRoute()) }
                    )
                }
                composable<CalendarRoute> {
                    val viewModel: CalendarViewModel = viewModel(factory = LocalViewModelFactory.current)
                    CalendarScreen(
                        viewModel = viewModel,
                        onOpenActivity = { activityId, date ->
                            navController.navigate(ActivityDetailRoute(activityId, date))
                        },
                        onAddActivity = { date ->
                            navController.navigate(
                                EditorRoute(kind = EditorKind.ACTIVITY, date = date)
                            )
                        },
                        onOpenCategories = { navController.navigate(CategoriesRoute) }
                    )
                }
                composable<CategoriesRoute> {
                    val viewModel: CategoryManagerViewModel =
                        viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    CategoryManagerScreen(
                        state = state,
                        onNameChange = viewModel::onNameChange,
                        onColorChange = viewModel::onColorChange,
                        onSave = viewModel::save,
                        onArchive = viewModel::setArchived,
                        onDelete = viewModel::delete,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<ActivityDetailRoute> {
                    val viewModel: ActivityDetailViewModel =
                        viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(viewModel) {
                        viewModel.closed.collect { navController.popBackStack() }
                    }
                    ActivityDetailScreen(
                        state = state,
                        onStatus = viewModel::setStatus,
                        onEdit = {
                            val item = state.item ?: return@ActivityDetailScreen
                            navController.navigate(
                                EditorRoute(
                                    kind = EditorKind.ACTIVITY,
                                    activityId = item.activityId,
                                    date = item.date.toString()
                                )
                            )
                        },
                        onSkip = viewModel::skipThisDay,
                        onDelete = viewModel::deleteSeries,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<TasksRoute> {
                    val viewModel: TasksViewModel = viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    ResumeOnStart(viewModel::onResume)
                    TasksScreen(
                        state = state,
                        onOpenTask = { taskId -> navController.navigate(TaskDetailRoute(taskId)) },
                        onAddTask = { navController.navigate(EditorRoute(EditorKind.TASK)) }
                    )
                }
                composable<TaskDetailRoute> { entry ->
                    val taskId = entry.toRoute<TaskDetailRoute>().taskId
                    val viewModel: TaskDetailViewModel = viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    LaunchedEffect(viewModel) {
                        viewModel.closed.collect { navController.popBackStack() }
                    }
                    TaskDetailScreen(
                        state = state,
                        onStatus = viewModel::setStatus,
                        onEdit = {
                            navController.navigate(EditorRoute(kind = EditorKind.TASK, taskId = taskId))
                        },
                        onDelete = viewModel::deleteTask,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<HabitsRoute> {
                    val viewModel: HabitsViewModel = viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    ResumeOnStart(viewModel::onResume)
                    HabitsScreen(
                        state = state,
                        onOpenHabit = { habitId -> navController.navigate(HabitDetailRoute(habitId)) },
                        onAddHabit = { navController.navigate(EditorRoute(EditorKind.HABIT)) }
                    )
                }
                composable<HabitDetailRoute> { entry ->
                    val habitId = entry.toRoute<HabitDetailRoute>().habitId
                    val viewModel: HabitDetailViewModel = viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    ResumeOnStart(viewModel::onResume)
                    LaunchedEffect(viewModel) {
                        viewModel.closed.collect { navController.popBackStack() }
                    }
                    HabitDetailScreen(
                        state = state,
                        onStatus = viewModel::setToday,
                        onClear = viewModel::clearToday,
                        onArchive = viewModel::setArchived,
                        onEdit = {
                            navController.navigate(EditorRoute(kind = EditorKind.HABIT, habitId = habitId))
                        },
                        onDelete = viewModel::deleteHabit,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<ReflectionRoute> {
                    val viewModel: ReflectionViewModel = viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    ReflectionScreen(
                        state = state,
                        onNoteChange = viewModel::onNoteChange,
                        onPreviousDay = viewModel::showPreviousDay,
                        onNextDay = viewModel::showNextDay,
                        onSave = viewModel::save,
                        onOpenWeek = { navController.navigate(WeekSummaryRoute(date = state.dateIso)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<WeekSummaryRoute> {
                    val viewModel: WeekSummaryViewModel = viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    WeekSummaryScreen(
                        state = state,
                        onPreviousWeek = viewModel::showPreviousWeek,
                        onNextWeek = viewModel::showNextWeek,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable<SettingsRoute> {
                    val viewModel: SettingsViewModel = viewModel(factory = LocalViewModelFactory.current)
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    SettingsScreen(
                        state = state,
                        onNameEdited = viewModel::onNameEdited,
                        onSaveName = viewModel::saveDisplayName,
                        onThemeSelected = viewModel::setTheme,
                        onAccentSelected = viewModel::setAccent,
                        onSectionVisibleChange = viewModel::setSectionVisible,
                        footer = { BackupSection() }
                    )
                }
                composable<EditorRoute> { entry ->
                    val route = entry.toRoute<EditorRoute>()
                    when (route.kind) {
                        EditorKind.ACTIVITY -> {
                            val viewModel: ActivityEditorViewModel =
                                viewModel(factory = LocalViewModelFactory.current)
                            val state by viewModel.uiState.collectAsStateWithLifecycle()
                            LaunchedEffect(viewModel) {
                                viewModel.saved.collect { navController.popBackStack() }
                            }
                            ActivityEditorScreen(
                                state = state,
                                onTitleChange = viewModel::onTitleChange,
                                onNotesChange = viewModel::onNotesChange,
                                onDateChange = viewModel::onDateChange,
                                onStartChange = viewModel::onStartChange,
                                onEndChange = viewModel::onEndChange,
                                onCategoryChange = viewModel::onCategoryChange,
                                onRepeatChange = viewModel::onRepeatChange,
                                onDayToggle = viewModel::onDayToggle,
                                onIntervalChange = viewModel::onIntervalChange,
                                onEndDateChange = viewModel::onEndDateChange,
                                onSave = viewModel::save,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        EditorKind.TASK -> {
                            val viewModel: TaskEditorViewModel =
                                viewModel(factory = LocalViewModelFactory.current)
                            val state by viewModel.uiState.collectAsStateWithLifecycle()
                            LaunchedEffect(viewModel) {
                                viewModel.saved.collect { navController.popBackStack() }
                            }
                            TaskEditorScreen(
                                state = state,
                                onTitleChange = viewModel::onTitleChange,
                                onNotesChange = viewModel::onNotesChange,
                                onDueChange = viewModel::onDueChange,
                                onPriorityChange = viewModel::onPriorityChange,
                                onCategoryChange = viewModel::onCategoryChange,
                                onSave = viewModel::save,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        EditorKind.HABIT -> {
                            val viewModel: HabitEditorViewModel =
                                viewModel(factory = LocalViewModelFactory.current)
                            val state by viewModel.uiState.collectAsStateWithLifecycle()
                            LaunchedEffect(viewModel) {
                                viewModel.saved.collect { navController.popBackStack() }
                            }
                            HabitEditorScreen(
                                state = state,
                                onTitleChange = viewModel::onTitleChange,
                                onNotesChange = viewModel::onNotesChange,
                                onCategoryChange = viewModel::onCategoryChange,
                                onRepeatChange = viewModel::onRepeatChange,
                                onDayToggle = viewModel::onDayToggle,
                                onSave = viewModel::save,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        else -> {
                            EditorNotReadyScreen(
                                kind = route.kind,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
        if (showAddSheet) {
            AddKindSheet(
                onDismiss = { showAddSheet = false },
                onPick = { kind ->
                    showAddSheet = false
                    navController.navigate(EditorRoute(kind))
                }
            )
        }
    }
}

@Composable
private fun ResumeOnStart(onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, onResume) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
