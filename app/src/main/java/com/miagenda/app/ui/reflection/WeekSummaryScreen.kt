package com.miagenda.app.ui.reflection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.miagenda.app.domain.summary.WeekActivityStats
import com.miagenda.app.domain.summary.WeekHabitStats
import com.miagenda.app.domain.summary.WeekTaskStats
import com.miagenda.app.ui.components.RoundedCard

@Composable
fun WeekSummaryScreen(
    state: WeekSummaryUiState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        Text(
            text = stringResource(R.string.week_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics { heading() }
        )
        Text(
            text = state.rangeLabel,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPreviousWeek, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.calendar_previous_week))
            }
            TextButton(onClick = onNextWeek, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.calendar_next_week))
            }
        }
        SummaryCard(title = stringResource(R.string.week_activities), body = activityBody(state.activities))
        SummaryCard(title = stringResource(R.string.week_tasks), body = taskBody(state.tasks))
        SummaryCard(title = stringResource(R.string.week_habits), body = habitBody(state.habits))
        Text(
            text = stringResource(R.string.week_notes),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (state.notes.isEmpty()) {
            Text(
                text = stringResource(R.string.week_notes_empty),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        } else {
            state.notes.forEach { note ->
                RoundedCard {
                    Text(text = note.label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = note.note,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, body: String) {
    RoundedCard {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun activityBody(stats: WeekActivityStats): String {
    val percent = stats.percent?.let { value ->
        stringResource(R.string.week_activity_percent, value)
    } ?: stringResource(R.string.week_activity_empty)
    return stringResource(
        R.string.week_activity_counts,
        stats.scheduled,
        stats.completed,
        stats.partial,
        stats.open,
        stats.cancelled
    ) + "\n" + percent
}

@Composable
private fun taskBody(stats: WeekTaskStats): String {
    return stringResource(
        R.string.week_task_counts,
        stats.due,
        stats.pending,
        stats.completed,
        stats.cancelled
    )
}

@Composable
private fun habitBody(stats: WeekHabitStats): String {
    val percent = stats.percent?.let { value ->
        stringResource(R.string.habit_percent, value)
    } ?: stringResource(R.string.habit_no_schedule)
    return stringResource(
        R.string.habit_counts,
        stats.completed,
        stats.partial,
        stats.notDone,
        stats.unmarked
    ) + "\n" + percent + "\n" + stringResource(R.string.week_habit_note)
}
