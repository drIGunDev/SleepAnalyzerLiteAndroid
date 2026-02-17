package de.igor.gun.sleep.analyzer.math.bridges

// Bridge: HCSleepPhase <-> SleepPhase (app-level type)
// Uncomment when SleepPhase and SleepState are available from the app module.

import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.HCSleepPhase
import de.igor.gun.sleep.analyzer.misc.toLocalDateTime
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.SleepSegment


fun List<HCSleepPhase>.hcSleepPhase2Segments(startTimeSeconds: Long): List<SleepSegment> {
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
        val segment = createSegment(currentTime, this[i].durationSeconds, this[i].state.toSleepState())
        results.add(segment)
        currentTime += this[i].durationSeconds.toLong() * 1000
    }

    return results
}