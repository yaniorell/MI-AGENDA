package com.miagenda.app.ui.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.miagenda.app.R
import com.miagenda.app.domain.activity.ActivityOccurrence
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.domain.model.Category
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AgendaItem(
    val activityId: Long,
    val date: LocalDate,
    val title: String,
    val timeLabel: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: ActivityStatus,
    val categoryName: String?,
    val colorArgb: Int?
)

private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatClock(time: LocalTime): String = clockFormatter.format(time)

fun ActivityOccurrence.toAgendaItem(categories: List<Category>): AgendaItem {
    val category = categories.firstOrNull { it.id == activity.categoryId }
    return AgendaItem(
        activityId = activity.id,
        date = date,
        title = activity.title,
        timeLabel = "${formatClock(startTime)} – ${formatClock(endTime)}",
        startTime = startTime,
        endTime = endTime,
        status = status,
        categoryName = category?.name,
        colorArgb = category?.colorArgb
    )
}

@Composable
fun statusLabel(status: ActivityStatus): String {
    val resource = when (status) {
        ActivityStatus.PENDING -> R.string.status_pending
        ActivityStatus.COMPLETED -> R.string.status_completed
        ActivityStatus.PARTIAL -> R.string.status_partial
        ActivityStatus.RESCHEDULED -> R.string.status_rescheduled
        ActivityStatus.CANCELLED -> R.string.status_cancelled
    }
    return stringResource(resource)
}

fun weekdayShortRes(index: Int): Int {
    return when (index) {
        1 -> R.string.weekday_mon
        2 -> R.string.weekday_tue
        3 -> R.string.weekday_wed
        4 -> R.string.weekday_thu
        5 -> R.string.weekday_fri
        6 -> R.string.weekday_sat
        else -> R.string.weekday_sun
    }
}

fun weekdayFullRes(index: Int): Int {
    return when (index) {
        1 -> R.string.weekday_mon_full
        2 -> R.string.weekday_tue_full
        3 -> R.string.weekday_wed_full
        4 -> R.string.weekday_thu_full
        5 -> R.string.weekday_fri_full
        6 -> R.string.weekday_sat_full
        else -> R.string.weekday_sun_full
    }
}
