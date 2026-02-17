package de.igor.gun.sleep.analyzer.math.bridges

// Bridge: HCSleepState <-> SleepState (app-level type)
// Uncomment when SleepState is available from the app module.

import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.HCSleepState
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder


fun HCSleepState.toSleepState(): HypnogramHolder.SleepState = when (this) {
    HCSleepState.AWAKE -> HypnogramHolder.SleepState.AWAKE
    HCSleepState.LIGHT_SLEEP -> HypnogramHolder.SleepState.LIGHT_SLEEP
    HCSleepState.DEEP_SLEEP -> HypnogramHolder.SleepState.DEEP_SLEEP
    HCSleepState.REM -> HypnogramHolder.SleepState.REM
}