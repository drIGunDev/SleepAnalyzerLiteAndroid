package de.igor.gun.sleep.analyzer.ui.tools.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Move
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Press
import androidx.compose.ui.input.pointer.PointerEventType.Companion.Release
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import de.igor.gun.sleep.analyzer.misc.formatToString
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Channel
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Channel.CalendarItem
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.BAR_WIDTH
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.BORDER_WIDTH
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.CLICK_DEAD_TIME
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.DATE_FORMAT
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.LINE_WIDTH
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.NET_GAP
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.POINT_RADIUS
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.YEAR_FORMAT
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Companion.getTextLayout
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.CalendarBorderColor
import de.igor.gun.sleep.analyzer.ui.theme.CalendarDayColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import java.time.LocalDateTime
import kotlin.reflect.KClass


@Composable
fun CalendarChart(
    modifier: Modifier = Modifier,
    chartHolders: List<CalendarChartHolder>,
    periodTypes: List<CalendarChartHolder.PeriodType>,
    visibleLines: List<Boolean>,
    activeHolderIndex: MutableState<Int>,
    invalidate: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    context: @Composable (Modifier, MutableState<Int>) -> Unit = { _, _ -> },
) {
    if (chartHolders.isEmpty()) return

    var selectedIndex by rememberSaveable { mutableIntStateOf(2) }
    var position by rememberSaveable { mutableFloatStateOf(0f) }
    var lastPressTime by rememberSaveable { mutableLongStateOf(0L) }
    var isPressed by rememberSaveable { mutableStateOf(false) }
    val textMeasurer = rememberTextMeasurer()
    var forceReset by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier.then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SingleChoiceSegmentedButtonRow {
            periodTypes.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = periodTypes.size
                    ),
                    onClick = {
                        selectedIndex = index
                        val periodType = CalendarChartHolder.PeriodType.toPeriodType(selectedIndex)
                        chartHolders.forEach { it.setPeriodOfType(periodType) }
                        invalidate.value = true
                    },
                    selected = index == selectedIndex,
                    label = {
                        val labelText = CalendarChartHolder.PeriodType.toStringResource(label)
                        Text(labelText)
                    }
                )
            }
        }

        context(modifier, activeHolderIndex)

        Canvas(
            modifier = Modifier
                .then(modifier)
                .fillMaxSize()
                .onSizeChanged {
                    if (chartHolders.any { it.channel.points.isEmpty() }) return@onSizeChanged
                    chartHolders.forEach { holder ->
                        holder.resetScreenWidth(it.width.toFloat(), force = forceReset)
                        val yearLayout = getTextLayout(LocalDateTime.now().formatToString(YEAR_FORMAT), textMeasurer)
                        holder.channel.setWindow(
                            Channel.Window(
                                Offset(it.width.toFloat(), it.height.toFloat()),
                                Offset(holder.channel.screen.xMax - holder.channel.window.size.x, 0f),
                                paddingTop = 0f,//yearLayout.size.height.toFloat() * 2,
                                paddingBottom = yearLayout.size.height.toFloat() * 4 + NET_GAP
                            ),
                            force = forceReset
                        )
                        holder.resetScreenWidth(force = forceReset)
                    }
                    forceReset = false
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                Release -> {
                                    isPressed = false
                                    invalidate.value = true
                                }

                                Press -> {
                                    if (System.currentTimeMillis() - lastPressTime < CLICK_DEAD_TIME) {
                                        invalidate.value = true
                                        continue
                                    }
                                    lastPressTime = System.currentTimeMillis()
                                    position = event.changes.first().position.x
                                    isPressed = true
                                }

                                Move -> {
                                    if (event.changes.size == 1 && isPressed) {
                                        val newPosition = event.changes.first().position.x
                                        val dragAmount = position - newPosition
                                        position = newPosition
                                        chartHolders.forEach { it.onScrollGesture(dragAmount) }
                                        invalidate.value = true
                                    }
                                }
                            }
                        }
                    }
                }
        ) {
            clipRect(0f, 0f, size.width, size.height) {
                if (invalidate.value) invalidate.value = false
                chartHolders.forEachIndexed { index, holder ->
                    if (index == activeHolderIndex.value) {
                        drawXScale(holder, textMeasurer)
                        drawData(holder, listOf(CalendarItem.Bar::class), visibleLines)
                        drawData(holder, listOf(CalendarItem.Graph::class), visibleLines)
                        drawBorder()
                        drawYScale(holder, textMeasurer)
                    }
                }
            }
        }
    }
}

