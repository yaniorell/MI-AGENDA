package com.miagenda.app.ui.habit

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
import androidx.compose.material.icons.rounded.Spa
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
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.ui.components.CategoryDot
import com.miagenda.app.ui.components.EmptyState
import com.miagenda.app.ui.components.RoundedCard

@Composable
fun HabitsScreen(
    state: HabitsUiState,
    onOpenHabit: (Long) -> Unit,
    onAddHabit: () -> Unit,
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
            text = stringResource(R.string.habits_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.semantics { heading() }
        )
        TextButton(onClick = onAddHabit, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.habits_add))
        }
        if (state.active.isEmpty() && state.archived.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.habits_title),
                message = stringResource(R.string.habits_body),
                icon = Icons.Rounded.Spa
            )
        } else {
            state.active.forEach { habit ->
                HabitRow(habit = habit, onClick = { onOpenHabit(habit.id) })
            }
            if (state.archived.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.habits_archived),
                    style = MaterialTheme.typography.titleMedium
                )
                state.archived.forEach { habit ->
                    HabitRow(habit = habit, onClick = { onOpenHabit(habit.id) })
                }
            }
        }
    }
}

@Composable
private fun HabitRow(habit: HabitRowUi, onClick: () -> Unit) {
    val percent = habit.percent?.let { value ->
        stringResource(R.string.habit_percent, value)
    } ?: stringResource(R.string.habit_no_schedule)
    val today = when {
        habit.archived -> stringResource(R.string.habit_archived_note)
        habit.scheduledToday -> stringResource(habit.todayStatus.logLabelRes())
        else -> stringResource(R.string.habit_not_today)
    }
    val detail = listOfNotNull(today, percent, habit.categoryName).joinToString(" · ")
    RoundedCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryDot(habit.colorArgb)
            Column {
                Text(text = habit.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun HabitLogStatus?.logLabelRes(): Int {
    return when (this) {
        HabitLogStatus.COMPLETED -> R.string.log_completed
        HabitLogStatus.PARTIAL -> R.string.log_partial
        HabitLogStatus.NOT_DONE -> R.string.log_not_done
        null -> R.string.log_unmarked
    }
}
