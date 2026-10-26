package com.miagenda.app.ui.mascot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miagenda.app.domain.mascot.MascotStage
import com.miagenda.app.domain.mascot.completedProgress
import com.miagenda.app.domain.mascot.mascotStage
import com.miagenda.app.domain.mascot.rememberedProgress
import com.miagenda.app.domain.repository.ActivityRepository
import com.miagenda.app.domain.repository.HabitRepository
import com.miagenda.app.domain.repository.TaskRepository
import com.miagenda.app.domain.usecase.ObserveUserPreferences
import com.miagenda.app.domain.usecase.RememberMascotProgress
import java.io.IOException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MascotUiState(
    val visible: Boolean = true,
    val stage: MascotStage = MascotStage.SLEEPING,
    val liveProgress: Int = 0
)

class MascotViewModel(
    observeUserPreferences: ObserveUserPreferences,
    activityRepository: ActivityRepository,
    taskRepository: TaskRepository,
    habitRepository: HabitRepository,
    private val rememberMascotProgress: RememberMascotProgress
) : ViewModel() {
    val uiState: StateFlow<MascotUiState> = combine(
        observeUserPreferences(),
        activityRepository.observeAll(),
        activityRepository.observeAllOverrides(),
        taskRepository.observeAll(),
        habitRepository.observeAllLogs()
    ) { preferences, activities, overrides, tasks, logs ->
        val live = completedProgress(activities, overrides, tasks, logs)
        val progress = rememberedProgress(preferences.mascotProgress, live)
        MascotUiState(
            visible = preferences.showMascot,
            stage = mascotStage(progress),
            liveProgress = live
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MascotUiState()
    )

    init {
        viewModelScope.launch {
            uiState
                .map { state -> state.liveProgress }
                .distinctUntilChanged()
                .collect { live ->
                    try {
                        rememberMascotProgress(live)
                    } catch (_: IOException) {
                        // Luma sigue viéndose con el progreso de esta sesión.
                    }
                }
        }
    }
}
