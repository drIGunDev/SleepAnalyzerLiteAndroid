package de.igor.gun.sleep.analyzer.repositories

import androidx.compose.runtime.State
import androidx.compose.ui.graphics.toArgb
import de.igor.gun.sleep.analyzer.db.DBManager
import de.igor.gun.sleep.analyzer.db.SeriesRecorder
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.misc.AppParameters
import de.igor.gun.sleep.analyzer.misc.MeasurementsChecker
import de.igor.gun.sleep.analyzer.misc.combineFlows
import de.igor.gun.sleep.analyzer.misc.toMillis
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import de.igor.gun.sleep.analyzer.ui.theme.ConstructionColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject


class MeasurementsRecorder @Inject constructor(
    private val dbManager: DBManager,
    private val seriesRecorder: SeriesRecorder,
    private val dataRepository: DataRepository,
    private val sensorAPI: SensorAPI,
    private val dataSource: SensorDataSource,
    private val measurementsChecker: MeasurementsChecker,
    private val chartBuilder: ChartBuilder,
    private val appParameters: AppParameters,
) {
    private fun getMeasurementFlow() = combineFlows(
        dataSource.hrFlow,
        dataSource.accFlow,
        dataSource.gyroFlow,
        sensorAPI.batteryFlow,
        sensorAPI.rssiFlow,
    )
        .transform { value -> measurementsChecker.getPointIfFits(value)?.let { emit(it) } }

    val isRecording: State<Boolean> get() = seriesRecorder.isRecording
    val series: Series? get() = seriesRecorder.series
    var startTimeMillis: Long = 0

    init {
        chartBuilder.setAxisColor(ConstructionColor.toArgb())
        chartBuilder.setAxisTextColor(MainWhiteColor.toArgb())
    }

    private var recordingJob: Job? = null

    fun startRecording(completion: (Boolean) -> Unit) {
        if (isRecording.value) {
            cancelRecording()
        }
        val startDate = LocalDateTime.now()
        this.startTimeMillis = startDate.toMillis()
        var i = 0
        recordingJob = CoroutineScope(Dispatchers.IO + Job()).launch {
            try {
                seriesRecorder.startRecording(startDate = startDate)
                completion(true)

                getMeasurementFlow()
                    .cancellable()
                    .collect { point ->
                        if (isRecording.value) {
                            seriesRecorder.recordMeasurementPoint(point)
                            Timber.d("---> saved point #${i++}")
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e)
                completion(false)
            }
        }
    }

    fun stopRecording(
        satisfaction: Series.Satisfaction = Series.Satisfaction.NEUTRAL,
        completion: (() -> Unit)? = null,
    ) {
        if (!isRecording.value) {
            completion?.invoke()
            return
        }

        cancelRecordingJob()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                series?.let { series ->
                    seriesRecorder.stopRecording(endDate = LocalDateTime.now(), satisfaction = satisfaction)
                    dbManager.insertUpdateCache(series, chartBuilder = chartBuilder, measurementIds = appParameters.getMeasurementIds(AppParameters.AppEntryPoints.TRACKING))
                    dataRepository.recreateHypnogram(series)
                }
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                completion?.invoke()
            }
        }
    }

    private var updateJob: Job? = null

    fun startChartUpdating(
        chartBuilder: ChartBuilder,
        onUpdate: (Series) -> Unit,
        onCompletion: (() -> Unit)? = null,
    ) {
        recordingJob?.let { recordingJob ->
            updateJob = CoroutineScope(Dispatchers.IO + recordingJob).launch {
                try {
                    seriesRecorder.recordingFlow
                        .conflate()
                        .distinctUntilChanged()
                        .collect {
                            series?.let { series ->
                                dataRepository.fillChartWithMeasurements(
                                    chartBuilder,
                                    seriesId = series.id,
                                    types = appParameters.getMeasurementIds(AppParameters.AppEntryPoints.TRACKING)
                                )
                                onUpdate(series)
                            }
                        }
                } catch (e: Exception) {
                    Timber.w(e)
                } finally {
                    onCompletion?.invoke()
                }
            }
        }
    }

    fun stopChartUpdating() {
        cancelChartUpdating()
    }

    private fun cancelRecording(completion: (() -> Unit)? = null) {
        stopRecording(completion = completion)
    }

    private fun cancelChartUpdating() {
        updateJob?.cancel()
        updateJob?.cancelChildren()
        updateJob = null
    }

    private fun cancelRecordingJob() {
        recordingJob?.cancel()
        recordingJob?.cancelChildren()
        recordingJob = null
    }
}