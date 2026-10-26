package com.miagenda.app.data.backup

import com.miagenda.app.domain.backup.AgendaSnapshot
import com.miagenda.app.domain.backup.BackupContents
import com.miagenda.app.domain.backup.BackupFormatException
import com.miagenda.app.domain.backup.BackupProblem
import com.miagenda.app.domain.model.AccentColor
import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.Category
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.model.Habit
import com.miagenda.app.domain.model.HabitFrequency
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.HomeSection
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.model.TaskPriority
import com.miagenda.app.domain.model.TaskStatus
import com.miagenda.app.domain.model.ThemeMode
import com.miagenda.app.domain.model.UserPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {
    @Test
    fun unaCopiaValidaVuelveIgual() {
        val original = sample()
        val json = JsonBackupCodec.encode(original)
        assertTrue(json.contains("\"format\": \"mi-agenda-backup\""))
        assertTrue(json.contains("\"version\": 1"))
        assertTrue(json.contains("\"exportedAt\": \"2026-10-25T21:05\""))
        assertFalse(json.contains("2026-10-25T21:05:00Z"))
        assertEquals(original, JsonBackupCodec.decode(json))
    }

    @Test
    fun laMascotaSeGuardaYUnaCopiaViejaLaDejaVisible() {
        val original = sample().copy(
            preferences = sample().preferences.copy(showMascot = false, mascotProgress = 24)
        )
        val decoded = JsonBackupCodec.decode(JsonBackupCodec.encode(original))
        assertFalse(decoded.preferences.showMascot)
        assertEquals(24, decoded.preferences.mascotProgress)
        val legacy = JsonBackupCodec.encode(sample())
            .replace(Regex(""",\s*"mascotProgress"\s*:\s*0"""), "")
            .replace(Regex(""",\s*"showMascot"\s*:\s*true"""), "")
        val fromLegacy = JsonBackupCodec.decode(legacy)
        assertTrue(fromLegacy.preferences.showMascot)
        assertEquals(0, fromLegacy.preferences.mascotProgress)
    }

    @Test
    fun rechazaUnProgresoDeMascotaNegativo() {
        val json = JsonBackupCodec.encode(
            sample().copy(preferences = sample().preferences.copy(mascotProgress = 1))
        ).replace("\"mascotProgress\": 1", "\"mascotProgress\": -1")
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode(json)
        }
        assertEquals(BackupProblem.INVALID, error.problem)
    }

    @Test
    fun unaCopiaSinSeccionesNoLasActivaTodas() {
        val original = sample().copy(
            preferences = UserPreferences(visibleSections = emptySet())
        )
        val decoded = JsonBackupCodec.decode(JsonBackupCodec.encode(original))
        assertEquals(emptySet<HomeSection>(), decoded.preferences.visibleSections)
        assertEquals(original.snapshot, decoded.snapshot)
    }

    @Test
    fun rechazaOtroFormatoAunqueLeFaltenDatos() {
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode("""{"format":"otro","version":1}""")
        }
        assertEquals(BackupProblem.WRONG_FORMAT, error.problem)
    }

    @Test
    fun rechazaOtraVersionAunqueLeFaltenDatos() {
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode("""{"format":"mi-agenda-backup","version":2}""")
        }
        assertEquals(BackupProblem.UNSUPPORTED_VERSION, error.problem)
    }

    @Test
    fun rechazaUnArchivoQueNoEsJson() {
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode("{")
        }
        assertEquals(BackupProblem.UNREADABLE, error.problem)
    }

    @Test
    fun rechazaUnaHoraFinalAnterior() {
        val json = JsonBackupCodec.encode(sample())
        val broken = json.replace("10:30", "08:00")
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode(broken)
        }
        assertEquals(BackupProblem.INVALID, error.problem)
    }

    @Test
    fun rechazaUnaCategoriaQueNoExiste() {
        val json = JsonBackupCodec.encode(sample())
        val broken = json.replace("\"categoryId\": 3", "\"categoryId\": 99")
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode(broken)
        }
        assertEquals(BackupProblem.INVALID, error.problem)
    }

    @Test
    fun rechazaUnaNotaEnBlanco() {
        val json = JsonBackupCodec.encode(sample())
        val broken = json.replace("Avance el informe", "   ")
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode(broken)
        }
        assertEquals(BackupProblem.INVALID, error.problem)
    }

    @Test
    fun rechazaUnaFechaEnUtc() {
        val json = JsonBackupCodec.encode(sample())
        val broken = json.replace("2026-10-25T21:05", "2026-10-25T21:05:00Z")
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode(broken)
        }
        assertEquals(BackupProblem.INVALID, error.problem)
    }

    @Test
    fun rechazaUnColorDeAcentoDesconocido() {
        val json = JsonBackupCodec.encode(sample())
        val broken = json.replace("TEAL", "PURPLE")
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.decode(broken)
        }
        assertEquals(BackupProblem.INVALID, error.problem)
    }

    @Test
    fun rechazaUnTituloEnBlanco() {
        val original = sample()
        val activity = original.snapshot.activities.single()
        val broken = original.copy(
            snapshot = original.snapshot.copy(
                activities = listOf(activity.copy(title = "   "))
            )
        )
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.encode(broken)
        }
        assertEquals(BackupProblem.INVALID, error.problem)
    }

    @Test
    fun rechazaDosRegistrosDelMismoHabitoYDia() {
        val original = sample()
        val log = original.snapshot.logs.single()
        val broken = original.copy(
            snapshot = original.snapshot.copy(
                logs = listOf(log, log.copy(id = log.id + 1))
            )
        )
        val error = assertThrows(BackupFormatException::class.java) {
            JsonBackupCodec.encode(broken)
        }
        assertEquals(BackupProblem.INVALID, error.problem)
    }

    private fun sample(): BackupContents {
        val now = LocalDateTime.of(2026, 10, 25, 12, 0)
        return BackupContents(
            exportedAt = LocalDateTime.of(2026, 10, 25, 21, 5),
            preferences = UserPreferences(
                displayName = "Ana",
                themeMode = ThemeMode.LIGHT,
                accentColor = AccentColor.TEAL,
                visibleSections = setOf(HomeSection.REFLECTION)
            ),
            snapshot = AgendaSnapshot(
                categories = listOf(
                    Category(
                        id = 3,
                        name = "Estudio",
                        colorArgb = 1,
                        iconKey = "menu_book",
                        sortOrder = 1,
                        isArchived = false,
                        createdAt = now,
                        updatedAt = now
                    )
                ),
                rules = listOf(
                    RecurrenceRule(
                        id = 4,
                        frequency = RecurrenceFrequency.SPECIFIC_DAYS,
                        intervalCount = 1,
                        daysOfWeek = "1,3",
                        startDate = LocalDate.of(2026, 10, 19),
                        endDate = null,
                        createdAt = now,
                        updatedAt = now
                    )
                ),
                activities = listOf(
                    AgendaActivity(
                        id = 8,
                        title = "Clase",
                        notes = "",
                        date = LocalDate.of(2026, 10, 26),
                        startTime = LocalTime.of(9, 0),
                        endTime = LocalTime.of(10, 30),
                        categoryId = 3,
                        status = ActivityStatus.PENDING,
                        recurrenceRuleId = 4,
                        createdAt = now,
                        updatedAt = now
                    )
                ),
                overrides = listOf(
                    ActivityOverride(
                        id = 9,
                        activityId = 8,
                        occurrenceDate = LocalDate.of(2026, 10, 26),
                        status = ActivityStatus.COMPLETED,
                        startTime = null,
                        endTime = null,
                        isSkipped = false,
                        createdAt = now,
                        updatedAt = now
                    )
                ),
                tasks = listOf(
                    AgendaTask(
                        id = 2,
                        title = "Informe",
                        notes = "",
                        dueDate = LocalDate.of(2026, 10, 27),
                        priority = TaskPriority.HIGH,
                        status = TaskStatus.PENDING,
                        categoryId = 3,
                        createdAt = now,
                        updatedAt = now
                    )
                ),
                habits = listOf(
                    Habit(
                        id = 6,
                        title = "Caminar",
                        notes = "",
                        categoryId = 3,
                        frequency = HabitFrequency.SPECIFIC_DAYS,
                        daysOfWeek = "1,3,5",
                        isArchived = false,
                        createdAt = now,
                        updatedAt = now
                    )
                ),
                logs = listOf(
                    HabitLog(
                        id = 11,
                        habitId = 6,
                        date = LocalDate.of(2026, 10, 21),
                        status = HabitLogStatus.PARTIAL,
                        createdAt = now,
                        updatedAt = now
                    )
                ),
                reflections = listOf(
                    DailyReflection(
                        date = LocalDate.of(2026, 10, 25),
                        note = "Avance el informe",
                        createdAt = now,
                        updatedAt = now
                    )
                )
            )
        )
    }
}
