package de.igor.gun.sleep.analyzer.repositories.tools

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.DSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.LSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.REMColor


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

    data class SleepStateDistribution(val absolutMillis: Map<SleepState, Float>) {
        fun relative(): Map<SleepState, Float> {
            val total = absolutMillis.values.sum()
            return absolutMillis.mapValues { if (total == 0f) 0f else it.value / total }
        }
        val isValid: Boolean get() = absolutMillis.values.sum() > 0
        fun ifValid(block:SleepStateDistribution.()->Unit) {
            if (isValid) block()
        }
    }

    data class SleepDataPoint(val time: Long, val state: SleepState)

    var sleepDataPoints: List<SleepDataPoint> = listOf()
    private var screenWight: Float = 0f
    private var screenHeight: Float = 0f

    private var xFactor: Float = 0f
    private var yFactor: Float = 0f

    fun setUniformSleepDataPoints(sleepDataPoints: List<SleepDataPoint>) = run { this.sleepDataPoints = sleepDataPoints; rescale() }
    fun setWidth(width: Float) = run { screenWight = width; rescale() }
    fun setHeight(height: Float) = run { screenHeight = height; rescale() }

    private fun List<SleepDataPoint>.duration(): Long = if (isEmpty()) 0L else last().time - first().time

    private fun rescale() {
        if (sleepDataPoints.isEmpty()) return
        val timeInterval = sleepDataPoints.last().time - sleepDataPoints.first().time
        xFactor = screenWight / timeInterval
        val stateInterval = (SleepState.entries.size - 1) * VERTICAL_UNIT_SIZE
        yFactor = (screenHeight - VERTICAL_GAP) / stateInterval
    }

    private fun transformToX(time: Long): Float = (time.toFloat() - sleepDataPoints.first().time) * xFactor
    private fun transformToY(stage: SleepState): Float = stage.level.toFloat() * yFactor * VERTICAL_UNIT_SIZE

    fun buildPath(): Path = buildList().convertToPath()

    fun buildBrush(): Brush =
        Brush.linearGradient(
            SleepState.toColorList(),
            start = Offset.Zero,
            end = Offset(0f, screenHeight)
        )

    fun buildSleepDataPoints(): List<SleepDataPoint> = sleepDataPoints

    private fun buildList(): List<PointF> {
        val results = mutableListOf<PointF>()
        results.addAll(buildLine())
        results.addAll(shift(buildLine().reversed()))
        return results
    }

    private fun buildLine(): List<PointF> = sleepDataPoints.map { PointF(transformToX(it.time), transformToY(it.state)) }

    companion object {
        const val VERTICAL_UNIT_SIZE = 10

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