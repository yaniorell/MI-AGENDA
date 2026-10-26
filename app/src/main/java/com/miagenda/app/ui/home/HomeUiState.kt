package com.miagenda.app.ui.home

import com.miagenda.app.domain.home.agendaDateLabel
import com.miagenda.app.domain.home.greetingText
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.UserPreferences
import com.miagenda.app.ui.activity.AgendaItem
import java.time.LocalDate

data class HomeHabitItem(
    val habitId: Long,
    val title: String,
    val status: HabitLogStatus?,
    val colorArgb: Int? = null
)

data class HomeUiState(
    val greeting: String,
    val dateLabel: String,
    val visibleSections: Set<HomeSection>,
    val preferencesRecovered: Boolean,
    val nextItem: AgendaItem? = null,
    val progressPercent: Int? = null,
    val agenda: List<AgendaItem> = emptyList(),
    val habits: List<HomeHabitItem> = emptyList(),
    val reflectionNote: String = ""
)

fun UserPreferences.toHomeUiState(today: LocalDate): HomeUiState {
    return HomeUiState(
        greeting = greetingText(displayName),
        dateLabel = agendaDateLabel(today),
        visibleSections = visibleSections,
        preferencesRecovered = recoveredFromError
    )
}
