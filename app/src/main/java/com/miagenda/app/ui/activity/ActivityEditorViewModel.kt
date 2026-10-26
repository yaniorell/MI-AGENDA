package com.miagenda.app.ui.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.activity.ActivityTimeCheck
import com.miagenda.app.domain.activity.checkActivityTimes
import com.miagenda.app.domain.activity.parseAgendaDate
import com.miagenda.app.domain.activity.parseClock
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.time.parseDaysOfWeek
import com.miagenda.app.ui.navigation.EditorRoute
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RepeatChoice {
    NONE,
    DAILY,
    SPECIFIC_DAYS,
    EVERY_N_WEEKS
}

data class ActivityEditorUiState(
    val title: String = "",
    val notes: String = "",
    val dateText: String = "",
    val startText: String = "09:00",
    val endText: String = "10:00",
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val repeat: RepeatChoice = RepeatChoice.NONE,
    val selectedDays: Set<Int> = emptySet(),
    val intervalText: String = "1",
    val endDateText: String = "",
    val isSeries: Boolean = false,
    val titleError: Boolean = false,
    val dateError: Boolean = false,
    val timeError: Boolean = false,
    val daysError: Boolean = false,
    val intervalError: Boolean = false,
    val endDateError: Boolean = false,
    val saveError: Boolean = false,
    val missing: Boolean = false
)

class ActivityEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val activityRepository: ActivityRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val route = savedStateHandle.toRoute<EditorRoute>()
    private val activityId = route.activityId
    private val stateInternal = MutableStateFlow(
        ActivityEditorUiState(dateText = initialDate(route.date))
    )
    val uiState: StateFlow<ActivityEditorUiState> = stateInternal.asStateFlow()
    private val savedInternal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saved: SharedFlow<Unit> = savedInternal.asSharedFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                stateInternal.update { it.copy(categories = categories.filter { category -> !category.isArchived }) }
            }
        }
        if (activityId >= 0) {
            viewModelScope.launch { loadExisting() }
        }
    }

    fun onTitleChange(value: String) = update { it.copy(title = value, titleError = false, saveError = false) }
    fun onNotesChange(value: String) = update { it.copy(notes = value) }
    fun onDateChange(value: String) = update { it.copy(dateText = value, dateError = false, endDateError = false) }
    fun onStartChange(value: String) = update { it.copy(startText = value, timeError = false) }
    fun onEndChange(value: String) = update { it.copy(endText = value, timeError = false) }
    fun onCategoryChange(id: Long?) = update { it.copy(categoryId = id) }
    fun onRepeatChange(choice: RepeatChoice) = update { current ->
        val days = if (choice == RepeatChoice.EVERY_N_WEEKS && current.selectedDays.isEmpty()) {
            parseAgendaDate(current.dateText)?.dayOfWeek?.value?.let(::setOf) ?: current.selectedDays
        } else {
            current.selectedDays
        }
        current.copy(repeat = choice, selectedDays = days, daysError = false, intervalError = false)
    }

    fun onDayToggle(day: Int) = update { current ->
        val days = if (day in current.selectedDays) current.selectedDays - day else current.selectedDays + day
        current.copy(selectedDays = days, daysError = false)
    }

    fun onIntervalChange(value: String) = update {
        it.copy(intervalText = value.filter(Char::isDigit).take(2), intervalError = false)
    }

    fun onEndDateChange(value: String) = update { it.copy(endDateText = value, endDateError = false) }

    fun save() {
        val current = stateInternal.value
        val date = parseAgendaDate(current.dateText)
        val start = parseClock(current.startText)
        val end = parseClock(current.endText)
        val endDate = current.endDateText.trim().takeIf { it.isNotEmpty() }?.let(::parseAgendaDate)
        val interval = current.intervalText.toIntOrNull()
        val titleBlank = current.title.isBlank()
        val dateInvalid = date == null
        val timeInvalid = start == null || end == null ||
            checkActivityTimes(start, end) != ActivityTimeCheck.Valid
        val daysInvalid = current.repeat == RepeatChoice.SPECIFIC_DAYS && current.selectedDays.isEmpty()
        val intervalInvalid = current.repeat == RepeatChoice.EVERY_N_WEEKS && (interval == null || interval < 1)
        val endInvalid = endDate == null && current.endDateText.isNotBlank() ||
            (date != null && endDate != null && endDate.isBefore(date))
        if (titleBlank || dateInvalid || timeInvalid || daysInvalid || intervalInvalid || endInvalid) {
            update {
                it.copy(
                    titleError = titleBlank,
                    dateError = dateInvalid,
                    timeError = timeInvalid,
                    daysError = daysInvalid,
                    intervalError = intervalInvalid,
                    endDateError = endInvalid
                )
            }
            return
        }
        viewModelScope.launch {
            try {
                persist(current, date, start, end, endDate, interval ?: 1)
                savedInternal.tryEmit(Unit)
            } catch (_: IllegalArgumentException) {
                update { it.copy(saveError = true) }
            }
        }
    }

    private suspend fun loadExisting() {
        val activity = activityRepository.get(activityId)
        if (activity == null) {
            update { it.copy(missing = true) }
            return
        }
        val rule = activity.recurrenceRuleId?.let { activityRepository.getRule(it) }
        val repeat = when (rule?.frequency) {
            null -> RepeatChoice.NONE
            RecurrenceFrequency.DAILY -> RepeatChoice.DAILY
            RecurrenceFrequency.SPECIFIC_DAYS -> RepeatChoice.SPECIFIC_DAYS
            RecurrenceFrequency.WEEKLY,
            RecurrenceFrequency.EVERY_N_WEEKS -> RepeatChoice.EVERY_N_WEEKS
        }
        update {
            it.copy(
                title = activity.title,
                notes = activity.notes,
                dateText = activity.date.toString(),
                startText = formatClock(activity.startTime),
                endText = formatClock(activity.endTime),
                categoryId = activity.categoryId,
                repeat = repeat,
                selectedDays = parseDaysOfWeek(rule?.daysOfWeek),
                intervalText = (rule?.intervalCount ?: 1).toString(),
                endDateText = rule?.endDate?.toString().orEmpty(),
                isSeries = rule != null
            )
        }
    }

    private suspend fun persist(
        current: ActivityEditorUiState,
        date: LocalDate,
        start: LocalTime,
        end: LocalTime,
        endDate: LocalDate?,
        interval: Int
    ) {
        val stamp = LocalDateTime.now()
        val base = AgendaActivity(
            id = if (activityId >= 0) activityId else 0,
            title = current.title,
            notes = current.notes,
            date = date,
            startTime = start,
            endTime = end,
            categoryId = current.categoryId,
            status = if (activityId >= 0) {
                activityRepository.get(activityId)?.status ?: ActivityStatus.PENDING
            } else {
                ActivityStatus.PENDING
            },
            recurrenceRuleId = null,
            createdAt = stamp,
            updatedAt = stamp
        )
        val existingRuleId = if (activityId >= 0) activityRepository.get(activityId)?.recurrenceRuleId else null
        if (current.repeat == RepeatChoice.NONE) {
            if (activityId >= 0) {
                activityRepository.update(base.copy(recurrenceRuleId = null))
            } else {
                activityRepository.insert(base)
            }
            if (existingRuleId != null) {
                activityRepository.deleteRule(existingRuleId)
            }
            return
        }
        val draftRule = RecurrenceRule(
            id = existingRuleId ?: 0,
            frequency = when (current.repeat) {
                RepeatChoice.NONE -> RecurrenceFrequency.DAILY
                RepeatChoice.DAILY -> RecurrenceFrequency.DAILY
                RepeatChoice.SPECIFIC_DAYS -> RecurrenceFrequency.SPECIFIC_DAYS
                RepeatChoice.EVERY_N_WEEKS -> RecurrenceFrequency.EVERY_N_WEEKS
            },
            intervalCount = if (current.repeat == RepeatChoice.EVERY_N_WEEKS) interval else 1,
            daysOfWeek = when (current.repeat) {
                RepeatChoice.SPECIFIC_DAYS,
                RepeatChoice.EVERY_N_WEEKS -> current.selectedDays.sorted().joinToString(",")
                else -> null
            },
            startDate = date,
            endDate = endDate,
            createdAt = stamp,
            updatedAt = stamp
        )
        val rule = if (existingRuleId == null) {
            activityRepository.insertRule(draftRule)
        } else {
            activityRepository.updateRule(draftRule)
        }
        val withRule = base.copy(recurrenceRuleId = rule.id)
        if (activityId >= 0) {
            activityRepository.update(withRule)
        } else {
            activityRepository.insert(withRule)
        }
    }

    private fun update(transform: (ActivityEditorUiState) -> ActivityEditorUiState) {
        stateInternal.update(transform)
    }

    private fun initialDate(raw: String): String {
        return parseAgendaDate(raw)?.toString() ?: LocalDate.now().toString()
    }
}
