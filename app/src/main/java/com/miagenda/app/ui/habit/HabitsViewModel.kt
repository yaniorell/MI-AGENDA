package com.miagenda.app.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.domain.habit.habitOccursOn
import com.miagenda.app.domain.habit.habitProgress
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class HabitRowUi(
    val id: Long,
    val title: String,
    val categoryName: String?,
    val colorArgb: Int?,
    val scheduledToday: Boolean,
    val todayStatus: HabitLogStatus?,
    val percent: Int?,
    val archived: Boolean
)

data class HabitsUiState(
    val active: List<HabitRowUi> = emptyList(),
    val archived: List<HabitRowUi> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel(
    habitRepository: HabitRepository,
    categoryRepository: CategoryRepository,
    private val dateProvider: DateProvider
) : ViewModel() {
    private val today = MutableStateFlow(dateProvider.today())

    val uiState: StateFlow<HabitsUiState> = today.flatMapLatest { day ->
        combine(
            habitRepository.observeAll(),
            habitRepository.observeAllLogs(),
            categoryRepository.observeAll()
        ) { habits, logs, categories ->
            val byId = categories.associateBy { category -> category.id }
            val rows = habits.map { habit ->
                val progress = habitProgress(habit, logs, day)
                val category = habit.categoryId?.let(byId::get)
                HabitRowUi(
                    id = habit.id,
                    title = habit.title,
                    categoryName = category?.name,
                    colorArgb = category?.colorArgb,
                    scheduledToday = habitOccursOn(habit, day),
                    todayStatus = logs.firstOrNull { it.habitId == habit.id && it.date == day }?.status,
                    percent = progress.percent,
                    archived = habit.isArchived
                )
            }
            HabitsUiState(
                active = rows.filter { !it.archived },
                archived = rows.filter { it.archived }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HabitsUiState()
    )

    fun onResume() {
        today.value = dateProvider.today()
    }
}
