package com.miagenda.app.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object CalendarRoute

@Serializable
data object TasksRoute

@Serializable
data object HabitsRoute

@Serializable
data object SettingsRoute

@Serializable
data class EditorRoute(
    val kind: String,
    val activityId: Long = -1,
    val date: String = "",
    val taskId: Long = -1,
    val habitId: Long = -1,
    val start: String = "",
    val preset: String = ""
)

@Serializable
data class TimetableRoute(
    val date: String = ""
)

@Serializable
data class ActivityDetailRoute(
    val activityId: Long,
    val date: String
)

@Serializable
data class TaskDetailRoute(
    val taskId: Long
)

@Serializable
data class HabitDetailRoute(
    val habitId: Long
)

@Serializable
data class ReflectionRoute(
    val date: String = ""
)

@Serializable
data class WeekSummaryRoute(
    val date: String = ""
)

@Serializable
data object CategoriesRoute

object EditorKind {
    const val ACTIVITY = "actividad"
    const val TASK = "tarea"
    const val HABIT = "habito"
    const val CLASS = "clase"
}

object EditorPreset {
    const val CLASS = "clase"
}
