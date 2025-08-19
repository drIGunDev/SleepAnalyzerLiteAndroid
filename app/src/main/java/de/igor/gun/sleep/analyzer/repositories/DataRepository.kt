package de.igor.gun.sleep.analyzer.repositories

import android.graphics.PointF
import androidx.compose.ui.graphics.toArgb
import de.igor.gun.sleep.analyzer.db.DBManager
import de.igor.gun.sleep.analyzer.db.entities.Measurement
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.db.entities.mapToValues
import de.igor.gun.sleep.analyzer.db.entities.toChart
import de.igor.gun.sleep.analyzer.db.entities.toIdWithColor
import de.igor.gun.sleep.analyzer.db.entities.toMillisSinceStart
import de.igor.gun.sleep.analyzer.hypnogram.computation.HypnogramComputation
import de.igor.gun.sleep.analyzer.math.bridges.mapToSleepDataPoint
import de.igor.gun.sleep.analyzer.misc.AppParameters
import de.igor.gun.sleep.analyzer.repositories.di.HCProvider
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.SleepPhasesHolder
import de.igor.gun.sleep.analyzer.repositories.tools.computeDistribution
import de.igor.gun.sleep.analyzer.repositories.tools.toSegments
import de.igor.gun.sleep.analyzer.ui.theme.BlueTranslucent
import de.igor.gun.sleep.analyzer.ui.theme.Green
import de.igor.gun.sleep.analyzer.ui.theme.Red
import de.igor.gun.sleep.analyzer.ui.theme.RedTranslucent
import de.igor.gun.sleep.analyzer.ui.theme.Yellow
import de.igor.gun.sleep.analyzer.ui.theme.YellowTranslucent
import java.time.LocalDateTime
import javax.inject.Inject


