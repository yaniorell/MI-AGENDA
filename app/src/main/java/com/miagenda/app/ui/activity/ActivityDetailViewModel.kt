package com.miagenda.app.ui.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.activity.expandActivities
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.ui.navigation.ActivityDetailRoute
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActivityDetailUiState(
    val missing: Boolean = false,
    val item: AgendaItem? = null,
    val notes: String = "",
    val isRecurring: Boolean = false
)

class ActivityDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val route = savedStateHandle.toRoute<ActivityDetailRoute>()
    private val date: LocalDate? = runCatching { LocalDate.parse(route.date) }.getOrNull()
    private val closedInternal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val closed: SharedFlow<Unit> = closedInternal.asSharedFlow()

    val uiState: StateFlow<ActivityDetailUiState> = combine(
        activityRepository.observeAll(),
        activityRepository.observeRules(),
        activityRepository.observeOverridesBetween(
            date ?: LocalDate.now(),
            date ?: LocalDate.now()
        ),
        categoryRepository.observeAll()
    ) { activities, rules, overrides, categories ->
        val day = date
        val activity = activities.firstOrNull { it.id == route.activityId }
        if (day == null || activity == null) {
            ActivityDetailUiState(missing = true)
        } else {
            val occurrence = expandActivities(
                activities = listOf(activity),
                rules = rules,
                overrides = overrides,
                rangeStart = day,
                rangeEnd = day
            ).singleOrNull()
            if (occurrence == null || occurrence.isSkipped) {
                ActivityDetailUiState(missing = true)
            } else {
                ActivityDetailUiState(
                    item = occurrence.toAgendaItem(categories),
                    notes = activity.notes,
                    isRecurring = activity.recurrenceRuleId != null
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivityDetailUiState()
    )

    fun setStatus(status: ActivityStatus) {
        val day = date ?: return
        viewModelScope.launch {
            val activity = activityRepository.get(route.activityId) ?: return@launch
            if (activity.recurrenceRuleId == null) {
                activityRepository.update(activity.copy(status = status))
            } else {
                saveOverride(activity.id, day) { current ->
                    current.copy(status = status, isSkipped = false)
                }
            }
        }
    }

    fun skipThisDay() {
        val day = date ?: return
        viewModelScope.launch {
            val activity = activityRepository.get(route.activityId) ?: return@launch
            if (activity.recurrenceRuleId == null) {
                return@launch
            }
            saveOverride(activity.id, day) { current -> current.copy(isSkipped = true) }
            closedInternal.tryEmit(Unit)
        }
    }

    fun deleteSeries() {
        viewModelScope.launch {
            activityRepository.delete(route.activityId)
            closedInternal.tryEmit(Unit)
        }
    }

    private suspend fun saveOverride(
        activityId: Long,
        day: LocalDate,
        transform: (ActivityOverride) -> ActivityOverride
    ) {
        val now = LocalDateTime.now()
        val existing = activityRepository.getOverride(activityId, day)
        if (existing == null) {
            activityRepository.insertOverride(
                transform(
                    ActivityOverride(
                        id = 0,
                        activityId = activityId,
                        occurrenceDate = day,
                        status = null,
                        startTime = null,
                        endTime = null,
                        isSkipped = false,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            )
        } else {
            activityRepository.updateOverride(transform(existing))
        }
    }
}
