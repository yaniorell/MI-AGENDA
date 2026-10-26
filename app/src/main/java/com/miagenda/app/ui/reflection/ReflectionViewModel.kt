package com.miagenda.app.ui.reflection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.miagenda.app.domain.activity.parseAgendaDate
import com.miagenda.app.domain.home.DateProvider
import com.miagenda.app.domain.home.agendaDateLabel
import com.miagenda.app.domain.model.DailyReflection
import com.miagenda.app.domain.repository.ReflectionRepository
import com.miagenda.app.ui.navigation.ReflectionRoute
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_REFLECTION_LENGTH = 2_000

data class ReflectionUiState(
    val dateIso: String = "",
    val dateLabel: String = "",
    val note: String = "",
    val canGoNext: Boolean = false,
    val saveError: Boolean = false,
    val saved: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class ReflectionViewModel(
    savedStateHandle: SavedStateHandle,
    private val reflectionRepository: ReflectionRepository,
    private val dateProvider: DateProvider
) : ViewModel() {
    private val today = dateProvider.today()
    private val selected = MutableStateFlow(initialDate(savedStateHandle.toRoute<ReflectionRoute>().date, today))
    private val draft = MutableStateFlow<String?>(null)
    private val feedback = MutableStateFlow(false to false)

    val uiState: StateFlow<ReflectionUiState> = selected.flatMapLatest { date ->
        combine(reflectionRepository.observe(date), draft, feedback) { stored, edited, status ->
            ReflectionUiState(
                dateIso = date.toString(),
                dateLabel = agendaDateLabel(date),
                note = (edited ?: stored?.note.orEmpty()).take(MAX_REFLECTION_LENGTH),
                canGoNext = date.isBefore(today),
                saveError = status.first,
                saved = status.second
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReflectionUiState(dateLabel = agendaDateLabel(selected.value))
    )

    fun onNoteChange(value: String) {
        draft.value = value.replace("\n\n\n", "\n\n").take(MAX_REFLECTION_LENGTH)
        feedback.value = false to false
    }

    fun showPreviousDay() {
        draft.value = null
        feedback.value = false to false
        selected.update { date -> date.minusDays(1) }
    }

    fun showNextDay() {
        draft.value = null
        feedback.value = false to false
        selected.update { date ->
            if (date.isBefore(today)) date.plusDays(1) else date
        }
    }

    fun save() {
        val date = selected.value
        val text = (draft.value ?: uiState.value.note).trim().take(MAX_REFLECTION_LENGTH)
        viewModelScope.launch {
            try {
                if (text.isEmpty()) {
                    reflectionRepository.delete(date)
                } else {
                    val stamp = LocalDateTime.now()
                    reflectionRepository.save(
                        DailyReflection(
                            date = date,
                            note = text,
                            createdAt = stamp,
                            updatedAt = stamp
                        )
                    )
                }
                draft.value = null
                feedback.value = false to true
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                feedback.value = true to false
            }
        }
    }
}

private fun initialDate(raw: String, today: LocalDate): LocalDate {
    val parsed = parseAgendaDate(raw) ?: return today
    return if (parsed.isAfter(today)) today else parsed
}