class DataRepository @Inject constructor(
    private val dbManager: DBManager,
    private val appParameters: AppParameters,
    @param:HCProvider private val hcBinding: HypnogramComputation,
) {
    private fun getMeasurements(seriesId: Long) = dbManager.getMeasurements(seriesId)
    private fun getEndDateFromMeasurements(seriesId: Long): LocalDateTime? =
        try {
            getMeasurements(seriesId).last().date
        } catch (e: Exception) {
            null
        }

    fun fillChartWithMeasurements(
        chartBuilder: ChartBuilder,
        seriesId: Long,
        types: List<Measurement.Id>,
    ) {
        getMeasurements(seriesId).also { measurements ->
            fillChartWithMeasurements(chartBuilder, types, measurements)
        }
    }

    fun fillChartWithMeasurementsAndRmse(
        chartBuilder: ChartBuilder,
        hypnogramHolder: HypnogramHolder,
        seriesId: Long,
        types: List<Measurement.Id>,
        typesRmse: List<Measurement.Id>,
        showRmse: Boolean,
    ) {
        getMeasurements(seriesId).also { measurements ->
            val hrRmse = computeHRRmse(measurements, frameSizeHR = appParameters.frameSizeHR, quantizationHR = appParameters.quantizationHR)
            val accRmse = computeACC(measurements, frameSizeACC = appParameters.frameSizeACC, quantizationACC = appParameters.quantizationACC)
            fillChart(
                chartBuilder,
                types,
                measurements,
                typesRmse,
                hrRmse,
                accRmse,
                showRmse,
                frameSizeHR = appParameters.frameSizeHR
            )
            fillHypnogram(
                hypnogramHolder,
                hrRmse,
                accRmse
            )
        }
    }

    fun fillChartWithMeasurements(
        chartBuilder: ChartBuilder,
        measurementIds: List<Measurement.Id>,
        measurements: List<Measurement>,
    ) {
        val idWithColor = measurementIds.toIdWithColor()
        val charts = measurements.toChart(idWithColor = idWithColor)
        chartBuilder.clear()
        charts.forEach { chartBuilder.addChart(it.value) }
    }

    fun fillHypnogramWithMeasurements(
        seriesId: Long,
        hypnogramHolder: HypnogramHolder,
    ) {
        getMeasurements(seriesId).also { measurements ->
            val hrRmse = computeHRRmse(measurements, frameSizeHR = appParameters.frameSizeHR, quantizationHR = appParameters.quantizationHR)
            val accRmse = computeACC(measurements, frameSizeACC = appParameters.frameSizeACC, quantizationACC = appParameters.quantizationACC)
            fillHypnogram(
                hypnogramHolder,
                hrRmse,
                accRmse,
            )
        }
    }

    fun recreateHypnogram(series: Series) {
        synchronized(this) {
            val hypnogramHolder = HypnogramHolder().apply {
                fillHypnogramWithMeasurements(series.id, this)
            }
            val endTime = getEndDateFromMeasurements(series.id) ?: LocalDateTime.now()
            SleepPhasesHolder().apply {
                val sleepDataPoints = hypnogramHolder.buildSleepDataPoints()
                setSleepDataPoints(
                    sleepDataPoints,
                    startTime = series.startDate,
                    endTime = series.endDate ?: endTime
                )
                dbManager.recreateHypnogram(series.id, this)
            }
            hypnogramHolder
                .buildSleepDataPoints()
                .toSegments(endTime = series.endDate ?: endTime, startTime = series.startDate)
                .computeDistribution()
                .ifValid {
                    dbManager.updateCache(
                        series,
                        hypnogram = this
                    )
                }
        }
    }

    private fun fillHypnogram(
        hypnogramHolder: HypnogramHolder,
        hrRmse: List<PointF>,
        accRmse: List<PointF>,
    ) {
        if (hrRmse.isEmpty() || accRmse.isEmpty()) return
        val hypnogram = computeUniformHypnogram(hrRmse, accRmse)
        if (hypnogram.isEmpty()) return
        hypnogramHolder.setUniformSleepDataPoints(hypnogram)
    }

    private fun fillChart(
        chartBuilder: ChartBuilder,
        measurementIds: List<Measurement.Id>,
        measurements: List<Measurement>,
        typesRmse: List<Measurement.Id>,
        hrRmse: List<PointF>,
        accRmse: List<PointF>,
        showRmse: Boolean = true,
        frameSizeHR: Int,
    ) {
        chartBuilder.clear()
        val idWithColor = measurementIds.toIdWithColor()
        val charts = measurements.toChart(idWithColor = idWithColor).apply {
            this[Measurement.Id.HR]?.rescaleY(30f, 100f)
        }
        charts.forEach { chartBuilder.addChart(it.value) }
        if (frameSizeHR == 0) return
        if (Measurement.Id.HR in typesRmse && showRmse) {
            val hrRmseChart = ChartBuilder.ChartPresentation.Chart(
                id = "RMSE:" + Measurement.Id.HR.name,
                points = hrRmse,
                color = Yellow.toArgb(),
                fillColor = YellowTranslucent.toArgb()
            )
            chartBuilder.addChart(hrRmseChart)
        }
        if (Measurement.Id.ACC in typesRmse && showRmse) {
            val accRMSEChart = ChartBuilder.ChartPresentation.Chart(
                id = "RMSE:" + Measurement.Id.ACC.name,
                points = accRmse,
                color = Green.toArgb(),
                fillColor = BlueTranslucent.toArgb()
            )
            chartBuilder.addChart(accRMSEChart)
        }
        if (hrRmse.isEmpty() || accRmse.isEmpty()) return
        if (Measurement.Id.HR in typesRmse || Measurement.Id.ACC in typesRmse) return
        val results = accRmse.zip(hrRmse) { acc, hr -> PointF(acc.x, acc.y * hr.y) }
        if (showRmse) {
            val accRMSEChart = ChartBuilder.ChartPresentation.Chart(
                id = "RMSE HR*ACC",
                points = results,
                color = Red.toArgb(),
                fillColor = RedTranslucent.toArgb()
            )
            chartBuilder.addChart(accRMSEChart)
        }
    }

    private fun computeHRRmse(
        measurements: List<Measurement>,
        frameSizeHR: Int,
        quantizationHR: Float,
    ): List<PointF> {
        val x = measurements.toMillisSinceStart()
        val y = measurements.mapToValues(Measurement.Id.HR)
        return hcBinding
            .segmentation(x = x, y = y, frameSizeHR, quantizationHR)
    }

    private fun computeACC(
        measurements: List<Measurement>,
        frameSizeACC: Int,
        quantizationACC: Float,
    ): List<PointF> {
        val x = measurements.toMillisSinceStart()
        val y = measurements.mapToValues(Measurement.Id.ACC)
        return hcBinding
            .segmentation(x = x, y = y, frameSizeACC, quantizationACC)
    }

    private fun computeUniformHypnogram(
        hrRmse: List<PointF>,
        accRmse: List<PointF>,
    ): List<HypnogramHolder.SleepDataPoint> {
        return hcBinding
            .computeUniformHypnogram(quantizedHR = hrRmse, quantizedACC = accRmse, minimalSleepPhaseLengthMs = 10 * 60 * 1000)
            .mapToSleepDataPoint()
    }
}