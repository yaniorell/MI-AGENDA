package com.miagenda.app.ui.task

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.domain.model.TaskPriority

@Composable
fun TaskEditorScreen(
    state: TaskEditorUiState,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDueChange: (String) -> Unit,
    onPriorityChange: (TaskPriority) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back)
            )
        }
        Text(
            text = stringResource(if (state.editing) R.string.editor_edit_task else R.string.editor_task_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
        if (state.missing) {
            Text(
                text = stringResource(R.string.task_missing),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            return@Column
        }
        if (state.editing) {
            Text(
                text = stringResource(R.string.task_reprogram_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.field_title)) },
            supportingText = if (state.titleError) {
                { Text(stringResource(R.string.error_title)) }
            } else {
                null
            },
            isError = state.titleError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        OutlinedTextField(
            value = state.dueText,
            onValueChange = onDueChange,
            label = { Text(stringResource(R.string.field_due)) },
            supportingText = {
                Text(stringResource(if (state.dateError) R.string.error_date else R.string.field_due_support))
            },
            isError = state.dateError,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.field_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        Text(
            text = stringResource(R.string.field_priority),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskPriority.entries.forEach { priority ->
                FilterChip(
                    selected = state.priority == priority,
                    onClick = { onPriorityChange(priority) },
                    label = { Text(stringResource(priority.labelRes())) }
                )
            }
        }
        Text(
            text = stringResource(R.string.field_category),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.categoryId == null,
                onClick = { onCategoryChange(null) },
                label = { Text(stringResource(R.string.field_no_category)) }
            )
            state.categories.forEach { category ->
                FilterChip(
                    selected = state.categoryId == category.id,
                    onClick = { onCategoryChange(category.id) },
                    label = { Text(category.name) }
                )
            }
        }
        if (state.saveError) {
            Text(
                text = stringResource(R.string.error_save),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Button(
            onClick = onSave,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.editor_save_task))
        }
    }
}

fun TaskPriority.labelRes(): Int {
    return when (this) {
        TaskPriority.LOW -> R.string.priority_low
        TaskPriority.MEDIUM -> R.string.priority_medium
        TaskPriority.HIGH -> R.string.priority_high
    }
}
