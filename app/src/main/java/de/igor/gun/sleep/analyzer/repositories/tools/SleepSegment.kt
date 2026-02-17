package de.igor.gun.sleep.analyzer.repositories.tools

import de.igor.gun.sleep.analyzer.misc.toLocalDateTime
import java.time.LocalDateTime

data class SleepSegment(
    val time: LocalDateTime,
    val durationSeconds: Float,
    val state: HypnogramHolder.SleepState
) {
    constructor(time: Long, duration: Long, state: HypnogramHolder.SleepState) :
            this(time.toLocalDateTime(), duration.toFloat(), state)
}