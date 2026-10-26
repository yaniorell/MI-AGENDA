package com.miagenda.app.ui.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.activity.expandActivities
import com.miagenda.app.domain.activity.mondayOf
import com.miagenda.app.domain.activity.parseAgendaDate
import com.miagenda.app.domain.activity.visibleOccurrences
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.ui.activity.AgendaItem
import com.miagenda.app.ui.activity.toAgendaItem
import com.miagenda.app.ui.navigation.TimetableRoute
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class TimetableUiState(
    val weekStart: LocalDate,
    val today: LocalDate,
    val items: List<AgendaItem>
)

@OptIn(ExperimentalCoroutinesApi::class)
class TimetableViewModel(
    savedStateHandle: SavedStateHandle,
    activityRepository: ActivityRepository,
    categoryRepository: CategoryRepository,
    private val dateProvider: DateProvider
) : ViewModel() {
    private val anchor = MutableStateFlow(
        parseAgendaDate(savedStateHandle.toRoute<TimetableRoute>().date) ?: dateProvider.today()
    )

    val uiState: StateFlow<TimetableUiState> = anchor.flatMapLatest { selected ->
        val weekStart = mondayOf(selected)
        val weekEnd = weekStart.plusDays(6)
        combine(
            activityRepository.observeAll(),
            activityRepository.observeRules(),
            activityRepository.observeOverridesBetween(weekStart, weekEnd),
            categoryRepository.observeAll()
        ) { activities, rules, overrides, categories ->
            val occurrences = visibleOccurrences(
                expandActivities(activities, rules, overrides, weekStart, weekEnd)
            )
            TimetableUiState(
                weekStart = weekStart,
                today = dateProvider.today(),
                items = occurrences.map { it.toAgendaItem(categories) }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimetableUiState(
            weekStart = mondayOf(dateProvider.today()),
            today = dateProvider.today(),
            items = emptyList()
        )
    )

    fun showPreviousWeek() {
        anchor.update { current -> current.minusWeeks(1) }
    }

    fun showNextWeek() {
        anchor.update { current -> current.plusWeeks(1) }
    }

    fun showToday() {
        anchor.value = dateProvider.today()
    }
}
