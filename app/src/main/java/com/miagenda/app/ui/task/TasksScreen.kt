package com.miagenda.app.ui.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.domain.home.agendaDateLabel
import com.miagenda.app.domain.task.TaskGroup
import com.miagenda.app.ui.components.CategoryDot
import com.miagenda.app.ui.components.EmptyState
import com.miagenda.app.ui.components.RoundedCard

@Composable
fun TasksScreen(
    state: TasksUiState,
    onOpenTask: (Long) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.tasks_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.semantics { heading() }
        )
        TextButton(onClick = onAddTask, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.tasks_add))
        }
        if (state.sections.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.tasks_title),
                message = stringResource(R.string.tasks_body),
                icon = Icons.Rounded.TaskAlt
            )
        } else {
            state.sections.forEach { section ->
                Text(
                    text = stringResource(section.group.labelRes()),
                    style = MaterialTheme.typography.titleMedium
                )
                section.tasks.forEach { task ->
                    TaskRow(task = task, onClick = { onOpenTask(task.id) })
                }
            }
        }
    }
}

@Composable
private fun TaskRow(task: TaskRowUi, onClick: () -> Unit) {
    val due = task.dueDate?.let(::agendaDateLabel) ?: stringResource(R.string.task_no_date)
    val priority = stringResource(task.priority.labelRes())
    val detail = listOfNotNull(due, priority, task.categoryName).joinToString(" · ")
    RoundedCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryDot(task.colorArgb)
            Column {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun TaskGroup.labelRes(): Int {
    return when (this) {
        TaskGroup.OVERDUE -> R.string.tasks_overdue
        TaskGroup.TODAY -> R.string.tasks_today
        TaskGroup.LATER -> R.string.tasks_later
        TaskGroup.UNDATED -> R.string.tasks_undated
        TaskGroup.DONE -> R.string.tasks_done
        TaskGroup.CANCELLED -> R.string.tasks_cancelled
    }
}
