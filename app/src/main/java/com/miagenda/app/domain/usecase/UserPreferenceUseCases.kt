package com.miagenda.app.domain.usecase

import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.domain.model.UserPreferences
import com.miagenda.app.domain.preferences.NameValidation
import com.miagenda.app.domain.preferences.validateDisplayName
import com.miagenda.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObserveUserPreferences(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> = repository.preferences
}

class UpdateDisplayName(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(raw: String): NameValidation {
        return when (val result = validateDisplayName(raw)) {
            is NameValidation.Ok -> {
                repository.setDisplayName(result.value)
                result
            }
            NameValidation.TooLong -> result
        }
    }
}

class UpdateThemeMode(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(mode: ThemeMode) {
        repository.setThemeMode(mode)
    }
}

class UpdateAccentColor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(color: AccentColor) {
        repository.setAccentColor(color)
    }
}

class UpdateHomeSections(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(sections: Set<HomeSection>) {
        repository.setVisibleSections(sections)
    }
}
