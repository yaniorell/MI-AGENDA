package com.miagenda.app.data.backup

import com.miagenda.app.data.local.AgendaDatabase
import com.miagenda.app.data.TestDeviceContext
import com.miagenda.app.data.local.RoomActivityRepository
import com.miagenda.app.data.local.RoomCategoryRepository
import com.miagenda.app.data.local.RoomHabitRepository
import com.miagenda.app.data.local.RoomReflectionRepository
import com.miagenda.app.data.local.RoomTaskRepository
import com.miagenda.app.domain.backup.AgendaSnapshot
import com.miagenda.app.domain.backup.BackupContents
import com.miagenda.app.domain.backup.BackupFormatException
import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.domain.model.UserPreferences
import com.miagenda.app.domain.repository.UserPreferencesRepository
import com.miagenda.app.domain.time.TimeProvider
import com.miagenda.app.domain.usecase.ImportBackup
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BackupPersistenceTest {
    private val now = LocalDateTime.of(2026, 10, 25, 12, 0)
    private val clock = TimeProvider { now }

    @Test
    fun exportarEImportarConservaLosDatosYNoReutilizaIds() = runTest {
        val database = openMemory()
        try {
            val store = RoomAgendaBackupStore(database)
            fill(database)
            val original = store.read()
            assertTrue(original.categories.any { category -> category.name == "Estudio" })
            assertEquals("Clase de cálculo", original.activities.single().title)
            assertEquals("Informe", original.tasks.single().title)
            assertEquals("Caminar", original.habits.single().title)
            assertEquals(HabitLogStatus.PARTIAL, original.logs.single().status)
            assertEquals("Avancé el informe", original.reflections.single().note)

            val contents = BackupContents(
                exportedAt = LocalDateTime.of(2026, 10, 25, 21, 5),
                preferences = UserPreferences(
                    displayName = "Ana",
                    themeMode = ThemeMode.LIGHT,
                    accentColor = AccentColor.TEAL,
                    visibleSections = setOf(HomeSection.REFLECTION)
                ),
                snapshot = original
            )
            val decoded = JsonBackupCodec.decode(JsonBackupCodec.encode(contents))
            assertEquals(original, decoded.snapshot)
            assertEquals("Ana", decoded.preferences.displayName)
            assertEquals(setOf(HomeSection.REFLECTION), decoded.preferences.visibleSections)

            store.replace(
                AgendaSnapshot(
                    categories = listOf(
                        Category(
                            id = 40,
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
            )
            val wiped = store.read()
            assertEquals(listOf("Club"), wiped.categories.map { category -> category.name })
            assertTrue(wiped.activities.isEmpty())
            assertTrue(wiped.tasks.isEmpty())
            assertTrue(wiped.habits.isEmpty())
            assertTrue(wiped.reflections.isEmpty())

            store.replace(decoded.snapshot)
            assertEquals(original, store.read())

            val added = RoomCategoryRepository(database.categoryDao(), clock).insert(
                Category(
                    id = 0,
                    name = "Nueva",
                    colorArgb = 1,
                    iconKey = "label",
                    sortOrder = 30,
                    isArchived = false,
                    createdAt = now,
                    updatedAt = now
                )
            )
            assertFalse(original.categories.map { category -> category.id }.contains(added.id))
            assertEquals(original.categories.size + 1, database.categoryDao().count())
        } finally {
            database.close()
        }
    }

    @Test
    fun unaReferenciaRotaNoDejaLaBaseAMedias() = runTest {
        val database = openMemory()
        try {
            val store = RoomAgendaBackupStore(database)
            val before = store.read()
            assertEquals(9, before.categories.size)
            var rejected = false
            try {
                store.replace(
                    AgendaSnapshot(
                        activities = listOf(
                            AgendaActivity(
                                id = 1,
                                title = "Huerfana",
                                notes = "",
                                date = LocalDate.of(2026, 10, 26),
                                startTime = LocalTime.of(9, 0),
                                endTime = LocalTime.of(10, 0),
                                categoryId = 999,
                                status = ActivityStatus.PENDING,
                                recurrenceRuleId = null,
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                    )
                )
            } catch (error: Exception) {
                rejected = true
                assertTrue(error.message != null || rejected)
            }
            assertTrue(rejected)
            assertEquals(before, store.read())
        } finally {
            database.close()
        }
    }

    @Test
    fun unArchivoInvalidoNoBorraLoGuardado() = runTest {
        val database = openMemory()
        try {
            val tasks = RoomTaskRepository(database.taskDao(), clock)
            tasks.insert(
                AgendaTask(
                    id = 0,
                    title = "Informe",
                    notes = "",
                    dueDate = LocalDate.of(2026, 10, 27),
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.PENDING,
                    categoryId = null,
                    createdAt = now,
                    updatedAt = now
                )
            )
            val store = RoomAgendaBackupStore(database)
            val before = store.read()
            try {
                ImportBackup(
                    store = store,
                    preferences = RestorePreferences(),
                    codec = JsonBackupCodec
                ).invoke("{")
                fail("El archivo invalido no debio importarse")
            } catch (error: BackupFormatException) {
                assertEquals(
                    com.miagenda.app.domain.backup.BackupProblem.UNREADABLE,
                    error.problem
                )
            }
            assertEquals(before, store.read())
            assertEquals("Informe", store.read().tasks.single().title)
        } finally {
            database.close()
        }
    }

    private suspend fun fill(database: AgendaDatabase) {
        val categories = RoomCategoryRepository(database.categoryDao(), clock)
        val activities = RoomActivityRepository(
            activityDao = database.activityDao(),
            recurrenceDao = database.recurrenceDao(),
            timeProvider = clock
        )
        val tasks = RoomTaskRepository(database.taskDao(), clock)
        val habits = RoomHabitRepository(database.habitDao(), clock)
        val reflections = RoomReflectionRepository(database.reflectionDao(), clock)
        val category = categories.getAll().first { category -> category.name == "Estudio" }
        val rule = activities.insertRule(
            RecurrenceRule(
                id = 0,
                frequency = RecurrenceFrequency.WEEKLY,
                intervalCount = 1,
                daysOfWeek = "1",
                startDate = LocalDate.of(2026, 10, 19),
                endDate = null,
                createdAt = now,
                updatedAt = now
            )
        )
        val activity = activities.insert(
            AgendaActivity(
                id = 0,
                title = "Clase de cálculo",
                notes = "Aula 2",
                date = LocalDate.of(2026, 10, 26),
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(10, 30),
                categoryId = category.id,
                status = ActivityStatus.PENDING,
                recurrenceRuleId = rule.id,
                createdAt = now,
                updatedAt = now
            )
        )
        activities.insertOverride(
            ActivityOverride(
                id = 0,
                activityId = activity.id,
                occurrenceDate = LocalDate.of(2026, 10, 26),
                status = ActivityStatus.COMPLETED,
                startTime = null,
                endTime = null,
                isSkipped = false,
                createdAt = now,
                updatedAt = now
            )
        )
        tasks.insert(
            AgendaTask(
                id = 0,
                title = "Informe",
                notes = "",
                dueDate = LocalDate.of(2026, 10, 27),
                priority = TaskPriority.HIGH,
                status = TaskStatus.PENDING,
                categoryId = category.id,
                createdAt = now,
                updatedAt = now
            )
        )
        val habit = habits.insert(
            Habit(
                id = 0,
                title = "Caminar",
                notes = "",
                categoryId = category.id,
                frequency = HabitFrequency.SPECIFIC_DAYS,
                daysOfWeek = "1,3,5",
                isArchived = false,
                createdAt = now,
                updatedAt = now
            )
        )
        habits.saveLog(habit.id, LocalDate.of(2026, 10, 21), HabitLogStatus.PARTIAL)
        reflections.save(
            DailyReflection(
                date = LocalDate.of(2026, 10, 25),
                note = "  Avancé el informe  ",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private fun openMemory(): AgendaDatabase {
        val directory = Files.createTempDirectory("mi-agenda-backup").toFile()
        return AgendaDatabase.createInMemory(TestDeviceContext(directory))
    }
}

private class RestorePreferences : UserPreferencesRepository {
    private val state = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = state

    override suspend fun setDisplayName(name: String) = Unit

    override suspend fun setThemeMode(mode: ThemeMode) = Unit

    override suspend fun setAccentColor(color: AccentColor) = Unit

    override suspend fun setVisibleSections(sections: Set<HomeSection>) = Unit

    override suspend fun setShowMascot(show: Boolean) = Unit

    override suspend fun rememberMascotProgress(live: Int) = Unit

    override suspend fun restore(preferences: UserPreferences) {
        state.value = preferences
    }
}
