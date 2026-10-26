package com.miagenda.app.domain.repository

import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setDisplayName(name: String)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setAccentColor(color: AccentColor)

    suspend fun setVisibleSections(sections: Set<HomeSection>)

    suspend fun restore(preferences: UserPreferences)
}
