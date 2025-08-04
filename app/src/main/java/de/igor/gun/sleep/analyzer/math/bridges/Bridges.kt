package de.igor.gun.sleep.analyzer.math.bridges

import de.igor.gun.sleep.analyzer.hypnogram.computation.HCSleepDataPoint
import de.igor.gun.sleep.analyzer.hypnogram.computation.HCSleepState
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder

fun HCSleepState.toSleepState(): HypnogramHolder.SleepState = when (this) {
    HCSleepState.AWAKE -> HypnogramHolder.SleepState.AWAKE
    HCSleepState.LIGHT_SLEEP -> HypnogramHolder.SleepState.LIGHT_SLEEP
    HCSleepState.DEEP_SLEEP -> HypnogramHolder.SleepState.DEEP_SLEEP
    HCSleepState.REM -> HypnogramHolder.SleepState.REM
}

fun HCSleepDataPoint.toSleepDataPoint(): HypnogramHolder.SleepDataPoint = HypnogramHolder.SleepDataPoint(time = time, state = state.toSleepState())

fun List<HCSleepDataPoint>.mapToSleepDataPoint(): List<HypnogramHolder.SleepDataPoint> = map { it.toSleepDataPoint() }
