package com.miagenda.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.domain.usecase.ObserveUserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    observeUserPreferences: ObserveUserPreferences
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = observeUserPreferences()
        .map { preferences -> preferences.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.DARK
        )

    val accentColor: StateFlow<AccentColor> = observeUserPreferences()
        .map { preferences -> preferences.accentColor }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccentColor.CORAL
        )
}
