package com.miagenda.app.data

import androidx.sqlite.SQLiteException
import com.miagenda.app.data.local.AgendaDatabase
import com.miagenda.app.data.local.RoomActivityRepository
import com.miagenda.app.data.local.RoomCategoryRepository
import com.miagenda.app.data.local.RoomHabitRepository
import com.miagenda.app.data.local.RoomReflectionRepository
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.time.TimeProvider
import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaPersistenceTest {
    private val clock = TimeProvider { LocalDateTime.of(2026, 10, 25, 12, 0) }

    @Test
    fun unaBaseNuevaTieneNueveCategoriasYNoLasRepite() = runTest {
        val directory = Files.createTempDirectory("mi-agenda-seed")
        val first = openFile(directory.toFile())
        try {
            val repository = RoomCategoryRepository(first.categoryDao(), clock)
            val names = repository.observeAll().first().map { it.name }
            assertEquals(
                listOf(
                    "Universidad",
                    "Estudio",
                    "Proyectos",
                    "Programación",
                    "Redes y ciberseguridad",
                    "Ejercicio",
                    "Descanso",
                    "Personal",
                    "Otros"
                ),
                names
            )
            assertEquals(9, repository.count())
        } finally {
            first.close()
        }

        val second = openFile(directory.toFile())
        try {
            val repository = RoomCategoryRepository(second.categoryDao(), clock)
            assertEquals(9, repository.count())
            val added = repository.insert(extraCategory())
            assertEquals(10, repository.count())
            assertEquals("Club de lectura", added.name)
        } finally {
            second.close()
        }

        val third = openFile(directory.toFile())
        try {
            val repository = RoomCategoryRepository(third.categoryDao(), clock)
            val names = repository.getAll().map { it.name }
            assertEquals(10, names.size)
            assertEquals(1, names.count { it == "Universidad" })
            assertEquals(1, names.count { it == "Club de lectura" })
        } finally {
            third.close()
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun borrarCategoriaDejaLaActividadSinCategoria() = runTest {
        val database = openMemory()
        try {
            val categories = RoomCategoryRepository(database.categoryDao(), clock)
            val activities = RoomActivityRepository(
                activityDao = database.activityDao(),
                recurrenceDao = database.recurrenceDao(),
                timeProvider = clock
            )
            val university = categories.getAll().first { it.name == "Universidad" }
            val saved = activities.insert(sampleActivity(categoryId = university.id))
            categories.delete(university.id)

            val stored = activities.get(saved.id)
            assertEquals(saved.title, stored?.title)
            assertNull(stored?.categoryId)
            assertEquals(8, categories.count())
        } finally {
            database.close()
        }
    }

    @Test
    fun elRegistroDeUnHabitoEsUnicoPorFecha() = runTest {
        val database = openMemory()
        try {
            val habits = RoomHabitRepository(database.habitDao(), clock)
            val habit = habits.insert(sampleHabit())
            val day = LocalDate.of(2026, 10, 25)
            val saved = habits.insertLog(
                HabitLog(
                    id = 0,
                    habitId = habit.id,
                    date = day,
                    status = HabitLogStatus.COMPLETED,
                    createdAt = clock.now(),
                    updatedAt = clock.now()
                )
            )
            var rejected = false
            try {
                habits.insertLog(saved.copy(id = 0, status = HabitLogStatus.PARTIAL))
            } catch (_: SQLiteException) {
                rejected = true
            }
            assertTrue(rejected)
            assertEquals(1, habits.observeLogs(habit.id).first().size)
            assertEquals(HabitLogStatus.COMPLETED, habits.getLog(habit.id, day)?.status)
        } finally {
            database.close()
        }
    }

    @Test
    fun borrarUnHabitoBorraTambienSusRegistros() = runTest {
        val database = openMemory()
        try {
            val habits = RoomHabitRepository(database.habitDao(), clock)
            val habit = habits.insert(sampleHabit())
            val day = LocalDate.of(2026, 10, 26)
            habits.insertLog(
                HabitLog(
                    id = 0,
                    habitId = habit.id,
                    date = day,
                    status = HabitLogStatus.PARTIAL,
                    createdAt = clock.now(),
                    updatedAt = clock.now()
                )
            )
            habits.delete(habit.id)
            assertNull(habits.get(habit.id))
            assertNull(habits.getLog(habit.id, day))
        } finally {
            database.close()
        }
    }

    @Test
    fun unaActividadNoPuedeTerminarAntesDeEmpezar() = runTest {
        val database = openMemory()
        try {
            val activities = RoomActivityRepository(
                activityDao = database.activityDao(),
                recurrenceDao = database.recurrenceDao(),
                timeProvider = clock
            )
            val error = runCatching {
                activities.insert(
                    sampleActivity(categoryId = null).copy(
                        startTime = LocalTime.of(21, 0),
                        endTime = LocalTime.of(8, 0)
                    )
                )
            }.exceptionOrNull()
            assertTrue(error is IllegalArgumentException)
            assertEquals(emptyList<AgendaActivity>(), activities.observeAll().first())
        } finally {
            database.close()
        }
    }

    @Test
    fun unaNotaVaciaBorraLaReflexionDelDia() = runTest {
        val database = openMemory()
        try {
            val reflections = RoomReflectionRepository(database.reflectionDao(), clock)
            val day = LocalDate.of(2026, 10, 25)
            reflections.save(
                DailyReflection(
                    date = day,
                    note = "  Avancé el informe  ",
                    createdAt = clock.now(),
                    updatedAt = clock.now()
                )
            )
            assertEquals("Avancé el informe", reflections.get(day)?.note)
            reflections.delete(day)
            assertNull(reflections.get(day))
        } finally {
            database.close()
        }
    }

    @Test
    fun guardarElMismoDiaDeUnHabitoActualizaEseRegistro() = runTest {
        val database = openMemory()
        try {
            val habits = RoomHabitRepository(database.habitDao(), clock)
            val habit = habits.insert(sampleHabit())
            val day = LocalDate.of(2026, 10, 25)
            habits.saveLog(habit.id, day, HabitLogStatus.COMPLETED)
            habits.saveLog(habit.id, day, HabitLogStatus.PARTIAL)
            val logs = habits.observeLogs(habit.id).first()
            assertEquals(1, logs.size)
            assertEquals(HabitLogStatus.PARTIAL, logs.single().status)
        } finally {
            database.close()
        }
    }

    private fun openMemory(): AgendaDatabase {
        val directory = Files.createTempDirectory("mi-agenda-memory").toFile()
        return AgendaDatabase.createInMemory(TestDeviceContext(directory))
    }

    private fun openFile(directory: File): AgendaDatabase {
        return AgendaDatabase.createForFile(TestDeviceContext(directory), "mi_agenda.db")
    }

    private fun extraCategory(): Category {
        val now = clock.now()
        return Category(
            id = 0,
            name = "  Club de lectura  ",
            colorArgb = 0xFF7AA2FF.toInt(),
            iconKey = "menu_book",
            sortOrder = 20,
            isArchived = false,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun sampleActivity(categoryId: Long?): AgendaActivity {
        val now = clock.now()
        return AgendaActivity(
            id = 0,
            title = "Clase de cálculo",
            notes = "",
            date = LocalDate.of(2026, 10, 26),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 30),
            categoryId = categoryId,
            status = ActivityStatus.PENDING,
            recurrenceRuleId = null,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun sampleHabit(): Habit {
        val now = clock.now()
        return Habit(
            id = 0,
            title = "Caminar",
            notes = "",
            categoryId = null,
            frequency = HabitFrequency.DAILY,
            daysOfWeek = null,
            isArchived = false,
            createdAt = now,
            updatedAt = now
        )
    }
}
