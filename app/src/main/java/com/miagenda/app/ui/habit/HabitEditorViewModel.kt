package com.miagenda.app.ui.habit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.HabitRepository
import com.miagenda.app.domain.time.parseDaysOfWeek
import com.miagenda.app.ui.navigation.EditorRoute
import java.time.LocalDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HabitRepeat {
    DAILY,
    SPECIFIC_DAYS
}

data class HabitEditorUiState(
    val title: String = "",
    val notes: String = "",
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val repeat: HabitRepeat = HabitRepeat.DAILY,
    val selectedDays: Set<Int> = emptySet(),
    val editing: Boolean = false,
    val titleError: Boolean = false,
    val daysError: Boolean = false,
    val saveError: Boolean = false,
    val missing: Boolean = false
)

class HabitEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val habitRepository: HabitRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val habitId = savedStateHandle.toRoute<EditorRoute>().habitId
    private val stateInternal = MutableStateFlow(HabitEditorUiState(editing = habitId >= 0))
    val uiState: StateFlow<HabitEditorUiState> = stateInternal.asStateFlow()
    private val savedInternal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val saved: SharedFlow<Unit> = savedInternal.asSharedFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                stateInternal.update { state ->
                    state.copy(
                        categories = categories.filter { category ->
                            !category.isArchived || category.id == state.categoryId
                        }
                    )
                }
            }
        }
        if (habitId >= 0) {
            viewModelScope.launch { loadExisting() }
        }
    }

    fun onTitleChange(value: String) = update { it.copy(title = value, titleError = false, saveError = false) }

    fun onNotesChange(value: String) = update { it.copy(notes = value) }

    fun onCategoryChange(id: Long?) = update { it.copy(categoryId = id) }

    fun onRepeatChange(repeat: HabitRepeat) = update { it.copy(repeat = repeat, daysError = false) }

    fun onDayToggle(day: Int) = update { current ->
        val days = if (day in current.selectedDays) current.selectedDays - day else current.selectedDays + day
        current.copy(selectedDays = days, daysError = false)
    }

    fun save() {
        val current = stateInternal.value
        val titleBlank = current.title.isBlank()
        val daysInvalid = current.repeat == HabitRepeat.SPECIFIC_DAYS && current.selectedDays.isEmpty()
        if (titleBlank || daysInvalid || current.missing) {
            update { it.copy(titleError = titleBlank, daysError = daysInvalid) }
            return
        }
        viewModelScope.launch {
            try {
                val stamp = LocalDateTime.now()
                val existing = if (habitId >= 0) habitRepository.get(habitId) else null
                if (habitId >= 0 && existing == null) {
                    update { it.copy(missing = true) }
                    return@launch
                }
                val habit = Habit(
                    id = existing?.id ?: 0,
                    title = current.title,
                    notes = current.notes,
                    categoryId = current.categoryId,
                    frequency = when (current.repeat) {
                        HabitRepeat.DAILY -> HabitFrequency.DAILY
                        HabitRepeat.SPECIFIC_DAYS -> HabitFrequency.SPECIFIC_DAYS
                    },
                    daysOfWeek = if (current.repeat == HabitRepeat.SPECIFIC_DAYS) {
                        current.selectedDays.sorted().joinToString(",")
                    } else {
                        null
                    },
                    isArchived = existing?.isArchived ?: false,
                    createdAt = existing?.createdAt ?: stamp,
                    updatedAt = stamp
                )
                if (existing == null) {
                    habitRepository.insert(habit)
                } else {
                    habitRepository.update(habit)
                }
                savedInternal.tryEmit(Unit)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                update { it.copy(saveError = true) }
            }
        }
    }

    private suspend fun loadExisting() {
        val habit = habitRepository.get(habitId)
        if (habit == null) {
            update { it.copy(missing = true) }
            return
        }
        update {
            it.copy(
                title = habit.title,
                notes = habit.notes,
                categoryId = habit.categoryId,
                repeat = when (habit.frequency) {
                    HabitFrequency.DAILY -> HabitRepeat.DAILY
                    HabitFrequency.SPECIFIC_DAYS -> HabitRepeat.SPECIFIC_DAYS
                },
                selectedDays = parseDaysOfWeek(habit.daysOfWeek),
                editing = true,
                missing = false
            )
        }
    }

    private fun update(transform: (HabitEditorUiState) -> HabitEditorUiState) {
        stateInternal.update(transform)
    }
}
