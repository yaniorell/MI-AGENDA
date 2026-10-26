package com.miagenda.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.domain.preferences.NameValidation
import com.miagenda.app.domain.usecase.ObserveUserPreferences
import com.miagenda.app.domain.usecase.UpdateAccentColor
import com.miagenda.app.domain.usecase.UpdateDisplayName
import com.miagenda.app.domain.usecase.UpdateHomeSections
import com.miagenda.app.domain.usecase.UpdateShowMascot
import com.miagenda.app.domain.usecase.UpdateThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class SettingsViewModel(
    observeUserPreferences: ObserveUserPreferences,
    private val updateDisplayName: UpdateDisplayName,
    private val updateThemeMode: UpdateThemeMode,
    private val updateAccentColor: UpdateAccentColor,
    private val updateHomeSections: UpdateHomeSections,
    private val updateShowMascot: UpdateShowMascot
) : ViewModel() {

    private val feedback = MutableStateFlow(SettingsFeedback())
    private var latestSections: Set<HomeSection> = HomeSection.entries.toSet()

    val uiState: StateFlow<SettingsUiState> = combine(
        observeUserPreferences(),
        feedback
    ) { preferences, currentFeedback ->
        latestSections = preferences.visibleSections
        SettingsUiState(
            savedName = preferences.displayName,
            themeMode = preferences.themeMode,
            accentColor = preferences.accentColor,
            visibleSections = preferences.visibleSections,
            showMascot = preferences.showMascot,
            nameError = currentFeedback.nameError,
            saveFailed = currentFeedback.saveFailed,
            showSaved = currentFeedback.showSaved,
            preferencesRecovered = preferences.recoveredFromError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onNameEdited() {
        feedback.update { current ->
            current.copy(showSaved = false, saveFailed = false, nameError = NameFieldError.None)
        }
    }

    fun saveDisplayName(raw: String) {
        viewModelScope.launch {
            try {
                when (updateDisplayName(raw)) {
                    is NameValidation.Ok -> feedback.update {
                        SettingsFeedback(showSaved = true)
                    }
                    NameValidation.TooLong -> feedback.update {
                        SettingsFeedback(nameError = NameFieldError.TooLong)
                    }
                }
            } catch (_: IOException) {
                feedback.update { SettingsFeedback(saveFailed = true) }
            }
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                updateThemeMode(mode)
            } catch (_: IOException) {
                feedback.update { current -> current.copy(saveFailed = true, showSaved = false) }
            }
        }
    }

    fun setAccent(color: AccentColor) {
        viewModelScope.launch {
            try {
                updateAccentColor(color)
            } catch (_: IOException) {
                feedback.update { current -> current.copy(saveFailed = true, showSaved = false) }
            }
        }
    }

    fun setShowMascot(show: Boolean) {
        viewModelScope.launch {
            try {
                updateShowMascot(show)
            } catch (_: IOException) {
                feedback.update { current -> current.copy(saveFailed = true, showSaved = false) }
            }
        }
    }

    fun setSectionVisible(section: HomeSection, visible: Boolean) {
        val updated = latestSections.toMutableSet().apply {
            if (visible) add(section) else remove(section)
        }
        viewModelScope.launch {
            try {
                updateHomeSections(updated)
            } catch (_: IOException) {
                feedback.update { current -> current.copy(saveFailed = true, showSaved = false) }
            }
        }
    }
}
