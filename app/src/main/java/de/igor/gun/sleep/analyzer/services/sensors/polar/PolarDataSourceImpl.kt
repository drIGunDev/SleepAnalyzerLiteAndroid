package de.igor.gun.sleep.analyzer.services.sensors.polar

import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarGyroData
import com.polar.sdk.api.model.PolarPpgData
import com.polar.sdk.api.model.PolarSensorSetting
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds


fun PolarPpgData.PolarPpgSample.toPgpSample() = SensorDataSource.PPGSamples(this.timeStamp, this.channelSamples)
fun List<PolarPpgData.PolarPpgSample>.toPgpSample(): List<SensorDataSource.PPGSamples> = this.map { it.toPgpSample() }

@OptIn(FlowPreview::class)
class PolarDataSourceImpl(private val sensorAPI: SensorAPI) : SensorDataSource {

    private val polarAPI = sensorAPI.apiImpl as PolarBleApi

    override val ppgFlow = MutableStateFlow<List<SensorDataSource.PPGSamples>>(mutableListOf())
    override var hrFlow = MutableStateFlow(0)
    override val accFlow = MutableStateFlow(SensorDataSource.XYZ(0.0, 0.0, 0.0))
    override val gyroFlow = MutableStateFlow(SensorDataSource.XYZ(0.0, 0.0, 0.0))

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var hrJob: Job? = null
    private var accJob: Job? = null
    private var grJob: Job? = null
    private var ppgJob: Job? = null

    private companion object {
        const val THROTTLE_INTERVAL_MILLISECOND = 2000L
    }

    init {
        Timber.w("----> init SensorDataRetrieval")
        scope.launch {
            sensorAPI.streamingStateFlow.collect { streamingState ->
                when (streamingState) {
                    is SensorAPI.StreamingState.Started -> startStreamConsuming(streamingState.sensorId)
                    is SensorAPI.StreamingState.Stopped -> startStreamConsuming(null)
                }
            }
        }
    }

    override fun resetFlows() {
        sensorAPI.resetFlows()
        hrFlow.value = 0
        accFlow.value = SensorDataSource.XYZ(0.0, 0.0, 0.0)
        gyroFlow.value = SensorDataSource.XYZ(0.0, 0.0, 0.0)
    }

    private fun startStreamConsuming(sensorId: String?) {
        startHRFlow(sensorId)
        startPPGFlow(sensorId)
        startAccFlow(sensorId)
        startGyroFlow(sensorId)
    }

    private fun startHRFlow(sensorId: String?) {
        sensorId?.let {
            Timber.w("--->>HR wont to be started with sensorId = $sensorId")
            hrJob?.cancel()
            hrJob = null

            hrJob = polarAPI.startHrStreaming(sensorId)
                .sample(THROTTLE_INTERVAL_MILLISECOND.milliseconds)
                .onEach { polarHrData ->
                    polarHrData.samples.forEach { sample ->
                        hrFlow.value = sample.hr
                    }
                }
                .catch { error: Throwable -> Timber.e("--->>HR stream failed. Reason $error") }
                .onCompletion { Timber.w("--->>HR stream complete") }
                .launchIn(scope)
        }
            ?: run {
                hrJob?.cancel()
                hrJob = null
                Timber.w("--->>HR stream disposed")
            }
    }

    private fun startAccFlow(sensorId: String?) {
        sensorId?.let {
            Timber.w("--->>ACC wont to be started with sensorId = $sensorId")
            accJob?.cancel()
            accJob = null

            accJob = scope.launch {
                val settings = requestStreamSettings(sensorId, PolarBleApi.PolarDeviceDataType.ACC)
                polarAPI.startAccStreaming(sensorId, settings)
                    .sample(THROTTLE_INTERVAL_MILLISECOND.milliseconds)
                    .onEach { polarAccelerometerData: PolarAccelerometerData ->
                        for (data in polarAccelerometerData.samples) {
                            val xyz = SensorDataSource.XYZ(data.x.toDouble(), data.y.toDouble(), data.z.toDouble())
                            accFlow.value = xyz
                        }
                    }
                    .catch { error: Throwable -> Timber.e("--->>ACC stream failed. Reason $error") }
                    .onCompletion { Timber.w("--->>ACC stream complete") }
                    .collect()
            }
        }
            ?: run {
                accJob?.cancel()
                accJob = null
                Timber.w("--->>ACC stream disposed")
            }
    }

    private fun startGyroFlow(sensorId: String?) {
        sensorId?.let {
            Timber.w("--->>GYRO wont to be started with sensorId = $sensorId")
            grJob?.cancel()
            grJob = null

            grJob = scope.launch {
                val settings = requestStreamSettings(sensorId, PolarBleApi.PolarDeviceDataType.GYRO)
                polarAPI.startGyroStreaming(sensorId, settings)
                    .sample(THROTTLE_INTERVAL_MILLISECOND.milliseconds)
                    .onEach { polarGyroData: PolarGyroData ->
                        for (data in polarGyroData.samples) {
                            val xyz = SensorDataSource.XYZ(data.x.toDouble(), data.y.toDouble(), data.z.toDouble())
                            gyroFlow.value = xyz
                        }
                    }
                    .catch { error: Throwable -> Timber.e("--->>GYRO stream failed. Reason $error") }
                    .onCompletion { Timber.w("--->>GYRO stream complete") }
                    .collect()
            }
        }
            ?: run {
                grJob?.cancel()
                grJob = null
                Timber.w("--->>GYRO stream disposed")
            }
    }

    private fun startPPGFlow(sensorId: String?) {
        sensorId?.let {
            Timber.w("--->>PPG wont to be started with sensorId = $sensorId")
            ppgJob?.cancel()
            ppgJob = null

            ppgJob = scope.launch {
                val settings = requestStreamSettings(sensorId, PolarBleApi.PolarDeviceDataType.PPG)
                polarAPI.startPpgStreaming(sensorId, settings)
                    .onEach { polarPpgData: PolarPpgData ->
                        if (polarPpgData.type == PolarPpgData.PpgDataType.PPG3_AMBIENT1) {
                            ppgFlow.value = polarPpgData.samples.toPgpSample()
                        }
                    }
                    .catch { error: Throwable -> Timber.e("--->>PPG stream failed. Reason $error") }
                    .onCompletion { Timber.w("--->>PPG stream complete") }
                    .collect()
            }
        }
            ?: run {
                ppgJob?.cancel()
                ppgJob = null
                Timber.w("--->>PPG stream disposed")
            }
    }

    private suspend fun requestStreamSettings(identifier: String, feature: PolarBleApi.PolarDeviceDataType): PolarSensorSetting {
        return try {
            val settings = polarAPI.requestStreamSettings(identifier, feature)
            Timber.d("Sensor: $identifier, feature $feature available settings ${settings.settings}")
            settings
        } catch (error: Throwable) {
            Timber.w("Stream settings are not available for feature $feature. REASON: $error")
            PolarSensorSetting(emptyMap())
        }
    }
}
