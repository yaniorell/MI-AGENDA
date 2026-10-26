package com.miagenda.app.domain.activity

import com.miagenda.app.domain.model.ActivityOverride
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.AgendaActivity
import com.miagenda.app.domain.model.RecurrenceFrequency
import com.miagenda.app.domain.model.RecurrenceRule
import com.miagenda.app.domain.time.parseDaysOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class ActivityOccurrence(
    val activity: AgendaActivity,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: ActivityStatus,
    val isSkipped: Boolean
)

fun mondayOf(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

fun expandActivities(
    activities: List<AgendaActivity>,
    rules: List<RecurrenceRule>,
    overrides: List<ActivityOverride>,
    rangeStart: LocalDate,
    rangeEnd: LocalDate
): List<ActivityOccurrence> {
    val rulesById = rules.associateBy { it.id }
    val overridesByKey = overrides.associateBy { it.activityId to it.occurrenceDate }
    val dates = generateSequence(rangeStart) { previous ->
        val next = previous.plusDays(1)
        if (next > rangeEnd) null else next
    }
    return activities.flatMap { activity ->
        val rule = activity.recurrenceRuleId?.let(rulesById::get)
        dates.mapNotNull { date ->
            occurrenceOn(activity, rule, overridesByKey[activity.id to date], date)
        }
    }.sortedWith(compareBy({ it.date }, { it.startTime }, { it.activity.id }))
}

fun visibleOccurrences(occurrences: List<ActivityOccurrence>): List<ActivityOccurrence> {
    return occurrences.filter { occurrence -> !occurrence.isSkipped }
}

data class DayProgress(
    val percent: Int,
    val scheduled: Int
)

fun dayProgress(occurrences: List<ActivityOccurrence>): DayProgress? {
    val scheduled = visibleOccurrences(occurrences).filter { it.status != ActivityStatus.CANCELLED }
    if (scheduled.isEmpty()) {
        return null
    }
    val completed = scheduled.count { it.status == ActivityStatus.COMPLETED }
    val partial = scheduled.count { it.status == ActivityStatus.PARTIAL }
    val percent = ((completed + (0.5 * partial)) / scheduled.size * 100.0).roundToInt()
    return DayProgress(percent = percent, scheduled = scheduled.size)
}

fun nextOpenOccurrence(
    occurrences: List<ActivityOccurrence>,
    now: LocalTime
): ActivityOccurrence? {
    return visibleOccurrences(occurrences)
        .filter { occurrence ->
            occurrence.status == ActivityStatus.PENDING ||
                occurrence.status == ActivityStatus.PARTIAL ||
                occurrence.status == ActivityStatus.RESCHEDULED
        }
        .filter { occurrence -> occurrence.endTime >= now }
        .minWithOrNull(compareBy({ it.startTime }, { it.activity.id }))
}

private fun occurrenceOn(
    activity: AgendaActivity,
    rule: RecurrenceRule?,
    override: ActivityOverride?,
    date: LocalDate
): ActivityOccurrence? {
    val happens = if (rule == null) {
        activity.date == date
    } else {
        ruleOccursOn(rule, date)
    }
    if (!happens) {
        return null
    }
    return ActivityOccurrence(
        activity = activity,
        date = date,
        startTime = override?.startTime ?: activity.startTime,
        endTime = override?.endTime ?: activity.endTime,
        status = override?.status ?: activity.status,
        isSkipped = override?.isSkipped == true
    )
}

internal fun ruleOccursOn(rule: RecurrenceRule, date: LocalDate): Boolean {
    if (date < rule.startDate) {
        return false
    }
    if (rule.endDate != null && date > rule.endDate) {
        return false
    }
    val interval = rule.intervalCount.coerceAtLeast(1).toLong()
    val days = parseDaysOfWeek(rule.daysOfWeek)
    return when (rule.frequency) {
        RecurrenceFrequency.DAILY -> ChronoUnit.DAYS.between(rule.startDate, date) % interval == 0L
        RecurrenceFrequency.WEEKLY,
        RecurrenceFrequency.EVERY_N_WEEKS -> {
            val startWeek = rule.startDate.with(DayOfWeek.MONDAY)
            val dateWeek = date.with(DayOfWeek.MONDAY)
            val weeks = ChronoUnit.WEEKS.between(startWeek, dateWeek)
            weeks >= 0 && weeks % interval == 0L && date.dayOfWeek.value in chosenDays(days, rule.startDate)
        }
        RecurrenceFrequency.SPECIFIC_DAYS -> date.dayOfWeek.value in days
    }
}

private fun chosenDays(days: Set<Int>, startDate: LocalDate): Set<Int> {
    return days.ifEmpty { setOf(startDate.dayOfWeek.value) }
}
