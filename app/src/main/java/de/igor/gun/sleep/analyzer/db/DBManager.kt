package de.igor.gun.sleep.analyzer.db

import android.graphics.Bitmap
import de.igor.gun.sleep.analyzer.db.entities.Cache
import de.igor.gun.sleep.analyzer.db.entities.CacheHypnogram
import de.igor.gun.sleep.analyzer.db.entities.Measurement
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.db.entities.toChart
import de.igor.gun.sleep.analyzer.db.entities.toIdWithColor
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlin.coroutines.CoroutineContext


typealias ExternalWorkHandler = (Int, Int, Series) -> Unit
typealias ProgressHandler = (Int, Int) -> Unit
typealias ProgressCompletionHandler = () -> Unit
typealias ProgressCancelHandler = () -> Boolean

class DBManager(private val appDatabase: AppDatabase) {
    enum class SortOrder {
        DESC, ASC
    }

    fun insertUpdateCache(
        series: Series,
        chartBuilder: ChartBuilder,
        measurementIds: List<Measurement.Id>,
        minHRScaled: Float? = null,
        maxHRScaled: Float? = null,
        maxXScaled: Float? = null,
        hypnogram: HypnogramHolder.SleepStateDistribution? = null,
    ) {
        synchronized(this) {
            chartBuilder.clear()

            val measurements = getMeasurements(series)
            val charts = measurements.toChart(idWithColor = measurementIds.toIdWithColor())

            charts[Measurement.Id.RSSI]
                ?.let { rssi ->
                    if (maxXScaled != null) {
                        rssi.rescaleX(minX = 0f, maxX = maxXScaled)
                    }
                    chartBuilder.addChart(rssi)
                }

            charts[Measurement.Id.ACC]
                ?.let { acc ->
                    if (maxXScaled != null) {
                        acc.rescaleX(minX = 0f, maxX = maxXScaled)
                    }
                    chartBuilder.addChart(acc)
                }

            charts[Measurement.Id.GYRO]
                ?.let { gyro ->
                    if (maxXScaled != null) {
                        gyro.rescaleX(minX = 0f, maxX = maxXScaled)
                    }
                    chartBuilder.addChart(gyro)
                }

            val hr = charts[Measurement.Id.HR]
                ?.let { hr ->
                    if (minHRScaled != null && maxHRScaled != null) {
                        hr.rescaleY(minY = minHRScaled, maxY = maxHRScaled)
                    }
                    if (maxXScaled != null) {
                        hr.rescaleX(minX = 0f, maxX = maxXScaled)
                    }
                    chartBuilder.addChart(hr)
                    hr
                }

            val bitmap = chartBuilder.buildBitmap()
            insertUpdateCache(
                series = series,
                chartBitmap = bitmap,
                minHRScaled = hr?.minY,
                maxHRScaled = hr?.maxY,
                duration = maxXScaled,
                hypnogram = hypnogram
            )
        }
    }

    private fun insertUpdateCache(
        series: Series,
        chartBitmap: Bitmap? = null,
        minHRScaled: Float? = null,
        maxHRScaled: Float? = null,
        duration: Float? = null,
        hypnogram: HypnogramHolder.SleepStateDistribution? = null,
    ) {
        synchronized(this) {
            chartBitmap?.let {
                val minHR = appDatabase.measurementDAO().getMinHR(series.id)
                val maxHR = appDatabase.measurementDAO().getMaxHR(series.id)
                val savedCache = appDatabase.cacheDAO().get(series.id)
                if (savedCache == null) {
                    appDatabase.cacheDAO().insert(
                        Cache(
                            maxHR = maxHR ?: 0f,
                            minHR = minHR ?: 0f,
                            maxHRScaled = maxHRScaled ?: 0f,
                            minHRScaled = minHRScaled ?: 0f,
                            duration = duration ?: series.durationMillis,
                            chartImage = chartBitmap,
                            awake = hypnogram?.absolutMillis?.get(HypnogramHolder.SleepState.AWAKE) ?: 0f,
                            rem = hypnogram?.absolutMillis?.get(HypnogramHolder.SleepState.REM) ?: 0f,
                            lSleep = hypnogram?.absolutMillis?.get(HypnogramHolder.SleepState.LIGHT_SLEEP) ?: 0f,
                            dSleep = hypnogram?.absolutMillis?.get(HypnogramHolder.SleepState.DEEP_SLEEP) ?: 0f,
                            seriesId = series.id,
                        )
                    )
                } else {
                    savedCache.minHR = minHR ?: 0f
                    savedCache.maxHR = maxHR ?: 0f
                    savedCache.minHRScaled = minHRScaled ?: 0f
                    savedCache.maxHRScaled = maxHRScaled ?: 0f
                    savedCache.duration = duration ?: series.durationMillis
                    savedCache.chartImage = chartBitmap
                    if (hypnogram != null) {
                        savedCache.awake = hypnogram.absolutMillis.get(HypnogramHolder.SleepState.AWAKE) ?: 0f
                        savedCache.rem = hypnogram.absolutMillis.get(HypnogramHolder.SleepState.REM) ?: 0f
                        savedCache.lSleep = hypnogram.absolutMillis.get(HypnogramHolder.SleepState.LIGHT_SLEEP) ?: 0f
                        savedCache.dSleep = hypnogram.absolutMillis.get(HypnogramHolder.SleepState.DEEP_SLEEP) ?: 0f
                    }
                    appDatabase.cacheDAO().update(savedCache)
                }
            }
        }
    }

