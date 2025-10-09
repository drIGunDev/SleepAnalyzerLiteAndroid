package de.igor.gun.sleep.analyzer.ui.screens.archive.model

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.igor.gun.sleep.analyzer.R
import de.igor.gun.sleep.analyzer.db.DBManager
import de.igor.gun.sleep.analyzer.db.entities.Cache
import de.igor.gun.sleep.analyzer.db.entities.Measurement
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.misc.AppParameters
import de.igor.gun.sleep.analyzer.misc.ReactiveLRUCache
import de.igor.gun.sleep.analyzer.repositories.DataRepository
import de.igor.gun.sleep.analyzer.repositories.MeasurementsRecorder
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.ui.theme.ConstructionColor
import de.igor.gun.sleep.analyzer.ui.theme.MainWhiteColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class SeriesWrapper(val series: Series) {
    class Measurements(series: Series, val measurements: List<Measurement>) : SeriesWrapper(series)
    class Cached(series: Series, val cache: Cache) : SeriesWrapper(series) {
        fun isValidHypnogram(): Boolean = cache.awake > 0 || cache.lSleep > 0 || cache.dSleep > 0 || cache.rem > 0
    }
}

@HiltViewModel
class ArchiveListViewModel @Inject constructor(
    private val dbManager: DBManager,
    private val recorder: MeasurementsRecorder,
    private val dataRepository: DataRepository,
    private val appParameters: AppParameters,
    private val chartBuilder: ChartBuilder,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {

    sealed interface RescalingState {
        data class Progress(val progress: Float, val text: String) : RescalingState
        data object Completed : RescalingState
    }

    val isRecording = recorder.isRecording
    val rescalingSate = mutableStateOf<RescalingState?>(null)
    val isDebugVersion = appParameters.isDebugVersion

    private companion object {
        const val SERIES_WRAPPER_CACHE_SIZE = 50
    }

    init {
        chartBuilder.setAxisColor(ConstructionColor.toArgb())
        chartBuilder.setAxisTextColor(MainWhiteColor.toArgb())
        chartBuilder.screen.xAxisType = ChartBuilder.XAxisType.DATE
    }

    fun startSingleHRRescaling(
        series: Series,
        minHRScaled: Float?,
        maxHRScaled: Float?,
        maxXScaled: Float?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dbManager.rescaleHRSeries(
                    series,
                    minHRScaled,
                    maxHRScaled,
                    maxXScaled,
                    chartBuilder,
                    measurementIds = appParameters.getMeasurementIds(AppParameters.AppEntryPoints.ARCHIVE),
                    progressHandler = { total, current ->
                        rescalingSate.value = RescalingState.Progress(
                            progress = current / total.toFloat(),
                            text = "${context.getText(R.string.rescale_series)}: $current/$total"
                        )
                    },
                    completion = {
                        dataRepository.recreateHypnogram(series)
                        rescalingSate.value = RescalingState.Completed
                        seriesWrapperCache.remove(series.id)
                    }
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val cancelRescalingState = mutableStateOf(false)
    fun stopHRRescaling() {
        cancelRescalingState.value = true
    }

    fun startSeriesHRRescaling(
        minHRScaled: Float?,
        maxHRScaled: Float?,
        maxXScaled: Float?,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            cancelRescalingState.value = false
            dbManager.rescaleHRSeries(
                minHRScaled,
                maxHRScaled,
                maxXScaled,
                chartBuilder,
                measurementIds = appParameters.getMeasurementIds(AppParameters.AppEntryPoints.ARCHIVE),
                hypnogram = null,
                this.coroutineContext,
                progressHandler = { total, current ->
                    rescalingSate.value = RescalingState.Progress(
                        progress = current / total.toFloat(),
                        text = "${context.getText(R.string.rescale_series)}: $current/$total"
                    )
                },
                externalWorkHandler = { _, _, series ->
                    dataRepository.recreateHypnogram(series = series)
                },
                progressCancelHandler = { cancelRescalingState.value },
                completion = {
                    rescalingSate.value = if (cancelRescalingState.value) null else RescalingState.Completed
                    seriesWrapperCache.clear()
                }
            )
        }
    }

    var previewList = mutableStateOf<List<SeriesWrapper>?>(null)
    fun updatePreviewList() {
        viewModelScope.launch(Dispatchers.IO) {
            previewList.value = measurementsToWrappers()
        }
    }

    fun cleanPreviewList() {
        previewList.value = null
    }

    var seriesList = mutableStateOf<List<Series>?>(null)
    fun updateSeriesList() {
        viewModelScope.launch(Dispatchers.IO) {
            seriesList.value = getSeriesList()
        }
    }

    val seriesWrapperCache = ReactiveLRUCache<Long, SeriesWrapper>(SERIES_WRAPPER_CACHE_SIZE)
    fun updateSeriesCache(series: Series) {
        viewModelScope.launch(Dispatchers.IO) {
            seriesWrapperCache.put(series.id, measurementsToWrappers(series))
        }
    }

    fun deleteSeries(series: Series) {
        viewModelScope.launch(Dispatchers.IO) {
            dbManager.delete(series)
            seriesWrapperCache.remove(series.id)
            updateSeriesList()
        }
    }

    fun requestChartBuilder(measurements: List<Measurement>): ChartBuilder {
        val chartBuilder = ChartBuilder(ChartBuilder.Screen.default())
        dataRepository.fillChartWithMeasurements(
            chartBuilder = chartBuilder,
            measurements = measurements,
            measurementIds = appParameters.getMeasurementIds(AppParameters.AppEntryPoints.ARCHIVE),
        )
        return chartBuilder
    }

    val chartBuilderState = mutableStateOf(ChartBuilder(ChartBuilder.Screen.default()))
    val hypnogramBuilderState = mutableStateOf(HypnogramHolder())
    val seriesState = mutableStateOf<Series?>(null)
    fun requestChart(
        seriesId: Long,
        types: List<Measurement.Id> = appParameters.getMeasurementIds(AppParameters.AppEntryPoints.ARCHIVE),
        typesRmse: List<Measurement.Id> = listOf(Measurement.Id.HR, Measurement.Id.ACC),
        showRmse: Boolean = true,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val hypnogramHolder = HypnogramHolder()
            dataRepository.fillChartWithMeasurementsAndRmse(
                chartBuilder = chartBuilder,
                hypnogramHolder = hypnogramHolder,
                seriesId = seriesId,
                types = types,
                typesRmse = typesRmse,
                showRmse = showRmse
            )
            chartBuilderState.value = chartBuilder
            hypnogramBuilderState.value = hypnogramHolder
            seriesState.value = dbManager.getSeries(seriesId)
        }
    }

    fun cleanChart() {
        chartBuilder.clear()
        chartBuilderState.value = chartBuilder
        hypnogramBuilderState.value = HypnogramHolder()
    }

    val appParametersLive = mutableStateOf(appParameters)
    fun requestAppParameters() = run { appParametersLive.value = appParameters }

    fun updateAppParameters(appParameters: AppParameters) {
        this.appParameters.frameSizeHR = appParameters.frameSizeHR
        this.appParameters.frameSizeACC = appParameters.frameSizeACC
        this.appParameters.quantizationHR = appParameters.quantizationHR
        this.appParameters.quantizationACC = appParameters.quantizationACC
        this.appParameters.saveToPreferences()
    }

    fun updateAppParameters(hrFrameSize: Int, accFrameSize: Int, hrQuantization: Float, accQuantization: Float) {
        this.appParameters.frameSizeHR = hrFrameSize
        this.appParameters.frameSizeACC = accFrameSize
        this.appParameters.quantizationHR = hrQuantization
        this.appParameters.quantizationACC = accQuantization
    }

    private fun getSeriesList(): List<Series> {
        return dbManager.getAllSeries(order = DBManager.SortOrder.DESC)
    }

    private fun measurementsToWrappers(series: Series): SeriesWrapper {
        val cache = dbManager.getCache(series)
        return cache?.chartImage?.let { SeriesWrapper.Cached(series, cache) }
            ?: run {
                val measurements = dbManager.getMeasurements(series)
                SeriesWrapper.Measurements(series, measurements)
            }
    }

    private fun measurementsToWrappers(): List<SeriesWrapper> {
        val seriesList = dbManager.getAllSeries(order = DBManager.SortOrder.DESC)
        val allCache = dbManager.getAllCache(order = DBManager.SortOrder.DESC)
        val cacheMap = allCache.associateBy { it.seriesId }
        return seriesList.map { series ->
            val image = cacheMap[series.id]
            image?.let { SeriesWrapper.Cached(series, it) }
                ?: run {
                    val measurements = dbManager.getMeasurements(series)
                    SeriesWrapper.Measurements(series, measurements)
                }
        }
    }
}