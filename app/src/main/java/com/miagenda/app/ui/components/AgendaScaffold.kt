package com.miagenda.app.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.miagenda.app.ui.navigation.ActivityDetailRoute
import com.miagenda.app.ui.navigation.CategoriesRoute
import com.miagenda.app.ui.navigation.EditorRoute
import com.miagenda.app.ui.navigation.HabitDetailRoute
import com.miagenda.app.ui.navigation.ReflectionRoute
import com.miagenda.app.ui.navigation.TaskDetailRoute
import com.miagenda.app.ui.navigation.WeekSummaryRoute

@Composable
fun AgendaScaffold(
    currentDestination: NavDestination?,
    showBottomBar: Boolean,
    onNavigate: (MainDestination) -> Unit,
    onAddClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = onNavigate,
                    onAddClick = onAddClick
                )
            }
        },
        content = content
    )
}

fun NavDestination?.showsBottomBar(): Boolean {
    val destination = this ?: return true
    return !destination.hasRoute<EditorRoute>() &&
        !destination.hasRoute<ActivityDetailRoute>() &&
        !destination.hasRoute<TaskDetailRoute>() &&
        !destination.hasRoute<HabitDetailRoute>() &&
        !destination.hasRoute<ReflectionRoute>() &&
        !destination.hasRoute<WeekSummaryRoute>() &&
        !destination.hasRoute<CategoriesRoute>()
}
