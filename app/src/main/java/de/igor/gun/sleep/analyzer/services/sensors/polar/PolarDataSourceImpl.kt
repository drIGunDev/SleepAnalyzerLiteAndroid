package de.igor.gun.sleep.analyzer.services.sensors.polar

import android.annotation.SuppressLint
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarGyroData
import com.polar.sdk.api.model.PolarPpgData
import com.polar.sdk.api.model.PolarSensorSetting
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit


fun PolarPpgData.PolarPpgSample.toPgpSample() = SensorDataSource.PPGSamples(this.timeStamp, this.channelSamples)
fun List<PolarPpgData.PolarPpgSample>.toPgpSample(): List<SensorDataSource.PPGSamples> = this.map { it.toPgpSample() }

class PolarDataSourceImpl(private val sensorAPI: SensorAPI) : SensorDataSource {

    private val polarAPI = sensorAPI.apiImpl as PolarBleApi

    override val ppgFlow = MutableStateFlow<List<SensorDataSource.PPGSamples>>(mutableListOf())
    override var hrFlow = MutableStateFlow(0)
    override val accFlow = MutableStateFlow(SensorDataSource.XYZ(0.0, 0.0, 0.0))
    override val gyroFlow = MutableStateFlow(SensorDataSource.XYZ(0.0, 0.0, 0.0))

    private var hrDisposable: Disposable? = null
    private var accDisposable: Disposable? = null
    private var grDisposable: Disposable? = null
    private var ppgDisposable: Disposable? = null

    private companion object {
        const val THROTTLE_INTERVAL_MILLISECOND = 2000L
        val THROTTLE_SCHEDULER: Scheduler = Schedulers.computation()
    }

    init {
        Timber.w("----> init SensorDataRetrieval")
        CoroutineScope(Dispatchers.Default).launch {
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
            hrDisposable?.dispose()
            hrDisposable = null

            hrDisposable = polarAPI.startHrStreaming(sensorId)
                .throttleLatest(THROTTLE_INTERVAL_MILLISECOND, TimeUnit.MILLISECONDS, THROTTLE_SCHEDULER)
                .subscribe(
                { polarHrData ->
                    polarHrData.samples.forEach { sample ->
                        hrFlow.value = sample.hr
                    }
                },
                { error: Throwable -> Timber.e("--->>HR stream failed. Reason $error") },
                { Timber.w("--->>HR stream complete") }
            )
        }
            ?: run {
                hrDisposable?.dispose()
                hrDisposable = null
                Timber.w("--->>HR stream disposed")
            }
    }

    private fun startAccFlow(sensorId: String?) {
        sensorId?.let {
            Timber.w("--->>ACC wont to be started with sensorId = $sensorId")
            accDisposable?.dispose()
            accDisposable = null

            accDisposable = requestStreamSettings(sensorId, PolarBleApi.PolarDeviceDataType.ACC)
                .flatMap { settings: PolarSensorSetting ->
                    polarAPI.startAccStreaming(sensorId, settings)
                }
                .throttleLatest(THROTTLE_INTERVAL_MILLISECOND, TimeUnit.MILLISECONDS, THROTTLE_SCHEDULER)
                .subscribe(
                    { polarAccelerometerData: PolarAccelerometerData ->
                        for (data in polarAccelerometerData.samples) {
                            val xyz = SensorDataSource.XYZ(data.x.toDouble(), data.y.toDouble(), data.z.toDouble())
                            accFlow.value = xyz
                        }
                    },
                    { error: Throwable -> Timber.e("--->>ACC stream failed. Reason $error") },
                    { Timber.w("--->>ACC stream complete") }
                )
        }
            ?: run {
                accDisposable?.dispose()
                accDisposable = null
                Timber.w("--->>ACC stream disposed")
            }
    }

    private fun startGyroFlow(sensorId: String?) {
        sensorId?.let {
            Timber.w("--->>GYRO wont to be started with sensorId = $sensorId")
            grDisposable?.dispose()
            grDisposable = null

            accDisposable = requestStreamSettings(sensorId, PolarBleApi.PolarDeviceDataType.GYRO)
                .flatMap { settings: PolarSensorSetting ->
                    polarAPI.startGyroStreaming(sensorId, settings)
                }
                .throttleLatest(THROTTLE_INTERVAL_MILLISECOND, TimeUnit.MILLISECONDS, THROTTLE_SCHEDULER)
                .subscribe(
                    { polarGyroData: PolarGyroData ->
                        for (data in polarGyroData.samples) {
                            val xyz = SensorDataSource.XYZ(data.x.toDouble(), data.y.toDouble(), data.z.toDouble())
                            gyroFlow.value = xyz
                        }
                    },
                    { error: Throwable -> Timber.e("--->>GYRO stream failed. Reason $error") },
                    { Timber.w("--->>GYRO stream complete") }
                )
        }
            ?: run {
                grDisposable?.dispose()
                grDisposable = null
                Timber.w("--->>GYRO stream disposed")
            }
    }

    private fun startPPGFlow(sensorId: String?) {
        sensorId?.let {
            Timber.w("--->>PPG wont to be started with sensorId = $sensorId")
            ppgDisposable?.dispose()
            ppgDisposable = null

            ppgDisposable =
                requestStreamSettings(sensorId, PolarBleApi.PolarDeviceDataType.PPG)
                    .flatMap { settings: PolarSensorSetting ->
                        polarAPI.startPpgStreaming(sensorId, settings)
                    }
                    .subscribe(
                        { polarPpgData: PolarPpgData ->
                            if (polarPpgData.type == PolarPpgData.PpgDataType.PPG3_AMBIENT1) {
                                ppgFlow.value = polarPpgData.samples.toPgpSample()
                            }
                        },
                        { error: Throwable -> Timber.e("--->>PPG stream failed. Reason $error") },
                        { Timber.w("--->>PPG stream complete") }
                    )
        }
            ?: run {
                ppgDisposable?.dispose()
                ppgDisposable = null
                Timber.w("--->>PPG stream disposed")
            }
    }

    @SuppressLint("BinaryOperationInTimber")
    private fun requestStreamSettings(identifier: String, feature: PolarBleApi.PolarDeviceDataType): Flowable<PolarSensorSetting> {
        val availableSettings = polarAPI.requestStreamSettings(identifier, feature)
            .onErrorReturn { error: Throwable ->
                Timber.w("Stream settings are not available for feature $feature. REASON: $error")
                PolarSensorSetting(emptyMap())
            }
        val settings = availableSettings.blockingGet()
        Timber.d("Sensor: $identifier, feature $feature available settings ${settings.settings}")
        return availableSettings
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable()
    }
}