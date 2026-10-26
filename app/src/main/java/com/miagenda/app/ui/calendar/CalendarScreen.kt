package com.miagenda.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miagenda.app.R
import com.miagenda.app.ui.activity.AgendaRow
import com.miagenda.app.ui.activity.weekdayFullRes
import com.miagenda.app.ui.activity.weekdayShortRes
import com.miagenda.app.ui.components.EmptyState

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onOpenActivity: (Long, String) -> Unit,
    onAddActivity: (String) -> Unit,
    onOpenTimetable: (String) -> Unit,
    onOpenCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.calendar_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.semantics { heading() }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = viewModel::showPreviousWeek, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.cd_previous_week)
                )
            }
            Text(
                text = state.selectedLabel,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = viewModel::showNextWeek, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.cd_next_week)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            state.days.forEach { day ->
                val index = day.date.dayOfWeek.value
                val description = stringResource(weekdayFullRes(index)) + " " + day.date.dayOfMonth
                Column(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.select(day.date) }
                        .background(
                            if (day.isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                            }
                        )
                        .clearAndSetSemantics {
                            role = Role.Button
                            selected = day.isSelected
                            contentDescription = description
                        }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dayText = if (day.isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                    val weekdayText = if (day.isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = stringResource(weekdayShortRes(index)),
                        style = MaterialTheme.typography.labelMedium,
                        color = weekdayText
                    )
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = dayText
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !day.hasItems -> MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                                    day.isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                    )
                }
            }
        }
        TextButton(
            onClick = { onAddActivity(state.selectedDate.toString()) },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.calendar_add_day))
        }
        TextButton(
            onClick = { onOpenTimetable(state.selectedDate.toString()) },
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.timetable_open))
        }
        TextButton(onClick = onOpenCategories, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.calendar_categories))
        }
        if (state.items.isEmpty()) {
            EmptyState(
                title = state.selectedLabel,
                message = stringResource(R.string.calendar_body),
                icon = Icons.Rounded.CalendarMonth
            )
        } else {
            state.items.forEach { item ->
                AgendaRow(
                    item = item,
                    onClick = { onOpenActivity(item.activityId, item.date.toString()) }
                )
            }
        }
    }
}
