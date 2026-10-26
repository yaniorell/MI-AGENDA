package com.miagenda.app.ui.settings

import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode

enum class NameFieldError {
    None,
    TooLong
}

data class SettingsUiState(
    val savedName: String = "",
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColor: AccentColor = AccentColor.CORAL,
    val visibleSections: Set<HomeSection> = HomeSection.entries.toSet(),
    val showMascot: Boolean = true,
    val nameError: NameFieldError = NameFieldError.None,
    val saveFailed: Boolean = false,
    val showSaved: Boolean = false,
    val preferencesRecovered: Boolean = false
)

data class SettingsFeedback(
    val nameError: NameFieldError = NameFieldError.None,
    val saveFailed: Boolean = false,
    val showSaved: Boolean = false
)
