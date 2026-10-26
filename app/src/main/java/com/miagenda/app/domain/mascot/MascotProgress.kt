package com.miagenda.app.domain.mascot

import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.AgendaTask
import com.miagenda.app.domain.model.HabitLog
import com.miagenda.app.domain.model.HabitLogStatus
import com.miagenda.app.domain.model.TaskStatus
import kotlin.math.max

enum class MascotStage {
    SLEEPING,
    AWAKE,
    STUDYING,
    SPECIAL
}

fun completedProgress(
    activities: List<AgendaActivity>,
    overrides: List<ActivityOverride>,
    tasks: List<AgendaTask>,
    logs: List<HabitLog>
): Int {
    val singleActivities = activities.count { activity ->
        activity.recurrenceRuleId == null && activity.status == ActivityStatus.COMPLETED
    }
    val recurringDays = overrides.count { override ->
        !override.isSkipped && override.status == ActivityStatus.COMPLETED
    }
    val doneTasks = tasks.count { task -> task.status == TaskStatus.COMPLETED }
    val doneHabits = logs.count { log -> log.status == HabitLogStatus.COMPLETED }
    return singleActivities + recurringDays + doneTasks + doneHabits
}

fun mascotStage(progress: Int): MascotStage {
    return when {
        progress >= 24 -> MascotStage.SPECIAL
        progress >= 8 -> MascotStage.STUDYING
        progress >= 1 -> MascotStage.AWAKE
        else -> MascotStage.SLEEPING
    }
}

fun rememberedProgress(stored: Int, live: Int): Int {
    return max(stored.coerceAtLeast(0), live.coerceAtLeast(0))
}
