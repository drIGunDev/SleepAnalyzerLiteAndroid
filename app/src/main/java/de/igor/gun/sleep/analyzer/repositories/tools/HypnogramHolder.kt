package de.igor.gun.sleep.analyzer.repositories.tools

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.igor.gun.sleep.analyzer.misc.toSeconds
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.DSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.LSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.REMColor
import java.time.LocalDateTime
import java.time.ZoneOffset


class HypnogramHolder {
    enum class SleepState(val value: Int, val level: Int) {
        AWAKE(0, 0),
        LIGHT_SLEEP(1, 2),
        DEEP_SLEEP(2, 3),
        REM(3, 1);

        companion object {
            fun findOrDefault(value: Int, default: SleepState = AWAKE): SleepState = entries.find { it.value == value } ?: default
            fun toColorMap(): Map<SleepState, Color> = mapOf(AWAKE to AWAKEColor, REM to REMColor, LIGHT_SLEEP to LSLEEPColor, DEEP_SLEEP to DSLEEPColor)
            fun toColorList(): List<Color> = listOf(AWAKEColor, REMColor, LSLEEPColor, DSLEEPColor)
        }
    }

    private var sleepSegments: List<SleepSegment> = listOf()
    private var screenWight: Float = 0f
    private var screenHeight: Float = 0f

    private var xFactor: Float = 0f
    private var yFactor: Float = 0f

    fun setSleepSegments(sleepSegments: List<SleepSegment>) = run { this.sleepSegments = sleepSegments; rescale() }
    fun isEmpty() = sleepSegments.isEmpty()
    fun setWidth(width: Float) = run { screenWight = width; rescale() }
    fun setHeight(height: Float) = run { screenHeight = height; rescale() }

    fun buildPath(): Path = buildList().convertToPath()

    fun buildBrush(): Brush =
        Brush.linearGradient(
            SleepState.toColorList(),
            start = Offset.Zero,
            end = Offset(0f, screenHeight)
        )

    fun buildSleepSegments(): List<SleepSegment> {
        if (!sleepSegments.isEmpty()) return sleepSegments
        return listOf(SleepSegment(LocalDateTime.now(ZoneOffset.UTC), 10f, SleepState.AWAKE))
    }

    private fun rescale() {
        if (sleepSegments.isEmpty()) return
        val duration = sleepSegments.last().time.toSeconds() - sleepSegments.first().time.toSeconds() + sleepSegments.last().durationSeconds
        xFactor = screenWight / duration
        val stateInterval = (SleepState.entries.size - 1) * VERTICAL_UNIT_SIZE
        yFactor = (screenHeight - VERTICAL_GAP) / stateInterval
    }

    private fun transformToX(time: Long): Float = (time.toFloat() - sleepSegments.first().time.toSeconds()) * xFactor
    private fun transformToY(stage: SleepState): Float = stage.level.toFloat() * yFactor * VERTICAL_UNIT_SIZE

    private fun buildList(): List<PointF> {
        val lines = buildLines()
        if (lines.isEmpty()) return listOf()
        val results = mutableListOf<PointF>()
        results.addAll(lines)
        results.addAll(shift(lines.reversed()))
        return results
    }

    private fun buildLines(): List<PointF> {
        val result = mutableListOf<PointF>()
        var statePrevious = sleepSegments.first().state
        for (i in 0..<sleepSegments.size) {
            if (i != 0) {
                result.add(PointF(transformToX(sleepSegments[i].time.toSeconds()), transformToY(statePrevious)))
            }
            result.add(PointF(transformToX(sleepSegments[i].time.toSeconds()), transformToY(sleepSegments[i].state)))
            statePrevious = sleepSegments[i].state
            if (i == sleepSegments.size - 1) {
                result.add(PointF(transformToX(sleepSegments[i].time.toSeconds() + sleepSegments[i].durationSeconds.toLong()), transformToY(statePrevious)))
            }
        }
        return result
    }

    companion object {
        const val VERTICAL_UNIT_SIZE = 10
        val INNER_GAP = 10.dp
        val FONT_SIZE = 20.sp
        val H_MARKER_SIZE = 2.dp
        val DIAL_THICKNESS = 1.dp
        val BAR_THICKNESS = 10.dp

        val colorMap = mapOf(
            SleepState.AWAKE to AWAKEColor,
            SleepState.LIGHT_SLEEP to LSLEEPColor,
            SleepState.DEEP_SLEEP to DSLEEPColor,
            SleepState.REM to REMColor,
        )

        private const val VERTICAL_GAP = 15
        private const val HORIZONTAL_GAP = 1

        private fun shift(points: List<PointF>): List<PointF> {
            val results = mutableListOf<PointF>()
            results.addAll(points.map { PointF(it.x, it.y + VERTICAL_GAP) })
            for (i in 1..<points.size - 1) {
                if (results[i].y < results[i + 1].y) {
                    results[i].x += HORIZONTAL_GAP
                } else {
                    if (results[i].y > results[i + 1].y) {
                        results[i].x -= HORIZONTAL_GAP
                    } else {
                        if (results[i - 1].x < results[i].x) {
                            results[i].x -= HORIZONTAL_GAP
                        } else {
                            results[i].x += HORIZONTAL_GAP
                        }
                    }
                }
            }
            return results
        }

        private fun List<PointF>.convertToPath(): Path {
            val path = Path()
            if (this.isEmpty()) return path
            path.moveTo(this.first().x, this.first().y)
            for (i in 1..<this.size) {
                path.lineTo(this[i].x, this[i].y)
            }
            path.close()
            return path
        }
    }
}