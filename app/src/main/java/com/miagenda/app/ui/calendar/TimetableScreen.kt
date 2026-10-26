package com.miagenda.app.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.miagenda.app.R
import com.miagenda.app.domain.activity.LaneSpan
import com.miagenda.app.domain.activity.TimedSpan
import com.miagenda.app.domain.activity.assignLanes
import com.miagenda.app.domain.activity.timetableHourRange
import com.miagenda.app.domain.model.ActivityStatus
import com.miagenda.app.ui.activity.AgendaItem
import com.miagenda.app.ui.activity.formatClock
import com.miagenda.app.ui.activity.weekdayFullRes
import com.miagenda.app.ui.activity.weekdayShortRes
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class TimetableLayout {
    COLUMNS,
    ROWS
}

private val weekRangeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-ES"))

private val hourColumnWidth = 52.dp
private val dayHeaderHeight = 56.dp
private val dayLabelWidth = 76.dp
private val columnDayWidth = 108.dp
private val rowHourWidth = 84.dp
private val columnHourHeight = 64.dp
private val rowLaneHeight = 44.dp

@Composable
fun TimetableScreen(
    state: TimetableUiState,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
    onOpenActivity: (Long, String) -> Unit,
    onAddActivity: (String, String) -> Unit,
    onAddClass: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var layoutName by rememberSaveable { mutableStateOf(TimetableLayout.ROWS.name) }
    val layout = TimetableLayout.entries.firstOrNull { it.name == layoutName } ?: TimetableLayout.ROWS
    val days = (0..6).map { offset -> state.weekStart.plusDays(offset.toLong()) }
    val range = timetableHourRange(state.items.map { item -> item.toSpan() })
    val lanes = state.items.groupBy { item -> item.date }.mapValues { (_, dayItems) ->
        assignLanes(dayItems.map { item -> item.toSpan() }).associateBy { lane -> lane.id }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back)
                )
            }
            Text(
                text = stringResource(R.string.timetable_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousWeek, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.cd_previous_week)
                )
            }
            Text(
                text = weekRangeLabel(state.weekStart),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNextWeek, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.cd_next_week)
                )
            }
        }
        TextButton(onClick = onToday, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.timetable_today))
        }
        Button(
            onClick = onAddClass,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(stringResource(R.string.timetable_add_class))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = layout == TimetableLayout.COLUMNS,
                onClick = { layoutName = TimetableLayout.COLUMNS.name },
                label = { Text(stringResource(R.string.timetable_columns)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
            FilterChip(
                selected = layout == TimetableLayout.ROWS,
                onClick = { layoutName = TimetableLayout.ROWS.name },
                label = { Text(stringResource(R.string.timetable_rows)) },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
        Text(
            text = stringResource(
                if (state.items.isEmpty()) R.string.timetable_empty else R.string.timetable_help
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (layout == TimetableLayout.COLUMNS) {
                ColumnsTimetable(
                    days = days,
                    today = state.today,
                    hours = range,
                    items = state.items,
                    lanes = lanes,
                    onOpenActivity = onOpenActivity,
                    onAddActivity = onAddActivity
                )
            } else {
                RowsTimetable(
                    days = days,
                    today = state.today,
                    hours = range,
                    items = state.items,
                    lanes = lanes,
                    onOpenActivity = onOpenActivity,
                    onAddActivity = onAddActivity
                )
            }
        }
    }
}

@Composable
private fun ColumnsTimetable(
    days: List<LocalDate>,
    today: LocalDate,
    hours: IntRange,
    items: List<AgendaItem>,
    lanes: Map<LocalDate, Map<Long, LaneSpan>>,
    onOpenActivity: (Long, String) -> Unit,
    onAddActivity: (String, String) -> Unit
) {
    val horizontal = rememberScrollState()
    val vertical = rememberScrollState()
    val gridWidth = columnDayWidth * days.size
    val gridHeight = columnHourHeight * hours.count()
    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.width(hourColumnWidth)) {
            Spacer(modifier = Modifier.height(dayHeaderHeight))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clipToBounds()
            ) {
                Column(
                    modifier = Modifier.graphicsLayer { translationY = -vertical.value.toFloat() }
                ) {
                    hours.forEach { hour ->
                        Text(
                            text = formatClock(LocalTime.of(hour, 0)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .height(columnHourHeight)
                                .padding(start = 6.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.height(dayHeaderHeight).clipToBounds()) {
                Row(
                    modifier = Modifier
                        .width(gridWidth)
                        .graphicsLayer { translationX = -horizontal.value.toFloat() }
                ) {
                    days.forEach { day ->
                        DayHeading(day = day, today = today, modifier = Modifier.width(columnDayWidth))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(vertical)
                    .horizontalScroll(horizontal)
            ) {
                Box(modifier = Modifier.size(gridWidth, gridHeight)) {
                    GridLines(
                        width = gridWidth,
                        height = gridHeight,
                        columns = days.size,
                        rows = hours.count(),
                        cellWidth = columnDayWidth,
                        cellHeight = columnHourHeight
                    )
                    days.forEachIndexed { index, day ->
                        Box(
                            modifier = Modifier
                                .offset(x = columnDayWidth * index)
                                .size(columnDayWidth, gridHeight)
                                .background(
                                    if (day == today) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                        )
                        hours.forEach { hour ->
                            val slot = formatClock(LocalTime.of(hour, 0))
                            val description = stringResource(
                                R.string.timetable_cd_slot,
                                stringResource(weekdayFullRes(day.dayOfWeek.value)),
                                day.dayOfMonth,
                                slot
                            )
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = columnDayWidth * index,
                                        y = columnHourHeight * (hour - hours.first)
                                    )
                                    .size(columnDayWidth, columnHourHeight)
                                    .clickable {
                                        onAddActivity(day.toString(), slot)
                                    }
                                    .semantics { contentDescription = description }
                            )
                        }
                    }
                    items.forEach { item ->
                        val lane = lanes[item.date]?.get(item.activityId) ?: return@forEach
                        val dayIndex = ChronoUnit.DAYS.between(days.first(), item.date).toInt()
                        if (dayIndex !in days.indices) {
                            return@forEach
                        }
                        TimetableBlock(
                            item = item,
                            lane = lane,
                            x = columnDayWidth * dayIndex,
                            y = minutesTop(item.startTime, hours.first, columnHourHeight),
                            laneSpan = columnDayWidth,
                            length = blockLength(item, columnHourHeight),
                            horizontal = false,
                            onClick = { onOpenActivity(item.activityId, item.date.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowsTimetable(
    days: List<LocalDate>,
    today: LocalDate,
    hours: IntRange,
    items: List<AgendaItem>,
    lanes: Map<LocalDate, Map<Long, LaneSpan>>,
    onOpenActivity: (Long, String) -> Unit,
    onAddActivity: (String, String) -> Unit
) {
    val horizontal = rememberScrollState()
    val vertical = rememberScrollState()
    val gridWidth = rowHourWidth * hours.count()
    val rowHeights = days.map { day ->
        val count = lanes[day]?.values?.maxOfOrNull { lane -> lane.laneCount } ?: 1
        rowLaneHeight * count + 12.dp
    }
    val gridHeight = rowHeights.fold(0.dp) { total, height -> total + height }
    Column(modifier = Modifier.fillMaxSize()) {
        Row {
            Spacer(modifier = Modifier.width(dayLabelWidth).height(dayHeaderHeight))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(dayHeaderHeight)
                    .clipToBounds()
            ) {
                Row(
                    modifier = Modifier
                        .width(gridWidth)
                        .graphicsLayer { translationX = -horizontal.value.toFloat() }
                ) {
                    hours.forEach { hour ->
                        Text(
                            text = formatClock(LocalTime.of(hour, 0)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .width(rowHourWidth)
                                .padding(start = 6.dp, top = 16.dp)
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.width(dayLabelWidth).clipToBounds()) {
                Column(modifier = Modifier.graphicsLayer { translationY = -vertical.value.toFloat() }) {
                    days.forEachIndexed { index, day ->
                        DayHeading(
                            day = day,
                            today = today,
                            modifier = Modifier
                                .width(dayLabelWidth)
                                .height(rowHeights[index])
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(vertical)
                    .horizontalScroll(horizontal)
            ) {
                Box(modifier = Modifier.size(gridWidth, gridHeight)) {
                    RowGridLines(width = gridWidth, height = gridHeight, hours = hours.count(), rowHeights = rowHeights)
                    var top = 0.dp
                    days.forEachIndexed { index, day ->
                        val rowHeight = rowHeights[index]
                        Box(
                            modifier = Modifier
                                .offset(y = top)
                                .size(gridWidth, rowHeight)
                                .background(
                                    if (day == today) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                    } else {
                                        Color.Transparent
                                    }
                                )
                        )
                        hours.forEach { hour ->
                            val slot = formatClock(LocalTime.of(hour, 0))
                            val description = stringResource(
                                R.string.timetable_cd_slot,
                                stringResource(weekdayFullRes(day.dayOfWeek.value)),
                                day.dayOfMonth,
                                slot
                            )
                            Box(
                                modifier = Modifier
                                    .offset(x = rowHourWidth * (hour - hours.first), y = top)
                                    .size(rowHourWidth, rowHeight)
                                    .clickable { onAddActivity(day.toString(), slot) }
                                    .semantics { contentDescription = description }
                            )
                        }
                        items.filter { it.date == day }.forEach { item ->
                            val lane = lanes[day]?.get(item.activityId) ?: return@forEach
                            TimetableBlock(
                                item = item,
                                lane = lane,
                                x = minutesTop(item.startTime, hours.first, rowHourWidth),
                                y = top,
                                laneSpan = rowHeight - 8.dp,
                                length = blockLength(item, rowHourWidth),
                                horizontal = true,
                                onClick = { onOpenActivity(item.activityId, item.date.toString()) }
                            )
                        }
                        top += rowHeight
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeading(day: LocalDate, today: LocalDate, modifier: Modifier = Modifier) {
    val color = if (day == today) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = modifier.padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(weekdayShortRes(day.dayOfWeek.value)),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}

@Composable
private fun TimetableBlock(
    item: AgendaItem,
    lane: LaneSpan,
    x: Dp,
    y: Dp,
    laneSpan: Dp,
    length: Dp,
    horizontal: Boolean,
    onClick: () -> Unit
) {
    val cancelled = item.status == ActivityStatus.CANCELLED
    val background = when {
        cancelled -> MaterialTheme.colorScheme.surfaceVariant
        item.colorArgb != null -> Color(item.colorArgb)
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val content = if (cancelled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        foregroundOn(background)
    }
    val slot = laneSpan / lane.laneCount
    val cross = slot * lane.lane
    val thickness = (slot - 6.dp).coerceAtLeast(28.dp)
    val offsetX = if (horizontal) x else x + cross + 3.dp
    val offsetY = if (horizontal) y + cross + 4.dp else y
    val blockWidth = if (horizontal) length else thickness
    val blockHeight = if (horizontal) thickness else length
    Column(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(width = blockWidth, height = blockHeight)
            .zIndex(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (cancelled) TextDecoration.LineThrough else null
        )
        if (blockHeight >= 40.dp && blockWidth >= 56.dp) {
            Text(
                text = formatClock(item.startTime),
                style = MaterialTheme.typography.labelSmall,
                color = content,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RowGridLines(
    width: Dp,
    height: Dp,
    hours: Int,
    rowHeights: List<Dp>
) {
    val line = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    Canvas(modifier = Modifier.size(width, height)) {
        val hourPx = rowHourWidth.toPx()
        for (column in 0..hours) {
            val x = hourPx * column
            drawLine(
                line,
                start = androidx.compose.ui.geometry.Offset(x, 0f),
                end = androidx.compose.ui.geometry.Offset(x, size.height)
            )
        }
        var y = 0f
        drawLine(
            line,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f)
        )
        rowHeights.forEach { row ->
            y += row.toPx()
            drawLine(
                line,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y)
            )
        }
    }
}

@Composable
private fun GridLines(
    width: Dp,
    height: Dp,
    columns: Int,
    rows: Int,
    cellWidth: Dp,
    cellHeight: Dp
) {
    val line = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    Canvas(modifier = Modifier.size(width, height)) {
        val columnPx = cellWidth.toPx()
        val rowPx = cellHeight.toPx()
        for (column in 0..columns) {
            val x = columnPx * column
            drawLine(line, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, size.height))
        }
        for (row in 0..rows) {
            val y = rowPx * row
            drawLine(line, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y))
        }
    }
}

private fun AgendaItem.toSpan(): TimedSpan {
    return TimedSpan(id = activityId, start = startTime, end = endTime)
}

private fun minutesTop(time: LocalTime, firstHour: Int, unit: Dp): Dp {
    val minutes = ChronoUnit.MINUTES.between(LocalTime.of(firstHour, 0), time).toInt().coerceAtLeast(0)
    return unit * (minutes / 60f)
}

private fun blockLength(item: AgendaItem, unit: Dp): Dp {
    val minutes = ChronoUnit.MINUTES.between(item.startTime, item.endTime).toInt().coerceAtLeast(0)
    return maxOf(unit * (minutes / 60f), 36.dp)
}

private fun weekRangeLabel(weekStart: LocalDate): String {
    val end = weekStart.plusDays(6)
    return "${weekRangeFormatter.format(weekStart)} – ${weekRangeFormatter.format(end)}"
}

private fun foregroundOn(background: Color): Color {
    val ink = Color(0xFF2A1430)
    val light = Color(0xFFF8F7FF)
    return if (contrast(light, background) >= contrast(ink, background)) light else ink
}

private fun contrast(first: Color, second: Color): Double {
    val lighter = maxOf(luminance(first), luminance(second))
    val darker = minOf(luminance(first), luminance(second))
    return (lighter + 0.05) / (darker + 0.05)
}

private fun luminance(color: Color): Double {
    fun channel(value: Float): Double {
        val encoded = value.toDouble()
        return if (encoded <= 0.04045) encoded / 12.92 else Math.pow((encoded + 0.055) / 1.055, 2.4)
    }
    return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
}
