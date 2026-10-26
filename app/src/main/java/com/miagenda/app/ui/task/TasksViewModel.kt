package com.miagenda.app.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.repository.CategoryRepository
import com.miagenda.app.domain.repository.TaskRepository
import com.miagenda.app.domain.task.TaskGroup
import com.miagenda.app.domain.task.groupedTasks
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class TaskRowUi(
    val id: Long,
    val title: String,
    val dueDate: LocalDate?,
    val priority: TaskPriority,
    val categoryName: String?,
    val colorArgb: Int?
)

data class TaskSectionUi(
    val group: TaskGroup,
    val tasks: List<TaskRowUi>
)

data class TasksUiState(
    val sections: List<TaskSectionUi> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(
    taskRepository: TaskRepository,
    categoryRepository: CategoryRepository,
    private val dateProvider: DateProvider
) : ViewModel() {
    private val today = MutableStateFlow(dateProvider.today())

    val uiState: StateFlow<TasksUiState> = today.flatMapLatest { day ->
        combine(taskRepository.observeAll(), categoryRepository.observeAll()) { tasks, categories ->
            val byId = categories.associateBy { category -> category.id }
            TasksUiState(
                sections = groupedTasks(tasks, day).map { section ->
                    TaskSectionUi(
                        group = section.group,
                        tasks = section.tasks.map { task ->
                            val category = task.categoryId?.let(byId::get)
                            TaskRowUi(
                                id = task.id,
                                title = task.title,
                                dueDate = task.dueDate,
                                priority = task.priority,
                                categoryName = category?.name,
                                colorArgb = category?.colorArgb
                            )
                        }
                    )
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TasksUiState()
    )

    fun onResume() {
        today.value = dateProvider.today()
    }
}
