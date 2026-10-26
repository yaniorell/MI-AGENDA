package com.miagenda.app.ui.reflection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.activity.parseAgendaDate
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.home.agendaDateLabel
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.HabitRepository
import com.miagenda.app.domain.repository.ReflectionRepository
import com.miagenda.app.domain.repository.TaskRepository
import com.miagenda.app.domain.summary.WeekHabitStats
import com.miagenda.app.domain.summary.WeekActivityStats
import com.miagenda.app.domain.summary.WeekTaskStats
import com.miagenda.app.domain.summary.summarizeWeek
import com.miagenda.app.domain.summary.weekBounds
import com.miagenda.app.ui.navigation.WeekSummaryRoute
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class WeekNoteUi(
    val label: String,
    val note: String
)

data class WeekSummaryUiState(
    val rangeLabel: String = "",
    val activities: WeekActivityStats = WeekActivityStats(0, 0, 0, 0, 0, null),
    val tasks: WeekTaskStats = WeekTaskStats(0, 0, 0, 0),
    val habits: WeekHabitStats = WeekHabitStats(0, 0, 0, 0, 0, null),
    val notes: List<WeekNoteUi> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class WeekSummaryViewModel(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val reflectionRepository: ReflectionRepository,
    dateProvider: DateProvider
) : ViewModel() {
    private val anchor = MutableStateFlow(
        parseAgendaDate(savedStateHandle.toRoute<WeekSummaryRoute>().date) ?: dateProvider.today()
    )

    val uiState: StateFlow<WeekSummaryUiState> = anchor.flatMapLatest { date ->
        val bounds = weekBounds(date)
        combine(
            combine(
                activityRepository.observeAll(),
                activityRepository.observeRules(),
                activityRepository.observeOverridesBetween(bounds.start, bounds.end),
                taskRepository.observeAll()
            ) { activities, rules, overrides, tasks ->
                WeekInputs(activities, rules, overrides, tasks)
            },
            habitRepository.observeAll(),
            habitRepository.observeAllLogs(),
            reflectionRepository.observeBetween(bounds.start, bounds.end)
        ) { inputs, habits, logs, notes ->
            val summary = summarizeWeek(
                activities = inputs.activities,
                rules = inputs.rules,
                overrides = inputs.overrides,
                tasks = inputs.tasks,
                habits = habits,
                logs = logs,
                dateInWeek = date
            )
            WeekSummaryUiState(
                rangeLabel = "${agendaDateLabel(summary.bounds.start)} – ${agendaDateLabel(summary.bounds.end)}",
                activities = summary.activities,
                tasks = summary.tasks,
                habits = summary.habits,
                notes = notes.map { note -> note.toUi() }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeekSummaryUiState()
    )

    fun showPreviousWeek() {
        anchor.update { date -> date.minusDays(7) }
    }

    fun showNextWeek() {
        anchor.update { date -> date.plusDays(7) }
    }
}

private data class WeekInputs(
    val activities: List<com.miagenda.app.domain.model.AgendaActivity>,
    val rules: List<com.miagenda.app.domain.model.RecurrenceRule>,
    val overrides: List<com.miagenda.app.domain.model.ActivityOverride>,
    val tasks: List<com.miagenda.app.domain.model.AgendaTask>
)

private fun DailyReflection.toUi(): WeekNoteUi {
    return WeekNoteUi(label = agendaDateLabel(date), note = note)
}