    fun updateCache(series: Series, hypnogram: HypnogramHolder.SleepStateDistribution) {
        synchronized(this) {
            val savedCache = appDatabase.cacheDAO().get(series.id)
            if (savedCache != null) {
                savedCache.awake = hypnogram.absolutMillis[HypnogramHolder.SleepState.AWAKE] ?: 0f
                savedCache.rem = hypnogram.absolutMillis[HypnogramHolder.SleepState.REM] ?: 0f
                savedCache.lSleep = hypnogram.absolutMillis[HypnogramHolder.SleepState.LIGHT_SLEEP] ?: 0f
                savedCache.dSleep = hypnogram.absolutMillis[HypnogramHolder.SleepState.DEEP_SLEEP] ?: 0f
                appDatabase.cacheDAO().update(savedCache)
            }
        }
    }

    fun getSeries(seriesId: Long): Series = synchronized(this) { appDatabase.seriesDAO().get(seriesId) }

    fun insertSeries(series: Series): Long {
        synchronized(this) {
            return appDatabase.seriesDAO().insert(series)
        }
    }

    fun updateSeries(series: Series) {
        synchronized(this) {
            appDatabase.seriesDAO().update(series)
        }
    }

    fun getAllSeries(order: SortOrder = SortOrder.DESC): List<Series> =
        synchronized(this) {
            when (order) {
                SortOrder.DESC -> appDatabase.seriesDAO().getAllDesc()
                SortOrder.ASC -> appDatabase.seriesDAO().getAllAsc()
            }
        }

    fun getAllCache(order: SortOrder = SortOrder.DESC): List<Cache> =
        synchronized(this) {
            when (order) {
                SortOrder.DESC -> appDatabase.cacheDAO().getAllDesc()
                SortOrder.ASC -> appDatabase.cacheDAO().getAllAsc()
            }
        }

    fun getMeasurementCountAsFlow(seriesId: Long?): Flow<Long> {
        synchronized(this) {
            return appDatabase.measurementDAO().getMeasurementCountAsFlow(seriesId)
        }
    }

    fun getMeasurements(series: Series): List<Measurement> =
        getMeasurements(seriesId = series.id)

    fun getMeasurements(seriesId: Long): List<Measurement> =
        synchronized(this) {
            appDatabase.measurementDAO().get(seriesId)
        }

    fun insertMeasurement(measurement: Measurement) {
        synchronized(this) {
            appDatabase.measurementDAO().insert(measurement)
        }
    }

    fun delete(series: Series) {
        synchronized(this) {
            appDatabase.seriesDAO().delete(series)
        }
    }

    fun getCache(series: Series): Cache? =
        synchronized(this) {
            return appDatabase.cacheDAO().get(series.id)
        }

    fun getCacheHypnogram(series: Series): List<CacheHypnogram> =
        synchronized(this) {
            return appDatabase.cacheHypnogramDAO().get(series.id)
        }

