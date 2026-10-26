package com.miagenda.app.ui.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.habit.habitOccursOn
import com.miagenda.app.domain.habit.habitProgress
import com.miagenda.app.domain.habit.recentScheduledDays
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.home.agendaDateLabel
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.HabitRepository
import com.miagenda.app.domain.time.parseDaysOfWeek
import com.miagenda.app.ui.navigation.HabitDetailRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitHistoryRow(
    val label: String,
    val status: HabitLogStatus?
)

data class HabitDetailUiState(
    val missing: Boolean = false,
    val title: String = "",
    val notes: String = "",
    val categoryName: String? = null,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val selectedDays: Set<Int> = emptySet(),
    val archived: Boolean = false,
    val scheduledToday: Boolean = false,
    val todayStatus: HabitLogStatus? = null,
    val percent: Int? = null,
    val completed: Int = 0,
    val partial: Int = 0,
    val notDone: Int = 0,
    val unmarked: Int = 0,
    val streak: Int = 0,
    val history: List<HabitHistoryRow> = emptyList(),
    val actionError: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
    categoryRepository: CategoryRepository,
    private val dateProvider: DateProvider
) : ViewModel() {
    private val habitId = savedStateHandle.toRoute<HabitDetailRoute>().habitId
    private val today = MutableStateFlow(dateProvider.today())
    private val actionError = MutableStateFlow(false)
    private val closedInternal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val closed: SharedFlow<Unit> = closedInternal.asSharedFlow()

    val uiState: StateFlow<HabitDetailUiState> = today.flatMapLatest { day ->
        combine(
            habitRepository.observeAll(),
            habitRepository.observeLogs(habitId),
            categoryRepository.observeAll(),
            actionError
        ) { habits, logs, categories, failed ->
            val habit = habits.firstOrNull { it.id == habitId }
            if (habit == null) {
                HabitDetailUiState(missing = true, actionError = failed)
            } else {
                val progress = habitProgress(habit, logs, day)
                HabitDetailUiState(
                    title = habit.title,
                    notes = habit.notes,
                    categoryName = categories.firstOrNull { it.id == habit.categoryId }?.name,
                    frequency = habit.frequency,
                    selectedDays = parseDaysOfWeek(habit.daysOfWeek),
                    archived = habit.isArchived,
                    scheduledToday = habitOccursOn(habit, day),
                    todayStatus = logs.firstOrNull { it.date == day }?.status,
                    percent = progress.percent,
                    completed = progress.completed,
                    partial = progress.partial,
                    notDone = progress.notDone,
                    unmarked = progress.unmarked,
                    streak = progress.streak,
                    history = recentScheduledDays(habit, logs, day).map { record ->
                        HabitHistoryRow(
                            label = agendaDateLabel(record.date),
                            status = record.status
                        )
                    },
                    actionError = failed
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HabitDetailUiState()
    )

    fun onResume() {
        today.value = dateProvider.today()
    }

    fun setToday(status: HabitLogStatus) {
        viewModelScope.launch {
            try {
                habitRepository.saveLog(habitId, today.value, status)
                actionError.value = false
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                actionError.value = true
            }
        }
    }

    fun clearToday() {
        viewModelScope.launch {
            try {
                val log = habitRepository.getLog(habitId, today.value) ?: return@launch
                habitRepository.deleteLog(log.id)
                actionError.value = false
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                actionError.value = true
            }
        }
    }

    fun setArchived(archived: Boolean) {
        viewModelScope.launch {
            val habit = habitRepository.get(habitId) ?: return@launch
            try {
                habitRepository.update(habit.copy(isArchived = archived))
                actionError.value = false
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                actionError.value = true
            }
        }
    }

    fun deleteHabit() {
        viewModelScope.launch {
            try {
                habitRepository.delete(habitId)
                closedInternal.tryEmit(Unit)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                actionError.value = true
            }
        }
    }
}
