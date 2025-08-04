package de.igor.gun.sleep.analyzer.repositories.tools

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.igor.gun.sleep.analyzer.misc.toLocalDateTime
import de.igor.gun.sleep.analyzer.misc.toMillis
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder.Segment
import de.igor.gun.sleep.analyzer.ui.theme.AWAKEColor
import de.igor.gun.sleep.analyzer.ui.theme.DSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.LSLEEPColor
import de.igor.gun.sleep.analyzer.ui.theme.REMColor
import java.time.LocalDateTime
import kotlin.time.DurationUnit
import kotlin.time.toDuration


fun List<HypnogramHolder.SleepDataPoint>.toSegments(
    endTime: LocalDateTime? = null,
    startTime: LocalDateTime? = null,
): List<Segment> {
    fun Long.toStartAngle(): Float = toDuration(DurationUnit.MILLISECONDS).inWholeMinutes * 0.5f - 90f
    fun Long.toDuration(startTime: Long): Float = (this.toStartAngle() - startTime.toStartAngle()) * 2f * 60 * 1000

    fun createSegment(previousTime: Long, time: Long, previousType: HypnogramHolder.SleepState): Segment =
        Segment(
            time = previousTime.toLocalDateTime(),
            startAngle = previousTime.toStartAngle(),
            duration = time.toDuration(previousTime),
            state = previousType
        )

    val results = mutableListOf<Segment>()

    if (this.isEmpty()) {
        val endTimeMillis = endTime?.toMillis() ?: LocalDateTime.now().toMillis()
        val segment = createSegment(endTimeMillis, endTimeMillis, HypnogramHolder.SleepState.AWAKE)
        results.add(segment)
        return results
    }

    val startTimeMillis = startTime?.toMillis() ?: 0
    var previousTime = this.first().time + startTimeMillis
    var previousType = this.first().state
    for (i in 1..<this.size) {
        if (this[i].state != previousType) {
            val segment = createSegment(previousTime, this[i].time + startTimeMillis, previousType)
            results.add(segment)
            previousType = this[i].state
            previousTime = this[i].time + startTimeMillis
        }
    }

    val endTimeMillis = endTime?.toMillis() ?: (this.last().time + startTimeMillis)
    if (results.isEmpty()) {
        results.add(createSegment(previousTime, endTimeMillis, previousType))
        return results
    }

    if (previousTime < endTimeMillis) {
        val segment = createSegment(previousTime, endTimeMillis, previousType)
        results.add(segment)
    }
    return results
}

fun List<Segment>.computeDistribution(): HypnogramHolder.SleepStateDistribution {
    val results = mutableMapOf<HypnogramHolder.SleepState, Float>()
    results.putAll(HypnogramHolder.SleepState.entries.map { Pair(it, 0f) })
    this.forEach {
        results[it.state] = (results[it.state] ?: 0f) + it.duration
    }
    return HypnogramHolder.SleepStateDistribution(results)
}

class SleepPhasesHolder {

    private var sleepDataPoints = mutableListOf<HypnogramHolder.SleepDataPoint>()

    private var startTimeMillis: Long = 0
    private var endTimeMillis: Long = 0

    fun setSleepDataPoints(
        sleepDataPoints: List<HypnogramHolder.SleepDataPoint>,
        endTime: LocalDateTime,
        startTime: LocalDateTime? = null,
    ) {
        this.endTimeMillis = endTime.toMillis()
        this.startTimeMillis = startTime?.toMillis() ?: 0
        this.sleepDataPoints.clear()
        this.sleepDataPoints.addAll(sleepDataPoints)
    }

    data class Segment(val time: LocalDateTime, val duration: Float, val state: HypnogramHolder.SleepState, val startAngle: Float)

    fun buildSegments(): List<Segment> = sleepDataPoints.toSegments(endTimeMillis.toLocalDateTime(), startTimeMillis.toLocalDateTime())

    companion object {
        val INNER_GAP = 10.dp
        val FONT_SIZE = 20.sp
        val H_MARKER_SIZE = 2.dp
        val DIAL_THICKNESS = 1.dp
        val BAR_THICKNESS = 10.dp
        val colorMap = mapOf(
            HypnogramHolder.SleepState.AWAKE to AWAKEColor,
            HypnogramHolder.SleepState.LIGHT_SLEEP to LSLEEPColor,
            HypnogramHolder.SleepState.DEEP_SLEEP to DSLEEPColor,
            HypnogramHolder.SleepState.REM to REMColor,
        )
    }
}