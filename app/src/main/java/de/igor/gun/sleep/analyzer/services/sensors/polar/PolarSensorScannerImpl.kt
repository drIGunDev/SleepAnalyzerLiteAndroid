package de.igor.gun.sleep.analyzer.services.sensors.polar

import com.polar.sdk.api.PolarBleApi
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorScanner
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject

class PolarSensorScannerImpl @Inject constructor(private val bleAPI: PolarBleApi) : SensorScanner {

    override val availableSensorsFlow = MutableStateFlow<List<SensorAPI.SensorInfo>>(emptyList())

    private var searchDisposable: Disposable? = null

    override fun startScan() {

        class SensorList() {
            private val _devices: ArrayList<SensorAPI.SensorInfo> = arrayListOf()

            @Suppress("UNCHECKED_CAST")
            val devices: ArrayList<SensorAPI.SensorInfo>
                get() = synchronized(_devices) {
                    return _devices.clone() as ArrayList<SensorAPI.SensorInfo>
                }

            fun addIfUnique(sensorInfo: SensorAPI.SensorInfo) {
                synchronized(_devices) {
                    val same = _devices.find { it.deviceId == sensorInfo.deviceId }
                    if (same == null) {
                        _devices.add(sensorInfo)
                    }
                }
            }
        }

        reset()

        searchDisposable?.dispose()
        searchDisposable = null

        val sensorList = SensorList()

        searchDisposable = bleAPI.searchForDevice().subscribe(
            { sensor ->
                Timber.d("--->found sensor: id = ${sensor.deviceId}, name = ${sensor.name}, address = ${sensor.address}, rssi = ${sensor.rssi}, connectable = ${sensor.isConnectable}")
                sensorList.addIfUnique(sensor.toSensorInfo())
                availableSensorsFlow.value = sensorList.devices
            },
            { error: Throwable -> Timber.e("searchForDevice failed. Reason $error") },
            { Timber.w("searchForDevice complete") }
        )
    }

    override fun reset() {
        availableSensorsFlow.value = emptyList()
    }
}