    fun recreateHypnogram(seriesId: Long, sleepPhasesHolder: SleepPhasesHolder) {
        synchronized(this) {
            appDatabase.cacheHypnogramDAO().delete(seriesId)
            val hypnogram = sleepPhasesHolder.buildSegments()
            if (hypnogram.isEmpty()) return
            hypnogram.forEach {
                appDatabase.cacheHypnogramDAO().insert(CacheHypnogram(it.state, it.time, seriesId))
            }
        }
    }

    val isSeriesNeededToRepair: Boolean
        get() =
            synchronized(this) {
                val cacheMap = getAllCache().associateBy { it.seriesId }
                return !getAllSeries().all { series ->
                    cacheMap[series.id] != null
                }
            }

    fun repairSeries(
        chartBuilder: ChartBuilder,
        measurementIds: List<Measurement.Id> = listOf(Measurement.Id.HR, Measurement.Id.ACC, Measurement.Id.GYRO, Measurement.Id.RSSI),
        externalWorkHandler: ExternalWorkHandler? = null,
        progressHandler: ProgressHandler? = null,
        progressCancelHandler: ProgressCancelHandler? = null,
        completion: ProgressCompletionHandler? = null,
    ) {
        synchronized(this) {
            val cacheMap = getAllCache().associateBy { it.seriesId }
            val seriesToRepair = getAllSeries(order = SortOrder.ASC).filter { cacheMap[it.id] == null }
            for ((index, series) in seriesToRepair.iterator().withIndex()) {
                progressHandler?.invoke(seriesToRepair.size, index + 1)
                insertUpdateCache(series, chartBuilder, measurementIds)
                externalWorkHandler?.invoke(seriesToRepair.size, index + 1, series)
                progressHandler?.invoke(seriesToRepair.size, index + 2)
                if (progressCancelHandler?.invoke() == true) break
            }
            completion?.invoke()
        }
    }

    fun rescaleHRSeries(
        minHRScaled: Float?,
        maxHRScaled: Float?,
        maxXScaled: Float?,
        chartBuilder: ChartBuilder,
        measurementIds: List<Measurement.Id>,
        hypnogram: HypnogramHolder.SleepStateDistribution? = null,
        coroutineContext: CoroutineContext,
        progressCancelHandler: ProgressCancelHandler? = null,
        progressHandler: ProgressHandler? = null,
        externalWorkHandler: ExternalWorkHandler? = null,
        completion: ProgressCompletionHandler? = null,
    ) {
        val seriesToRescale = getAllSeries()
        val totalSeries = seriesToRescale.size
        var currentSeries = 1
        for (series in seriesToRescale) {
            progressHandler?.invoke(totalSeries, currentSeries)
            val cache = appDatabase.cacheDAO().get(seriesId = series.id)
            if (cache != null) {
                appDatabase.cacheDAO().delete(cache)
            }
            insertUpdateCache(series, chartBuilder, measurementIds, minHRScaled, maxHRScaled, maxXScaled, hypnogram)
            externalWorkHandler?.invoke(totalSeries, currentSeries, series)
            progressHandler?.invoke(totalSeries, currentSeries++)
            if (!coroutineContext.isActive) break
            if (progressCancelHandler?.invoke() == true) break
        }
        completion?.invoke()
    }

    suspend fun rescaleHRSeries(
        series: Series,
        minHRScale: Float?,
        maxHRScale: Float?,
        maxXScale: Float?,
        chartBuilder: ChartBuilder,
        measurementIds: List<Measurement.Id>,
        hypnogram: HypnogramHolder.SleepStateDistribution? = null,
        progressHandler: ProgressHandler? = null,
        completion: ProgressCompletionHandler? = null,
    ) {
        val totalSeries = 1
        var currentSeries = 0
        progressHandler?.invoke(totalSeries, currentSeries)
        delay(500)
        val cache = appDatabase.cacheDAO().get(seriesId = series.id)
        if (cache != null) {
            appDatabase.cacheDAO().delete(cache)
        }
        insertUpdateCache(series, chartBuilder, measurementIds, minHRScale, maxHRScale, maxXScale, hypnogram)
        progressHandler?.invoke(totalSeries, ++currentSeries)
        delay(500)
        completion?.invoke()
    }
}
