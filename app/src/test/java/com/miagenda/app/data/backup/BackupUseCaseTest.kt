package com.miagenda.app.data.backup

import com.miagenda.app.domain.backup.AgendaSnapshot
import com.miagenda.app.domain.backup.BackupContents
import com.miagenda.app.domain.backup.BackupFormatException
import com.miagenda.app.domain.backup.BackupPreferencesException
import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.domain.model.UserPreferences
import com.miagenda.app.domain.repository.AgendaBackupStore
import com.miagenda.app.domain.repository.UserPreferencesRepository
import com.miagenda.app.domain.time.TimeProvider
import com.miagenda.app.domain.usecase.ExportBackup
import com.miagenda.app.domain.usecase.ImportBackup
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupUseCaseTest {
    @Test
    fun exportarLeeLaAgendaYLosAjustes() = runTest {
        val now = LocalDateTime.of(2026, 10, 25, 21, 5)
        val store = MemoryStore(sampleSnapshot())
        val preferences = MemoryPreferences(
            UserPreferences(displayName = "Ana", themeMode = ThemeMode.LIGHT, accentColor = AccentColor.TEAL)
        )
        val json = ExportBackup(
            store = store,
            preferences = preferences,
            codec = JsonBackupCodec,
            timeProvider = TimeProvider { now }
        ).invoke()
        val decoded = JsonBackupCodec.decode(json)
        assertEquals(now, decoded.exportedAt)
        assertEquals("Ana", decoded.preferences.displayName)
        assertEquals(ThemeMode.LIGHT, decoded.preferences.themeMode)
        assertEquals(AccentColor.TEAL, decoded.preferences.accentColor)
        assertEquals("Club", decoded.snapshot.categories.single().name)
    }

    @Test
    fun unArchivoInvalidoNoSustituyeLaAgenda() = runTest {
        val store = MemoryStore(sampleSnapshot())
        val preferences = MemoryPreferences(UserPreferences(displayName = "Ana"))
        try {
            ImportBackup(store, preferences, JsonBackupCodec).invoke("{")
            fail("El archivo invalido no debio importarse")
        } catch (error: BackupFormatException) {
            assertEquals(com.miagenda.app.domain.backup.BackupProblem.UNREADABLE, error.problem)
        }
        assertEquals(0, store.replaceCalls)
        assertEquals("Club", store.snapshot.categories.single().name)
        assertEquals("Ana", preferences.current.displayName)
    }

    @Test
    fun siLosAjustesFallanLaAgendaYaQuedoSustituida() = runTest {
        val store = MemoryStore(sampleSnapshot())
        val preferences = MemoryPreferences(UserPreferences(displayName = "Ana"))
        preferences.failOnRestore = true
        val json = JsonBackupCodec.encode(
            BackupContents(
                exportedAt = LocalDateTime.of(2026, 10, 25, 21, 5),
                preferences = UserPreferences(
                    displayName = "Nico",
                    themeMode = ThemeMode.DARK,
                    accentColor = AccentColor.VIOLET,
                    visibleSections = setOf(HomeSection.REFLECTION)
                ),
                snapshot = AgendaSnapshot()
            )
        )
        try {
            ImportBackup(store, preferences, JsonBackupCodec).invoke(json)
            fail("La copia debio avisar que los ajustes no se guardaron")
        } catch (error: BackupPreferencesException) {
            assertEquals(1, store.replaceCalls)
        }
        assertEquals(1, store.replaceCalls)
        assertTrue(store.snapshot.categories.isEmpty())
        assertEquals("Ana", preferences.current.displayName)
    }

    private fun sampleSnapshot(): AgendaSnapshot {
        val now = LocalDateTime.of(2026, 10, 25, 12, 0)
        return AgendaSnapshot(
            categories = listOf(
                Category(
                    id = 5,
                    name = "Club",
                    colorArgb = 1,
                    iconKey = "label",
                    sortOrder = 1,
                    isArchived = false,
                    createdAt = now,
                    updatedAt = now
                )
            )
        )
    }
}

private class MemoryStore(
    var snapshot: AgendaSnapshot
) : AgendaBackupStore {
    var replaceCalls: Int = 0

    override suspend fun read(): AgendaSnapshot = snapshot

    override suspend fun replace(snapshot: AgendaSnapshot) {
        replaceCalls += 1
        this.snapshot = snapshot
    }
}

private class MemoryPreferences(
    initial: UserPreferences
) : UserPreferencesRepository {
    var current: UserPreferences = initial
    var failOnRestore: Boolean = false
    private val state = MutableStateFlow(initial)
    override val preferences: Flow<UserPreferences> = state

    override suspend fun setDisplayName(name: String) = Unit

    override suspend fun setThemeMode(mode: ThemeMode) = Unit

    override suspend fun setAccentColor(color: AccentColor) = Unit

    override suspend fun setVisibleSections(sections: Set<HomeSection>) = Unit

    override suspend fun setShowMascot(show: Boolean) = Unit

    override suspend fun rememberMascotProgress(live: Int) = Unit

    override suspend fun restore(preferences: UserPreferences) {
        if (failOnRestore) {
            throw IOException("disk")
        }
        current = preferences
        state.value = preferences
    }
}
