package com.miagenda.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.miagenda.app.R
import com.miagenda.app.ui.navigation.CalendarRoute
import com.miagenda.app.ui.navigation.HabitsRoute
import com.miagenda.app.ui.navigation.HomeRoute
import com.miagenda.app.ui.navigation.SettingsRoute
import com.miagenda.app.ui.navigation.TasksRoute
import com.miagenda.app.ui.navigation.TimetableRoute
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars

enum class MainDestination(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: Any
) {
    HOME(R.string.nav_home, Icons.Rounded.Home, HomeRoute),
    CALENDAR(R.string.nav_calendar, Icons.Rounded.CalendarMonth, CalendarRoute),
    TIMETABLE(R.string.nav_timetable, Icons.Rounded.School, TimetableRoute()),
    TASKS(R.string.nav_tasks, Icons.Rounded.TaskAlt, TasksRoute),
    HABITS(R.string.nav_habits, Icons.Rounded.Spa, HabitsRoute),
    SETTINGS(R.string.nav_settings, Icons.Rounded.Settings, SettingsRoute)
}

fun NavDestination?.isMainDestination(destination: MainDestination): Boolean {
    if (this == null) {
        return false
    }
    return when (destination) {
        MainDestination.HOME -> hasRoute<HomeRoute>()
        MainDestination.CALENDAR -> hasRoute<CalendarRoute>()
        MainDestination.TIMETABLE -> hasRoute<TimetableRoute>()
        MainDestination.TASKS -> hasRoute<TasksRoute>()
        MainDestination.HABITS -> hasRoute<HabitsRoute>()
        MainDestination.SETTINGS -> hasRoute<SettingsRoute>()
    }
}

@Composable
fun BottomNavigationBar(
    currentDestination: NavDestination?,
    onNavigate: (MainDestination) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DestinationGroup(
                        destinations = listOf(
                            MainDestination.HOME,
                            MainDestination.CALENDAR,
                            MainDestination.TIMETABLE
                        ),
                        currentDestination = currentDestination,
                        onNavigate = onNavigate
                    )
                    Spacer(modifier = Modifier.width(72.dp))
                    DestinationGroup(
                        destinations = listOf(
                            MainDestination.TASKS,
                            MainDestination.HABITS,
                            MainDestination.SETTINGS
                        ),
                        currentDestination = currentDestination,
                        onNavigate = onNavigate
                    )
                }
            }
            CircularActionButton(
                onClick = onAddClick,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun RowScope.DestinationGroup(
    destinations: List<MainDestination>,
    currentDestination: NavDestination?,
    onNavigate: (MainDestination) -> Unit
) {
    Row(modifier = Modifier.weight(1f).fillMaxHeight()) {
        destinations.forEach { destination ->
            BarDestination(
                destination = destination,
                selected = currentDestination.isMainDestination(destination),
                onClick = { onNavigate(destination) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BarDestination(
    destination: MainDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val iconTint = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                role = Role.Tab
                this.selected = selected
            }
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                tint = iconTint
            )
        }
        Text(
            text = stringResource(destination.labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
