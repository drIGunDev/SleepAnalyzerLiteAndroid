package de.igor.gun.sleep.analyzer.repositories.tools

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.misc.daysRelativeMillis
import de.igor.gun.sleep.analyzer.misc.toLocalDateTime
import de.igor.gun.sleep.analyzer.misc.toMillis
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.Channel.CalendarItem
import de.igor.gun.sleep.analyzer.repositories.tools.CalendarChartHolder.PeriodType.Companion.toEntryCount
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit


class CalendarChartHolder {
    class Channel {
        var world: Region = Region(0f, 0f, 0f, 0f)
        var screen: Region = Region(0f, 0f, 0f, 0f)
        var window: Window = Window(Offset(0f, 0f), Offset(0f, 0f))
        var points: List<CalendarItem> = listOf()

        data class Region(val xMin: Float, val xMax: Float, val yMin: Float, val yMax: Float)
        data class Window(val size: Offset, var position: Offset, var paddingTop: Float = 0f, var paddingBottom: Float = 0f)

        sealed class CalendarItem(val time: LocalDateTime, val x: Float) {
            class Bar(time: LocalDateTime, x: Float, val min: Float, val max: Float, val color: Color) : CalendarItem(time, x)
            class Graph(time: LocalDateTime, x: Float, val points: List<Pair<Color, Float>>) : CalendarItem(time, x)
        }

        class DateNetEntity(val x: Float, val deltaX: Float, val date: LocalDateTime)

        fun setWorld(world: Region) = run {
            this.world = Region(
                world.xMin - 2 * DAY_IN_MILLIS,
                world.xMax + 2 * DAY_IN_MILLIS,
                world.yMin,
                world.yMax
            )
            this
        }

        fun setScreen(screen: Region) = run { this.screen = screen; this }

        fun setWindow(window: Window, force: Boolean = true) = run {
            val tmpXPosition = this.window.position.x
            this.window = Window(
                Offset(window.size.x, window.size.y - (window.paddingTop + window.paddingBottom)),
                window.position,
                window.paddingTop,
                window.paddingBottom
            )
            if (!force) this.window.position = Offset(tmpXPosition, this.window.position.y)
            this
        }

        fun setPoints(points: List<CalendarItem>) = run { this.points = points; this }

        val windowXMin: Float get() = screen.xMin
        val windowXMax: Float get() = screen.xMax
        val windowYMin: Float get() = screen.yMin
        val windowYMax: Float get() = windowYMin + window.size.y

        fun transformX(x: Float): Float {
            return (x - world.xMin) * (windowXMax - windowXMin) / (world.xMax - world.xMin) - window.position.x
        }

        fun transformY(y: Float): Float =
            windowYMax - (y - world.yMin) * (windowYMax - windowYMin) / (world.yMax - world.yMin) + window.position.y + window.paddingTop
    }

    init {
        Timber.w("created CalendarChartHolder")
    }

    enum class PeriodType(val value: String) {
        DAY("Day"),
        WEEK("Week"),
        MONTH("Month");

        companion object {
            fun toPeriodType(index: Int): PeriodType = when (index) {
                0 -> DAY
                1 -> WEEK
                2 -> MONTH
                else -> throw IllegalArgumentException("Invalid period type")
            }

            fun toEntryCount(periodType: PeriodType): Int = when (periodType) {
                DAY -> 5
                WEEK -> 8
                MONTH -> 15
            }

            @Composable
            fun toStringResource(periodType: PeriodType): String = when (periodType) {
                DAY -> stringResource(R.string.report_day)
                WEEK -> stringResource(R.string.report_week)
                MONTH -> stringResource(R.string.report_month)
            }
        }
    }

    var yAxisFormatter: (Float) -> String = { "" }

    val channel = Channel()

    private var periodType: PeriodType = PeriodType.DAY

    private fun computeScreenWidth(windowWidth: Float): Float {
        val width = channel.points.size * windowWidth / toEntryCount(periodType)
        return if (width < windowWidth) windowWidth else width
    }

