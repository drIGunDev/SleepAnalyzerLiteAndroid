package de.igor.gun.sleep.analyzer.services.sensors

import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

interface SensorDataSource {

    data class PPGSamples(val timeStamp: Long, val channelSamples: List<Int>)

    data class XYZ(val x: Double, val y: Double, val z: Double) {
        val rms = rms(x, y, z)

        private companion object {
            fun rms(x: Double, y: Double, z: Double): Double = run { sqrt((x * x + y * y + z * z) / 3.0) }
        }
    }

    val ppgFlow: StateFlow<List<PPGSamples>>
    val hrFlow: StateFlow<Int>
    val accFlow: StateFlow<XYZ>
    val gyroFlow: StateFlow<XYZ>

    fun resetFlows()
}