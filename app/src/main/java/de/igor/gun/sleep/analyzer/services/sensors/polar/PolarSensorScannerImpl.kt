package de.igor.gun.sleep.analyzer.services.sensors.polar

import com.polar.sdk.api.PolarBleApi
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class PolarSensorScannerImpl @Inject constructor(private val bleAPI: PolarBleApi) : SensorScanner {

    override val availableSensorsFlow = MutableStateFlow<List<SensorAPI.SensorInfo>>(emptyList())

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var searchJob: Job? = null

    override fun startScan() {

        class SensorList() {
            private val _devices: ArrayList<SensorAPI.SensorInfo> = arrayListOf()

            @Suppress("UNCHECKED_CAST")
            val devices: ArrayList<SensorAPI.SensorInfo>
                get() = synchronized(_devices) {
                    return _devices.clone() as ArrayList<SensorAPI.SensorInfo>
                }

            fun addIfUnique(sensorInfo: SensorAPI.SensorInfo): Boolean {
                synchronized(_devices) {
                    val same = _devices.find { it.deviceId == sensorInfo.deviceId }
                    if (same == null) {
                        _devices.add(sensorInfo)
                        return true
                    }
                    return false
                }
            }
        }

        reset()

        searchJob?.cancel()
        searchJob = null

        val sensorList = SensorList()

        searchJob = bleAPI.searchForDevice(null)
            .onEach { sensor ->
                if (sensorList.addIfUnique(sensor.toSensorInfo())) {
                    Timber.d("--->found sensor: id = ${sensor.deviceId}, name = ${sensor.name}, address = ${sensor.address}, rssi = ${sensor.rssi}, connectable = ${sensor.isConnectable}")
                    availableSensorsFlow.value = sensorList.devices
                }
            }
            .catch { error: Throwable -> Timber.e("searchForDevice failed. Reason $error") }
            .onCompletion { Timber.w("searchForDevice complete") }
            .launchIn(scope)
    }

    override fun reset() {
        availableSensorsFlow.value = emptyList()
    }
}
