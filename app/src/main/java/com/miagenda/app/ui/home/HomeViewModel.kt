package com.miagenda.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.domain.activity.dayProgress
import com.miagenda.app.domain.activity.expandActivities
import com.miagenda.app.domain.activity.nextOpenOccurrence
import com.miagenda.app.domain.activity.visibleOccurrences
import com.miagenda.app.domain.habit.habitOccursOn
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.model.UserPreferences
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.HabitRepository
import com.miagenda.app.domain.repository.ReflectionRepository
import com.miagenda.app.domain.time.TimeProvider
import com.miagenda.app.domain.usecase.ObserveUserPreferences
import com.miagenda.app.ui.activity.toAgendaItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    observeUserPreferences: ObserveUserPreferences,
    private val activityRepository: ActivityRepository,
    private val categoryRepository: CategoryRepository,
    private val habitRepository: HabitRepository,
    private val reflectionRepository: ReflectionRepository,
    private val dateProvider: DateProvider,
    private val timeProvider: TimeProvider
) : ViewModel() {
    private val today = MutableStateFlow(dateProvider.today())

    val uiState: StateFlow<HomeUiState> = today.flatMapLatest { day ->
        combine(
            combine(
                observeUserPreferences(),
                activityRepository.observeAll(),
                activityRepository.observeRules(),
                activityRepository.observeOverridesBetween(day, day),
                categoryRepository.observeAll()
            ) { preferences, activities, rules, overrides, categories ->
                HomeSources(preferences, activities, rules, overrides, categories)
            },
            habitRepository.observeAll(),
            habitRepository.observeAllLogs(),
            reflectionRepository.observe(day)
        ) { sources, habits, logs, reflection ->
            val occurrences = visibleOccurrences(
                expandActivities(sources.activities, sources.rules, sources.overrides, day, day)
            )
            sources.preferences.toHomeUiState(day).copy(
                nextItem = nextOpenOccurrence(occurrences, timeProvider.now().toLocalTime())
                    ?.toAgendaItem(sources.categories),
                progressPercent = dayProgress(occurrences)?.percent,
                agenda = occurrences.map { it.toAgendaItem(sources.categories) },
                habits = habits
                    .filter { habit -> !habit.isArchived && habitOccursOn(habit, day) }
                    .map { habit ->
                        HomeHabitItem(
                            habitId = habit.id,
                            title = habit.title,
                            status = logs.firstOrNull { log ->
                                log.habitId == habit.id && log.date == day
                            }?.status,
                            colorArgb = sources.categories
                                .firstOrNull { category -> category.id == habit.categoryId }
                                ?.colorArgb
                        )
                    },
                reflectionNote = reflection?.note.orEmpty()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = com.miagenda.app.domain.model.UserPreferences().toHomeUiState(dateProvider.today())
    )

    fun onResume() {
        today.value = dateProvider.today()
    }
}

private data class HomeSources(
    val preferences: UserPreferences,
    val activities: List<AgendaActivity>,
    val rules: List<RecurrenceRule>,
    val overrides: List<ActivityOverride>,
    val categories: List<Category>
)
