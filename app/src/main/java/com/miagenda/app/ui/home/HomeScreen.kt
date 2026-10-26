package com.miagenda.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.miagenda.app.R
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.ui.activity.AgendaItem
import com.miagenda.app.ui.activity.AgendaRow
import com.miagenda.app.ui.components.CategoryDot
import com.miagenda.app.ui.components.EmptyState
import com.miagenda.app.ui.habit.logLabelRes
import com.miagenda.app.ui.components.RoundedCard
import com.miagenda.app.ui.theme.LocalMiAgendaColors
import com.miagenda.app.ui.theme.MiAgendaTheme
import com.miagenda.app.ui.theme.Violet

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenCalendar: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenActivity: (AgendaItem) -> Unit,
    onOpenHabit: (Long) -> Unit,
    onOpenReflection: () -> Unit,
    onOpenWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = state.visibleSections
    val extra = LocalMiAgendaColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = state.dateLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = state.greeting,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.semantics { heading() }
        )
        if (state.preferencesRecovered) {
            Text(
                text = stringResource(R.string.home_preferences_recovered),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (HomeSection.NEXT_ACTIVITY in sections) {
            val next = state.nextItem
            if (next == null) {
                EmptyState(
                    title = stringResource(R.string.home_next_title),
                    message = stringResource(R.string.home_next_empty),
                    icon = Icons.Rounded.Schedule,
                    containerColor = extra.hero,
                    contentColor = extra.heroContent,
                    messageColor = extra.heroMuted,
                    iconTint = Violet
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.home_next_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    AgendaRow(item = next, onClick = { onOpenActivity(next) })
                }
            }
        }
        if (HomeSection.DAY_PROGRESS in sections) {
            RoundedCard {
                Text(
                    text = stringResource(R.string.home_progress_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = state.progressPercent?.let { percent ->
                            stringResource(R.string.home_progress_value, percent)
                        } ?: stringResource(R.string.home_progress_empty),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        if (HomeSection.TODAY_AGENDA in sections) {
            if (state.agenda.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.home_agenda_title),
                    message = stringResource(R.string.home_agenda_empty),
                    icon = Icons.Rounded.CalendarMonth
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.home_agenda_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    state.agenda.forEach { item ->
                        AgendaRow(item = item, onClick = { onOpenActivity(item) })
                    }
                }
            }
        }
        if (HomeSection.TODAY_HABITS in sections) {
            if (state.habits.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.home_habits_title),
                    message = stringResource(R.string.home_habits_empty),
                    icon = Icons.Rounded.Spa
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.home_habits_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    state.habits.forEach { habit ->
                        RoundedCard(modifier = Modifier.clickable { onOpenHabit(habit.habitId) }) {
                            Row(
                                modifier = Modifier.heightIn(min = 48.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryDot(habit.colorArgb)
                                Column {
                                    Text(text = habit.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = stringResource(habit.status.logLabelRes()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (HomeSection.REFLECTION in sections) {
            RoundedCard(modifier = Modifier.clickable(onClick = onOpenReflection)) {
                Column(modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(
                        text = stringResource(R.string.home_reflection_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = state.reflectionNote.ifBlank {
                            stringResource(R.string.home_reflection_empty)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            TextButton(onClick = onOpenWeek, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.week_open))
            }
        }
        if (HomeSection.QUICK_LINKS in sections) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.home_links_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAccess(
                        label = stringResource(R.string.nav_calendar),
                        icon = Icons.Rounded.CalendarMonth,
                        description = stringResource(R.string.cd_open_calendar),
                        onClick = onOpenCalendar,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccess(
                        label = stringResource(R.string.nav_tasks),
                        icon = Icons.Rounded.TaskAlt,
                        description = stringResource(R.string.cd_open_tasks),
                        onClick = onOpenTasks,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccess(
                        label = stringResource(R.string.nav_habits),
                        icon = Icons.Rounded.Spa,
                        description = stringResource(R.string.cd_open_habits),
                        onClick = onOpenHabits,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        if (sections.isEmpty()) {
            Text(
                text = stringResource(R.string.home_all_hidden),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun QuickAccess(
    label: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .clearAndSetSemantics { contentDescription = description }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF11152F)
@Composable
private fun HomeScreenPreview() {
    MiAgendaTheme(themeMode = ThemeMode.DARK) {
        HomeScreen(
            state = HomeUiState(
                greeting = "Hola",
                dateLabel = "domingo 25 de octubre",
                visibleSections = HomeSection.entries.toSet(),
                preferencesRecovered = false
            ),
            onOpenCalendar = {},
            onOpenTasks = {},
            onOpenHabits = {},
            onOpenActivity = { _ -> },
            onOpenHabit = { _ -> },
            onOpenReflection = {},
            onOpenWeek = {}
        )
    }
}
