package de.igor.gun.sleep.analyzer.ui.screens.tracking.model

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.db.DBManager
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.misc.AppParameters
import de.igor.gun.sleep.analyzer.misc.durationUntilNow
import de.igor.gun.sleep.analyzer.repositories.DataRepository
import de.igor.gun.sleep.analyzer.repositories.MeasurementsRecorder
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.SleepSegment
import de.igor.gun.sleep.analyzer.services.sensors.PPGSource
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import de.igor.gun.sleep.analyzer.ui.theme.ConstructionColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val dbManager: DBManager,
    private val recorder: MeasurementsRecorder,
    private val sensorAPI: SensorAPI,
    private val dataSource: SensorDataSource,
    private val dataRepository: DataRepository,
    private val appParameters: AppParameters,
    @param:ApplicationContext private val context: Context,
    val ppgSource: PPGSource,
    val chartBuilder: ChartBuilder,
) : ViewModel() {

    val hrFlow get() = dataSource.hrFlow
    val batteryFlow get() = sensorAPI.batteryFlow
    val rssiFlow get() = sensorAPI.rssiFlow

    val recordedSeries get() = recorder.series
    val isRecording = recorder.isRecording

    init {
        chartBuilder.setAxisColor(ConstructionColor.toArgb())
        chartBuilder.setAxisTextColor(MainWhiteColor.toArgb())
        if (isRecording.value) startChartUpdating()
    }

    override fun onCleared() {
        super.onCleared()
        ppgSource.stopStreaming()
        recorder.stopChartUpdating()
    }

    fun resetFlows() = dataSource.resetFlows()

    fun startRecording(completion: (Boolean) -> Unit) {
        recorder.startRecording(completion)
    }

    fun stopRecording(
        satisfaction: Series.Satisfaction,
        completion: (() -> Unit)? = null,
    ) {
        recorder.stopRecording(satisfaction, completion)
    }

    val isDataInconsistent = mutableStateOf(false)

    fun checkSeriesToRepair() {
        viewModelScope.launch(Dispatchers.IO) {
            val seriesNeededToRepair = dbManager.isSeriesNeededToRepair
            isDataInconsistent.value = seriesNeededToRepair
        }
    }

    sealed interface RepairState {
        data class Progress(val progress: Float, val text: String) : RepairState
        data object Completed : RepairState
    }

    val repairState = mutableStateOf<RepairState?>(null)
    private var isRepairCanceled = false

    fun repairSeries() {
        viewModelScope.launch(Dispatchers.IO) {
            isRepairCanceled = false
            dbManager.repairSeries(
                chartBuilder,
                measurementIds = appParameters.getMeasurementIds(AppParameters.AppEntryPoints.ARCHIVE),
                externalWorkHandler = { _, _, series ->
                    dataRepository.recreateHypnogram(series = series)
                },
                progressHandler = { total, current ->
                    repairState.value = RepairState.Progress(
                        progress = current / total.toFloat(),
                        text = "${context.getText(R.string.repair_series)}: $current/$total"
                    )
                },
                progressCancelHandler = { isRepairCanceled },
                completion = {
                    repairState.value = if (isRepairCanceled) null else RepairState.Completed
                }
            )
        }
    }

    fun cancelRepairSeries() {
        isRepairCanceled = true
    }

    val chartUpdateState = mutableStateOf(false)
    val hypnogramUpdateState = mutableStateOf(false)
    val recordingDuration = mutableStateOf<String?>(null)

    fun startChartUpdating() {
        recorder.startChartUpdating(
            chartBuilder,
            onUpdate = { series ->
                chartUpdateState.value = true
                hypnogramUpdateState.value = true
                recordingDuration.value = series.startDate.durationUntilNow()
                requestHypnogram(seriesId = series.id)
            }
        )
    }

    val sleepSegments = mutableStateOf<List<SleepSegment>>(listOf())

    private fun requestHypnogram(seriesId: Long) {
        val hypnogramHolder = HypnogramHolder()
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.fillHypnogramWithMeasurements(
                hypnogramHolder = hypnogramHolder,
                seriesId = seriesId
            )
            sleepSegments.value = hypnogramHolder.buildSleepSegments()
        }
    }
}
