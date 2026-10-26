package com.miagenda.app.domain.task

import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import java.time.LocalDate

enum class TaskGroup {
    OVERDUE,
    TODAY,
    LATER,
    UNDATED,
    DONE,
    CANCELLED
}

data class TaskSection(
    val group: TaskGroup,
    val tasks: List<AgendaTask>
)

fun taskGroup(task: AgendaTask, today: LocalDate): TaskGroup {
    return when (task.status) {
        TaskStatus.COMPLETED -> TaskGroup.DONE
        TaskStatus.CANCELLED -> TaskGroup.CANCELLED
        TaskStatus.PENDING -> when {
            task.dueDate == null -> TaskGroup.UNDATED
            task.dueDate.isBefore(today) -> TaskGroup.OVERDUE
            task.dueDate.isEqual(today) -> TaskGroup.TODAY
            else -> TaskGroup.LATER
        }
    }
}

fun groupedTasks(tasks: List<AgendaTask>, today: LocalDate): List<TaskSection> {
    val grouped = tasks.groupBy { task -> taskGroup(task, today) }
    return TaskGroup.entries.mapNotNull { group ->
        val items = grouped[group].orEmpty()
        if (items.isEmpty()) {
            null
        } else {
            TaskSection(group = group, tasks = items.sortedWith(taskOrder(group)))
        }
    }
}

private fun taskOrder(group: TaskGroup): Comparator<AgendaTask> {
    val byPriority = compareBy<AgendaTask> { task -> priorityRank(task.priority) }
    val byDueDate = compareBy<AgendaTask> { task -> task.dueDate }
    val byId = compareBy<AgendaTask> { task -> task.id }
    val byRecentUpdate = compareByDescending<AgendaTask> { task -> task.updatedAt }
    return when (group) {
        TaskGroup.OVERDUE,
        TaskGroup.LATER -> byDueDate.then(byPriority).then(byId)
        TaskGroup.TODAY,
        TaskGroup.UNDATED -> byPriority.then(byId)
        TaskGroup.DONE,
        TaskGroup.CANCELLED -> byRecentUpdate.then(byId)
    }
}

private fun priorityRank(priority: TaskPriority): Int {
    return when (priority) {
        TaskPriority.HIGH -> 0
        TaskPriority.MEDIUM -> 1
        TaskPriority.LOW -> 2
    }
}