fun DrawScope.drawYScale(holder: CalendarChartHolder, textMeasurer: TextMeasurer) {
    val n = 4
    val d = (holder.channel.world.yMax - holder.channel.world.yMin) / n
    val d1 = (holder.channel.windowYMax - holder.channel.windowYMin) / n
    for (i in 0..n) {
        val y = holder.channel.windowYMin + i * d1
        drawLine(CalendarBorderColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
        val value = holder.channel.world.yMax - d * i
        val yearLayout = getTextLayout(holder.yAxisFormatter(value), textMeasurer)
        val yText = if (i == 0) {
            holder.channel.windowYMin + i * d1
        } else {
            holder.channel.windowYMin + i * d1 - yearLayout.size.height
        }
        drawText(
            textLayoutResult = yearLayout,
            color = MainWhiteColor,
            topLeft = Offset(
                x = 10f,
                y = yText
            ),
        )
    }
}

fun DrawScope.drawData(holder: CalendarChartHolder, visiblePointTypes: List<KClass<*>>, visibleLine: List<Boolean>) {
    val data = holder.buildData()
    if (CalendarItem.Graph::class in visiblePointTypes) drawLines(data, visibleLine)
    if (CalendarItem.Bar::class in visiblePointTypes) drawBars(data)
}

fun DrawScope.drawBars(data: List<CalendarItem>) {
    data.forEach { point ->
        when (point) {
            is CalendarItem.Bar -> {
                drawBar(point.x, point.min, point.max)
            }

            else -> {}
        }
    }
}

fun DrawScope.drawLines(data: List<CalendarItem>, visibleLine: List<Boolean>) {
    for (i in data.indices) {
        val point = data[i]
        val nextPoint = data.getOrNull(i + 1)
        when (point) {
            is CalendarItem.Graph -> {
                val nextPoint1 = nextPoint as? CalendarItem.Graph
                point.points.forEachIndexed { j, it ->
                    if (!visibleLine[j]) return@forEachIndexed
                    drawPoint(Offset(point.x, it.second), point.points[j].first)
                    if (nextPoint1 != null) {
                        drawLine(
                            point.points[j].first,
                            Offset(point.x, it.second),
                            Offset(nextPoint.x, nextPoint1.points[j].second),
                            strokeWidth = LINE_WIDTH,
                        )
                    }
                }
            }

            else -> {}
        }
    }
}

fun DrawScope.drawBar(x: Float, minY: Float, maxY: Float) {
    drawLine(
        AWAKEColor,
        Offset(x, minY),
        Offset(x, maxY),
        strokeWidth = BAR_WIDTH,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )
}

fun DrawScope.drawPoint(point: Offset, color: Color) {
    drawCircle(
        color = color,
        radius = POINT_RADIUS,
        center = point
    )
}

fun DrawScope.drawXScale(
    holder: CalendarChartHolder,
    textMeasurer: TextMeasurer,
) {
    val net = holder.buildCalendarDates()
    var showYear = true
    net.forEach { point ->
        val time = point.date.formatToString(DATE_FORMAT)
        val dayLayout = getTextLayout(time, textMeasurer)
        drawText(
            textLayoutResult = dayLayout,
            color = MainWhiteColor,
            topLeft = Offset(
                x = point.x + point.deltaX / 2 - dayLayout.size.width / 2,
                y = size.height - dayLayout.size.height * 4
            ),
        )
        if (point.x > holder.channel.windowXMin && point.x < holder.channel.windowXMax && showYear) {
            showYear = false
            val yearLayout = getTextLayout(point.date.formatToString(YEAR_FORMAT), textMeasurer)
            drawText(
                textLayoutResult = yearLayout,
                color = MainWhiteColor,
                topLeft = Offset(
                    x = size.width / 2 - yearLayout.size.width / 2,
                    y = size.height - yearLayout.size.height * 2
                ),
            )
        }
    }

    val yearLayout = getTextLayout(LocalDateTime.now().formatToString(YEAR_FORMAT), textMeasurer)
    drawDateNet(net, yearLayout.size.height.toFloat() * 4 + NET_GAP)
}

fun DrawScope.drawDateNet(net: List<Channel.DateNetEntity>, yGap: Float) {
    net.forEach {
        drawDayBoundary(it.x, it.deltaX, yGap)
    }
}

fun DrawScope.drawDayBoundary(
    x: Float,
    deltaX: Float,
    yGap: Float,
) {
    drawRect(
        color = CalendarDayColor,
        topLeft = Offset(x - deltaX, 0f),
        size = Size(deltaX * 2, size.height - yGap),
        style = Stroke(width = 4f)
    )
}

fun DrawScope.drawBorder() {
    drawRect(
        CalendarBorderColor,
        topLeft = Offset(1f, 1f),
        size = Size(size.width - 2f, size.height - 2f),
        style = Stroke(width = BORDER_WIDTH)
    )
}