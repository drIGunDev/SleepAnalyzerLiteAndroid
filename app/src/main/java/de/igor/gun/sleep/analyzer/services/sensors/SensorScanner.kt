package de.igor.gun.sleep.analyzer.services.sensors

import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI.SensorInfo
import kotlinx.coroutines.flow.StateFlow

interface SensorScanner {
    val availableSensorsFlow: StateFlow<List<SensorInfo>>

    fun startScan()
    fun reset()
}