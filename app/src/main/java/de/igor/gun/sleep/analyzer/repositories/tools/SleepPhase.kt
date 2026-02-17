package de.igor.gun.sleep.analyzer.repositories.tools

import de.igor.gun.sleep.analyzer.misc.toLocalDateTime

data class SleepPhase(
    val state: HypnogramHolder.SleepState,
    val durationSeconds: Double
)

fun List<SleepPhase>.sleepPhases2Segments(startTimeSeconds: Long): List<SleepSegment> {
    fun createSegment(time: Long, duration: Double, state: HypnogramHolder.SleepState): SleepSegment {
        return SleepSegment(
            time = time.toLocalDateTime(),
            durationSeconds = duration.toFloat(),
            state = state
        )
    }

    val results = mutableListOf<SleepSegment>()

    if (this.isEmpty()) {
        return results
    }

    var currentTime = startTimeSeconds * 1000
    for (i in 0..<this.size) {
        val segment = createSegment(currentTime, this[i].durationSeconds, this[i].state)
        results.add(segment)
        currentTime += this[i].durationSeconds.toLong() * 1000
    }

    return results
}