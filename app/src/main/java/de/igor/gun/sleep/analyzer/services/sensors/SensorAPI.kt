package de.igor.gun.sleep.analyzer.services.sensors

import kotlinx.coroutines.flow.StateFlow

interface SensorAPI {
    data class SensorInfo(
        val deviceId: String,
        val address: String,
        val rssi: Int,
        val name: String,
        val isConnectable: Boolean
    )

    sealed interface SensorState {
        data class Connecting(val sensorInfo: SensorInfo) : SensorState
        data class Connected(val sensorInfo: SensorInfo) : SensorState
        data class Disconnected(val sensorInfo: SensorInfo) : SensorState
        data class Powered(val powered: Boolean) : SensorState
        data class Streaming(val identifier: String) : SensorState
        data class BatteryLevelReceived(val identifier: String, val level: Int) : SensorState
        data object Undefined : SensorState
    }

    sealed interface StreamingState {
        data class Started(val sensorId: String) : StreamingState
        data object Stopped : StreamingState
    }

    val apiImpl: Any

    val batteryFlow: StateFlow<Int>
    val rssiFlow: StateFlow<Int>
    val sensorConnectionFlow: StateFlow<String?>
    val sensorStateFlow: StateFlow<SensorState>
    val streamingStateFlow: StateFlow<StreamingState>

    fun setApiCallback()
    fun bind(sensorId: String, forceUnbind: Boolean = true, completion: ((Boolean) -> Unit)? = null)
    fun unbind(completion: (() -> Unit)? = null)
    fun unbindAndShutDown(completion: (() -> Unit)? = null)
    fun resetFlows()
    fun isReadyToUse(sensorId: String): Boolean
}