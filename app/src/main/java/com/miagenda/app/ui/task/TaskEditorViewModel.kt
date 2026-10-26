package com.miagenda.app.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.activity.parseAgendaDate
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.TaskRepository
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

data class TaskEditorUiState(
    val title: String = "",
    val notes: String = "",
    val dueText: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val categoryId: Long? = null,
    val categories: List<Category> = emptyList(),
    val editing: Boolean = false,
    val titleError: Boolean = false,
    val dateError: Boolean = false,
    val saveError: Boolean = false,
    val missing: Boolean = false
)

class TaskEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val taskId = savedStateHandle.toRoute<EditorRoute>().taskId
    private val stateInternal = MutableStateFlow(TaskEditorUiState(editing = taskId >= 0))
    val uiState: StateFlow<TaskEditorUiState> = stateInternal.asStateFlow()
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
        if (taskId >= 0) {
            viewModelScope.launch { loadExisting() }
        }
    }

    fun onTitleChange(value: String) = update { it.copy(title = value, titleError = false, saveError = false) }

    fun onNotesChange(value: String) = update { it.copy(notes = value) }

    fun onDueChange(value: String) = update { it.copy(dueText = value, dateError = false, saveError = false) }

    fun onPriorityChange(priority: TaskPriority) = update { it.copy(priority = priority) }

    fun onCategoryChange(id: Long?) = update { it.copy(categoryId = id) }

    fun save() {
        val current = stateInternal.value
        val dueBlank = current.dueText.isBlank()
        val dueDate = if (dueBlank) null else parseAgendaDate(current.dueText)
        val titleBlank = current.title.isBlank()
        val dateInvalid = !dueBlank && dueDate == null
        if (titleBlank || dateInvalid || current.missing) {
            update { it.copy(titleError = titleBlank, dateError = dateInvalid) }
            return
        }
        viewModelScope.launch {
            try {
                val stamp = LocalDateTime.now()
                val existing = if (taskId >= 0) taskRepository.get(taskId) else null
                if (taskId >= 0 && existing == null) {
                    update { it.copy(missing = true) }
                    return@launch
                }
                val task = AgendaTask(
                    id = existing?.id ?: 0,
                    title = current.title,
                    notes = current.notes,
                    dueDate = dueDate,
                    priority = current.priority,
                    status = existing?.status ?: TaskStatus.PENDING,
                    categoryId = current.categoryId,
                    createdAt = existing?.createdAt ?: stamp,
                    updatedAt = stamp
                )
                if (existing == null) {
                    taskRepository.insert(task)
                } else {
                    taskRepository.update(task)
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
        val task = taskRepository.get(taskId)
        if (task == null) {
            update { it.copy(missing = true) }
            return
        }
        update {
            it.copy(
                title = task.title,
                notes = task.notes,
                dueText = task.dueDate?.toString().orEmpty(),
                priority = task.priority,
                categoryId = task.categoryId,
                editing = true,
                missing = false
            )
        }
    }

    private fun update(transform: (TaskEditorUiState) -> TaskEditorUiState) {
        stateInternal.update(transform)
    }
}
