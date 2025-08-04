package de.igor.gun.sleep.analyzer.ui.screens.tracking.model

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.igor.gun.sleep.analyzer.misc.AppSettings
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorScanner
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SensorViewModel @Inject constructor(
    private val sensorAPI: SensorAPI,
    private val sensorScanner: SensorScanner
) : ViewModel() {

    enum class ConnectionState {
        UNBOUND,
        BINDING,
        BOUND,
        STREAMING
    }

    private val _connectionState = mutableStateOf(ConnectionState.UNBOUND)
    val connectionState: State<ConnectionState> = _connectionState

    val sensorStateFlow get() = sensorAPI.sensorStateFlow
    val sensorConnectFlow get() = sensorAPI.sensorConnectionFlow
    val availableSensorsFlow get() = sensorScanner.availableSensorsFlow

    init {
        watchSensors()
    }

    fun automaticBindSensor(
        context: Context,
        completion: ((Boolean) -> Unit)? = null,
    ) {
        val sensorId = AppSettings(context).deviceId
        sensorAPI.bind(sensorId, completion = completion)
    }

    fun bindDefaultSensorIfServiceBound(
        context: Context,
        completion: ((Boolean) -> Unit)? = null,
    ) {
        if (ServiceViewModel.isServiceRunning(context)) {
            val sensorId = AppSettings(context).deviceId
            _connectionState.value = if (sensorAPI.isReadyToUse(sensorId)) ConnectionState.STREAMING else ConnectionState.UNBOUND
            completion?.invoke(true)
        } else {
            completion?.invoke(false)
        }
    }

    fun bindSensor(
        sensorId: String,
        forceUnbind: Boolean = true,
        completion: ((Boolean) -> Unit)? = null,
    ) = sensorAPI.bind(sensorId, forceUnbind, completion)

    fun unbindSensor() = sensorAPI.unbind()

    fun resetAvailableSensors() = sensorScanner.reset()
    fun startScanSensors() = sensorScanner.startScan()

    private fun watchSensors() {
        viewModelScope.launch {
            sensorAPI.sensorStateFlow.collect {
                when (it) {
                    is SensorAPI.SensorState.Connected -> _connectionState.value = ConnectionState.BOUND
                    is SensorAPI.SensorState.Disconnected -> _connectionState.value = ConnectionState.UNBOUND
                    is SensorAPI.SensorState.Connecting -> _connectionState.value = ConnectionState.BINDING
                    is SensorAPI.SensorState.Streaming -> _connectionState.value = ConnectionState.STREAMING
                    else -> {}
                }
            }
        }
    }
}