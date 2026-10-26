package com.miagenda.app.ui.activity

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.miagenda.app.R

@Composable
fun ActivityEditorScreen(
    state: ActivityEditorUiState,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onRepeatChange: (RepeatChoice) -> Unit,
    onDayToggle: (Int) -> Unit,
    onIntervalChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
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
            text = stringResource(
                when {
                    state.isClass -> R.string.editor_class_title
                    state.isSeries -> R.string.editor_edit_activity
                    else -> R.string.editor_activity_title
                }
            ),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
        if (state.missing) {
            Text(
                text = stringResource(R.string.activity_missing),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            return@Column
        }
        if (state.isClass) {
            Text(
                text = stringResource(R.string.editor_class_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (state.isSeries) {
            Text(
                text = stringResource(R.string.editor_series_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = {
                Text(stringResource(if (state.isClass) R.string.field_subject else R.string.field_title))
            },
            supportingText = if (state.titleError) {
                {
                    Text(
                        stringResource(
                            if (state.isClass) R.string.error_subject else R.string.error_title
                        )
                    )
                }
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
            value = state.dateText,
            onValueChange = onDateChange,
            label = { Text(stringResource(R.string.field_date)) },
            supportingText = {
                Text(
                    stringResource(if (state.dateError) R.string.error_date else R.string.field_date_support)
                )
            },
            isError = state.dateError,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        OutlinedTextField(
            value = state.startText,
            onValueChange = onStartChange,
            label = { Text(stringResource(R.string.field_start)) },
            supportingText = {
                Text(stringResource(if (state.timeError) R.string.error_time else R.string.field_time_support))
            },
            isError = state.timeError,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        OutlinedTextField(
            value = state.endText,
            onValueChange = onEndChange,
            label = { Text(stringResource(R.string.field_end)) },
            isError = state.timeError,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChange,
            label = {
                Text(stringResource(if (state.isClass) R.string.field_classroom else R.string.field_notes))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        Text(
            text = stringResource(R.string.field_category),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
        Text(
            text = stringResource(R.string.field_repeat),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        RepeatChoice.entries.forEach { choice ->
            FilterChip(
                selected = state.repeat == choice,
                onClick = { onRepeatChange(choice) },
                label = { Text(stringResource(choice.labelRes())) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (state.repeat == RepeatChoice.SPECIFIC_DAYS || state.repeat == RepeatChoice.EVERY_N_WEEKS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..7).forEach { day ->
                    val fullName = stringResource(weekdayFullRes(day))
                    FilterChip(
                        selected = day in state.selectedDays,
                        onClick = { onDayToggle(day) },
                        label = { Text(stringResource(weekdayShortRes(day))) },
                        modifier = Modifier.semantics { contentDescription = fullName }
                    )
                }
            }
            if (state.daysError) {
                Text(
                    text = stringResource(R.string.error_days),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        if (state.repeat == RepeatChoice.EVERY_N_WEEKS) {
            OutlinedTextField(
                value = state.intervalText,
                onValueChange = onIntervalChange,
                label = { Text(stringResource(R.string.field_interval)) },
                supportingText = if (state.intervalError) {
                    { Text(stringResource(R.string.error_interval)) }
                } else {
                    null
                },
                isError = state.intervalError,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
        if (state.repeat != RepeatChoice.NONE) {
            OutlinedTextField(
                value = state.endDateText,
                onValueChange = onEndDateChange,
                label = { Text(stringResource(R.string.field_repeat_end)) },
                supportingText = {
                    Text(
                        stringResource(
                            if (state.endDateError) R.string.error_end_date else R.string.field_repeat_end_support
                        )
                    )
                },
                isError = state.endDateError,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
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
            Text(stringResource(if (state.isClass) R.string.editor_save_class else R.string.editor_save))
        }
    }
}

private fun RepeatChoice.labelRes(): Int {
    return when (this) {
        RepeatChoice.NONE -> R.string.repeat_none
        RepeatChoice.DAILY -> R.string.repeat_daily
        RepeatChoice.SPECIFIC_DAYS -> R.string.repeat_days
        RepeatChoice.EVERY_N_WEEKS -> R.string.repeat_weeks
    }
}
