package com.miagenda.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.domain.activity.expandActivities
import com.miagenda.app.domain.activity.mondayOf
import com.miagenda.app.domain.activity.visibleOccurrences
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.home.agendaDateLabel
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.ui.activity.AgendaItem
import com.miagenda.app.ui.activity.toAgendaItem
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CalendarDay(
    val date: LocalDate,
    val hasItems: Boolean,
    val isSelected: Boolean
)

data class CalendarUiState(
    val weekStart: LocalDate,
    val selectedDate: LocalDate,
    val selectedLabel: String,
    val days: List<CalendarDay>,
    val items: List<AgendaItem>
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    activityRepository: ActivityRepository,
    categoryRepository: CategoryRepository,
    dateProvider: DateProvider
) : ViewModel() {
    private val selection = MutableStateFlow(dateProvider.today())

    val uiState: StateFlow<CalendarUiState> = selection.flatMapLatest { selected ->
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
            CalendarUiState(
                weekStart = weekStart,
                selectedDate = selected,
                selectedLabel = agendaDateLabel(selected),
                days = (0..6).map { offset ->
                    val date = weekStart.plusDays(offset.toLong())
                    CalendarDay(
                        date = date,
                        hasItems = occurrences.any { it.date == date },
                        isSelected = date == selected
                    )
                },
                items = occurrences.filter { it.date == selected }.map { it.toAgendaItem(categories) }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(
            weekStart = mondayOf(dateProvider.today()),
            selectedDate = dateProvider.today(),
            selectedLabel = agendaDateLabel(dateProvider.today()),
            days = emptyList(),
            items = emptyList()
        )
    )

    fun select(date: LocalDate) {
        selection.value = date
    }

    fun showPreviousWeek() {
        selection.update { current -> current.minusWeeks(1) }
    }

    fun showNextWeek() {
        selection.update { current -> current.plusWeeks(1) }
    }
}