    fun resetScreenWidth(optionalWidth: Float? = null, force: Boolean = true) {
        val width: Float = optionalWidth ?: channel.window.size.x
        val screenWidth = computeScreenWidth(width)
        channel.setScreen(Channel.Region(0f, screenWidth, 0f, 0f))
        val tmpXPosition = channel.window.position.x
        if (channel.screen.xMax < channel.window.size.x) {
            channel.window.position = Offset(if (force) 0f else tmpXPosition, channel.window.position.y)
        } else {
            channel.window.position = Offset(if (force) channel.screen.xMax - channel.window.size.x else tmpXPosition, channel.window.position.y)
        }
    }

    fun setPeriodOfType(periodType: PeriodType) {
        this.periodType = periodType
        if (channel.points.isEmpty()) return
        resetScreenWidth()
    }

    fun setData(points: List<CalendarItem>) =
        synchronized(channel) {
            channel.points = points
        }

    fun onScrollGesture(distanceX: Float) {
        if (distanceX == 0f) return

        val scrollLimitLeft = channel.windowXMin
        val scrollLimitRight = channel.windowXMax - channel.window.size.x

        if (channel.window.position.x + distanceX < scrollLimitLeft) return
        if (channel.window.position.x + distanceX > scrollLimitRight) return

        channel.window.position = Offset(channel.window.position.x + distanceX, channel.window.position.y)
    }

    fun buildCalendarDates(): List<Channel.DateNetEntity> {
        fun getX(daysAgoOrAfter: Int): Pair<Float, LocalDateTime> {
            val date = LocalDateTime
                .of(LocalDate.now(), LocalTime.MIDNIGHT)
                .daysRelativeMillis(daysAgoOrAfter)
                .toLocalDateTime()
            val x = channel.transformX(date.toMillis().toFloat())
            return Pair(x, date)
        }

        if (channel.points.isEmpty()) return listOf()

        val days = channel
            .points
            .minBy { it.time }
            .time
            .until(LocalDateTime.now(), ChronoUnit.DAYS)
            .toInt() + 2
        val results = mutableListOf<Channel.DateNetEntity>()
        for (i in 0..<days + 1) {
            val x1 = getX(i + 1 - days)
            val x0 = getX(i - days)
            results.add(Channel.DateNetEntity(x0.first, x1.first - x0.first, x0.second))
        }
        return results
    }

    fun buildData(): List<CalendarItem> =
        synchronized(channel.points) {
            val results = channel
                .points
                .map { point ->
                    when (point) {
                        is CalendarItem.Bar -> {
                            val x = channel.transformX(point.x)
                            val minY = channel.transformY(point.min)
                            val maxY = channel.transformY(point.max)
                            CalendarItem.Bar(point.time, x, minY, maxY, point.color)
                        }

                        is CalendarItem.Graph -> {
                            val x = channel.transformX(point.x)
                            val points = point.points.map {
                                Pair(it.first, channel.transformY(it.second))
                            }
                            CalendarItem.Graph(point.time, x, points)
                        }
                    }
                }
                .toMutableList()
            results
        }

    companion object {
        const val BAR_WIDTH = 20f
        const val POINT_RADIUS = 10f
        const val CLICK_DEAD_TIME = 500
        private const val HOUR_IN_MILLIS = 60 * 60 * 1000L
        const val DAY_IN_MILLIS = 24 * HOUR_IN_MILLIS
        const val YEAR_FORMAT = "yyyy MMMM"
        const val DATE_FORMAT = "d"
        const val NET_GAP = 10f
        const val BORDER_WIDTH = 2f
        const val LINE_WIDTH = 2f

        fun getTextLayout(text: String, textMeasurer: TextMeasurer): TextLayoutResult {
            return textMeasurer.measure(
                text = text,
                style = TextStyle(color = MainWhiteColor, fontSize = 10.sp, fontFamily = FontFamily.Serif)
            )
        }
    }
}
