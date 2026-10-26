package com.miagenda.app.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.TaskRepository
import com.miagenda.app.ui.navigation.TaskDetailRoute
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val missing: Boolean = false,
    val title: String = "",
    val notes: String = "",
    val dueDate: LocalDate? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val categoryName: String? = null,
    val actionError: Boolean = false
)

class TaskDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {
    private val taskId = savedStateHandle.toRoute<TaskDetailRoute>().taskId
    private val actionError = MutableStateFlow(false)
    private val closedInternal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val closed: SharedFlow<Unit> = closedInternal.asSharedFlow()

    val uiState: StateFlow<TaskDetailUiState> = combine(
        taskRepository.observeAll(),
        categoryRepository.observeAll(),
        actionError
    ) { tasks, categories, failed ->
        val task = tasks.firstOrNull { it.id == taskId }
        if (task == null) {
            TaskDetailUiState(missing = true, actionError = failed)
        } else {
            TaskDetailUiState(
                title = task.title,
                notes = task.notes,
                dueDate = task.dueDate,
                priority = task.priority,
                status = task.status,
                categoryName = categories.firstOrNull { it.id == task.categoryId }?.name,
                actionError = failed
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskDetailUiState()
    )

    fun setStatus(status: TaskStatus) {
        viewModelScope.launch {
            val task = taskRepository.get(taskId) ?: return@launch
            try {
                taskRepository.update(task.copy(status = status))
                actionError.value = false
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                actionError.value = true
            }
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            try {
                taskRepository.delete(taskId)
                closedInternal.tryEmit(Unit)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                actionError.value = true
            }
        }
    }
}
