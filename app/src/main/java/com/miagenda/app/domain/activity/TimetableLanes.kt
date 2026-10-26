package com.miagenda.app.domain.activity

import java.time.LocalTime
import kotlin.math.max
import kotlin.math.min

data class TimedSpan(
    val id: Long,
    val start: LocalTime,
    val end: LocalTime
)

data class LaneSpan(
    val id: Long,
    val lane: Int,
    val laneCount: Int
)

fun assignLanes(spans: List<TimedSpan>): List<LaneSpan> {
    val sorted = spans.sortedWith(compareBy({ it.start }, { it.end }, { it.id }))
    val laneEnds = mutableListOf<LocalTime>()
    val laneOf = LinkedHashMap<Long, Int>()
    sorted.forEach { span ->
        val free = laneEnds.indexOfFirst { end -> end <= span.start }
        val lane = if (free == -1) {
            laneEnds += span.end
            laneEnds.lastIndex
        } else {
            laneEnds[free] = span.end
            free
        }
        laneOf[span.id] = lane
    }
    return sorted.map { span ->
        val group = sorted.filter { other -> overlaps(span, other) }
        val count = group.maxOf { other -> (laneOf[other.id] ?: 0) + 1 }
        LaneSpan(id = span.id, lane = laneOf[span.id] ?: 0, laneCount = count.coerceAtLeast(1))
    }
}

fun timetableHourRange(spans: List<TimedSpan>): IntRange {
    var first = 7
    var last = 22
    spans.forEach { span ->
        first = min(first, span.start.hour)
        last = max(last, exclusiveEndHour(span))
    }
    if (last <= first) {
        last = first + 1
    }
    return first until last.coerceAtMost(24)
}

private fun overlaps(first: TimedSpan, second: TimedSpan): Boolean {
    return first.start < second.end && second.start < first.end
}

private fun exclusiveEndHour(span: TimedSpan): Int {
    val end = if (span.end <= span.start) span.start.plusMinutes(1) else span.end
    return if (end.minute == 0) end.hour else end.hour + 1
}
