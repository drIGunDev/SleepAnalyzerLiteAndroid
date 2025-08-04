package de.igor.gun.sleep.analyzer.services.sensors.polar

import android.os.Handler
import android.os.Looper
import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHealthThermometerData
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID


fun PolarDeviceInfo.toSensorInfo() =
    SensorAPI.SensorInfo(this.deviceId, this.address, this.rssi, this.name, this.isConnectable)

class PolarAPIImpl(private val bleAPI: PolarBleApi) : SensorAPI {

    override val apiImpl = bleAPI

    override val batteryFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    override val rssiFlow = MutableStateFlow(-200)
    override val sensorStateFlow = MutableStateFlow<SensorAPI.SensorState>(SensorAPI.SensorState.Undefined)
    override val sensorConnectionFlow = MutableStateFlow<String?>(null)
    override val streamingStateFlow = MutableStateFlow<SensorAPI.StreamingState>(SensorAPI.StreamingState.Stopped)

    private val polarBleApiCallback = object : PolarBleApiCallback() {
        override fun blePowerStateChanged(powered: Boolean) {
            Timber.d("BLE power: $powered")
            sensorStateFlow.value = SensorAPI.SensorState.Powered(powered)
        }

        override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
            Timber.d("CONNECTED: ${polarDeviceInfo.deviceId}")
            sensorConnectionFlow.value = polarDeviceInfo.deviceId
            sensorStateFlow.value = SensorAPI.SensorState.Connected(polarDeviceInfo.toSensorInfo())
        }

        override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
            Timber.d("CONNECTING: ${polarDeviceInfo.deviceId}")
            sensorStateFlow.value = SensorAPI.SensorState.Connecting(polarDeviceInfo.toSensorInfo())
        }

        override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
            Timber.d("DISCONNECTED: ${polarDeviceInfo.deviceId}")
            sensorConnectionFlow.value = null
            streamingStateFlow.value = SensorAPI.StreamingState.Stopped
            sensorStateFlow.value = SensorAPI.SensorState.Disconnected(polarDeviceInfo.toSensorInfo())
        }

        override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
            Timber.d("Polar BLE SDK feature $feature is ready")
            if (feature == PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING) {
                sensorStateFlow.value = SensorAPI.SensorState.Streaming(identifier)
                streamingStateFlow.value = SensorAPI.StreamingState.Started(identifier)
            }
        }

        override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
            Timber.d("DIS INFO uuid: $uuid value: $value")
        }

        override fun htsNotificationReceived(identifier: String, data: PolarHealthThermometerData) {
            Timber.d("htsNotificationReceived: $identifier - $data")
        }

        override fun disInformationReceived(identifier: String, disInfo: DisInfo) {
            Timber.d("disInformationReceived: $identifier - $disInfo")
        }

        override fun batteryLevelReceived(identifier: String, level: Int) {
            Timber.d("BATTERY LEVEL: $level")
            batteryFlow.value = level
            sensorStateFlow.value = SensorAPI.SensorState.BatteryLevelReceived(identifier, level)
        }
    }

    init {
        Timber.w("---> init PolarAPI")

        bleAPI.setPolarFilter(false)
        val enableSdkLogs = false
        if (enableSdkLogs) {
            bleAPI.setApiLogger { s: String -> Timber.d("PolarSDK--->+>$s") }
        }

        setApiCallback()
        startHRFlow()
    }

    override fun isReadyToUse(sensorId: String): Boolean =
        bleAPI.isFeatureReady(sensorId, PolarBleApi.PolarBleSdkFeature.FEATURE_HR) &&
                bleAPI.isFeatureReady(sensorId, PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING)

    override fun setApiCallback() {
        Timber.d("Setting PolarBleApiCallback")
        bleAPI.setApiCallback(polarBleApiCallback)
    }

    override fun resetFlows() {
        batteryFlow.value = 0
        rssiFlow.value = -200
        streamingStateFlow.value = SensorAPI.StreamingState.Stopped
    }

    private fun startHRFlow() {
        CoroutineScope(Dispatchers.Default).launch {
            sensorConnectionFlow.collect {
                it?.let { sensorId ->
                    startHRBroadcastFlow(sensorId)
                }
            }
        }
    }

    private var hrBroadcast: Disposable? = null
    private fun startHRBroadcastFlow(sensorId: String?) {
        rssiFlow.value = -200
        sensorId?.let {
            Timber.w("--->>HR broadcast wont to be started with sensorId = $sensorId")
            hrBroadcast?.dispose()
            hrBroadcast = null

            hrBroadcast = bleAPI.startListenForPolarHrBroadcasts(setOf(sensorId))
                .subscribe(
                    {
                        val rssi = it.polarDeviceInfo.rssi
                        rssiFlow.value = rssi
                    },
                    { error: Throwable -> Timber.e("--->>HR broadcast failed. Reason $error") },
                    { Timber.w("--->>HR broadcast complete") }
                )
        }
            ?: run {
                hrBroadcast?.dispose()
                hrBroadcast = null
                Timber.w("--->>HR broadcast disposed")
            }
    }

    override fun bind(
        sensorId: String,
        forceUnbind: Boolean,
        completion: ((Boolean) -> Unit)?,
    ) {
        fun disconnectAndConnectAgain(completion: ((Boolean) -> Unit)?) {
            Timber.w("--->>DISCONNECTING: $sensorId")
            bleAPI.disconnectFromDevice(sensorId)
            sensorConnectionFlow.value = null
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    Timber.w("--->>CONNECTING: $sensorId")
                    bleAPI.connectToDevice(sensorId)
                    completion?.invoke(true)
                },
                BINDING_DELAY
            )
        }

        try {
            if (forceUnbind) {
                disconnectAndConnectAgain(completion)
            } else {
                if (!isReadyToUse(sensorId)) {
                    disconnectAndConnectAgain(completion)
                } else {
                    completion?.invoke(true)
                }
            }
        } catch (error: Exception) {
            Timber.e("connection to sensor $sensorId failed. Reason $error")
            completion?.invoke(false)
        }
    }

    override fun unbind(completion: (() -> Unit)?) {
        sensorConnectionFlow.value?.let { sensorId ->
            Timber.w("--->>DISCONNECTING: $sensorId")
            bleAPI.disconnectFromDevice(sensorId)
            sensorConnectionFlow.value = null
            completion?.invoke()
            sensorId
        }
            ?: run { completion?.invoke() }
    }

    override fun unbindAndShutDown(completion: (() -> Unit)?) = unbind { shutDown(); completion?.invoke() }

    private fun shutDown() = bleAPI.shutDown()

    companion object {
        const val BINDING_DELAY = 1000L
    }
}