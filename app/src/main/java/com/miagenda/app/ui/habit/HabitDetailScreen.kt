package com.miagenda.app.ui.habit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLogStatus

@Composable
fun HabitDetailScreen(
    state: HabitDetailUiState,
    onStatus: (HabitLogStatus) -> Unit,
    onClear: () -> Unit,
    onArchive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
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
        if (state.missing) {
            Text(
                text = stringResource(R.string.habit_missing),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            return@Column
        }
        Text(
            text = state.title,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
        Text(
            text = scheduleText(state),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (state.categoryName != null) {
            Text(
                text = state.categoryName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (state.notes.isNotBlank()) {
            Text(
                text = state.notes,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (state.archived) {
            Text(
                text = stringResource(R.string.habit_archived_note),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Text(
            text = stringResource(R.string.habit_today_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (state.scheduledToday) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitLogStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.todayStatus == status,
                        onClick = { onStatus(status) },
                        label = { Text(stringResource(status.logLabelRes())) }
                    )
                }
            }
            if (state.todayStatus != null) {
                TextButton(onClick = onClear, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.habit_clear_log))
                }
            }
        } else {
            Text(
                text = stringResource(R.string.habit_not_today),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Text(
            text = state.percent?.let { percent ->
                stringResource(R.string.habit_percent, percent)
            } ?: stringResource(R.string.habit_no_schedule),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (state.percent != null) {
            Text(
                text = stringResource(
                    R.string.habit_counts,
                    state.completed,
                    state.partial,
                    state.notDone,
                    state.unmarked
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = stringResource(R.string.habit_unmarked_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (state.streak > 0) {
            Text(
                text = stringResource(R.string.habit_streak, state.streak),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        if (state.history.isNotEmpty()) {
            Text(
                text = stringResource(R.string.habit_history),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            state.history.forEach { row ->
                Text(
                    text = "${row.label}: ${stringResource(row.status.logLabelRes())}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 8.dp)
                .toggleable(
                    value = state.archived,
                    role = Role.Switch,
                    onValueChange = onArchive
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.habit_archive))
            Switch(checked = state.archived, onCheckedChange = null)
        }
        if (state.actionError) {
            Text(
                text = stringResource(R.string.error_save),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        TextButton(onClick = onEdit, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.detail_edit_single))
        }
        TextButton(onClick = { confirmDelete = true }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.habit_delete))
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.habit_delete_title)) },
            text = { Text(stringResource(R.string.habit_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    }
                ) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun scheduleText(state: HabitDetailUiState): String {
    val dayNames = listOf(
        stringResource(R.string.weekday_mon_full),
        stringResource(R.string.weekday_tue_full),
        stringResource(R.string.weekday_wed_full),
        stringResource(R.string.weekday_thu_full),
        stringResource(R.string.weekday_fri_full),
        stringResource(R.string.weekday_sat_full),
        stringResource(R.string.weekday_sun_full)
    )
    return when (state.frequency) {
        HabitFrequency.DAILY -> stringResource(R.string.habit_daily_note)
        HabitFrequency.SPECIFIC_DAYS -> {
            val days = state.selectedDays.sorted().map { day -> dayNames[day - 1] }.joinToString(", ")
            stringResource(R.string.habit_days_note) + " " + days
        }
    }
}
