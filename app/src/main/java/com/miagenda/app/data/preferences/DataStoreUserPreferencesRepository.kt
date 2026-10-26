package com.miagenda.app.data.preferences

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.domain.model.UserPreferences
import com.miagenda.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

private val preferencesRecovered = AtomicBoolean(false)

private val Context.userPreferencesDataStore by preferencesDataStore(
    name = "mi_agenda_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler {
        preferencesRecovered.set(true)
        emptyPreferences()
    }
)

class DataStoreUserPreferencesRepository(
    context: Context
) : UserPreferencesRepository {

    private val dataStore = context.applicationContext.userPreferencesDataStore

    override val preferences: Flow<UserPreferences> = dataStore.data
        .map { stored -> stored.toModel(recovered = preferencesRecovered.get()) }
        .catch { exception ->
            if (exception is IOException) {
                emit(UserPreferences(recoveredFromError = true))
            } else {
                throw exception
            }
        }

    override suspend fun setDisplayName(name: String) {
        dataStore.edit { preferences ->
            preferences[Keys.displayName] = name
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.themeMode] = mode.name
        }
    }

    override suspend fun setAccentColor(color: AccentColor) {
        dataStore.edit { preferences ->
            preferences[Keys.accentColor] = color.name
        }
    }

    override suspend fun setVisibleSections(sections: Set<HomeSection>) {
        dataStore.edit { preferences ->
            preferences[Keys.homeSections] = HomeSection.toStorage(sections)
        }
    }

    override suspend fun restore(preferences: UserPreferences) {
        dataStore.edit { stored ->
            stored[Keys.displayName] = preferences.displayName
            stored[Keys.themeMode] = preferences.themeMode.name
            stored[Keys.accentColor] = preferences.accentColor.name
            stored[Keys.homeSections] = HomeSection.toStorage(preferences.visibleSections)
        }
    }

    private fun Preferences.toModel(recovered: Boolean): UserPreferences {
        return UserPreferences(
            displayName = this[Keys.displayName].orEmpty(),
            themeMode = ThemeMode.fromStorage(this[Keys.themeMode]),
            accentColor = AccentColor.fromStorage(this[Keys.accentColor]),
            visibleSections = HomeSection.fromStorage(this[Keys.homeSections]),
            recoveredFromError = recovered
        )
    }

    private object Keys {
        val displayName = stringPreferencesKey("display_name")
        val themeMode = stringPreferencesKey("theme_mode")
        val accentColor = stringPreferencesKey("accent_color")
        val homeSections = stringPreferencesKey("home_sections")
    }
}
