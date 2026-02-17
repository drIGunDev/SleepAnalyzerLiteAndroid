package de.igor.gun.sleep.analyzer.repositories

import android.graphics.PointF
import androidx.compose.ui.graphics.toArgb
import de.igor.gun.sleep.analyzer.db.DBManager
import de.igor.gun.sleep.analyzer.db.entities.Measurement
import de.igor.gun.sleep.analyzer.db.entities.Series
import de.igor.gun.sleep.analyzer.db.entities.toChart
import de.igor.gun.sleep.analyzer.db.entities.toIdWithColor
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.HypnogramComputation
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.mean
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.normalize
import de.igor.gun.sleep.analyzer.hypnogram.computation.v2.classes.rmse
import de.igor.gun.sleep.analyzer.math.bridges.hcSleepPhase2Segments
import de.igor.gun.sleep.analyzer.math.bridges.toHCModelConfigurationParams
import de.igor.gun.sleep.analyzer.math.bridges.toHCPoint
import de.igor.gun.sleep.analyzer.math.bridges.toPointFs
import de.igor.gun.sleep.analyzer.misc.AppParameters
import de.igor.gun.sleep.analyzer.repositories.di.HCProvider
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.repositories.tools.HypnogramHolder
import de.igor.gun.sleep.analyzer.repositories.tools.SleepSegment
import de.igor.gun.sleep.analyzer.repositories.tools.computeDistribution
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
        } catch (_: Exception) {
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
            fillChart(
                chartBuilder,
                types,
                measurements,
                typesRmse,
                showRmse
            )
            fillHypnogram(
                hypnogramHolder,
                measurements
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
            fillHypnogram(
                hypnogramHolder,
                measurements
            )
        }
    }

    fun recreateHypnogram(series: Series) {
        synchronized(this) {
            val hypnogramHolder = HypnogramHolder().apply {
                fillHypnogramWithMeasurements(series.id, this)
                dbManager.recreateHypnogram(series.id, this)
            }
            hypnogramHolder
                .buildSleepSegments()
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
        measurements: List<Measurement>,
    ) {
        if (measurements.isEmpty()) return
        val hypnogram = computeUniformHypnogram(measurements)
        if (hypnogram.isEmpty()) return
        hypnogramHolder.setSleepSegments(hypnogram)
    }

    private fun fillChart(
        chartBuilder: ChartBuilder,
        measurementIds: List<Measurement.Id>,
        measurements: List<Measurement>,
        typesRmse: List<Measurement.Id>,
        showRmse: Boolean = true
    ) {
        chartBuilder.clear()
        val idWithColor = measurementIds.toIdWithColor()
        val charts = measurements.toChart(idWithColor = idWithColor).apply {
            this[Measurement.Id.HR]?.rescaleY(30f, 100f)
        }
        charts.forEach { chartBuilder.addChart(it.value) }
        if (appParameters.frameSizeHR == 0) return

        val hrRmse = createSquares(Measurement.Id.HR, measurements)
        val acc = createSquares(Measurement.Id.ACC, measurements)
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
                points = acc,
                color = Green.toArgb(),
                fillColor = BlueTranslucent.toArgb()
            )
            chartBuilder.addChart(accRMSEChart)
        }
        if (hrRmse.isEmpty() || acc.isEmpty()) return
        if (Measurement.Id.HR in typesRmse || Measurement.Id.ACC in typesRmse) return
        if (showRmse) {
            val overlay = createOverlay(measurements)
            val overlay1Chart = ChartBuilder.ChartPresentation.Chart(
                id = "OVERLAY 1",
                points = overlay.first,
                color = Red.toArgb(),
                fillColor = RedTranslucent.toArgb()
            )
            chartBuilder.addChart(overlay1Chart)
            val overlay2Chart = ChartBuilder.ChartPresentation.Chart(
                id = "OVERLAY 2",
                points = overlay.second,
                color = Red.toArgb(),
                fillColor = BlueTranslucent.toArgb()
            )
            chartBuilder.addChart(overlay2Chart)
        }
    }

    private fun createOverlay(
        measurements: List<Measurement>
    ): Pair<List<PointF>, List<PointF>> {
        val hr = measurements.toHCPoint(Measurement.Id.HR)
        val acc = measurements.toHCPoint(Measurement.Id.ACC)
        val results = hcBinding.createOverlay(hr, acc, appParameters.toHCModelConfigurationParams())
        val hrSquare = results
            .map { it.first }
            .toPointFs(hr)
        val accSquare = results
            .map { it.second }
            .toPointFs(acc)
        return Pair(hrSquare, accSquare)
    }

    private fun createSquares(
        id: Measurement.Id,
        measurements: List<Measurement>
    ): List<PointF> {
        val values = measurements.toHCPoint(id)
        when (id) {
            Measurement.Id.HR -> return hcBinding
                .createUniformInput(
                    values,
                    appParameters.frameSizeHR.toDouble(),
                    appParameters.quantizationHR.toDouble(),
                    appParameters.hrHiPassCutoff
                )
                .toPointFs()

            Measurement.Id.ACC -> return hcBinding
                .createUniformInput(
                    values,
                    appParameters.frameSizeACC.toDouble(),
                    appParameters.quantizationACC.toDouble(),
                    appParameters.accHiPassCutoff
                )
                .toPointFs()

            else -> return emptyList()
        }
    }

    private fun computeHRRmse(
        measurements: List<Measurement>
    ): List<PointF> {
        val hr = measurements.toHCPoint(Measurement.Id.HR)
        return hr
            .rmse(appParameters.frameSizeHR)
            .toPointFs()
    }

    private fun computeACC(
        measurements: List<Measurement>
    ): List<PointF> {
        val acc = measurements.toHCPoint(Measurement.Id.ACC)
        return acc
            .mean(appParameters.frameSizeACC)
            .normalize()
            .toPointFs()
    }

    private fun computeUniformHypnogram(
        measurements: List<Measurement>
    ): List<SleepSegment> {
        val hr = measurements.toHCPoint(Measurement.Id.HR)
        val acc = measurements.toHCPoint(Measurement.Id.ACC)
        val hypnogram = hcBinding.createHypnogram(
            hr,
            acc,
            appParameters.toHCModelConfigurationParams()
        )
        return hypnogram.hcSleepPhase2Segments(hr[0].x.toLong() / 1000)
    }
}