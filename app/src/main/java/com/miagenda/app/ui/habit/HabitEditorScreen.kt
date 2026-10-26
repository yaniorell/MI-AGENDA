package com.miagenda.app.ui.habit

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
import com.miagenda.app.ui.activity.weekdayFullRes
import com.miagenda.app.ui.activity.weekdayShortRes

@Composable
fun HabitEditorScreen(
    state: HabitEditorUiState,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onRepeatChange: (HabitRepeat) -> Unit,
    onDayToggle: (Int) -> Unit,
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
            text = stringResource(if (state.editing) R.string.editor_edit_habit else R.string.editor_habit_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
        if (state.missing) {
            Text(
                text = stringResource(R.string.habit_missing),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            return@Column
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
            value = state.notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.field_notes)) },
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
            text = stringResource(R.string.field_frequency),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HabitRepeat.entries.forEach { repeat ->
            FilterChip(
                selected = state.repeat == repeat,
                onClick = { onRepeatChange(repeat) },
                label = { Text(stringResource(repeat.labelRes())) },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Text(
            text = stringResource(
                if (state.repeat == HabitRepeat.DAILY) R.string.habit_daily_note else R.string.habit_days_note
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (state.repeat == HabitRepeat.SPECIFIC_DAYS) {
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
            Text(stringResource(R.string.editor_save_habit))
        }
    }
}

private fun HabitRepeat.labelRes(): Int {
    return when (this) {
        HabitRepeat.DAILY -> R.string.repeat_daily
        HabitRepeat.SPECIFIC_DAYS -> R.string.repeat_days
    }
}
